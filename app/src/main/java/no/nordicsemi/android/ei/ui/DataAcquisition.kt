package no.nordicsemi.android.ei.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import kotlinx.coroutines.flow.collect
import no.nordicsemi.android.ei.HorizontalPagerTab
import no.nordicsemi.android.ei.HorizontalPagerTab.*
import no.nordicsemi.android.ei.R
import no.nordicsemi.android.ei.model.Device
import no.nordicsemi.android.ei.model.Sample
import no.nordicsemi.android.ei.showSnackbar
import no.nordicsemi.android.ei.viewmodels.DataAcquisitionViewModel
import no.nordicsemi.android.ei.viewmodels.event.Error
import java.net.UnknownHostException

@OptIn(ExperimentalPagerApi::class)
@ExperimentalMaterialApi
@Composable
fun DataAcquisition(
    connectedDevice: List<Device>,
    modalBottomSheetState: ModalBottomSheetState,
    pagerState: PagerState,
    pages: List<HorizontalPagerTab>,
    viewModel: DataAcquisitionViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val trainingListState = rememberLazyListState()
    val testingListState = rememberLazyListState()
    val anomalyListState = rememberLazyListState()

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
                }
            }
        }
    }
    HorizontalPager(state = pagerState) { page ->
        when (page) {
            0 -> CollectedDataList(
                viewModel.trainingSamples,
                trainingListState,
                pages[page],
                viewModel.isRefreshingTrainingData
            )
            1 -> CollectedDataList(
                viewModel.trainingSamples,
                testingListState,
                pages[page],
                viewModel.isRefreshingTestData
            )
            else -> CollectedDataList(
                viewModel.anomalySamples,
                anomalyListState,
                pages[page],
                viewModel.isRefreshingAnomalyData
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun CollectedDataList(
    samples: List<Sample>,
    listState: LazyListState,
    tab: HorizontalPagerTab,
    isRefreshing: Boolean = false
) {
    samples.takeIf {
        it.isNotEmpty()
    }?.let { notEmptyList ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            state = listState
        ) {
            items(items = notEmptyList, key = {
                it.id
            }) { sample ->
                CollectedDataRow(sample = sample, tab)
                Divider()
            }
        }
    } ?: run {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(64.dp)
                )
                Spacer(modifier = Modifier.size(16.dp))
                Text(
                    text = when (tab) {
                        is Training -> stringResource(R.string.label_loading_collected_data)
                        is Testing -> stringResource(R.string.label_loading_collected_data)
                        is Anomaly -> stringResource(R.string.label_loading_collected_data)
                    }
                )
            } else {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.disabled) {
                    Icon(
                        imageVector = tab.emptyListIcon,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.label_no_collected_data_yet),
                        style = MaterialTheme.typography.h6
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RecordDataFloatingActionButton(onClick: () -> Unit) {
    // Toggle the visibility of the content with animation.
    FloatingActionButton(onClick = onClick) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
            Text(
                text = stringResource(R.string.action_record_new_data),
                modifier = Modifier
                    .padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun CollectedDataRow(sample: Sample, tab: HorizontalPagerTab) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colors.surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = tab.rowIcon,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = MaterialTheme.colors.primary,
                    shape = CircleShape
                )
                .padding(8.dp),
            tint = Color.White
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1.0f)) {
            Text(
                text = sample.filename,
                color = MaterialTheme.colors.onSurface,
                style = MaterialTheme.typography.body1
            )
            Text(
                text = sample.added,
                color = MaterialTheme.colors.onSurface,
                style = MaterialTheme.typography.caption
            )
        }
    }
}