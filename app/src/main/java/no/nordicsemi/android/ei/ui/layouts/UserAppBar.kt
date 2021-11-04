package no.nordicsemi.android.ei.ui.layouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.ContentAlpha
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.model.User
import no.nordicsemi.android.ei.ui.ShowDropdown

private val AppBarHeight = 56.dp
val UserAppBarImageSize = 120.dp

@OptIn(ExperimentalCoilApi::class)
@Composable
fun UserAppBar(
    title: @Composable () -> Unit,
    user: User,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.primarySurface,
    contentColor: Color = contentColorFor(backgroundColor),
    elevation: Dp = AppBarDefaults.TopAppBarElevation,
    onAboutClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
) {
    val imageSizePx = with(LocalDensity.current) {
        Size(UserAppBarImageSize.toPx(), UserAppBarImageSize.toPx())
    }
    val imageOffsetPx = with(LocalDensity.current) {
        Offset(16.dp.toPx(), AppBarHeight.toPx())
    }
    Surface(
        color = backgroundColor,
        contentColor = contentColor,
        shape = UserTopBarShape(imageSizePx, imageOffsetPx),
        elevation = elevation,
        modifier = modifier
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .height(AppBarHeight + UserAppBarImageSize / 2),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier
                    .padding(AppBarDefaults.ContentPadding)
                    .height(AppBarHeight)
            ) {
                Spacer(modifier = Modifier.width(12.dp)) // 16.dp - 4.dp
                Row(
                    Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProvideTextStyle(value = MaterialTheme.typography.h6) {
                        CompositionLocalProvider(
                            LocalContentAlpha provides ContentAlpha.high,
                            content = title
                        )
                    }
                }
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Row(
                        Modifier.fillMaxHeight(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.Top,
                        content = {
                            var showMenu by remember { mutableStateOf(false) }
                            IconButton(onClick = { showMenu = !showMenu }) {
                                Icon(Icons.Default.MoreVert, contentDescription = null)
                            }
                            ShowDropdown(
                                modifier = Modifier.wrapContentWidth(),
                                expanded = showMenu,
                                onDismiss = { showMenu = !showMenu }) {
                                DropdownMenuItem(onClick = {
                                    showMenu = !showMenu
                                    onAboutClick()
                                }) {
                                    Text(text = stringResource(id = R.string.action_about))
                                }
                                DropdownMenuItem(onClick = {
                                    showMenu = !showMenu
                                    onLogoutClick()
                                }) {
                                    Text(text = stringResource(id = R.string.action_logout))
                                }
                            }
                        }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = UserAppBarImageSize+16.dp)
            ) {
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = user.name,
                        color = MaterialTheme.colors.onPrimary,
                        style = MaterialTheme.typography.h6,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        modifier = Modifier
                            .padding(top = 4.dp),
                        text = user.email,
                        color = MaterialTheme.colors.onPrimary,
                        style = MaterialTheme.typography.caption,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}