package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TakeoutDining
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CashSessionStatus
import com.example.data.model.ProductCategory
import com.example.ui.theme.AmberBadgeBg
import com.example.ui.theme.AmberBadgeText
import com.example.ui.theme.BorderBotanical
import com.example.ui.theme.CardWhite
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.CoralBadgeBg
import com.example.ui.theme.CoralBadgeText
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldMedium
import com.example.ui.theme.SageLeaf
import com.example.ui.theme.SlateTechnical
import com.example.ui.theme.StockOkBg
import com.example.ui.theme.StockOkText
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.tabular
import com.example.util.Formatters

@Composable
fun BotanicalCard(
    modifier: Modifier = Modifier,
    containerColor: Color = CardWhite,
    borderColor: Color = BorderBotanical,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(14.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Surface(
        modifier = modifier
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            ),
        shape = shape,
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

@Composable
fun StatusPill(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

fun categoryIcon(category: ProductCategory): ImageVector = when (category) {
    ProductCategory.TODOS -> Icons.Filled.Apps
    ProductCategory.VASOS -> Icons.Filled.LocalCafe
    ProductCategory.ENVASES -> Icons.Filled.TakeoutDining
    ProductCategory.PLATOS -> Icons.Filled.Restaurant
    ProductCategory.BOLSAS -> Icons.Filled.ShoppingBag
    ProductCategory.SERVILLETAS -> Icons.Filled.Layers
    ProductCategory.OTROS -> Icons.Filled.Inventory2
}

fun categoryAccentBg(category: ProductCategory): Color = when (category) {
    ProductCategory.VASOS -> Color(0xFFE6F4EE)
    ProductCategory.ENVASES -> Color(0xFFE8F1F5)
    ProductCategory.PLATOS -> Color(0xFFFEF3C7)
    ProductCategory.BOLSAS -> Color(0xFFEDE9FE)
    ProductCategory.SERVILLETAS -> Color(0xFFF3F0E9)
    else -> Color(0xFFE5ECE8)
}

@Composable
fun SarangierTopBar(
    businessName: String,
    estimatedCashInDrawer: Long,
    cashStatus: CashSessionStatus,
    onRequestOpenSettings: () -> Unit,
    onRequestEditBase: () -> Unit,
    onRequestReopenCash: () -> Unit,
    onRequestFactoryReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = EmeraldDeep,
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(EmeraldMedium),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Eco,
                        contentDescription = businessName,
                        tint = SageLeaf,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = businessName,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val isOpen = cashStatus == CashSessionStatus.ABIERTA
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (isOpen) SageLeaf else CoralBadgeText)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = if (isOpen) "Caja Abierta" else "Caja Cerrada",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                val isOpen = cashStatus == CashSessionStatus.ABIERTA
                Surface(
                    onClick = onRequestEditBase,
                    enabled = isOpen,
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.13f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
                    modifier = Modifier.testTag("top_drawer_base_button")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "CAJÓN EST.",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.78f)
                            )
                            if (isOpen) {
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Modificar base",
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }
                        Text(
                            text = Formatters.formatCOP(estimatedCashInDrawer),
                            style = MaterialTheme.typography.titleSmall.tabular(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.testTag("top_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "Opciones del sistema",
                            tint = Color.White
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Configuración") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Settings,
                                    contentDescription = null,
                                    tint = EmeraldDeep
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onRequestOpenSettings()
                            },
                            modifier = Modifier.testTag("settings_menu_item")
                        )
                        if (cashStatus == CashSessionStatus.ABIERTA) {
                            DropdownMenuItem(
                                text = { Text("Modificar Base de Caja") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = null,
                                        tint = EmeraldDeep
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onRequestEditBase()
                                },
                                modifier = Modifier.testTag("edit_base_menu_item")
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("Reabrir Caja de Hoy") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.LockOpen,
                                        contentDescription = null,
                                        tint = EmeraldDeep
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onRequestReopenCash()
                                },
                                modifier = Modifier.testTag("reopen_cash_menu_item")
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Reiniciar Todo en Cero") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.RestartAlt,
                                    contentDescription = null,
                                    tint = CoralBadgeText
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onRequestFactoryReset()
                            },
                            modifier = Modifier.testTag("factory_reset_menu_item")
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickQuantityStepper(
    quantity: Int,
    maxQuantity: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    onQuantityEdited: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var textValue by remember(quantity) { mutableStateOf(quantity.toString()) }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceContainer)
            .border(1.dp, BorderBotanical, RoundedCornerShape(10.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onDecrement,
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .size(44.dp)
                .testTag("stepper_minus")
        ) {
            Icon(
                imageVector = Icons.Filled.Remove,
                contentDescription = "Disminuir cantidad",
                tint = CharcoalText,
                modifier = Modifier.size(18.dp)
            )
        }

        BasicTextField(
            value = textValue,
            onValueChange = { raw ->
                val filtered = raw.filter { it.isDigit() }.take(4)
                textValue = filtered
                filtered.toIntOrNull()?.let { parsed ->
                    if (parsed > 0) {
                        onQuantityEdited(parsed)
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    val parsed = textValue.toIntOrNull() ?: quantity
                    onQuantityEdited(parsed)
                }
            ),
            textStyle = MaterialTheme.typography.titleSmall.tabular().copy(
                color = CharcoalText,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier
                .width(42.dp)
                .padding(vertical = 6.dp)
        )

        IconButton(
            onClick = onIncrement,
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .size(44.dp)
                .testTag("stepper_plus")
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Aumentar cantidad",
                tint = if (quantity >= maxQuantity) SlateTechnical.copy(alpha = 0.45f) else EmeraldDeep,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun stockStatusColors(stock: Int, minStock: Int): Triple<String, Color, Color> = when {
    stock <= 0 -> Triple("Agotado", CoralBadgeBg, CoralBadgeText)
    stock <= minStock -> Triple("Stock Bajo ($stock)", AmberBadgeBg, AmberBadgeText)
    else -> Triple("$stock disp.", StockOkBg, StockOkText)
}
