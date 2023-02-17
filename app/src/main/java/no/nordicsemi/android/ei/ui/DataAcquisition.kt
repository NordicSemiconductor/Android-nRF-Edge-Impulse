/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import kotlinx.coroutines.flow.Flow
import no.nordicsemi.android.ei.HorizontalPagerTab
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.model.Message
import no.nordicsemi.android.ei.model.Sample
import no.nordicsemi.android.ei.ui.layouts.InfoLayout
import no.nordicsemi.android.ei.util.asMessage
import java.util.*

private const val PAGE_COUNT = 2

@OptIn(ExperimentalPagerApi::class)
@Composable
fun DataAcquisition(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    listStates: List<LazyListState>,
    samples: List<Flow<PagingData<Sample>>>,
    samplingState: Message.Sample
) {
    HorizontalPager(
        count = PAGE_COUNT,
        modifier = modifier,
        state = pagerState
    ) { page ->
        CollectedDataList(
            state = listStates[page],
            samplingState = samplingState,
            pagingDataFlow = samples[page],
            tab = HorizontalPagerTab.indexed(page)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CollectedDataList(
    modifier: Modifier = Modifier,
    state: LazyListState,
    samplingState: Message.Sample,
    pagingDataFlow: Flow<PagingData<Sample>>,
    tab: HorizontalPagerTab
) {
    val samples: LazyPagingItems<Sample> = pagingDataFlow.collectAsLazyPagingItems()
    // Refresh the samples when after a successful sampling
    val shouldRefresh by derivedStateOf {
        samplingState is Message.Sample.Finished && samplingState.sampleFinished && samplingState.error != null
    }
    if(shouldRefresh){
        samples.refresh()
    }

    samples.let { lazyPagingItems ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize(),
            state = state,
            contentPadding = PaddingValues(bottom = 144.dp)
        ) {
            if (lazyPagingItems.itemCount > 0) {
                stickyHeader {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colors.background)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.label_col_sample_name),
                            modifier = Modifier.weight(0.5f),
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(id = R.string.label_col_label),
                            modifier = Modifier.weight(0.5f),
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(id = R.string.label_col_length),
                            modifier = Modifier.width(60.dp),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Divider()
                }
            }
            items(lazyPagingItems) {
                it?.let { sample ->
                    CollectedDataRow(sample)
                    Divider()
                }
            }

            lazyPagingItems.apply {
                when {
                    loadState.refresh is LoadState.Loading -> {
                        item { LoadingIndicator(modifier = Modifier.fillParentMaxSize()) }
                    }
                    loadState.append is LoadState.Loading -> {
                        item { LoadingIndicator() }
                    }
                    loadState.refresh is LoadState.Error -> {
                        val e = lazyPagingItems.loadState.refresh as LoadState.Error
                        item {
                            Error(
                                message = e.error.asMessage(),
                                modifier = Modifier.fillParentMaxSize(),
                                onClickRetry = { retry() }
                            )
                        }
                    }
                    loadState.append is LoadState.Error -> {
                        val e = lazyPagingItems.loadState.append as LoadState.Error
                        item {
                            ErrorItem(
                                message = e.error.asMessage(),
                                onClickRetry = { retry() }
                            )
                        }
                    }
                    loadState.refresh is LoadState.NotLoading -> {
                        if (lazyPagingItems.itemCount == 0) item {
                            InfoLayout(
                                iconPainter = rememberVectorPainter(tab.icon),
                                text = stringResource(R.string.label_no_collected_data_yet),
                                modifier = Modifier.fillParentMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CollectedDataRow(
    sample: Sample,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colors.surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = sample.filename,
            modifier = Modifier.weight(0.5f),
            color = MaterialTheme.colors.onSurface,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.body1,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = sample.label,
            modifier = Modifier.weight(0.5f),
            color = MaterialTheme.colors.onSurface,
            style = MaterialTheme.typography.body1,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "${sample.totalLengthMs.toInt() / 1000}s",
            modifier = Modifier.width(60.dp),
            color = MaterialTheme.colors.onSurface,
            style = MaterialTheme.typography.body1,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun LoadingIndicator(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun Error(
    message: String,
    modifier: Modifier = Modifier,
    onClickRetry: () -> Unit
) {
    InfoLayout(
        imageVector = Icons.Outlined.ErrorOutline,
        modifier = modifier,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.h6
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onClickRetry) {
            Text(text = stringResource(R.string.action_try_again).uppercase(Locale.US))
        }
    }
}

@Composable
private fun ErrorItem(
    message: String,
    modifier: Modifier = Modifier,
    onClickRetry: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.disabled) {
            Icon(
                painter = rememberVectorPainter(image = Icons.Filled.Error),
                contentDescription = null,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.body1,
                modifier = Modifier.weight(1.0f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = onClickRetry) {
                Text(
                    text = stringResource(R.string.action_try_again).uppercase(Locale.US),
                    style = MaterialTheme.typography.button,
                )
            }
        }
    }
}