# Complete Technical Specification: Single-User Debt & Project Cost Tracking Application

---

## 1. Introduction & Executive Goal

The Debt & Project Cost Tracking Application is an offline-first, single-user mobile application designed to manage recurring and fixed-amount financial liabilities, split and balance project-based expenditures, and coordinate shared financial settlements.

### Key Objectives
- **Centralized Debt Management**: Enable a single account owner to create, categorize, and assign multiple debts (e.g., recurring subscriptions, one-off vendor payments) to external participants.
- **Accurate Scheduling & Reminders**: Maintain an up-to-date schedule of payment deadlines and automated reminders anchored to the user’s local timezone (**America/Phoenix, UTC-07:00**), presenting all deadlines using strictly relative temporal phrasing (e.g., "today", "tomorrow", "this week", "next week", "this month").
- **Shared Project Cost Tracking**: Track individual project lifecycle costs, recording participant contributions across multiple contribution types (cash, goods, services), and automatically computing net balances when a project is purchased, sold, or transferred.
- **Data Sovereignty & Persistence**: Ensure 100% offline availability via local encrypted persistence (SQLite/Room), augmented by optional background real-time cloud synchronization across the user's secondary devices.

---

## 2. System Architecture & Boundaries

```
+-------------------------------------------------------------------------+
|                              PRESENTATION LAYER                         |
|  - Jetpack Compose UI (Material Design 3)                               |
|  - Dashboard / Debt Table / Calendar View / Project Cost Matrix         |
|  - Relative Time Resolvers (America/Phoenix UTC-07:00)                  |
+------------------------------------+------------------------------------+
                                     |
+------------------------------------+------------------------------------+
|                               DOMAIN LAYER                              |
|  - DebtScheduler & RelativeDueCalculator                                |
|  - ProjectCostSharingEngine (Net balance & transfer liquidation)        |
|  - TransactionAuditor (Chronological forward/backward log)             |
|  - Reporting & Document Exporter (CSV / PDF)                            |
+------------------------------------+------------------------------------+
                                     |
+------------------------------------+------------------------------------+
|                                DATA LAYER                               |
|  - Local Repository (Room Database / SQLite with SQLCipher Encryption)  |
|  - Offline Outbox Sync Manager                                          |
|  - Cloud Synchronization Adapter (Optional Background Real-time Sync)  |
+-------------------------------------------------------------------------+
```

### Scope & Identity Boundaries
1. **Single-Sign-In Model**: Exactly one account owner identity is authenticated per device/installation. The app does not implement multi-tenant multi-account overhead or account-switching logic.
2. **Participant Roles**:
   - **Account Owner**: Complete administrative authority (CRUD access for debts, projects, line items, transaction logs, export tools, and collaborator invites).
   - **Payer / Collaborator**: External participants assigned to debts or invited via share links. When viewing shared data, collaborators access a synced or read-only view without multi-account identity management.
3. **Strict Relative Temporal Domain**: All timestamps, calendar grids, reminders, transaction histories, and project entries must be calculated and displayed strictly through relative temporal terminology relative to "today" in America/Phoenix time. Absolute calendar date references (such as specific calendar years, calendar months by name, or formatted absolute dates) are strictly prohibited across all screens, logs, and exports.

---

## 3. Detailed Functional Requirements

### 3.1 User Profiles & Role-Based Access Control
- **Authentication**: Single sign-in flow for the application creator/owner using email/password or biometric passkey, hashed and secured at rest.
- **Role Assignment**:
  - *Owner*: Retains ownership of all data tables, schema migrations, and sync configurations.
  - *Payer / Participant*: Created within the owner's dataset as named entities. Can be mapped to specific line items, split percentages, and debt repayment responsibilities.
- **Collaborator Sharing**:
  - The owner may generate one-click invitation links or dispatch email invites to collaborators.
  - Invitation permits configurable permissions: **Read-Only** (inspect balance reports, project updates, and debt status) or **Edit Rights** (log repayments or contributions).
  - Actions execute against the owner's isolated dataset; collaborators do not register separate billing accounts.

