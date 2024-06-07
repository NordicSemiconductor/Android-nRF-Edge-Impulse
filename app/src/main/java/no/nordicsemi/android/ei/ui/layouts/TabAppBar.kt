/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package no.nordicsemi.android.ei.ui.layouts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.theme.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TabTopAppBar1(
    title: @Composable () -> Unit,
    tabs: List<Pair<@Composable () -> Unit, (@Composable () -> Unit)?>>,
    pagerState: PagerState? = null,
    navigationIcon: @Composable (() -> Unit),
    actions: @Composable RowScope.() -> Unit = {},
    backgroundColor: Color = colorResource(id = R.color.appBarColor),
) {
    val selectedTabIndex = pagerState?.currentPage ?: 0
    Column {
        TopAppBar(
            title = title,
            navigationIcon = navigationIcon,
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
        )
        PrimaryTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = backgroundColor,
            indicator = {
                TabRowDefaults.PrimaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(
                        selectedTabIndex,
                        matchContentSize = false,
                    ),
                    color = MaterialTheme.colorScheme.secondary,
                    width = Dp.Unspecified,
                )
            }
        ) {
            // Adds tabs for all of pages
            val coroutineScope = rememberCoroutineScope()
            tabs.forEachIndexed { index, tab ->
                Tab(
                    text = tab.first,
                    icon = tab.second,
                    selected = pagerState?.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState?.scrollToPage(index)
                        }
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TabTopAppBar(
    title: @Composable () -> Unit,
    tabs: List<Pair<@Composable () -> Unit, (@Composable () -> Unit)?>>,
    modifier: Modifier = Modifier,
    pagerState: PagerState? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    backgroundColor: Color = colorResource(id = R.color.appBarColor),
    contentColor: Color = contentColorFor(backgroundColor),
    elevation: Dp = 0.dp,
) {
    TabAppBar(
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        elevation = elevation,
        contentPadding = PaddingValues(4.dp),
        modifier = modifier,
        tabs = tabs,
        pagerState = pagerState
    ) {
        if (navigationIcon == null) {
            Spacer(TitleInsetWithoutIcon)
        } else {
            Row(/*TitleIconModifier,*/ verticalAlignment = Alignment.CenterVertically) {
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.onSurface,
                    content = navigationIcon
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProvideTextStyle(value = MaterialTheme.typography.titleLarge) {
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.onSurface,
                    content = title
                )
            }
        }

        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
            Row(
                Modifier.fillMaxHeight(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                content = actions
            )
        }
    }
}

/**
 * An empty Tab App Bar that expands to the parent's width.
 *
 * For an Tab App Bar that follows Material spec guidelines to be placed on the top of the screen,
 * see [TabTopAppBar].
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TabAppBar(
    backgroundColor: Color,
    contentColor: Color,
    elevation: Dp,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    tabs: List<Pair<@Composable () -> Unit, (@Composable () -> Unit)?>>,
    pagerState: PagerState? = null,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        color = backgroundColor,
        contentColor = contentColor,
        shadowElevation = elevation,
        tonalElevation = elevation,
        modifier = modifier
    ) {
        Column {
            Row(
                Modifier
                    .padding(contentPadding)
                    .padding(top = 8.dp)
                    .height(AppBarHeight + 32.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
            val selectedTabIndex = pagerState?.currentPage ?: 0
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = backgroundColor
            ) {
                // Adds tabs for all of pages
                val coroutineScope = rememberCoroutineScope()
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        text = tab.first,
                        icon = tab.second,
                        selected = pagerState?.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState?.scrollToPage(index)
                            }
                        },
                    )
                }
            }
        }
    }
}

private val AppBarHeight = 56.dp

// TODO: this should probably be part of the touch target of the start and end icons, clarify this
private val AppBarHorizontalPadding = 4.dp

// Start inset for the title when there is no navigation icon provided
private val TitleInsetWithoutIcon = Modifier.width(16.dp - AppBarHorizontalPadding)