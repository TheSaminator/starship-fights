package net.starshipfights.game

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun errorMain(message: String) {
	coroutineScope {
		launch { setupBackground() }
		launch { Popup.Error(message).display() }
	}
}