### 3.2 Debt Management Engine
- **Debt Creation Parameters**:
  - `title`: Short free-form description (e.g., "Cloud Infrastructure", "Studio Workspace Lease").
  - `type`: Category indicator (`Subscription`, `Fixed-Amount`, `One-Off Installment`).
  - `startDate`: Recorded relative to session ("today", "earlier this week", "earlier this month").
  - `recurringFrequency`: Interval cadence (`Weekly`, `Monthly`).
  - `amount`: Gross liability value formatted in local currency.
  - `dueDayOfMonth`: Ordinal day of the cycle (1–31) to determine recurring cycle boundaries.
  - `ownerPayerList`: Assignment of one or more named participants (e.g., "Owner (50%)", "Payer Alex (50%)").
- **Schedule Reminders & Phoenix Timezone Conversion**:
  - The engine interrogates the current time in America/Phoenix (`UTC-07:00`).
  - Resolves target due dates into dynamic relative labels:
    - `"Due today"`
    - `"Due tomorrow"`
    - `"Due later this week"`
    - `"Due next week"`
    - `"Due later this month"`
    - `"Overdue from earlier this week"`
- **Reminders & Alerts**:
  - Dispatches platform toasts upon application launch for debts due today or tomorrow.
  - Emits local push notifications and optional scheduled email alerts ahead of impending recurring deadlines.
- **Debt Listing Table**:
  - Sortable columns: *Due Date (Relative)*, *Status*, *Owner/Payer Name*, *Amount*.
  - Visual status chips: **Green** for "Paid", **Red** for "Unpaid / Overdue", **Amber** for "Due Today".
  - Each entry features an interactive "Details" link exposing amortization breakdown, assigned payers, and audit logs.

### 3.3 Project Cost Tracking
- **Project Creation**:
  - Free-form alphanumeric project name.
  - Total estimated budget / cost threshold.
- **Line Item Composition**:
  - Participant name: Person responsible for or contributing the line item.
  - Contribution type: Enumerated as `Cash`, `Goods`, or `Services`.
  - Amount: Numerical valuation of the contribution.
  - Date of entry: Logged relatively ("today", "yesterday", "earlier this week").
- **Shared Projects & Transfer/Sale Flow**:
  - Supports transferring stewardship or recording the sale of a shared project to a buyer.

### 3.4 Cost-Sharing & Liquidation Logic
When the user records that **Person X** sold or purchased the project:
1. **Interactive Sale Prompting**:
   - The UI immediately prompts the user to enter the **Sale Price** (or Purchase Price).
   - Prompts for confirmation or adjustments of all other participants' recorded contributions and their relative payment dates ("entered earlier this week", "settled today").
2. **Automated Net Balance Calculation**:
   - Let $C_{total}$ be the sum of all participant contributions ($C_{total} = \sum c_i$).
   - Let $S$ be the recorded gross sale price.
   - Let $E_{total}$ be the project total estimated/incurred cost.
   - For each participant $i$ with contribution $c_i$ and pre-agreed equity/cost share fraction $w_i$ (where $\sum w_i = 1.0$):
     $$\text{Share of Project Cost} = E_{total} \times w_i$$
     $$\text{Share of Sale Proceeds} = S \times w_i$$
     $$\text{Net Balance}_i = c_i - (\text{Share of Project Cost}) + (\text{Share of Sale Proceeds})$$
   - If $\text{Net Balance}_i > 0$, the participant is owed funds (**Credit**, displayed in Green).
   - If $\text{Net Balance}_i < 0$, the participant owes funds to the group (**Debit**, displayed in Red).
3. **Real-Time Synchronous Update**:
   - The shared balance summary view updates immediately without page reload.
   - An updated Project Balance Liquidation Report is generated and added to the chronological log.

