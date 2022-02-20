package starshipfights.info

import kotlinx.html.HEAD

data class PageMetadata(
	val title: String,
	val description: String,
	val type: PageMetadataType,
) {
	companion object {
		val default = PageMetadata(
			title = "Starship Fights",
			description = "Starship Fights is a space fleet battle game. Choose your allegiance, create your admiral, build up your fleet, and destroy your enemies' fleets with it!",
			type = PageMetadataType.Website,
		)
	}
}

sealed class PageMetadataType {
	object Website : PageMetadataType()
	
	data class Profile(
		val name: String,
		val isFemale: Boolean?,
	) : PageMetadataType()
}

fun HEAD.metadata(pageMetadata: PageMetadata, url: String) {
	metaOG("og:title", pageMetadata.title)
	metaOG("og:description", pageMetadata.description)
	metaOG("og:url", url)
	
	when (pageMetadata.type) {
		is PageMetadataType.Profile -> {
			metaOG("og:type", "profile")
			metaOG("og:profile:username", pageMetadata.type.name)
			pageMetadata.type.isFemale?.let {
				metaOG("og:profile:gender", if (it) "female" else "male")
			}
		}
		PageMetadataType.Website -> {
			metaOG("og:type", "website")
		}
	}
	
	metaOG("og:site_name", "Starship Fights")
	metaOG("og:image", "https://starshipfights.net/static/images/embed-logo.png")
}
