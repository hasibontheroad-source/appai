package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Project
import com.example.ui.project.FileTab
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CardDark
import com.example.ui.theme.CyanBrand
import com.example.ui.theme.PurpleBrand
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CodeEditorView(
    project: Project,
    activeTab: FileTab,
    onTabSelected: (FileTab) -> Unit,
    onCodeChanged: (String) -> Unit,
    onRunCode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val currentCode = when (activeTab) {
        FileTab.HTML -> project.htmlContent
        FileTab.CSS -> project.cssContent
        FileTab.JS -> project.jsContent
        FileTab.PYTHON -> "# Python companion script\ndef greet(name):\n    return f'Hello from CodeForge, {name}!'\n\nif __name__ == '__main__':\n    print(greet('Developer'))\n"
        FileTab.JAVA -> "// Java companion class\npublic class App {\n    public static void main(String[] args) {\n        System.out.println(\"CodeForge AI Java Backend Engine\");\n    }\n}\n"
    }

    val lineCount by remember(currentCode) {
        derivedStateOf { currentCode.lines().size.coerceAtLeast(1) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
    ) {
        // Multi-file tabs
        ScrollableTabRow(
            selectedTabIndex = activeTab.ordinal,
            containerColor = CardDark,
            contentColor = CyanBrand,
            edgePadding = 8.dp,
            indicator = { tabPositions ->
                if (activeTab.ordinal < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[activeTab.ordinal]),
                        color = CyanBrand,
                        height = 3.dp
                    )
                }
            }
        ) {
            FileTabItem("index.html", FileTab.HTML, activeTab == FileTab.HTML) { onTabSelected(FileTab.HTML) }
            FileTabItem("styles.css", FileTab.CSS, activeTab == FileTab.CSS) { onTabSelected(FileTab.CSS) }
            FileTabItem("script.js", FileTab.JS, activeTab == FileTab.JS) { onTabSelected(FileTab.JS) }
            FileTabItem("main.py", FileTab.PYTHON, activeTab == FileTab.PYTHON) { onTabSelected(FileTab.PYTHON) }
            FileTabItem("App.java", FileTab.JAVA, activeTab == FileTab.JAVA) { onTabSelected(FileTab.JAVA) }
        }

        // Subheader Info & Actions Toolbar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF161B22),
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "$lineCount lines",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted
                    )
                    Text(
                        text = "${currentCode.length} chars",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Copy button
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Code", currentCode)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy code",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Run Preview Button
                    Button(
                        onClick = onRunCode,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanBrand,
                            contentColor = Color(0xFF0D1117)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("run_code_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = " RUN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Code Editor Body with Line Numbers Gutter
        val verticalScrollState = rememberScrollState()
        val horizontalScrollState = rememberScrollState()

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            // Line numbers column
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(42.dp)
                    .background(Color(0xFF0D1117))
                    .padding(vertical = 8.dp, horizontal = 4.dp)
                    .verticalScroll(verticalScrollState),
                horizontalAlignment = Alignment.End
            ) {
                for (i in 1..lineCount) {
                    Text(
                        text = "$i",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted,
                        lineHeight = 18.sp
                    )
                }
            }

            // Gutter divider
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(BorderDark)
            )

            // Code Text Field
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(verticalScrollState)
                    .horizontalScroll(horizontalScrollState)
                    .padding(8.dp)
            ) {
                BasicTextField(
                    value = currentCode,
                    onValueChange = onCodeChanged,
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = TextPrimary
                    ),
                    cursorBrush = SolidColor(CyanBrand),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("code_editor_input")
                )
            }
        }
    }
}

@Composable
private fun FileTabItem(
    title: String,
    tab: FileTab,
    selected: Boolean,
    onClick: () -> Unit
) {
    Tab(
        selected = selected,
        onClick = onClick,
        modifier = Modifier.testTag("tab_${tab.name.lowercase()}"),
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = null,
                    tint = if (selected) CyanBrand else TextMuted,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = if (selected) CyanBrand else TextSecondary
                )
            }
        }
    )
}