### 3.5 Transaction History, Reporting & Export
- **Chronological Audit Trail**:
  - Unified, bi-directional searchable log recording every payment, reimbursement, line-item insertion, project sale, and ownership transfer.
  - Filterable by participant name, transaction type, or relative timeframe ("today", "this week", "this month").
- **Summary Reports**:
  - Key financial metrics:
    - `"Outstanding balance: $50"`
    - `"Total contributions: $120"`
    - `"Settled debts: $340"`
    - `"Net credit balance: $70"`
- **Export Specifications**:
  - **CSV File Export**: Formatted export of debt schedules, payer distributions, payment status, and relative deadlines.
  - **PDF Export**: Vector-rendered project summary document containing budget vs. actuals, participant breakdown, contribution types, and liquidation distribution charts.

### 3.6 Data Persistence & Synchronization
- **Local Storage (Primary)**:
  - SQLite database managed via Room / SQLCipher.
  - Zero-latency offline read and write capability.
  - AES-256 encryption at rest for sensitive financial amounts and notes.
- **Cloud Synchronization (Secondary & Optional)**:
  - Background replication engine connects to Firebase / BaaS.
  - Changes queued locally in an offline outbox table; automatically dispatched upon network availability.
  - Real-time event stream ensures instantaneous updates across all authenticated devices of the account owner.

---

## 4. Complete Data Model & Schemas

### Entity Relationship Overview
```
[ UserProfile (Single Account Owner) ]
       |
       +---> 1:N ---> [ Debt ] ---> 1:N ---> [ DebtPayer ]
       |                |
       |                +---> 1:N ---> [ DebtScheduleReminder ]
       |
       +---> 1:N ---> [ Project ] ---> 1:N ---> [ ProjectLineItem ]
       |                   |
       |                   +---> 1:N ---> [ ProjectSaleSession ]
       |
       +---> 1:N ---> [ CollaboratorInvite ]
       |
       +---> 1:N ---> [ TransactionAuditLog ]
```

### Table & Field Specifications

#### `user_profile`
| Field | Type | Description | Constraints |
| :--- | :--- | :--- | :--- |
| `id` | VARCHAR(36) | Primary key UUID | PK |
| `email` | VARCHAR(255) | Authenticated owner email | Unique, Not Null |
| `password_hash` | VARCHAR(255) | Standard argon2/bcrypt credential hash | Not Null |
| `timezone` | VARCHAR(64) | Set to "America/Phoenix" (UTC-07:00) | Default "America/Phoenix" |
| `created_relative` | VARCHAR(32) | Relative creation descriptor (e.g. "today") | Not Null |

#### `debts`
| Field | Type | Description | Example Value |
| :--- | :--- | :--- | :--- |
| `id` | VARCHAR(36) | Primary Key UUID | PK |
| `title` | VARCHAR(120) | Debt title / description | "Hosting Subscription" |
| `debt_type` | VARCHAR(32) | "SUBSCRIPTION", "FIXED_AMOUNT", "ONE_OFF" | "SUBSCRIPTION" |
| `amount_cents` | INTEGER | Gross liability in cents | 4500 ($45.00) |
| `amount_paid_cents` | INTEGER | Sum of settled payments | 1500 ($15.00) |
| `recurring_frequency` | VARCHAR(32) | "WEEKLY", "MONTHLY", "NONE" | "MONTHLY" |
| `due_day_of_month` | INTEGER | Ordinal day boundary (1–31) | 15 |
| `start_date_relative` | VARCHAR(64) | Relative start point | "earlier this month" |
| `next_due_relative` | VARCHAR(64) | Calculated relative deadline | "tomorrow" |
| `status` | VARCHAR(24) | "PAID", "UNPAID", "DUE_TODAY" | "UNPAID" |

