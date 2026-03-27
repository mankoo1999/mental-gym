package com.mentalgym.app.ui.home

import com.mentalgym.app.data.repository.TrainingContentRepository
import com.mentalgym.app.data.repository.WorkoutRepository
import com.mentalgym.app.domain.content.WorkoutContentProvider
import com.mentalgym.app.domain.model.TrainingProgram
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class HomeViewModelTest {

    @Mock
    private lateinit var repository: WorkoutRepository

    private lateinit var trainingContentRepository: TrainingContentRepository

    private lateinit var viewModel: HomeViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        trainingContentRepository = mockk()
        every { trainingContentRepository.getWeeklyPlan(any()) } returns
            WorkoutContentProvider.getStandardWeeklyPlan()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading`() {
        whenever(repository.userPreferences).thenReturn(flowOf(null))

        viewModel = HomeViewModel(repository, trainingContentRepository)

        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `complete onboarding updates repository`() = runTest(testDispatcher) {
        whenever(repository.userPreferences).thenReturn(flowOf(null))
        viewModel = HomeViewModel(repository, trainingContentRepository)

        viewModel.completeOnboarding(TrainingProgram.STANDARD)
        advanceUntilIdle()

        verify(repository).completeOnboarding(TrainingProgram.STANDARD)
    }

    @Test
    fun `loads user preferences on init`() = runTest(testDispatcher) {
        val mockPrefs = com.mentalgym.app.data.local.entity.UserPreferencesEntity(
            id = 1,
            selectedProgram = "STANDARD",
            currentStreak = 5,
            longestStreak = 10,
            onboardingCompleted = true
        )
        whenever(repository.userPreferences).thenReturn(flowOf(mockPrefs))

        viewModel = HomeViewModel(repository, trainingContentRepository)
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertEquals(TrainingProgram.STANDARD, uiState.currentProgram)
        assertEquals(5, uiState.currentStreak)
        assertTrue(uiState.isOnboarded)
        assertFalse(uiState.isLoading)
    }
}
