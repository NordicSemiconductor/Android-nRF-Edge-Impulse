package no.nordicsemi.android.ei.ui.layouts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
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
import com.google.accompanist.coil.rememberCoilPainter
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.model.User
import no.nordicsemi.android.ei.ui.ShowDropdown

private val AppBarHeight = 56.dp

@Composable
fun UserAppBar(
    title: @Composable () -> Unit,
    user: User,
    modifier: Modifier = Modifier,
    imageSize: Dp = 120.dp,
    backgroundColor: Color = MaterialTheme.colors.primarySurface,
    contentColor: Color = contentColorFor(backgroundColor),
    elevation: Dp = AppBarDefaults.TopAppBarElevation,
    onAboutClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
) {
    val imageSizePx = with(LocalDensity.current) {
        Size(imageSize.toPx(), imageSize.toPx())
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
                .height(AppBarHeight + imageSize / 2),
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
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = null)
                            }
                            ShowDropdown(
                                modifier = Modifier.wrapContentWidth(),
                                expanded = showMenu,
                                onDismiss = { showMenu = false }) {
                                DropdownMenuItem(onClick = onAboutClick) {
                                    Text(text = stringResource(id = R.string.action_about))
                                }
                                DropdownMenuItem(onClick = onLogoutClick) {
                                    Text(text = stringResource(id = R.string.action_logout))
                                }
                            }
                        }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    modifier = Modifier
                        .offset(y = imageSize / 4)
                        .padding(start = 16.dp)
                        .border(
                            border = BorderStroke(3.dp, color = MaterialTheme.colors.onPrimary),
                            shape = CircleShape
                        )
                        .requiredHeight(imageSize)
                        .aspectRatio(1.0f),
                    shape = CircleShape,
                ) {
                    Image(
                        painter = rememberCoilPainter(
                            request = user.photo ?: Image(
                                Icons.Filled.AccountCircle,
                                contentDescription = null,
                                alpha = 0.1f
                            ),
                            shouldRefetchOnSizeChange = { _, _ -> false },
                        ),
                        contentDescription = stringResource(R.string.content_description_user_image),
                        alignment = Alignment.Center,
                    )
                }
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
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        modifier = Modifier
                            .padding(top = 4.dp),
                        text = user.email,
                        color = MaterialTheme.colors.onPrimary,
                        style = MaterialTheme.typography.caption,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}