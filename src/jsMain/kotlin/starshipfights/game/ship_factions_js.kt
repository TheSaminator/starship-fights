package starshipfights.game

import kotlinx.html.TagConsumer
import kotlinx.html.p
import kotlinx.html.style

fun Faction?.writeBlurb(tagConsumer: TagConsumer<*>) = if (this == null)
	tagConsumer.p {
		style = "text-align:center"
		+"Select a faction by clicking one of the flags above"
	}
else
	tagConsumer.blurbDesc()
