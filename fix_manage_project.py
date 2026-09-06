import re

with open('app/src/main/java/com/example/ui/ManageProjectScreen.kt', 'r') as f:
    content = f.read()

# Line 1 is a mess of concatenated imports. Let's try to fix it.
# package com.example.uiimport ...
# We can replace all "import " with "\nimport "
content = content.replace("import androidx", "\nimport androidx")
content = content.replace("package com.example.ui", "package com.example.ui\n")

with open('app/src/main/java/com/example/ui/ManageProjectScreen.kt', 'w') as f:
    f.write(content)