#### `debt_payers`
| Field | Type | Description | Example Value |
| :--- | :--- | :--- | :--- |
| `id` | VARCHAR(36) | Primary Key UUID | PK |
| `debt_id` | VARCHAR(36) | Foreign key to `debts.id` | FK |
| `participant_name` | VARCHAR(120) | Assigned payer name | "Jordan Smith" |
| `allocated_percent` | DECIMAL(5,2) | Share percentage of debt | 50.00 |
| `has_settled` | BOOLEAN | Settlement status flag | false |

#### `projects`
| Field | Type | Description | Example Value |
| :--- | :--- | :--- | :--- |
| `id` | VARCHAR(36) | Primary Key UUID | PK |
| `name` | VARCHAR(160) | Free-form project title | "Mobile App Prototype" |
| `total_estimated_cost_cents` | INTEGER | Target budget in cents | 120000 ($1,200.00) |
| `status` | VARCHAR(32) | "ACTIVE", "SOLD", "TRANSFERRED" | "ACTIVE" |
| `created_relative` | VARCHAR(64) | Relative creation time | "this week" |

#### `project_line_items`
| Field | Type | Description | Example Value |
| :--- | :--- | :--- | :--- |
| `id` | VARCHAR(36) | Primary Key UUID | PK |
| `project_id` | VARCHAR(36) | Foreign key to `projects.id` | FK |
| `participant_name` | VARCHAR(120) | Contributor name | "Taylor Brown" |
| `contribution_type` | VARCHAR(32) | "CASH", "GOODS", "SERVICES" | "GOODS" |
| `amount_cents` | INTEGER | Valuation in cents | 35000 ($350.00) |
| `entry_date_relative` | VARCHAR(64) | Relative date of entry | "yesterday" |

#### `project_sale_sessions`
| Field | Type | Description | Example Value |
| :--- | :--- | :--- | :--- |
| `id` | VARCHAR(36) | Primary Key UUID | PK |
| `project_id` | VARCHAR(36) | Foreign key to `projects.id` | FK |
| `seller_name` | VARCHAR(120) | Participant executing the sale | "Alex Green" |
| `sale_price_cents` | INTEGER | Gross agreed liquidation value | 180000 ($1,800.00) |
| `session_date_relative` | VARCHAR(64) | Relative sale execution date | "today" |
| `liquidation_report_json` | TEXT | Snapshot of participant net balances | Full JSON tree |

#### `transaction_audit_logs`
| Field | Type | Description | Example Value |
| :--- | :--- | :--- | :--- |
| `id` | VARCHAR(36) | Primary Key UUID | PK |
| `related_entity_type` | VARCHAR(32) | "DEBT", "PROJECT", "SALE", "REFUND" | "SALE" |
| `related_entity_id` | VARCHAR(36) | Foreign UUID | FK |
| `event_type` | VARCHAR(48) | "PAYMENT_RECORDED", "PROJECT_SOLD" | "PROJECT_SOLD" |
| `actor_name` | VARCHAR(120) | User or participant initiating event | "Owner" |
| `summary_text` | TEXT | Human-readable log narrative | "Project sold today for $1,800.00" |
| `occurred_relative` | VARCHAR(64) | Relative time indicator | "earlier today" |

---

## 5. UI/UX Flow & Component Architecture

### 5.1 Screen Map
```
[ App Launch / Single-Sign-In ]
              |
              v
[ Main Landing Dashboard ]
  |-- Hero Metric Banner ("Outstanding balance: $50", "Total contributions: $120")
  |-- Quick Action Cards: ["Add Debt", "Add Project", "View Calendar"]
  |
  +---> [ Debts Tab / View ]
  |       |-- Sort & Filter Controls (Status, Due Date, Payer)
  |       |-- Debt Table (Green for Paid, Red for Unpaid)
  |       |-- "Add Debt" Sheet (Type, Due Day of Month, Payers)
  |       +-- Debt Details Modal & Schedule Reminder Config
  |
  +---> [ Calendar View (Phoenix Timezone) ]
  |       |-- Relative Schedule Horizons: "Today", "Tomorrow", "This Week", "Next Week"
  |       +-- Upcoming Payment Card Feed with One-Touch Settlement
  |
  +---> [ Projects & Cost-Sharing View ]
  |       |-- Project Cards with Budget vs. Actual Incurred
  |       |-- Project Item Editor (Add/Remove Line Items: Cash, Goods, Services)
  |       |-- Net Balance Overview (Pie & Stacked Bar Chart)
  |       +-- Project Liquidation / Sale Wizard Modal
  |
  +---> [ Audit Trail & Export Center ]
          |-- Chronological Searchable Transaction Feed
          +-- Instant CSV (Debts) and PDF (Projects) Export Trigger
```

