package starshipfights.info

import kotlinx.html.A

var A.method: String?
	get() = attributes["data-method"]
	set(value) {
		if (value != null)
			attributes["data-method"] = value
		else
			attributes.remove("data-method")
	}
