package starshipfights.data

import com.aventrix.jnanoid.jnanoid.NanoIdUtils

private val tokenAlphabet = "0123456789ABCDEFGHILMNOPQRSTVXYZ".toCharArray()
private const val tokenLength = 8

fun newToken(): String = NanoIdUtils.randomNanoId(NanoIdUtils.DEFAULT_NUMBER_GENERATOR, tokenAlphabet, tokenLength)

private val idAlphabet = "BCDFGHLMNPQRSTXZ".toCharArray()
private const val idLength = 42

operator fun <T> Id.Companion.invoke() = Id<T>(NanoIdUtils.randomNanoId(NanoIdUtils.DEFAULT_NUMBER_GENERATOR, idAlphabet, idLength))
