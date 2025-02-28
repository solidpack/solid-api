package io.github.solid.resourcepack.api.util

import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type


data class Range(
    val start: Int,
    val end: Int? = null,
)

object RangeSerializer : TypeSerializer<Range> {
    override fun deserialize(type: Type, node: ConfigurationNode): Range {
        if (node.isList) {
            val list = node.getList(Int::class.java) ?: throw SerializationException("No int list provided")
            return Range(list[0], list[1])
        }
        if (!node.hasChild("min_inclusive") && !node.hasChild("max_inclusive")) {
            return Range(node.int)
        }
        val start = node.node("min_inclusive").int
        val end = node.node("max_inclusive").int
        return Range(start, end)
    }

    override fun serialize(type: Type, obj: Range?, node: ConfigurationNode) {
        if (obj == null) return
        if (obj.end == null) {
            node.set(obj.start)
            return
        }

        node.node("min_inclusive").set(obj.start)
        node.node("max_inclusive").set(obj.end)
    }
}


