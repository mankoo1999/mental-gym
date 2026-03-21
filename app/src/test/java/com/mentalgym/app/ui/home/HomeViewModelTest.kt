package com.mentalgym.app.ui.home

import com.mentalgym.app.data.repository.WorkoutRepository
import com.mentalgym.app.domain.model.TrainingProgram
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.*

@ExperimentalCoroutinesApi
class HomeViewModelTest {
    
    @Mock
    private lateinit var repository: WorkoutRepository
    
    private lateinit var viewModel: HomeViewModel
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state is loading`() {
        // Given
        whenever(repository.userPreferences).thenReturn(flowOf(null))
        
        // When
        viewModel = HomeViewModel(repository)
        
        // Then
        assertTrue(viewModel.uiState.value.isLoading)
    }
    
    @Test
    fun `complete onboarding updates repository`() = runTest {
        // Given
        whenever(repository.userPreferences).thenReturn(flowOf(null))
        viewModel = HomeViewModel(repository)
        
        // When
        viewModel.completeOnboarding(TrainingProgram.STANDARD)
        advanceUntilIdle()
        
        // Then
        verify(repository).completeOnboarding(TrainingProgram.STANDARD)
    }
    
    @Test
    fun `loads user preferences on init`() = runTest {
        // Given
        val mockPrefs = com.mentalgym.app.data.local.entity.UserPreferencesEntity(
            id = 1,
            selectedProgram = "STANDARD",
            currentStreak = 5,
            longestStreak = 10,
            onboardingCompleted = true
        )
        whenever(repository.userPreferences).thenReturn(flowOf(mockPrefs))
        
        // When
        viewModel = HomeViewModel(repository)
        advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertEquals(TrainingProgram.STANDARD, uiState.currentProgram)
        assertEquals(5, uiState.currentStreak)
        assertTrue(uiState.isOnboarded)
        assertFalse(uiState.isLoading)
    }
}
