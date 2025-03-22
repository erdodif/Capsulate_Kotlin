package com.erdodif.capsulate.utility

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource


@Composable
fun IconTextButton(icon: DrawableResource, text: StringResource, onClick: () -> Unit) =
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.padding(5.dp, 1.dp).pointerHoverIcon(PointerIcon.Hand),
        contentPadding = PaddingValues(8.dp, 2.dp)
    ) {
        Row {
            Icon(painterResource(icon), stringResource(text), Modifier.size(20.dp))
            Text(stringResource(text))
        }
    }