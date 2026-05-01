package com.udacity.project4.locationreminders.savereminder

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.FakeAndroidDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import androidx.test.core.app.ApplicationProvider.getApplicationContext

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@MediumTest
class SaveReminderFragmentTest : KoinTest {

    private lateinit var dataSource: FakeAndroidDataSource
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun setup() {
        stopKoin()
        dataSource = FakeAndroidDataSource()
        val myModule = module {
            single {
                SaveReminderViewModel(getApplicationContext(), get() as ReminderDataSource)
            }
            single<ReminderDataSource> { dataSource }
        }
        startKoin { modules(myModule) }
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun teardown() {
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
        stopKoin()
    }

    @Test
    fun navigateToSelectLocation_whenLocationClicked() {
        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.requireView(), navController)
        }

        onView(withId(R.id.selectLocation)).perform(click())

        verify(navController).navigate(
            SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment()
        )
    }

    @Test
    fun showSnackbar_whenTitleEmpty() {
        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        onView(withId(R.id.saveReminder)).perform(click())

        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.err_enter_title)))
    }

    @Test
    fun showSnackbar_whenLocationEmpty() {
        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        scenario.onFragment { fragment ->
            fragment._viewModel.reminderTitle.value = "Test Title"
        }

        onView(withId(R.id.saveReminder)).perform(click())

        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.err_select_location)))
    }
}
