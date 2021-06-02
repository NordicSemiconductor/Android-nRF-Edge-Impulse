package no.nordicsemi.android.ei.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
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
import no.nordicsemi.android.ei.HorizontalPagerTab.Testing
import no.nordicsemi.android.ei.HorizontalPagerTab.Training
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.model.Device
import no.nordicsemi.android.ei.model.Sample
import no.nordicsemi.android.ei.ui.layouts.InfoLayout
import no.nordicsemi.android.ei.util.asMessage
import no.nordicsemi.android.ei.util.exhaustive
import no.nordicsemi.android.ei.viewmodels.DataAcquisitionViewModel
import java.util.*

@OptIn(ExperimentalPagerApi::class)
@Composable
fun DataAcquisition(
    connectedDevice: List<Device>,
    pagerState: PagerState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    viewModel: DataAcquisitionViewModel
) {
    val trainingListState = rememberLazyListState()
    val testingListState = rememberLazyListState()

    HorizontalPager(state = pagerState) { page ->
        val tab = HorizontalPagerTab.indexed(page)
        when (tab) {
            Training -> CollectedDataList(
                state = trainingListState,
                pagingDataFlow = viewModel.trainingSamples,
                tab = tab,
                snackbarHostState = snackbarHostState,
            )
            Testing -> CollectedDataList(
                state = testingListState,
                pagingDataFlow = viewModel.testingSamples,
                tab = tab,
                snackbarHostState = snackbarHostState,
            )
        }.exhaustive
    }
}

@Composable
private fun CollectedDataList(
    state: LazyListState,
    pagingDataFlow: Flow<PagingData<Sample>>,
    tab: HorizontalPagerTab,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val samples: LazyPagingItems<Sample> = pagingDataFlow.collectAsLazyPagingItems()
    samples.let { lazyPagingItems ->
        LazyColumn(
            state = state,
            contentPadding = PaddingValues(bottom = 144.dp)
        ) {
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
                        item {
                            InfoLayout(
                                iconPainter = rememberVectorPainter(tab.emptyListIcon),
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
            modifier = Modifier.weight(0.4f),
            color = MaterialTheme.colors.onSurface,
            style = MaterialTheme.typography.body1,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "${sample.totalLengthMs.toInt() / 1000}s",
            modifier = Modifier.weight(0.1f),
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
        iconPainter = rememberVectorPainter(image = Icons.Outlined.ErrorOutline),
        modifier = modifier,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.h6
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onClickRetry) {
            Text(text = stringResource(R.string.action_try_again).toUpperCase(Locale.US))
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
                    text = stringResource(R.string.action_try_again).toUpperCase(Locale.US),
                    style = MaterialTheme.typography.button,
                )
            }
        }
    }
}