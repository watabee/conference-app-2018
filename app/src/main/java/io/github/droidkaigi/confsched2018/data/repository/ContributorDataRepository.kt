package io.github.droidkaigi.confsched2018.data.repository

import io.github.droidkaigi.confsched2018.data.api.GithubApi
import io.github.droidkaigi.confsched2018.data.db.ContributorDatabase
import io.github.droidkaigi.confsched2018.data.db.entity.mapper.toContributors
import io.github.droidkaigi.confsched2018.model.Contributor
import io.github.droidkaigi.confsched2018.util.rx.SchedulerProvider
import io.reactivex.Flowable
import kotlinx.coroutines.experimental.rx2.asCoroutineDispatcher
import kotlinx.coroutines.experimental.rx2.await
import kotlinx.coroutines.experimental.withContext
import timber.log.Timber
import javax.inject.Inject
import io.github.droidkaigi.confsched2018.data.api.response.Contributor as ContributorResponse

class ContributorDataRepository @Inject constructor(
        private val api: GithubApi,
        private val contributorDatabase: ContributorDatabase,
        private val schedulerProvider: SchedulerProvider
) : ContributorRepository {
    override suspend fun loadContributors() {
        // We want to implement paging logic,
        // But The GitHub API does not return the total count of contributors in response data.
        // And we want to show total count.
        // So we fetch all contributors when first time.
        val result = (1..MAX_PAGE)
                .map { page -> api.getContributors(OWNER, REPO, MAX_PER_PAGE, page) }
                .map { withContext(schedulerProvider.io().asCoroutineDispatcher()) { it.await() } }
                .reduce { acc, list -> acc + list }

        if (DEBUG) {
            result.forEach { Timber.d("$it") }
        }
        withContext(schedulerProvider.io().asCoroutineDispatcher()) {
            contributorDatabase.save(result)
        }
    }

    override val contributors: Flowable<List<Contributor>> =
            contributorDatabase.getAll()
                    .map { it.toContributors() }
                    .subscribeOn(schedulerProvider.io())

    companion object {
        private const val OWNER = "DroidKaigi"
        private const val REPO = "conference-app-2018"
        private const val MAX_PER_PAGE = 100
        // Max page num of github api for contributors.
        // If the number of the contributors will be over 200, this number must be changed to 3.
        private const val MAX_PAGE = 2
        private const val DEBUG = false
    }
}
