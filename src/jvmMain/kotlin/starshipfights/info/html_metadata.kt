package starshipfights.info

import kotlinx.html.HEAD

data class PageMetadata(
	val title: String,
	val description: String,
) {
	companion object {
		val default = PageMetadata(
			title = "Starship Fights",
			description = "Starship Fights is a space fleet battle game. Choose your allegiance, create your admiral, build up your fleet, and destroy your enemies' fleets with it!",
		)
	}
}

fun HEAD.metadata(pageMetadata: PageMetadata, url: String) {
	metaOG("og:title", pageMetadata.title)
	metaOG("og:description", pageMetadata.description)
	metaOG("og:image", "https://starshipfights.net/static/images/embed-logo.png")
	metaOG("og:type", "website")
	metaOG("og:site_name", "Starship Fights")
	metaOG("og:url", url)
}
