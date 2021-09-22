// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import javax.swing.GroupLayout

fun main() = application {
    val windowState = rememberWindowState(placement = WindowPlacement.Floating)
    var xCount by remember { mutableStateOf(1) }
    Window(
        state = windowState,
        title = "Simplex Method",
        onCloseRequest = ::exitApplication
    ) {
        DesktopMaterialTheme {
            Column(
                modifier = Modifier.padding(28.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    TextField(
                        label = { Text("Колличество неизвестных")},
                        value = if (xCount == 0) "" else xCount.toString(),
                        placeholder = { Text("0")},
                        onValueChange = { newValue ->
                            newValue.toIntOrNull()?.let {
                                xCount = it
                            }
                            if (newValue == "") xCount = 0
                        }
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("F = ")
                    repeat(xCount) {
                        Icon(Icons.Default.Add, null)
                        TextField(
                            value = "",
                            onValueChange = {},
                            modifier = Modifier.width(60.dp).height(30.dp)
                        )
                    }
                }
            }
        }
    }
}
