package io.github.solid.resourcepack.api.link

import net.kyori.adventure.key.Key

interface ModelLinkHolder {
    fun collect(): List<ModelLink>
}

data class ModelLink(
    val key: Key,
    val parent: Key? = null,
    val itemModel: Key? = null,
    val predicates: Map<String, Any>? = null,
    val modelType: ModelType = ModelType.ITEM,
)

enum class ModelType {
    ITEM,
    BLOCK;
}