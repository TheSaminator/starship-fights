package starshipfights.info

import io.ktor.application.*
import io.ktor.features.*
import kotlinx.html.*
import starshipfights.CurrentConfiguration

private fun SECTIONS.devModeCallId(callId: String?) {
	callId?.let { id ->
		section {
			style = if (CurrentConfiguration.isDevEnv) "" else "display:none"
			+"If you think this is a bug, report it with the call ID #"
			+id
			+"."
		}
	}
}

suspend fun ApplicationCall.error400(): HTML.() -> Unit = page("Bad Request", standardNavBar()) {
	section {
		h1 { +"Bad Request" }
		p { +"The request your browser sent was improperly formatted." }
	}
	devModeCallId(callId)
}

suspend fun ApplicationCall.error403(): HTML.() -> Unit = page("Not Allowed", standardNavBar()) {
	section {
		h1 { +"Not Allowed" }
		p { +"You are not allowed to do that." }
	}
	devModeCallId(callId)
}

suspend fun ApplicationCall.error403InvalidCsrf(): HTML.() -> Unit = page("CSRF Validation Failed", standardNavBar()) {
	section {
		h1 { +"CSRF Validation Failed" }
		p { +"Unfortunately, the received CSRF failed to validate. Please try again." }
	}
	devModeCallId(callId)
}

suspend fun ApplicationCall.error404(): HTML.() -> Unit = page("Not Found", standardNavBar()) {
	section {
		h1 { +"Not Found" }
		p { +"Unfortunately, we could not find what you were looking for." }
	}
	devModeCallId(callId)
}

suspend fun ApplicationCall.error429(): HTML.() -> Unit = page("Too Many Requests", standardNavBar()) {
	section {
		h1 { +"Too Many Requests" }
		p { +"Our server is being bogged down in a quagmire of HTTP requests. Please try again later." }
	}
	devModeCallId(callId)
}

suspend fun ApplicationCall.error503(): HTML.() -> Unit = page("Internal Error", standardNavBar()) {
	section {
		h1 { +"Internal Error" }
		p { +"The servers made a bit of a mistake. Please be patient while we clean up our mess." }
	}
	devModeCallId(callId)
}