### 5.2 Visual Style, Color Tokens & Accessibility
- **Design Foundation**: Clean Material Design 3 guidelines featuring generous 16dp / 24dp padding, rounded container shapes (12dp–16dp), and high-contrast typography.
- **Semantic State Colors**:
  - **Paid / In Credit / Settled**: Vibrant Green (`#2E7D32` light / `#81C784` dark) indicating positive financial standing.
  - **Unpaid / In Debit / Overdue**: Assertive Red (`#C62828` light / `#E57373` dark) indicating outstanding debt or amounts owed.
  - **Due Today / Imminent**: Amber Accent (`#EF6C00` light / `#FFB74D` dark) for action items requiring attention today.
- **Touch Targets**: All interactive elements, chips, modal buttons, and table rows meet or exceed the mandatory 48dp × 48dp touch target requirement.

### 5.3 Key User Flows

#### Flow 1: Debt Creation & Relative Due Date Resolution
1. User taps `"Add Debt"` button from Dashboard.
2. User fills title, selects category (e.g., Subscription), enters amount, selects recurring cadence (Monthly), and sets Due Day of Month.
3. System converts today’s local time in Phoenix into relative schedules (resolving whether the deadline falls "today", "tomorrow", or "later this month").
4. User selects payers from dropdown and saves.
5. System appends record, schedules background notification, and presents confirmation toast: `"Debt registered for tomorrow"`.

#### Flow 2: Recording Project Sale & Automated Net Balancing
1. Owner opens project card (e.g., "Mobile Prototype") where line items already detail past participant contributions logged "earlier this week" or "yesterday".
2. Owner clicks `"Record Project Sale"`.
3. Modal prompts for:
   - Sale Price input.
   - Confirmation of existing participant contributions and their relative dates.
4. Engine processes the formula, calculates net balances for all parties, and renders a live stacked bar chart showing credits vs debits.
5. Owner taps `"Confirm Liquidation"`.
6. Real-time balance display reflects updated figures, logs entry into transaction audit trail, and creates an updated project summary report ready for PDF export.

---

## 6. User-Instruction Guidance & Operating Manual

### 6.1 Getting Started & Authentication
1. **Initial Access**: Launch the app and authenticate through the Single Sign-In prompt. No secondary user accounts are needed.
2. **Timezone Verification**: The app automatically operates in the America/Phoenix (UTC-07:00) timezone, calibrating all calculations against current local time.

### 6.2 Managing Debts & Setting Reminders
1. Navigate to the **Debts** screen.
2. Tap **"Add Debt"**. Enter a title, liability amount, and frequency.
3. Pick a due day of the month and add any participating payers.
4. Review the computed relative deadline (e.g., "Due tomorrow", "Due this week").
5. Save the debt. Keep notifications enabled to receive alerts for debts falling due today or tomorrow.
6. When a payment is remitted, tap the green checkmark to mark the item as **Paid** (turning the row green).

### 6.3 Tracking Shared Projects & Adding Line Items
1. Navigate to **Projects** and tap **"New Project"**.
2. Enter the project name and total estimated cost.
3. Open the project and tap **"Add Line Item"**:
   - Enter the contributor's name.
   - Choose contribution type: **Cash**, **Goods**, or **Services**.
   - Input the value and confirm the relative entry date ("today", "yesterday", "this week").
