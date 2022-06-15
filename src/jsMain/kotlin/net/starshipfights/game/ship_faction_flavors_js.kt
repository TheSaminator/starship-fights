package net.starshipfights.game

import externals.threejs.*

fun IntColor.to3JS() = Color(this.toString())

private val black = Color("#000000")
private val white = Color("#FFFFFF")

private val shipShaderMaterial: ShaderMaterial by lazy {
	val customFragmentShader = """
		|#define PHONG
		|
		|uniform vec3 diffuse;
		|uniform vec3 emissive;
		|uniform vec3 specular;
		|uniform float shininess;
		|uniform float opacity;
		|
		|#include <common>
		|#include <packing>
		|#include <dithering_pars_fragment>
		|#include <color_pars_fragment>
		|#include <uv_pars_fragment>
		|#include <uv2_pars_fragment>
		|#include <map_pars_fragment>
		|#include <alphamap_pars_fragment>
		|#include <alphatest_pars_fragment>
		|#include <aomap_pars_fragment>
		|#include <lightmap_pars_fragment>
		|#include <emissivemap_pars_fragment>
		|#include <envmap_common_pars_fragment>
		|#include <envmap_pars_fragment>
		|#include <cube_uv_reflection_fragment>
		|#include <fog_pars_fragment>
		|#include <bsdfs>
		|#include <lights_pars_begin>
		|#include <normal_pars_fragment>
		|#include <lights_phong_pars_fragment>
		|#include <shadowmap_pars_fragment>
		|#include <bumpmap_pars_fragment>
		|#include <normalmap_pars_fragment>
		|#include <specularmap_pars_fragment>
		|#include <logdepthbuf_pars_fragment>
		|#include <clipping_planes_pars_fragment>
		|
		|uniform vec3 oldColorDiff;
		|uniform vec3 oldColorSpec;
		|uniform vec3 newColorDiff;
		|uniform vec3 newColorSpec;
		|uniform vec3 tintColor;
		|
		|// Add the mapTexelToLinear function manually
		|vec4 mapTexelToLinear( vec4 value ) {
		|	return LinearToLinear( value );
		|}
		|
		|void main() {
		|	#include <clipping_planes_fragment>
		|
		|	vec4 diffuseColor = vec4( diffuse, opacity );
		|	ReflectedLight reflectedLight = ReflectedLight( vec3( 0.0 ), vec3( 0.0 ), vec3( 0.0 ), vec3( 0.0 ) );
		|	vec3 totalEmissiveRadiance = emissive;
		|
		|	#include <logdepthbuf_fragment>
		|
		|	// Replaces the include of map_fragment
		|#ifdef USE_MAP
		|	vec4 texelColor = texture2D( map, vUv );
		|	if (texelColor.rgb == oldColorDiff) {
		|		texelColor = vec4(newColorDiff, texelColor.a);
		|	}
		|	texelColor = mapTexelToLinear( texelColor );
		|	diffuseColor *= texelColor;
		|#endif
		|
		|	#include <color_fragment>
		|	#include <alphamap_fragment>
		|	#include <alphatest_fragment>
		|
		|	// Replaces the include of specularmap_fragment
		|	vec3 specularStrength;
		|#ifdef USE_SPECULARMAP
		|	vec4 texelSpecular = texture2D( specularMap, vUv );
		|	if (texelSpecular.rgb == oldColorSpec) {
		|		texelSpecular = vec4(oldColorSpec, texelSpecular.a);
		|	}
		|	specularStrength = texelSpecular.rgb;
		|#else
		|	specularStrength = vec3( 1.0, 1.0, 1.0 );
		|#endif
		|
		|	#include <normal_fragment_begin>
		|	#include <normal_fragment_maps>
		|	#include <emissivemap_fragment>
		|
		|	// accumulation
		|	#include <lights_phong_fragment>
		|	#include <lights_fragment_begin>
		|	#include <lights_fragment_maps>
		|	#include <lights_fragment_end>
		|
		|	// modulation
		|	#include <aomap_fragment>
		|	vec3 outgoingLight = reflectedLight.directDiffuse + reflectedLight.indirectDiffuse + reflectedLight.directSpecular + reflectedLight.indirectSpecular + totalEmissiveRadiance;
		|
		|	#include <envmap_fragment>
		|	#include <output_fragment>
		|
		|	// Inserted custom code
		|	gl_FragColor = vec4(gl_FragColor.rgb * tintColor, gl_FragColor.a);
		|	// End of custom code
		|
		|	#include <tonemapping_fragment>
		|	#include <encodings_fragment>
		|	#include <fog_fragment>
		|	#include <premultiplied_alpha_fragment>
		|	#include <dithering_fragment>
		|}
	""".trimMargin()
	
	ShaderMaterial(
		configure {
			uniforms = UniformsUtils.merge(
				arrayOf(
					ShaderLib.phong.uniforms,
					configure<AnonymousStruct8> {
						this["oldColorDiff"] = configure { value = black }
						this["oldColorSpec"] = configure { value = black }
						this["newColorDiff"] = configure { value = black }
						this["newColorSpec"] = configure { value = black }
						this["tintColor"] = configure { value = white }
					}
				)
			).unsafeCast<AnonymousStruct8>()
			defines = configure<StringDict<Any>> {
				this["USE_UV"] = ""
				this["USE_MAP"] = ""
				this["USE_SPECULARMAP"] = ""
			}
			vertexShader = ShaderLib.phong.vertexShader
			fragmentShader = customFragmentShader
			lights = true
		}
	)
}

fun MeshPhongMaterial.forShip(faction: Faction, flavor: FactionFlavor): ShaderMaterial {
	return shipShaderMaterial.clone().unsafeCast<ShaderMaterial>().also { material ->
		material.uniforms["diffuse"]?.value?.unsafeCast<Color>()?.copy(color)
		material.uniforms["specular"]?.value?.unsafeCast<Color>()?.copy(specular)
		material.uniforms["shininess"]?.value = shininess.toDouble().coerceAtLeast(EPSILON)
		
		map?.let { material.uniforms["map"]?.value = it }
		specularMap?.let { material.uniforms["specularMap"]?.value = it }
		
		faction.trimColor?.let { oldColorDiff ->
			val newColorDiff = flavor.colorReplacement
			
			val oldColorSpec = oldColorDiff.highlight
			val newColorSpec = newColorDiff.highlight
			
			material.uniforms["oldColorDiff"]?.value?.unsafeCast<Color>()?.copy(oldColorDiff.to3JS())
			material.uniforms["oldColorSpec"]?.value?.unsafeCast<Color>()?.copy(oldColorSpec.to3JS())
			material.uniforms["newColorDiff"]?.value?.unsafeCast<Color>()?.copy(newColorDiff.to3JS())
			material.uniforms["newColorSpec"]?.value?.unsafeCast<Color>()?.copy(newColorSpec.to3JS())
		} ?: material.uniforms["tintColor"]?.value?.unsafeCast<Color>()?.copy(flavor.colorReplacement.to3JS())
	}
}
