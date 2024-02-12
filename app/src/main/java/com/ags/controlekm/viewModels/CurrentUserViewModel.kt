package com.ags.controlekm.viewModels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ags.controlekm.database.firebaseRepositories.FirebaseCurrentUserRepository
import com.ags.controlekm.models.CurrentUser
import com.ags.controlekm.database.repositorys.CurrentUserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CurrentUserViewModel @Inject constructor(
    private val firebaseRepository: FirebaseCurrentUserRepository,
    private val repository: CurrentUserRepository
): ViewModel() {
    var currentUserData: Flow<CurrentUser> = repository.currentUser

    init {
        viewModelScope.launch(Dispatchers.IO) {
            firebaseRepository.getCurrentUser().collect{ currentUser ->
                if (currentUser != null) {
                    repository.insert(currentUser)
                    firebaseRepository.emailIsVerifield(
                        FirebaseAuth.getInstance().currentUser?.isEmailVerified ?: false
                    )
                }
            }
        }
    }

    fun insert(currentUser: CurrentUser) {
        viewModelScope.launch {
            repository.insert(currentUser)
        }
    }

    fun update(currentUser: CurrentUser) {
        viewModelScope.launch {
            repository.update(currentUser)
        }
    }

    fun delete(currentUser: CurrentUser) {
        viewModelScope.launch {
            repository.delete(currentUser)
        }
    }
}