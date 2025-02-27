package io.github.solid.resourcepack.api.link.modern

import io.github.solid.resourcepack.api.link.ModelLink
import io.github.solid.resourcepack.api.link.ModelLinkHolder
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * A vanilla item model (in /items) in 1.21.4 and up
 */
@ConfigSerializable
data class ModernResourcePackLink(
    val model: ModernResourcePackModel
) : ModelLinkHolder {
    override fun collect(): List<ModelLink> {
        return listOf(ModelLink(model.model))
    }
}

@ConfigSerializable
data class ModernResourcePackModel(
    val type: Key,
    val model: Key,
)