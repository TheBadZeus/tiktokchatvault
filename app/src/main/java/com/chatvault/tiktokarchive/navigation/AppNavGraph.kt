package com.chatvault.tiktokarchive.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.chatvault.tiktokarchive.AppContainer
import com.chatvault.tiktokarchive.ui.conversation.ConversationScreen
import com.chatvault.tiktokarchive.ui.conversation.ConversationViewModel
import com.chatvault.tiktokarchive.ui.details.EvidenceDetailsScreen
import com.chatvault.tiktokarchive.ui.details.EvidenceDetailsViewModel
import com.chatvault.tiktokarchive.ui.inbox.InboxScreen
import com.chatvault.tiktokarchive.ui.inbox.InboxViewModel
import com.chatvault.tiktokarchive.ui.settings.SettingsScreen
import com.chatvault.tiktokarchive.ui.settings.SettingsViewModel

object Routes {
    const val INBOX = "inbox"
    const val CONVERSATION = "conversation/{id}"
    const val DETAILS = "details/{id}"
    const val SETTINGS = "settings"

    fun conversation(id: String) = "conversation/${Uri.encode(id)}"
    fun details(id: String) = "details/${Uri.encode(id)}"
}

@Composable
fun AppNavGraph(container: AppContainer) {
    val nav = rememberNavController()
    val idArg = listOf(navArgument("id") { type = NavType.StringType })

    NavHost(navController = nav, startDestination = Routes.INBOX) {
        composable(Routes.INBOX) {
            val vm: InboxViewModel = viewModel(factory = viewModelFactory {
                initializer { InboxViewModel(container.repository) }
            })
            InboxScreen(
                vm = vm,
                onOpenConversation = { nav.navigate(Routes.conversation(it)) },
                onOpenSettings = { nav.navigate(Routes.SETTINGS) },
            )
        }
        composable(Routes.CONVERSATION, arguments = idArg) { entry ->
            val id = entry.arguments?.getString("id").orEmpty()
            val vm: ConversationViewModel = viewModel(factory = viewModelFactory {
                initializer { ConversationViewModel(container.repository, id) }
            })
            ConversationScreen(
                vm = vm,
                onBack = { nav.popBackStack() },
                onOpenDetails = { nav.navigate(Routes.details(it)) },
            )
        }
        composable(Routes.DETAILS, arguments = idArg) { entry ->
            val id = entry.arguments?.getString("id").orEmpty()
            val vm: EvidenceDetailsViewModel = viewModel(factory = viewModelFactory {
                initializer { EvidenceDetailsViewModel(container.repository, id) }
            })
            EvidenceDetailsScreen(vm = vm, onBack = { nav.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            val vm: SettingsViewModel = viewModel(factory = viewModelFactory {
                initializer { SettingsViewModel(container.repository, container.settings, container::databaseSizeBytes) }
            })
            SettingsScreen(vm = vm, onBack = { nav.popBackStack() })
        }
    }
}
