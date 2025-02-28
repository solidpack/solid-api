package io.github.solid.resourcepack.api.link.legacy

import net.kyori.adventure.key.Key
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class ModelOverride(
    val predicate: Map<String, Any>,
    val model: Key,
)