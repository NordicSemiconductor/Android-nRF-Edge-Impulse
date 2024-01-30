/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

@file:OptIn(ExperimentalMaterial3Api::class)

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.common.theme.R.color
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.model.User
import no.nordicsemi.android.ei.ui.ShowDropdown

private val AppBarHeight = 56.dp
val UserAppBarImageSize = 120.dp

@Composable
fun UserAppBar(
    title: @Composable () -> Unit,
    user: User,
    modifier: Modifier = Modifier,
    backgroundColor: Color = colorResource(id = color.appBarColor),
    contentColor: Color = contentColorFor(backgroundColor),
    elevation: Dp = 4.dp,
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
        shadowElevation = elevation,
        tonalElevation = elevation,
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
                    .padding(4.dp)
                    .height(AppBarHeight)
            ) {
                Spacer(modifier = Modifier.width(12.dp)) // 16.dp - 4.dp
                Row(
                    Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProvideTextStyle(value = MaterialTheme.typography.titleLarge) {
                        CompositionLocalProvider(
                            LocalContentColor provides MaterialTheme.colorScheme.onSurface,
                            content = title
                        )
                    }
                }
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
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
                                DropdownMenuItem(
                                    text =  {
                                        Text(text = stringResource(id = R.string.action_about))
                                    },
                                    onClick = {
                                        showMenu = !showMenu
                                        onAboutClick()
                                    }
                                )
                                DropdownMenuItem(
                                    text =  {
                                        Text(text = stringResource(id = R.string.action_logout))
                                    },
                                    onClick = {
                                    showMenu = !showMenu
                                    onLogoutClick()
                                })
                            }
                        }
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = UserAppBarImageSize + 16.dp)
            ) {
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = user.name,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        modifier = Modifier
                            .padding(top = 4.dp),
                        text = user.email,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}