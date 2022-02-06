@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external interface IUniform<TValue> {
	var value: TValue
}

external interface IUniform__0 : IUniform<Any>

external interface AnonymousStruct9 {
	var diffuse: IUniform__0
	var opacity: IUniform__0
	var map: IUniform__0
	var uvTransform: IUniform__0
	var uv2Transform: IUniform__0
	var alphaMap: IUniform__0
}

external interface AnonymousStruct10 {
	var specularMap: IUniform__0
}

external interface AnonymousStruct11 {
	var envMap: IUniform__0
	var flipEnvMap: IUniform__0
	var reflectivity: IUniform__0
	var refractionRatio: IUniform__0
	var maxMipLevel: IUniform__0
}

external interface AnonymousStruct12 {
	var aoMap: IUniform__0
	var aoMapIntensity: IUniform__0
}

external interface AnonymousStruct13 {
	var lightMap: IUniform__0
	var lightMapIntensity: IUniform__0
}

external interface AnonymousStruct14 {
	var emissiveMap: IUniform__0
}

external interface AnonymousStruct15 {
	var bumpMap: IUniform__0
	var bumpScale: IUniform__0
}

external interface AnonymousStruct16 {
	var normalMap: IUniform__0
	var normalScale: IUniform__0
}

external interface AnonymousStruct17 {
	var displacementMap: IUniform__0
	var displacementScale: IUniform__0
	var displacementBias: IUniform__0
}

external interface AnonymousStruct18 {
	var roughnessMap: IUniform__0
}

external interface AnonymousStruct19 {
	var metalnessMap: IUniform__0
}

external interface AnonymousStruct20 {
	var gradientMap: IUniform__0
}

external interface AnonymousStruct21 {
	var fogDensity: IUniform__0
	var fogNear: IUniform__0
	var fogFar: IUniform__0
	var fogColor: IUniform__0
}

external interface AnonymousStruct22 {
	var direction: Any
	var color: Any
}

external interface AnonymousStruct23 {
	var value: Array<Any>
	var properties: AnonymousStruct22
}

external interface AnonymousStruct24 {
	var shadowBias: Any
	var shadowNormalBias: Any
	var shadowRadius: Any
	var shadowMapSize: Any
}

external interface AnonymousStruct25 {
	var value: Array<Any>
	var properties: AnonymousStruct24
}

external interface AnonymousStruct26 {
	var color: Any
	var position: Any
	var direction: Any
	var distance: Any
	var coneCos: Any
	var penumbraCos: Any
	var decay: Any
}

external interface AnonymousStruct27 {
	var value: Array<Any>
	var properties: AnonymousStruct26
}

external interface AnonymousStruct28 {
	var value: Array<Any>
	var properties: AnonymousStruct24
}

external interface AnonymousStruct29 {
	var color: Any
	var position: Any
	var decay: Any
	var distance: Any
}

external interface AnonymousStruct30 {
	var value: Array<Any>
	var properties: AnonymousStruct29
}

external interface AnonymousStruct31 {
	var value: Array<Any>
	var properties: AnonymousStruct24
}

external interface AnonymousStruct32 {
	var direction: Any
	var skycolor: Any
	var groundColor: Any
}

external interface AnonymousStruct33 {
	var value: Array<Any>
	var properties: AnonymousStruct32
}

external interface AnonymousStruct34 {
	var color: Any
	var position: Any
	var width: Any
	var height: Any
}

external interface AnonymousStruct35 {
	var value: Array<Any>
	var properties: AnonymousStruct34
}

external interface AnonymousStruct36 {
	var ambientLightColor: IUniform__0
	var directionalLights: AnonymousStruct23
	var directionalLightShadows: AnonymousStruct25
	var directionalShadowMap: IUniform__0
	var directionalShadowMatrix: IUniform__0
	var spotLights: AnonymousStruct27
	var spotLightShadows: AnonymousStruct28
	var spotShadowMap: IUniform__0
	var spotShadowMatrix: IUniform__0
	var pointLights: AnonymousStruct30
	var pointLightShadows: AnonymousStruct31
	var pointShadowMap: IUniform__0
	var pointShadowMatrix: IUniform__0
	var hemisphereLights: AnonymousStruct33
	var rectAreaLights: AnonymousStruct35
}

external interface AnonymousStruct37 {
	var diffuse: IUniform__0
	var opacity: IUniform__0
	var size: IUniform__0
	var scale: IUniform__0
	var map: IUniform__0
	var uvTransform: IUniform__0
}

external object UniformsLib {
	var common: AnonymousStruct9
	var specularmap: AnonymousStruct10
	var envmap: AnonymousStruct11
	var aomap: AnonymousStruct12
	var lightmap: AnonymousStruct13
	var emissivemap: AnonymousStruct14
	var bumpmap: AnonymousStruct15
	var normalmap: AnonymousStruct16
	var displacementmap: AnonymousStruct17
	var roughnessmap: AnonymousStruct18
	var metalnessmap: AnonymousStruct19
	var gradientmap: AnonymousStruct20
	var fog: AnonymousStruct21
	var lights: AnonymousStruct36
	var points: AnonymousStruct37
}
