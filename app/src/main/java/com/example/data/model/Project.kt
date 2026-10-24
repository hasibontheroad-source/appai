package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Project(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val htmlContent: String = "",
    val cssContent: String = "",
    val jsContent: String = "",
    val activeLanguage: String = "web",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    /**
     * Synthesizes HTML, CSS, and JS into a complete self-contained HTML document
     * ready to run inside an Android WebView sandbox.
     */
    fun buildRenderHtml(): String {
        return """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=2.0">
  <title>${title.ifBlank { "Live Preview" }}</title>
  <!-- Tailwind CSS CDN for modern utilities -->
  <script src="https://cdn.tailwindcss.com"></script>
  <script>
    tailwind.config = {
      darkMode: 'class',
      theme: {
        extend: {
          colors: {
            brand: '#58A6FF',
            neon: '#8957E5'
          }
        }
      }
    }
  </script>
  <style>
    /* User Custom CSS */
$cssContent
  </style>
</head>
<body class="bg-slate-900 text-slate-100 antialiased min-h-screen">
$htmlContent

  <script>
    // User Custom JavaScript
    try {
$jsContent
    } catch (e) {
      console.error(e.name + ": " + e.message);
    }
  </script>
</body>
</html>
        """.trimIndent()
    }
}
