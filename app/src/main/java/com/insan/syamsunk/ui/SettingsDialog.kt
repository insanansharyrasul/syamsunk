package com.insan.syamsunk.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Madhab

private val DarkBg = Color(0xFF1A232A)
private val CardBg = Color(0xFF222D35)
private val TealHighlight = Color(0xFF0D7A75)
private val AmberYellow = Color(0xFFFFC107)
private val TextWhite = Color.White
private val TextMuted = Color(0xFFB0BEC5)

fun CalculationMethod.displayName(): String = when (this) {
    CalculationMethod.MUSLIM_WORLD_LEAGUE -> "Muslim World League"
    CalculationMethod.EGYPTIAN -> "Egyptian General Authority"
    CalculationMethod.KARACHI -> "Univ. of Islamic Sciences, Karachi"
    CalculationMethod.UMM_AL_QURA -> "Umm al-Qura University, Makkah"
    CalculationMethod.DUBAI -> "Dubai"
    CalculationMethod.MOON_SIGHTING_COMMITTEE -> "Moonsighting Committee Worldwide"
    CalculationMethod.NORTH_AMERICA -> "ISNA (North America)"
    CalculationMethod.KUWAIT -> "Kuwait"
    CalculationMethod.QATAR -> "Qatar"
    CalculationMethod.SINGAPORE -> "Singapore (MUIS)"
    CalculationMethod.OTHER -> "Other"
}

fun Madhab.displayName(): String = when (this) {
    Madhab.SHAFI -> "Shafi'i / Maliki / Hanbali"
    Madhab.HANAFI -> "Hanafi"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    currentMethod: CalculationMethod,
    currentMadhab: Madhab,
    onDismiss: () -> Unit,
    onSave: (CalculationMethod, Madhab) -> Unit
) {
    var selectedMethod by remember { mutableStateOf(currentMethod) }
    var selectedMadhab by remember { mutableStateOf(currentMadhab) }
    var methodExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = CardBg,
        title = {
            Text(
                text = "Settings",
                color = TextWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Calculation Method",
                    color = TextWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = methodExpanded,
                    onExpandedChange = { methodExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedMethod.displayName(),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = methodExpanded)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = TealHighlight,
                            unfocusedBorderColor = TextMuted,
                            focusedContainerColor = DarkBg,
                            unfocusedContainerColor = DarkBg
                        ),
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = methodExpanded,
                        onDismissRequest = { methodExpanded = false },
                        containerColor = CardBg
                    ) {
                        CalculationMethod.values().forEach { method ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = method.displayName(),
                                        color = if (method == selectedMethod) AmberYellow else TextWhite,
                                        fontWeight = if (method == selectedMethod) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    selectedMethod = method
                                    methodExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Madhab (Asr Juristic Method)",
                    color = TextWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Shafi'i / Standard
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedMadhab == Madhab.SHAFI,
                            onClick = { selectedMadhab = Madhab.SHAFI },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = selectedMadhab == Madhab.SHAFI,
                        onClick = null,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = AmberYellow,
                            unselectedColor = TextMuted
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Standard (Shafi'i, Maliki, Hanbali)",
                            color = TextWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Earlier Asr time (shadow length = 1)",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Hanafi
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedMadhab == Madhab.HANAFI,
                            onClick = { selectedMadhab = Madhab.HANAFI },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = selectedMadhab == Madhab.HANAFI,
                        onClick = null,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = AmberYellow,
                            unselectedColor = TextMuted
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Hanafi",
                            color = TextWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Later Asr time (shadow length = 2)",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedMethod, selectedMadhab) },
                colors = ButtonDefaults.buttonColors(containerColor = TealHighlight),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Save", color = TextWhite, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", color = TextMuted)
            }
        }
    )
}
