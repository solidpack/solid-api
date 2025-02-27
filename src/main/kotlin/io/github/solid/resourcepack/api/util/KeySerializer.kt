package io.github.solid.resourcepack.api.util

import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type

object KeySerializer : TypeSerializer<Key> {
    override fun deserialize(type: Type, node: ConfigurationNode): Key {
        val value = node.string ?: throw SerializationException("No value present in node")

        if (type !is Key) {
            throw SerializationException("Type is not a key class")
        }

        return try {
            Key.key(value)
        } catch (e: Exception) {
            throw SerializationException("Invalid key syntax")
        }
    }

    override fun serialize(type: Type, obj: Key?, node: ConfigurationNode) {
        node.set(obj?.asString())
    }
}
