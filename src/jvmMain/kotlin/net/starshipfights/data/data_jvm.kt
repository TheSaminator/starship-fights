package net.starshipfights.data

import com.aventrix.jnanoid.jnanoid.NanoIdUtils

private val alphabet32 = "BCDFGHLMNPQRSTXZbcdfghlmnpqrstxz".toCharArray()

private const val tokenLength = 8
fun createToken(): String = NanoIdUtils.randomNanoId(NanoIdUtils.DEFAULT_NUMBER_GENERATOR, alphabet32, tokenLength)

private const val nonceLength = 16
fun createNonce(): String = NanoIdUtils.randomNanoId(NanoIdUtils.DEFAULT_NUMBER_GENERATOR, alphabet32, nonceLength)

private const val idLength = 24
operator fun <T> Id.Companion.invoke() = Id<T>(NanoIdUtils.randomNanoId(NanoIdUtils.DEFAULT_NUMBER_GENERATOR, alphabet32, idLength))
