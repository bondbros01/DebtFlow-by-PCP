import re

with open('app/src/main/java/com/example/ui/ManageProjectScreen.kt', 'r') as f:
    content = f.read()

old_row = """      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = item.participantName,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          color = HighDensityTextPrimary,
        )
        Text(
          text = "${item.contributionType} • ${item.relativeDate}",
          fontSize = 11.sp,
          color = HighDensityTextSecondary,
          modifier = Modifier.padding(top = 2.dp),
        )
      }"""

new_row = """      Column(modifier = Modifier.weight(1f)) {
        if (item.title.isNotBlank()) {
          Text(
            text = item.title,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = HighDensityTextPrimary,
          )
          Text(
            text = "${item.participantName} • ${item.contributionType} • ${item.relativeDate}",
            fontSize = 11.sp,
            color = HighDensityTextSecondary,
            modifier = Modifier.padding(top = 2.dp),
          )
        } else {
          Text(
            text = item.participantName,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = HighDensityTextPrimary,
          )
          Text(
            text = "${item.contributionType} • ${item.relativeDate}",
            fontSize = 11.sp,
            color = HighDensityTextSecondary,
            modifier = Modifier.padding(top = 2.dp),
          )
        }
      }"""

content = content.replace(old_row, new_row)
with open('app/src/main/java/com/example/ui/ManageProjectScreen.kt', 'w') as f:
    f.write(content)
