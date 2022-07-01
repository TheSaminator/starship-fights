package net.starshipfights.admin

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

private val announcementFlow = MutableSharedFlow<String>(replay = 4, extraBufferCapacity = 1020)

val announcements = announcementFlow.asSharedFlow()

suspend fun sendAdminAnnouncement(announcement: String) {
	announcementFlow.emit(announcement)
}
