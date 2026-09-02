package com.qbtester.app.data.remote

import com.qbtester.app.model.QbLookupResult

/** Seam so [com.qbtester.app.data.repository.QuarterbackRepositoryImpl] can be tested with a fake. */
interface QbRemoteDataSource {
    suspend fun fetchStartingQuarterbacks(): Map<String, QbLookupResult>
}
