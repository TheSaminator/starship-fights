package starshipfights.info

import io.ktor.application.*
import io.ktor.features.*
import kotlinx.html.*
import starshipfights.CurrentConfiguration

private fun MAIN.devModeCallId(callId: String?) {
	callId?.let { id ->
		section {
			style = if (CurrentConfiguration.isDevEnv) "" else "display:none"
			+"If you think this is a bug, report it with the call ID #"
			+id
			+"."
		}
	}
}

suspend fun ApplicationCall.error400(): HTML.() -> Unit = page("Bad Request", standardNavBar(), IndexSidebar) {
	section {
		h1 { +"Bad Request" }
		p { +"The request your browser sent was improperly formatted." }
	}
	devModeCallId(callId)
}

suspend fun ApplicationCall.error403(): HTML.() -> Unit = page("Not Allowed", standardNavBar(), IndexSidebar) {
	section {
		h1 { +"Not Allowed" }
		p { +"You are not allowed to do that." }
	}
	devModeCallId(callId)
}

suspend fun ApplicationCall.error404(): HTML.() -> Unit = page("Not Found", standardNavBar(), IndexSidebar) {
	section {
		h1 { +"Not Found" }
		p { +"Unfortunately, we could not find what you were looking for." }
	}
	devModeCallId(callId)
}

suspend fun ApplicationCall.error503(): HTML.() -> Unit = page("Internal Error", standardNavBar(), IndexSidebar) {
	section {
		h1 { +"Internal Error" }
		p { +"The servers made a bit of a mistake. Please be patient while we fix our mess." }
	}
	devModeCallId(callId)
}
