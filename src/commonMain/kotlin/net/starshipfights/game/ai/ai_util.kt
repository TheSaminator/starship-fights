package net.starshipfights.game.ai

import kotlinx.serialization.builtins.serializer

val shipAttackPriority by neuron(Double.serializer()) { 1.0 }
