package io.github.solid.resourcepack.api.link.legacy

import io.github.solid.resourcepack.api.link.ModelLink
import io.github.solid.resourcepack.api.link.ModelLinkHolder
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class LegacyResourcePackItemLink(
    val parent: Key, val textures: Map<String, Key>, val overrides: List<ModelOverride>
) : ModelLinkHolder {
    override fun collect(): List<ModelLink> {
        return overrides.map { ModelLink(it.model, predicates =  it.predicate) }
    }
}