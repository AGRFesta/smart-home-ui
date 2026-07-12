package org.agrfesta.sh.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import smart_home_ui.shared.generated.resources.Res
import smart_home_ui.shared.generated.resources.pikesta_name_logo

@Composable
fun AppHeader(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(Res.drawable.pikesta_name_logo),
        contentDescription = "Pikesta",
        contentScale = ContentScale.Fit,
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .testTag("app_header_logo")
    )
}
