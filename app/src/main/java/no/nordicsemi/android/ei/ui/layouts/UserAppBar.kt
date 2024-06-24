/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package no.nordicsemi.android.ei.ui.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.model.User
import no.nordicsemi.android.ei.ui.ShowDropdown
import no.nordicsemi.android.common.ui.R as uiR

private val AppBarHeight = 56.dp
val UserAppBarImageSize = 120.dp

@Composable
fun UserAppBar(
    title: String,
    user: User,
    backgroundColor: Color = colorResource(id = uiR.color.appBarColor),
    onAboutClick: () -> Unit = {},
    onDeleteUserClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
) {
    var showMenu by remember { mutableStateOf(false) }
    Column {
        TopAppBar(
            title = {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = backgroundColor
            ),
            actions = {
                IconButton(onClick = { showMenu = !showMenu }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                ShowDropdown(
                    modifier = Modifier.wrapContentWidth(),
                    expanded = showMenu,
                    onDismiss = { showMenu = !showMenu }) {
                    DropdownMenuItem(
                        text = {
                            Text(text = stringResource(id = R.string.action_about))
                        },
                        onClick = {
                            showMenu = !showMenu
                            onAboutClick()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(text = stringResource(id = R.string.action_delete_user))
                        },
                        onClick = {
                            showMenu = !showMenu
                            onDeleteUserClick()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(text = stringResource(id = R.string.action_logout))
                        },
                        onClick = {
                            showMenu = !showMenu
                            onLogoutClick()
                        }
                    )
                }
            }
        )
        Column(
            modifier = Modifier
                .height(AppBarHeight)
                .fillMaxWidth()
                .background(
                    color = TopAppBarDefaults.topAppBarColors(
                        containerColor = backgroundColor
                    ).containerColor
                )
                .padding(start = UserAppBarImageSize + 32.dp)
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

