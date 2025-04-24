package com.erdodif.capsulate.utility

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

val IconButtonModifier = Modifier.padding(5.dp, 1.dp).pointerHoverIcon(PointerIcon.Hand)
val IconButtonPaddingValues = PaddingValues(8.dp, 2.dp)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IconTextButton(
    icon: DrawableResource,
    text: StringResource,
    enabled: Boolean = true,
    modifier: Modifier = IconButtonModifier,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    val colors = IconButtonDefaults.iconButtonColors()
    Surface(
        shape = RoundedCornerShape(50),
        contentColor = colors.contentColor,
        modifier = modifier
            .semantics { role = Role.Button }
            .background(colors.containerColor, RoundedCornerShape(100)),
        border = ButtonDefaults.outlinedButtonBorder()
    ) {
        Row(
            Modifier.defaultMinSize(
                minWidth = ButtonDefaults.MinWidth,
                minHeight = ButtonDefaults.MinHeight
            ).combinedClickable(
                interactionSource = MutableInteractionSource(),
                indication = LocalIndication.current,
                enabled = enabled,
                role = Role.Button,
                onLongClick = onLongClick,
                onClick = onClick
            ).padding(IconButtonPaddingValues),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painterResource(icon),
                stringResource(text),
                Modifier.requiredSize(24.dp),
                MaterialTheme.colorScheme.primary
            )
            Text(
                stringResource(text),
                Modifier.padding(start = ButtonDefaults.ContentPadding.calculateTopPadding()),
                MaterialTheme.colorScheme.primary,
                18.sp,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
