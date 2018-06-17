package io.github.droidkaigi.confsched2018.presentation.contributor

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.OnLifecycleEvent
import android.arch.lifecycle.ViewModel
import io.github.droidkaigi.confsched2018.data.repository.ContributorRepository
import io.github.droidkaigi.confsched2018.model.Contributor
import io.github.droidkaigi.confsched2018.presentation.Result
import io.github.droidkaigi.confsched2018.presentation.common.mapper.toResult
import io.github.droidkaigi.confsched2018.util.ext.toLiveData
import io.github.droidkaigi.confsched2018.util.rx.SchedulerProvider
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import javax.inject.Inject

class ContributorsViewModel @Inject constructor(
        private val repository: ContributorRepository,
        private val schedulerProvider: SchedulerProvider
) : ViewModel(), LifecycleObserver {

    private val job = Job()

    val contributors: LiveData<Result<List<Contributor>>> by lazy {
        repository.contributors
                .toResult(schedulerProvider)
                .toLiveData()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        refreshContributors()
    }

    fun onRefreshContributors() {
        refreshContributors()
    }

    private fun refreshContributors() {
        launch(UI, parent = job) {
            try {
                repository.loadContributors()
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}