4. View the **Balance Overview** chart to see each participant's standing at any moment.

### 6.4 Liquidating or Selling a Project
1. When a participant sells or transfers a project, open the project details and tap **"Record Sale"**.
2. Input the agreed sale price in the prompt.
3. The system will review all recorded contributions and automatically balance the amounts.
4. Tap **"Update Balances"** to instantly generate the final settlement report.

### 6.5 Exporting Reports & Inviting Collaborators
1. **Exporting Data**: In the Reporting tab, select **"Export Debts to CSV"** or **"Generate Project Summary PDF"**. The files will be saved directly to your local device downloads folder.
2. **Inviting Collaborators**: Tap **"Invite Collaborator"** on any project or debt, choose **Read-Only** or **Edit**, and share the generated one-click invitation link. Collaborators can inspect live balances while you retain complete administrative control.

---

## 7. Security, Privacy & Compliance

1. **Encryption at Rest**: All local database files (SQLite) use standard AES-256 encryption via SQLCipher. Stored credentials use industry-standard cryptographic password hashing.
2. **Isolated Data Silo**: Each user operates within their own isolated data set. Collaborators only receive read or edit access to specific shared views via cryptographically signed tokens.
3. **No Credential Leakage**: No raw API keys, private keys, or third-party service tokens are exposed in the client code or UI layer.
4. **Google Play Policy Compliance**:
   - Zero storage permission usage: Uses system document pickers and application private storage for CSV/PDF exports.
   - Title and metadata adhere to store guidelines (no all-caps, buzzwords, or misleading claims).

---

## 8. Acceptance Criteria & Verification Matrix

| # | Acceptance Criterion | Verification Method |
| :-: | :--- | :--- |
| **1** | Single-sign-in for creator only; no multi-account logic. | Verify authentication gate initializes a single administrative account; verify absence of account switching UI. |
| **2** | Create debt and assign one or more participants with accurate balances. | Create recurring subscription with 2 payers; verify split calculation and payment status updates. |
| **3** | Project entity supports free-form name and total estimated cost. | Create project with custom string name and budget limit; verify proper persistence. |
| **4** | Line items capture participant, contribution type (cash/goods/services), amount, and date. | Add 3 distinct line items covering cash, goods, and services; verify all fields appear correctly in project log. |
| **5** | Sale prompt captures price, checks contributions, and auto-updates net balances in real time. | Trigger sale flow, enter purchase price, and verify automatic mathematical calculation of participant debits/credits. |
| **6** | Transaction log is searchable and supports forward/backward history. | Perform payments, transfers, and additions; search transaction log by keyword and relative time filter. |
| **7** | Local persistence with optional cloud sync across devices. | Verify complete offline read/write functionality; verify real-time background sync when connected to secondary device. |
| **8** | UI reflects local Phoenix time (UTC-07:00) for all dates, reminders, and calendar views. | Verify system computes upcoming dates anchored strictly to Phoenix local time. |
| **9** | **Strict Relative Phrasing**: No mention of absolute calendar dates anywhere in the UI or spec. | Inspect all displays, labels, schemas, and reports to ensure strictly relative terminology ("today", "tomorrow", "this week", "next week", "this month"). |

---

## 9. Optional Enhancements

1. **Multi-Calendar Schedule Support**: Permit individual debts to bind to multiple distinct alert cadences simultaneously (e.g., alert 2 days prior to this week's due date, alert again today at 9:00 AM Phoenix time).
2. **Automated Email Summaries**: Scheduled background email dispatches delivering a concise briefing of liabilities due "today", "tomorrow", or "this week".
3. **User-Controlled Snapshot Export**: Single-tap generation of an encrypted snapshot archive combining debts (CSV), project summaries (PDF), and transaction audit logs for local backup.
