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

var META.property: String?
	get() = attributes["property"]
	set(value) {
		if (value != null)
			attributes["property"] = value
		else
			attributes.remove("property")
	}

fun HEAD.metaOG(property: String, content: String) = meta {
	this.property = property
	this.content = content
}
