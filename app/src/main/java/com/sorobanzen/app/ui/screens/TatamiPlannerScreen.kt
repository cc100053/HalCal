package com.sorobanzen.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sorobanzen.app.R
import com.sorobanzen.app.domain.TatamiPlanner
import com.sorobanzen.app.viewmodel.ZenViewModel

@Composable
fun TatamiPlannerScreen(
    viewModel: ZenViewModel,
    modifier: Modifier = Modifier
) {
    val widthVal by viewModel.tatamiWidth.collectAsState()
    val lengthVal by viewModel.tatamiLength.collectAsState()
    val region by viewModel.tatamiRegion.collectAsState()

    var widthInput by remember(widthVal) { mutableStateOf(widthVal.toString()) }
    var lengthInput by remember(lengthVal) { mutableStateOf(lengthVal.toString()) }

    val scrollState = rememberScrollState()

    // Calculate layout
    val mats = remember(widthVal, lengthVal, region) {
        TatamiPlanner.calculateLayout(widthVal, lengthVal, region)
    }

    val unitMatArea = region.matWidth * region.matLength
    val totalMatsCount = (widthVal * lengthVal) / unitMatArea

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.tatami_planner),
            fontSize = 24.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Dimension inputs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = widthInput,
                onValueChange = {
                    widthInput = it
                    it.toDoubleOrNull()?.let { d -> viewModel.updateTatamiDimensions(d, lengthVal) }
                },
                label = { Text(stringResource(id = R.string.width)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            OutlinedTextField(
                value = lengthInput,
                onValueChange = {
                    lengthInput = it
                    it.toDoubleOrNull()?.let { d -> viewModel.updateTatamiDimensions(widthVal, d) }
                },
                label = { Text(stringResource(id = R.string.length_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Region radio groups
        Text(
            text = stringResource(id = R.string.region),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TatamiPlanner.Region.values().forEach { r ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setTatamiRegion(r) }
                ) {
                    RadioButton(
                        selected = region == r,
                        onClick = { viewModel.setTatamiRegion(r) },
                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                    )
                    
                    val label = when (r) {
                        TatamiPlanner.Region.KYOTO -> stringResource(id = R.string.kyoto_kyouma)
                        TatamiPlanner.Region.NAGOYA -> stringResource(id = R.string.nagoya_ainoma)
                        TatamiPlanner.Region.TOKYO -> stringResource(id = R.string.tokyo_edoma)
                    }
                    Text(
                        text = label,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 8.dp),
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Calculations card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.mats_needed, totalMatsCount),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Visual simulation grid
        Text(
            text = stringResource(id = R.string.layout_suggestion),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.Start).padding(bottom = 12.dp)
        )

        // Draw mats in canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.2f)
                .clip(RoundedCornerShape(16.dp))
                .background(if (MaterialTheme.colorScheme.background.value == 0xFF121212.toULong().value) Color(0xFF1E1E1E) else Color(0xFFF3EFEB))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasW = size.width
                val canvasH = size.height

                // Calculate scaling factor to fit the room inside the canvas coordinates
                val paddingPercent = 0.1f
                val availableW = canvasW * (1.0f - paddingPercent)
                val availableH = canvasH * (1.0f - paddingPercent)

                // Room aspect ratio
                val roomRatio = widthVal / lengthVal
                val canvasRatio = availableW / availableH

                val scale: Float
                val offsetX: Float
                val offsetY: Float

                if (roomRatio > canvasRatio) {
                    scale = (availableW / widthVal).toFloat()
                    offsetX = (canvasW - widthVal * scale).toFloat() / 2f
                    offsetY = (canvasH - lengthVal * scale).toFloat() / 2f
                } else {
                    scale = (availableH / lengthVal).toFloat()
                    offsetX = (canvasW - widthVal * scale).toFloat() / 2f
                    offsetY = (canvasH - lengthVal * scale).toFloat() / 2f
                }

                // Draw Room Outer Bounds
                drawRect(
                    color = Color(0xFF5C4033), // Wood border for room boundary
                    topLeft = Offset(offsetX, offsetY),
                    size = Size((widthVal * scale).toFloat(), (lengthVal * scale).toFloat()),
                    style = Stroke(width = 8f)
                )

                // Draw Tatami Mats
                val isDark = MaterialTheme.colorScheme.background.value == 0xFF121212.toULong().value
                
                // Traditional colors: dry straw green
                val tatamiFill = if (isDark) Color(0xFF3B443B) else Color(0xFFD2E2CA)
                val tatamiBorder = if (isDark) Color(0xFF232B23) else Color(0xFF6E8E64)
                // Tatami bindings/borders (black / dark brown borders on edges)
                val bindingColor = if (isDark) Color(0xFF151915) else Color(0xFF333333)

                mats.forEach { mat ->
                    val matX = (offsetX + mat.x * scale).toFloat()
                    val matY = (offsetY + mat.y * scale).toFloat()
                    val matW = (mat.width * scale).toFloat()
                    val matH = (mat.height * scale).toFloat()

                    // Fill mat
                    drawRect(
                        color = tatamiFill,
                        topLeft = Offset(matX, matY),
                        size = Size(matW, matH)
                    )

                    // Mat boundary lines
                    drawRect(
                        color = tatamiBorder,
                        topLeft = Offset(matX, matY),
                        size = Size(matW, matH),
                        style = Stroke(width = 2f)
                    )

                    // Draw traditional tatami binding stripes (black bands along the long edges)
                    if (mat.isHorizontal) {
                        // Horizontal mat: bands on top and bottom edges
                        drawRect(
                            color = bindingColor,
                            topLeft = Offset(matX, matY),
                            size = Size(matW, 6f)
                        )
                        drawRect(
                            color = bindingColor,
                            topLeft = Offset(matX, matY + matH - 6f),
                            size = Size(matW, 6f)
                        )
                    } else {
                        // Vertical mat: bands on left and right edges
                        drawRect(
                            color = bindingColor,
                            topLeft = Offset(matX, matY),
                            size = Size(6f, matH)
                        )
                        drawRect(
                            color = bindingColor,
                            topLeft = Offset(matX + matW - 6f, matY),
                            size = Size(6f, matH)
                        )
                    }
                }
            }
        }
    }
}
