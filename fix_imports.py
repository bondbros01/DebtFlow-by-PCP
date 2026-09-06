with open('app/src/main/java/com/example/ui/ManageProjectScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("import com.example.model.ProjectRecord", "import com.example.model.ProjectRecord\nimport com.example.model.PersonRecord\nimport androidx.compose.foundation.lazy.LazyRow\nimport androidx.compose.material3.OutlinedTextFieldDefaults")

with open('app/src/main/java/com/example/ui/ManageProjectScreen.kt', 'w') as f:
    f.write(content)
