# Mental Gym - Implementation Guide

This guide provides detailed information about the Mental Gym Android app implementation.

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Key Components](#key-components)
3. [Data Flow](#data-flow)
4. [Adding New Features](#adding-new-features)
5. [Best Practices](#best-practices)

## Architecture Overview

### Clean Architecture Layers

```
Presentation Layer (UI)
    ↓ ViewModel communicates via
Domain Layer (Business Logic)
    ↓ Repository pattern
Data Layer (Database & Storage)
```

### Dependency Rule
- **Outer layers depend on inner layers**
- Domain layer has no dependencies on other layers
- Data layer depends only on domain
- UI layer depends on domain and uses ViewModels

## Key Components

### 1. Data Layer

#### Room Database
```kotlin
@Database(
    entities = [
        WorkoutCompletionEntity::class,
        UserPreferencesEntity::class,
        ExerciseProgressEntity::class
    ],
    version = 1
)
abstract class MentalGymDatabase : RoomDatabase()
```

**Entities:**
- `WorkoutCompletionEntity` - Stores completed workout records
- `UserPreferencesEntity` - User settings and program selection
- `ExerciseProgressEntity` - Exercise-level progress tracking

#### Repository Pattern
```kotlin
@Singleton
class WorkoutRepository @Inject constructor(
    private val workoutCompletionDao: WorkoutCompletionDao,
    private val userPreferencesDao: UserPreferencesDao,
    private val exerciseProgressDao: ExerciseProgressDao
)
```

**Responsibilities:**
- Abstracts data sources from UI
- Provides clean API for data operations
- Handles data transformations
- Manages caching strategies

### 2. Domain Layer

#### Models
Core business entities without Android dependencies:
- `CognitiveSystem` - Enum of 7 brain systems
- `TrainingProgram` - Essential/Standard/Elite
- `Exercise` - Individual cognitive exercise
- `WorkoutSession` - Daily workout configuration
- `UserProgress` - Aggregated user metrics

#### Content Provider
```kotlin
object WorkoutContentProvider {
    fun getEliteWeeklyPlan(): List<WorkoutSession>
    fun getStandardWeeklyPlan(): List<WorkoutSession>
    fun getEssentialWeeklyPlan(): List<WorkoutSession>
}
```

**Purpose:**
- Centralized workout content management
- Easy to modify exercises
- Supports future dynamic content loading

### 3. Presentation Layer

#### MVVM Pattern
```
View (Composable) ← StateFlow ← ViewModel ← Repository
```

**Example:**
```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    // Business logic here
}
```

#### Composable Screens
- `HomeScreen` - Main dashboard
- `OnboardingScreen` - First-time setup
- `WorkoutScreen` - Exercise execution
- `ProgressScreen` - Analytics and history

## Data Flow

### 1. User Completes Workout

```
WorkoutScreen
    ↓ onComplete(score)
WorkoutViewModel
    ↓ completeWorkout()
WorkoutRepository
    ↓ insertCompletion()
Room Database
    ↓ Flow<List<WorkoutCompletionEntity>>
HomeViewModel (observes changes)
    ↓ StateFlow update
HomeScreen (recomposes)
```

### 2. Displaying Today's Workout

```
App Launch
    ↓
HomeViewModel.init()
    ↓ loadUserData()
Repository.userPreferences
    ↓ Flow from Room
HomeViewModel
    ↓ getWeekPlanForProgram()
WorkoutContentProvider
    ↓ getTodaysWorkout()
HomeViewModel
    ↓ _uiState.update()
HomeScreen
    ↓ Composable renders
```

## Adding New Features

### Adding a New Cognitive Exercise

1. **Define the exercise in WorkoutContentProvider.kt:**
```kotlin
private fun getNewExercises() = listOf(
    Exercise(
        id = "new_exercise_id",
        name = "New Exercise Name",
        cognitiveSystem = CognitiveSystem.ATTENTION_FOCUS,
        description = "Exercise description",
        durationMinutes = 10,
        difficultyLevel = 5,
        instructions = listOf(
            "Step 1",
            "Step 2",
            "Step 3"
        )
    )
)
```

2. **Add to relevant workout plan:**
```kotlin
fun getEliteWeeklyPlan(): List<WorkoutSession> = listOf(
    WorkoutSession(
        id = "monday_focus",
        dayOfWeek = DayOfWeek.MONDAY,
        cognitiveSystem = CognitiveSystem.ATTENTION_FOCUS,
        exercises = getNewExercises(), // Add here
        totalDurationMinutes = 20
    )
)
```

### Adding a New Cognitive System

1. **Add to CognitiveSystem enum:**
```kotlin
enum class CognitiveSystem(val displayName: String, val description: String) {
    // ... existing systems
    NEW_SYSTEM(
        "New System Name",
        "Description of the system"
    )
}
```

2. **Add color mapping in theme/Color.kt:**
```kotlin
val NewSystemColor = Color(0xFFHEXCODE)
```

3. **Update helper functions:**
```kotlin
private fun getCognitiveSystemColor(system: CognitiveSystem): Color {
    return when (system) {
        // ... existing cases
        CognitiveSystem.NEW_SYSTEM -> NewSystemColor
    }
}

private fun getCognitiveSystemIcon(system: CognitiveSystem): ImageVector {
    return when (system) {
        // ... existing cases
        CognitiveSystem.NEW_SYSTEM -> Icons.Default.YourIcon
    }
}
```

### Adding a New Screen

1. **Create the route in Screen.kt:**
```kotlin
sealed class Screen(val route: String) {
    // ... existing screens
    object NewScreen : Screen("new_screen")
}
```

2. **Create ViewModel:**
```kotlin
@HiltViewModel
class NewScreenViewModel @Inject constructor(
    private val repository: YourRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(NewScreenUiState())
    val uiState: StateFlow<NewScreenUiState> = _uiState.asStateFlow()
    
    // Your logic here
}
```

3. **Create Composable:**
```kotlin
@Composable
fun NewScreen(
    viewModel: NewScreenViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = { /* TopBar */ }
    ) { padding ->
        // Your UI here
    }
}
```

4. **Add to NavHost in MainActivity:**
```kotlin
composable(Screen.NewScreen.route) {
    NewScreen(
        onNavigateBack = { navController.popBackStack() }
    )
}
```

## Best Practices

### State Management

**DO:**
```kotlin
// Immutable state
data class UiState(
    val items: List<Item> = emptyList(),
    val isLoading: Boolean = false
)

// Update with copy
_uiState.update { current ->
    current.copy(isLoading = true)
}
```

**DON'T:**
```kotlin
// Mutable state
var items: MutableList<Item> = mutableListOf()
var isLoading = false

// Direct mutation
items.add(newItem)
```

### Composable Functions

**DO:**
```kotlin
@Composable
fun MyComponent(
    data: Data,
    modifier: Modifier = Modifier,
    onAction: () -> Unit
) {
    // Composable implementation
}
```

**DON'T:**
```kotlin
@Composable
fun MyComponent() {
    // Accessing ViewModel directly
    val viewModel: MyViewModel = viewModel()
    // This breaks reusability
}
```

### Database Operations

**DO:**
```kotlin
viewModelScope.launch {
    repository.insertData(data)
}
```

**DON'T:**
```kotlin
// Never on main thread
runBlocking {
    repository.insertData(data)
}
```

### Navigation

**DO:**
```kotlin
// Pass data through ViewModel or SavedStateHandle
navController.navigate(Screen.Detail.route)
viewModel.selectItem(itemId)
```

**DON'T:**
```kotlin
// Don't pass complex objects in navigation
navController.navigate("detail/${complexObject}")
```

### Resource Management

**DO:**
```kotlin
// Use string resources
Text(stringResource(R.string.workout_title))

// Use dimension resources
Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))
```

**DON'T:**
```kotlin
// Hard-coded strings
Text("Workout Title")

// Magic numbers
Spacer(modifier = Modifier.height(16.dp))
```

### Performance

**DO:**
```kotlin
// Remember expensive calculations
val expensiveValue = remember(key) {
    expensiveCalculation()
}

// Derive state
val derivedState by remember {
    derivedStateOf { 
        items.filter { it.isActive }
    }
}
```

**DON'T:**
```kotlin
// Recalculate on every recomposition
val filtered = items.filter { it.isActive }
```

## Testing Strategy

### Unit Tests
- ViewModels business logic
- Repository data operations
- Utility functions

### Integration Tests
- Database operations
- Repository with fake DAOs

### UI Tests
- User workflows
- Navigation
- State changes

## Debugging Tips

### Logging State Changes
```kotlin
_uiState.onEach { state ->
    Log.d(TAG, "State updated: $state")
}.launchIn(viewModelScope)
```

### Compose Preview
```kotlin
@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    MentalGymTheme {
        HomeContent(
            uiState = HomeUiState(/* sample data */),
            onStartWorkout = {},
            onNavigateToProgress = {}
        )
    }
}
```

### Database Inspection
Use Android Studio's Database Inspector:
1. Run app on device/emulator
2. View → Tool Windows → App Inspection
3. Select Database Inspector tab

## Common Issues & Solutions

### Issue: Hilt dependency not found
**Solution:** Ensure `@HiltAndroidApp` is on Application class and `@AndroidEntryPoint` is on Activity

### Issue: Compose recomposing too often
**Solution:** Use `remember` and `derivedStateOf` for expensive calculations

### Issue: Database query on main thread
**Solution:** Always use `suspend` functions and call from coroutine scope

### Issue: Navigation state not preserved
**Solution:** Use `SavedStateHandle` in ViewModel

## Performance Optimization

1. **LazyColumn optimization:**
   - Use `key` parameter
   - Keep item content simple
   - Use `contentPadding` instead of wrapping in Column

2. **State hoisting:**
   - Hoist state to prevent unnecessary recompositions
   - Use `remember` wisely

3. **Database queries:**
   - Use `Flow` for reactive updates
   - Index frequently queried columns
   - Use pagination for large datasets

## Security Considerations

1. **Data encryption:**
   - Room supports SQLCipher for encrypted databases
   - Use Android Keystore for sensitive data

2. **ProGuard:**
   - Keep data models unobfuscated for Room
   - Test release builds thoroughly

---

For more information, see the main README.md or contact the development team.
