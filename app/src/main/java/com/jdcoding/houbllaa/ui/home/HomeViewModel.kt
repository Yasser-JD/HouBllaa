package com.jdcoding.houbllaa.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jdcoding.houbllaa.data.repository.EventRepository
import com.jdcoding.houbllaa.data.repository.NoteRepository
import com.jdcoding.houbllaa.data.repository.UserRepository
import com.jdcoding.houbllaa.models.Event
import com.jdcoding.houbllaa.models.Note
import com.jdcoding.houbllaa.models.User
import com.jdcoding.houbllaa.utils.PregnancyCalculator
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * ViewModel for the Home/Dashboard screen
 */
class HomeViewModel(
    private val userRepository: UserRepository,
    private val noteRepository: NoteRepository,
    private val eventRepository: EventRepository
) : ViewModel() {
    
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user
    
    private val _currentWeek = MutableLiveData<Int>()
    val currentWeek: LiveData<Int> = _currentWeek
    
    private val _currentDay = MutableLiveData<Int>()
    val currentDay: LiveData<Int> = _currentDay
    
    private val _daysLeft = MutableLiveData<Int>()
    val daysLeft: LiveData<Int> = _daysLeft
    
    private val _trimester = MutableLiveData<Int>()
    val trimester: LiveData<Int> = _trimester
    
    private val _todayNote = MutableLiveData<Note?>()
    val todayNote: LiveData<Note?> = _todayNote
    
    private val _upcomingEvents = MutableLiveData<List<Event>>()
    val upcomingEvents: LiveData<List<Event>> = _upcomingEvents
    
    private val _babySize = MutableLiveData<String>()
    val babySize: LiveData<String> = _babySize
    
    private val _tipOfTheDay = MutableLiveData<String>()
    val tipOfTheDay: LiveData<String> = _tipOfTheDay
    
    init {
        loadUserData()
    }
    
    /**
     * Load user data and calculate pregnancy information
     */
    private fun loadUserData() {
        val userId = userRepository.getCurrentUserId()
        if (userId != null) {
            viewModelScope.launch {
                try {
                    // First, try to sync with Firestore to get the latest data
                    firestoreSync(userId)
                    
                    // Then use Flow collection to observe changes
                    userRepository.getUserProfile(userId).collect { userProfile ->
                        _user.value = userProfile
                        
                        if (userProfile != null) {
                            calculatePregnancyStatus(userProfile)
                            loadTodayNote(userId)
                            loadUpcomingEvents(userId)
                            loadTipOfTheDay()
                        }
                    }
                } catch (e: Exception) {
                    // If there's an error syncing with Firestore, fall back to local data
                    Log.e("HomeViewModel", "Error syncing with Firestore: ${e.message}")
                    // Continue with local data
                    userRepository.getUserProfile(userId).collect { userProfile ->
                        _user.value = userProfile
                        
                        if (userProfile != null) {
                            calculatePregnancyStatus(userProfile)
                            loadTodayNote(userId)
                            loadUpcomingEvents(userId)
                            loadTipOfTheDay()
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Force sync with Firestore
     */
    private suspend fun firestoreSync(userId: String) {
        // Get data directly from Firestore
        val result = userRepository.syncUserFromFirestore(userId)
        if (result.isFailure) {
            throw result.exceptionOrNull() ?: Exception("Unknown error syncing with Firestore")
        }
    }
    
    /**
     * Calculate pregnancy week, days left, and trimester
     */
    private fun calculatePregnancyStatus(user: User) {
        // Calculate from LMP if available, otherwise from conception date or estimated due date
        val referenceDate = when {
            user.lastMenstrualPeriod != null -> user.lastMenstrualPeriod
            user.conceptionDate != null -> {
                // Adjust by 2 weeks if using conception date
                val calendar = Calendar.getInstance()
                calendar.time = user.conceptionDate
                calendar.add(Calendar.DAY_OF_YEAR, -14)
                calendar.time
            }
            user.estimatedDueDate != null -> {
                // Calculate backwards from due date
                val calendar = Calendar.getInstance()
                calendar.time = user.estimatedDueDate
                calendar.add(Calendar.DAY_OF_YEAR, -280)
                calendar.time
            }
            else -> null
        }
        
        if (referenceDate != null) {
            val (week, day) = PregnancyCalculator.getCurrentPregnancyWeek(referenceDate)
            _currentWeek.value = week
            _currentDay.value = day
            _trimester.value = PregnancyCalculator.getCurrentTrimester(week)
            
            if (user.estimatedDueDate != null) {
                _daysLeft.value = PregnancyCalculator.getDaysUntilDueDate(user.estimatedDueDate)
            }
            
            // Set baby size comparison based on week
            _babySize.value = getBabySizeComparison(week)
        }
    }
    
    /**
     * Get today's note if it exists
     */
    private fun loadTodayNote(userId: String) {
        viewModelScope.launch {
            val today = Calendar.getInstance().time
            val startOfDay = Calendar.getInstance().apply {
                time = today
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
            val endOfDay = Calendar.getInstance().apply {
                time = today
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.time
            
            noteRepository.getNotesByDateRange(userId, startOfDay, endOfDay).collect { notes ->
                _todayNote.value = notes.firstOrNull()
            }
        }
    }
    
    /**
     * Save a new daily note
     */
    fun saveDailyNote(content: String, mood: String? = null) {
        val userId = userRepository.getCurrentUserId() ?: return
        
        viewModelScope.launch {
            val existingNote = _todayNote.value
            if (existingNote != null) {
                // Update existing note
                val updatedNote = existingNote.copy(
                    content = content,
                    mood = mood,
                    updatedAt = Calendar.getInstance().time
                )
                noteRepository.updateNote(updatedNote)
            } else {
                // Create new note
                val newNote = Note(
                    userId = userId,
                    date = Calendar.getInstance().time,
                    content = content,
                    mood = mood,
                    createdAt = Calendar.getInstance().time,
                    updatedAt = Calendar.getInstance().time
                )
                noteRepository.createNote(newNote)
            }
            
            // Reload today's note
            loadTodayNote(userId)
        }
    }
    
    /**
     * Load upcoming events (next 7 days)
     */
    private fun loadUpcomingEvents(userId: String) {
        viewModelScope.launch {
            val today = Calendar.getInstance().time
            val nextWeek = Calendar.getInstance().apply {
                time = today
                add(Calendar.DAY_OF_YEAR, 7)
            }.time
            
            eventRepository.getEventsByDateRange(userId, today, nextWeek).collect { events ->
                _upcomingEvents.value = events
            }
        }
    }
    
    /**
     * Load tip of the day based on current pregnancy week
     */
    private fun loadTipOfTheDay() {
        val week = _currentWeek.value ?: 0
        _tipOfTheDay.value = getPregnancyTip(week)
    }
    
    /**
     * Refresh the dashboard data
     */
    fun refreshDashboard() {
        loadUserData()
        viewModelScope.launch {
            val userId = userRepository.getCurrentUserId() ?: return@launch
            try {
                firestoreSync(userId)
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error refreshing data: ${e.message}")
            }
        }
    }
    
    /**
     * Helper method to get baby size comparison for a given week
     */
    private fun getBabySizeComparison(week: Int): String {
        return when (week) {
            4 -> "a poppy seed"
            5 -> "a sesame seed"
            6 -> "a lentil"
            7 -> "a blueberry"
            8 -> "a kidney bean"
            9 -> "a grape"
            10 -> "a kumquat"
            11 -> "a fig"
            12 -> "a lime"
            13 -> "a lemon"
            14 -> "an orange"
            15 -> "an apple"
            16 -> "an avocado"
            17 -> "a pear"
            18 -> "a bell pepper"
            19 -> "a tomato"
            20 -> "a banana"
            21 -> "a carrot"
            22 -> "a spaghetti squash"
            23 -> "a grapefruit"
            24 -> "an ear of corn"
            25 -> "a rutabaga"
            26 -> "a scallion"
            27 -> "a cauliflower"
            28 -> "an eggplant"
            29 -> "a butternut squash"
            30 -> "a cabbage"
            31 -> "a coconut"
            32 -> "a jicama"
            33 -> "a pineapple"
            34 -> "a cantaloupe"
            35 -> "a honeydew melon"
            36 -> "a romaine lettuce"
            37 -> "a bunch of Swiss chard"
            38 -> "a leek"
            39 -> "a watermelon"
            40 -> "a small pumpkin"
            else -> "a little miracle"
        }
    }
    
    /**
     * Helper method to get pregnancy tip for a given week
     * In a real app, these would come from the API or Firestore
     */
    private fun getPregnancyTip(week: Int): String {
        return when {
            week < 5 -> "Take prenatal vitamins with folic acid to support early development."
            week < 9 -> "Stay hydrated and eat small, frequent meals to combat morning sickness."
            week < 13 -> "Your baby's organs are developing. Avoid alcohol and limit caffeine."
            week < 17 -> "You might start feeling better as morning sickness subsides."
            week < 21 -> "You may start to feel your baby move! It often feels like flutters."
            week < 25 -> "Your baby can now hear your voice. Talk and sing to your baby!"
            week < 29 -> "Stay active with pregnancy-safe exercises like walking and swimming."
            week < 33 -> "Start preparing for your baby's arrival by setting up the nursery."
            week < 37 -> "Pack your hospital bag and finalize your birth plan."
            else -> "Your baby is considered full-term! Labor could begin anytime now."
        }
    }
    
    /**
     * Factory for creating HomeViewModel with the necessary dependencies
     */
    class Factory(
        private val userRepository: UserRepository,
        private val noteRepository: NoteRepository,
        private val eventRepository: EventRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(userRepository, noteRepository, eventRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
