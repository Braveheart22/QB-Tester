package com.qbtester.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.qbtester.app.di.AppContainer
import com.qbtester.app.ui.home.HomeViewModel
import com.qbtester.app.ui.quiz.QuizViewModel

class AppViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(HomeViewModel::class.java) ->
            HomeViewModel(container.quarterbackRepository) as T
        modelClass.isAssignableFrom(QuizViewModel::class.java) ->
            QuizViewModel(container.quarterbackRepository) as T
        else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
