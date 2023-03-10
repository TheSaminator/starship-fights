@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external object ShaderChunk {
	@nativeGetter
	operator fun get(name: String): String?
	
	@nativeSetter
	operator fun set(name: String, value: String)
	var alphamap_fragment: String
	var alphamap_pars_fragment: String
	var alphatest_fragment: String
	var aomap_fragment: String
	var aomap_pars_fragment: String
	var begin_vertex: String
	var beginnormal_vertex: String
	var bsdfs: String
	var bumpmap_pars_fragment: String
	var clipping_planes_fragment: String
	var clipping_planes_pars_fragment: String
	var clipping_planes_pars_vertex: String
	var clipping_planes_vertex: String
	var color_fragment: String
	var color_pars_fragment: String
	var color_pars_vertex: String
	var color_vertex: String
	var common: String
	var cube_frag: String
	var cube_vert: String
	var cube_uv_reflection_fragment: String
	var defaultnormal_vertex: String
	var depth_frag: String
	var depth_vert: String
	var distanceRGBA_frag: String
	var distanceRGBA_vert: String
	var displacementmap_vertex: String
	var displacementmap_pars_vertex: String
	var emissivemap_fragment: String
	var emissivemap_pars_fragment: String
	var encodings_pars_fragment: String
	var encodings_fragment: String
	var envmap_fragment: String
	var envmap_common_pars_fragment: String
	var envmap_pars_fragment: String
	var envmap_pars_vertex: String
	var envmap_vertex: String
	var equirect_frag: String
	var equirect_vert: String
	var fog_fragment: String
	var fog_pars_fragment: String
	var linedashed_frag: String
	var linedashed_vert: String
	var lightmap_fragment: String
	var lightmap_pars_fragment: String
	var lights_lambert_vertex: String
	var lights_pars_begin: String
	var envmap_physical_pars_fragment: String
	var lights_pars_map: String
	var lights_phong_fragment: String
	var lights_phong_pars_fragment: String
	var lights_physical_fragment: String
	var lights_physical_pars_fragment: String
	var lights_fragment_begin: String
	var lights_fragment_maps: String
	var lights_fragment_end: String
	var logdepthbuf_fragment: String
	var logdepthbuf_pars_fragment: String
	var logdepthbuf_pars_vertex: String
	var logdepthbuf_vertex: String
	var map_fragment: String
	var map_pars_fragment: String
	var map_particle_fragment: String
	var map_particle_pars_fragment: String
	var meshbasic_frag: String
	var meshbasic_vert: String
	var meshlambert_frag: String
	var meshlambert_vert: String
	var meshphong_frag: String
	var meshphong_vert: String
	var meshphysical_frag: String
	var meshphysical_vert: String
	var metalnessmap_fragment: String
	var metalnessmap_pars_fragment: String
	var morphnormal_vertex: String
	var morphtarget_pars_vertex: String
	var morphtarget_vertex: String
	var normal_flip: String
	var normal_frag: String
	var normal_fragment_begin: String
	var normal_fragment_maps: String
	var normal_vert: String
	var normalmap_pars_fragment: String
	var clearcoat_normal_fragment_begin: String
	var clearcoat_normal_fragment_maps: String
	var clearcoat_pars_fragment: String
	var packing: String
	var points_frag: String
	var points_vert: String
	var shadow_frag: String
	var shadow_vert: String
	var premultiplied_alpha_fragment: String
	var project_vertex: String
	var roughnessmap_fragment: String
	var roughnessmap_pars_fragment: String
	var shadowmap_pars_fragment: String
	var shadowmap_pars_vertex: String
	var shadowmap_vertex: String
	var shadowmask_pars_fragment: String
	var skinbase_vertex: String
	var skinning_pars_vertex: String
	var skinning_vertex: String
	var skinnormal_vertex: String
	var specularmap_fragment: String
	var specularmap_pars_fragment: String
	var tonemapping_fragment: String
	var tonemapping_pars_fragment: String
	var uv2_pars_fragment: String
	var uv2_pars_vertex: String
	var uv2_vertex: String
	var uv_pars_fragment: String
	var uv_pars_vertex: String
	var uv_vertex: String
	var worldpos_vertex: String
}
