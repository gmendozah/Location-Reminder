package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [33])
class RemindersListViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var dataSource: FakeDataSource
    private lateinit var app: Application

    @Before
    fun setupViewModel() {
        stopKoin()
        app = ApplicationProvider.getApplicationContext()
        dataSource = FakeDataSource()
        remindersListViewModel = RemindersListViewModel(app, dataSource)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun loadReminders_loading() = runTest {
        // Load the reminders in the view model
        remindersListViewModel.loadReminders()

        // Then assert that the progress indicator is shown
        // Since we are using StandardTestDispatcher, the coroutine is queued but hasn't run yet.
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))

        // Execute pending coroutines
        advanceUntilIdle()

        // Then assert that the progress indicator is hidden
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun loadReminders_remindersFound() = runTest {
        val reminder = ReminderDTO("title", "description", "location", 0.0, 0.0)
        dataSource.reminders?.add(reminder)

        remindersListViewModel.loadReminders()
        advanceUntilIdle()

        assertThat(remindersListViewModel.remindersList.getOrAwaitValue().size, `is`(1))
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun loadReminders_noRemindersFound() = runTest {
        dataSource.reminders?.clear()

        remindersListViewModel.loadReminders()
        advanceUntilIdle()

        assertThat(remindersListViewModel.remindersList.getOrAwaitValue().size, `is`(0))
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(true))
    }

    @Test
    fun loadReminders_shouldReturnError() = runTest {
        dataSource.setReturnError(true)

        remindersListViewModel.loadReminders()
        advanceUntilIdle()

        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue(), `is`("Test exception"))
    }
}
