package com.qbtester.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.qbtester.app.ui.AppViewModelFactory
import com.qbtester.app.ui.home.HomeScreen
import com.qbtester.app.ui.home.HomeViewModel
import com.qbtester.app.ui.quiz.QuizScreen
import com.qbtester.app.ui.quiz.QuizViewModel
import com.qbtester.app.ui.results.ResultsScreen

private object Routes {
    const val HOME = "home"
    const val QUIZ_FLOW = "quiz_flow"
    const val QUIZ = "quiz"
    const val RESULTS = "results"
}

@Composable
fun QbTesterNavHost(
    factory: AppViewModelFactory,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            val homeViewModel: HomeViewModel = viewModel(factory = factory)
            HomeScreen(
                viewModel = homeViewModel,
                onStartQuiz = { navController.navigate(Routes.QUIZ_FLOW) },
            )
        }

        navigation(startDestination = Routes.QUIZ, route = Routes.QUIZ_FLOW) {
            composable(Routes.QUIZ) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Routes.QUIZ_FLOW)
                }
                val quizViewModel: QuizViewModel = viewModel(parentEntry, factory = factory)
                QuizScreen(
                    viewModel = quizViewModel,
                    onComplete = {
                        navController.navigate(Routes.RESULTS) {
                            popUpTo(Routes.QUIZ) { inclusive = true }
                        }
                    },
                    onExit = { navController.popBackStackToHome() },
                )
            }

            composable(Routes.RESULTS) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Routes.QUIZ_FLOW)
                }
                val quizViewModel: QuizViewModel = viewModel(parentEntry, factory = factory)
                val state by quizViewModel.uiState.collectAsState()
                ResultsScreen(
                    state = state,
                    onPlayAgain = {
                        quizViewModel.playAgain()
                        navController.navigate(Routes.QUIZ) {
                            popUpTo(Routes.RESULTS) { inclusive = true }
                        }
                    },
                    onHome = { navController.popBackStackToHome() },
                )
            }
        }
    }
}

private fun NavHostController.popBackStackToHome() {
    popBackStack(Routes.HOME, inclusive = false)
}
