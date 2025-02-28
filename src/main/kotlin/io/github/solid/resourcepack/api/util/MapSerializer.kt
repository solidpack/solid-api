package io.github.solid.resourcepack.api.util

import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type

object MapSerializer : TypeSerializer<Map<String, Any>> {
    override fun deserialize(type: Type?, node: ConfigurationNode?): Map<String, Any> {
        if(node == null) throw SerializationException("Map is null!")
        if(!node.isMap) throw SerializationException("Map is null!")
        return node.childrenMap().map { it.key.toString() to it.value.raw()!! }.toMap()
    }

    override fun serialize(type: Type?, obj: Map<String, Any>?, node: ConfigurationNode?) {
        if (node == null) return
        if (obj == null) return
        obj.forEach { (k, v) -> node.node(k).set(v) }
    }

}