package net.starshipfights.admin

import net.starshipfights.CurrentConfiguration
import net.starshipfights.data.auth.User
import net.starshipfights.redirect

val User?.isAdmin: Boolean
	get() = when (val discord = CurrentConfiguration.discordClient) {
		null -> CurrentConfiguration.isDevEnv
		else -> discord.ownerId == (this ?: redirect("/login")).discordId
	}
