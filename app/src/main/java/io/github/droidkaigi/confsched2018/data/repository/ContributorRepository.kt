package io.github.droidkaigi.confsched2018.data.repository

import io.github.droidkaigi.confsched2018.model.Contributor
import io.reactivex.Flowable

interface ContributorRepository {
    val contributors: Flowable<List<Contributor>>
    suspend fun loadContributors()
}
