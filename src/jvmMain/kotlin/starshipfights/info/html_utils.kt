package starshipfights.info

import kotlinx.html.*
import starshipfights.auth.CsrfProtector
import starshipfights.data.Id
import starshipfights.data.auth.UserSession

var A.method: String?
	get() = attributes["data-method"]
	set(value) {
		if (value != null)
			attributes["data-method"] = value
		else
			attributes.remove("data-method")
	}

fun A.csrfToken(cookie: Id<UserSession>) {
	attributes["data-csrf-token"] = CsrfProtector.newNonce(cookie, this.href)
}

fun FORM.csrfToken(cookie: Id<UserSession>) = hiddenInput {
	name = CsrfProtector.csrfInputName
	value = CsrfProtector.newNonce(cookie, this@csrfToken.action)
}

fun interface SECTIONS {
	fun section(classes: String?, body: SECTION.() -> Unit)
	fun section(body: SECTION.() -> Unit) = section(null, body)
}

fun MAIN.sectioned(): SECTIONS = MainSections(this)

private class MainSections(private val delegate: MAIN) : SECTIONS {
	override fun section(classes: String?, body: SECTION.() -> Unit) {
		delegate.section(classes, body)
	}
}
