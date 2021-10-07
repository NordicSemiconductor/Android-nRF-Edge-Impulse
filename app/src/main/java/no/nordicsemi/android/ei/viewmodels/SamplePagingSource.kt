package no.nordicsemi.android.ei.viewmodels

import androidx.paging.PagingSource
import androidx.paging.PagingState
import no.nordicsemi.android.ei.model.DevelopmentKeys
import no.nordicsemi.android.ei.model.Project
import no.nordicsemi.android.ei.model.Sample
import no.nordicsemi.android.ei.repository.ProjectRepository
import retrofit2.HttpException
import java.io.IOException

//Edge Impulse paging API is 0 based https://studio.edgeimpulse.com/v1/api/projectId/raw-data
private const val LIST_SAMPLES_PAGE_INDEX = 0

class SamplePagingSource(
    private val project: Project,
    private val keys: DevelopmentKeys,
    private val category: String,
    private val repository: ProjectRepository
) : PagingSource<Int, Sample>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Sample> {
        val offset = params.key ?: LIST_SAMPLES_PAGE_INDEX
        val limit = params.loadSize
        return try {
            val response = repository.listSamples(
                projectId = project.id,
                keys = keys,
                category = category,
                offset = offset,
                limit = limit
            )
            val nextKey = if (response.samples.size < limit || response.samples.isEmpty()) {
                null
            } else {
                // initial load size = 3 * PAGE_SIZE
                // ensure we're not requesting duplicating items, at the 2nd request
                offset + params.loadSize
            }
            LoadResult.Page(
                data = response.samples,
                prevKey = null,
                nextKey = nextKey
            )

        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return LoadResult.Error(exception)
        }
    }

    //TODO needs testing once data sample collection has started, this is to ensure that the samples list gets refreshed once
    // a new sample is collected.

    // The refresh key is used for subsequent refresh calls to PagingSource.load after the initial load
    override fun getRefreshKey(state: PagingState<Int, Sample>): Int? {
        // We need to get the previous key (or next key if previous is null) of the page
        // that was closest to the most recently accessed index.
        // Anchor position is the most recently accessed index
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(PAGE_SIZE)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(PAGE_SIZE)
        }
    }
}