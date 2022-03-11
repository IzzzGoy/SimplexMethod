// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import model.Simplex
import model.Simplex.ERROR
import viewmodel.ViewModel


fun main() = application {
    val viewModel = remember { ViewModel() }
    val windowState = rememberWindowState(placement = WindowPlacement.Floating)
    var xCount by remember { mutableStateOf(1) }
    var constraintsCount by remember { mutableStateOf(1)}
    val listOfValues = remember { mutableStateListOf(FloatArray(xCount + 1)) }
    var function by remember { mutableStateOf(FloatArray(xCount)) }
    val listOfTables = remember { mutableStateListOf<Array<FloatArray>>()}
    val scope = rememberCoroutineScope()
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
                                listOfValues.replaceAll { FloatArray(xCount + 1) }
                                function = FloatArray(xCount)
                            }
                            if (newValue == "") xCount = 0
                        }
                    )
                    Spacer(modifier = Modifier.widthIn(20.dp, 80.dp))
                    TextField(
                        label = { Text("Количество ограничений")},
                        value = if (constraintsCount == 0) "" else constraintsCount.toString(),
                        placeholder = { Text("0")},
                        onValueChange = { newValue ->
                            newValue.toIntOrNull()?.let {
                                constraintsCount = it
                                listOfValues.clear()
                                repeat(constraintsCount) { _ ->
                                    listOfValues.add(FloatArray(xCount + 1))
                                }
                            }
                            if (newValue == "") constraintsCount = 0
                        }
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.scrollable(rememberScrollState(), orientation = Orientation.Horizontal)
                ) {
                    Text(
                        text = "F = ",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    function.forEachIndexed { index, it ->
                        Icon(Icons.Default.Add, null)
                        TextField(
                            value = it.toString(),
                            onValueChange = { newValue ->
                                val new = function.clone()
                                newValue.toFloatOrNull()?.let {
                                    new[index] = it
                                }
                                if (newValue == "") new[index] = 0f
                                function = new
                            },
                            modifier = Modifier.width(120.dp).height(50.dp)
                        )
                    }
                }
                listOfValues.map { arr ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 20.dp)
                    ) {
                        repeat(arr.size - 1){ index ->
                            Icon(Icons.Default.Add, null)
                            TextField(
                                value = if (arr[index] == 0f) "" else arr[index].toString(),
                                onValueChange = { newValue ->
                                    val new = arr.clone()
                                    newValue.toFloatOrNull()?.let {
                                        new[index] = it
                                    }
                                    if (newValue == "") new[index] = 0f
                                    listOfValues[listOfValues.indexOf(arr)] = new
                                },
                                modifier = Modifier.width(120.dp).height(50.dp)
                            )
                        }
                        Text(
                            text = "=",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        TextField(
                            value = if (arr.last() == 0f) "" else arr.last().toString(),
                            onValueChange = { newValue ->
                                val new = arr.clone()
                                newValue.toFloatOrNull()?.let {
                                    new[new.lastIndex] = it
                                }
                                if (newValue == "") new[new.lastIndex] = 0f
                                listOfValues[listOfValues.indexOf(arr)] = new
                            },
                            modifier = Modifier.width(120.dp).height(50.dp)
                        )
                    }
                }

                Button(onClick = {
                    val list = listOfValues.toList()
                    val m = list.mapIndexed { index, floats ->
                        floats.dropLast(1).toFloatArray() + sequence {
                            repeat(constraintsCount) {
                                if (it == index) yield(1f)
                                else yield(0f)
                            }
                        }
                            .toList()
                            .toFloatArray() + floatArrayOf(floats.last())
                    }
                    val last = function.map { it * -1 }.toFloatArray() + FloatArray(constraintsCount + 1) { 0f }
                    val standardized = m + last
                    println(standardized.map { it.toList() })
                    viewModel.initSimplex(xCount, constraintsCount + xCount, standardized.toTypedArray())
                    scope.launch {
                        listOfTables.clear()
                        listOfTables.add(standardized.toTypedArray())
                        viewModel.calculate().collect {
                            listOfTables.add(it.filterNotNull().toTypedArray())
                        }
                    }
                }) {
                    Text("Solve")
                }

                LazyColumn {
                    items(listOfTables) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            it.forEach { arr ->
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxSize().border(1.dp, Color.Black)
                                ) {
                                    arr.forEach { float ->
                                        Box( modifier = Modifier
                                            .padding(18.dp)
                                            .clipToBounds()
                                        ) {
                                            Text(
                                                text = float.toString(),
                                            )
                                        }

                                    }
                                }
                            }
                            Spacer(modifier = Modifier.heightIn(20.dp, 30.dp))
                        }
                    }
                }
            }
        }
    }
}
