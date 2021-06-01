package no.nordicsemi.android.ei.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import no.nordicsemi.android.ei.HorizontalPagerTab
import no.nordicsemi.android.ei.HorizontalPagerTab.Testing
import no.nordicsemi.android.ei.HorizontalPagerTab.Training
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.model.Device
import no.nordicsemi.android.ei.model.Sample
import no.nordicsemi.android.ei.showSnackbar
import no.nordicsemi.android.ei.ui.layouts.InfoLayout
import no.nordicsemi.android.ei.util.exhaustive
import no.nordicsemi.android.ei.viewmodels.DataAcquisitionViewModel
import no.nordicsemi.android.ei.viewmodels.event.Error
import java.net.UnknownHostException

@OptIn(ExperimentalPagerApi::class)
@Composable
fun DataAcquisition(
    connectedDevice: List<Device>,
    pagerState: PagerState,
    viewModel: DataAcquisitionViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val trainingListState = rememberLazyListState()
    val testingListState = rememberLazyListState()

    LocalLifecycleOwner.current.lifecycleScope.launchWhenStarted {
        viewModel.eventFlow.runCatching {
            this.collect {
                when (it) {
                    is Error -> {
                        showSnackbar(
                            coroutineScope = coroutineScope,
                            snackbarHostState = snackbarHostState,
                            message = when (it.throwable) {
                                is UnknownHostException -> context.getString(R.string.error_no_internet)
                                else -> it.throwable.localizedMessage
                                    ?: context.getString(R.string.error_refreshing_failed)
                            }
                        )
                    }
                    else -> {}
                }
            }
        }
    }
    HorizontalPager(state = pagerState) { page ->
        val tab = HorizontalPagerTab.indexed(page)
        when (tab) {
            Training -> CollectedDataList(
                state = trainingListState,
                pagingDataFlow = viewModel.trainingSamples,
                tab = tab
            )
            Testing -> CollectedDataList(
                state = testingListState,
                pagingDataFlow = viewModel.testingSamples,
                tab = tab
            )
        }.exhaustive
    }
}

@Composable
private fun CollectedDataList(
    state: LazyListState,
    pagingDataFlow: Flow<PagingData<Sample>>,
    tab: HorizontalPagerTab
) {
    val samples: LazyPagingItems<Sample> = pagingDataFlow.collectAsLazyPagingItems()
    samples.takeIf { it.itemCount > 0 }?.let { lazyPagingItems ->
        LazyColumn(
            contentPadding = PaddingValues(bottom = 56.dp),
            state = state
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
                        item {
                            Loading(modifier = Modifier.fillParentMaxSize())
                        }
                    }
                    loadState.append is LoadState.Loading -> {
                        item { LoadingItem(modifier = Modifier.fillMaxWidth()) }
                    }
                    loadState.refresh is LoadState.Error -> {
                        val e = lazyPagingItems.loadState.refresh as LoadState.Error
                        item {
                            ErrorItem(
                                message = e.error.localizedMessage!!,
                                modifier = Modifier.fillParentMaxSize(),
                                onClickRetry = { retry() }
                            )
                        }
                    }
                    loadState.refresh is LoadState.Error -> {
                        val e = lazyPagingItems.loadState.refresh as LoadState.Error
                        item {
                            ErrorItem(message = e.toString(), onClickRetry = { retry() })
                        }
                    }
                }
            }
        }
    } ?: InfoLayout(
            iconPainter = rememberVectorPainter(tab.emptyListIcon),
            text = stringResource(R.string.label_no_collected_data_yet),
            modifier = Modifier.fillMaxSize()
        )
}

@Composable
fun CollectedDataRow(sample: Sample) {
    Row(
        modifier = Modifier
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
private fun Loading(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(modifier = Modifier.size(56.dp))
    }
}

@Composable
private fun LoadingItem(
    modifier: Modifier = Modifier
) {
    CircularProgressIndicator(
        modifier = modifier
            .padding(16.dp)
            .wrapContentWidth(Alignment.CenterHorizontally)
    )
}

@Composable
private fun ErrorItem(
    message: String,
    modifier: Modifier = Modifier,
    onClickRetry: () -> Unit
) {
    Row(
        modifier = modifier.padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = message,
            maxLines = 1,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.h6,
            color = Color.Red
        )
        OutlinedButton(onClick = onClickRetry) {
            Text(text = stringResource(R.string.try_again))
        }
    }
}