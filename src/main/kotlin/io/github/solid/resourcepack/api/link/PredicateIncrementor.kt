package io.github.solid.resourcepack.api.link

import io.github.solid.resourcepack.api.link.legacy.ModelOverride
import net.kyori.adventure.key.Key

enum class PredicateIncrementorType(val predicates: Map<String, PredicateArgument<*>>) {
    NOTE_BLOCK(
        mapOf(
            "note" to RangePredicateArgument(0, 24),
            "instrument" to EnumPredicateArgument(
                "harp",
                "banjo",
                "basedrum",
                "bass",
                "bell",
                "bit",
                "chime",
                "cow_bell",
                "creeper",
                "custom_head",
                "didgeridoo",
                "dragon",
                "flute",
                "guitar",
                "hat",
                "iron_xylophone",
                "piglin",
                "pling",
                "skeleton",
                "snare",
                "wither_skeleton",
                "xylophone",
                "zombie"
            )
        )
    ),
    MUSHROOM_BLOCK(
        mapOf(
            "east" to BoolPredicateArgument(),
            "down" to BoolPredicateArgument(),
            "north" to BoolPredicateArgument(),
            "south" to BoolPredicateArgument(),
            "up" to BoolPredicateArgument(),
            "west" to BoolPredicateArgument()
        )
    ),
    CHORUS_FLOWER(
        mapOf(
            "age" to RangePredicateArgument(0, 5)
        )
    ),
    CUSTOM_MODEL_DATA(
        mapOf(
            "custom_model_data" to IntPredicateArgument()
        )
    )
}

interface PredicateArgument<T> {
    fun size(): Int
    fun of(index: Int): T
}

class EnumPredicateArgument(private vararg val values: String) : PredicateArgument<String> {
    override fun size(): Int {
        return values.size
    }

    override fun of(index: Int): String {
        return values[index]
    }
}

class RangePredicateArgument(private val from: Int, private val to: Int) : PredicateArgument<Int> {
    override fun size(): Int {
        return to - from
    }

    override fun of(index: Int): Int {
        return from + index
    }
}

class IntPredicateArgument() : PredicateArgument<Int> {
    override fun size(): Int {
        return Int.MAX_VALUE
    }

    override fun of(index: Int): Int {
        return index
    }
}

class BoolPredicateArgument() : PredicateArgument<Boolean> {
    override fun size(): Int {
        return 2
    }

    override fun of(index: Int): Boolean {
        return index == 1
    }
}

class DefaultPredicateArgument(private vararg val values: Any) : PredicateArgument<Any> {
    override fun size(): Int {
        return values.size
    }

    override fun of(index: Int): Any {
        return values[index]
    }
}


class PredicateGenerator(private val incrementor: PredicateIncrementor) {
    init {
        incrementor.setValue(1)
    }

    fun generate(predicates: List<ModelOverride>?, items: List<Key>): List<ModelOverride> {
        val predicateMaps =
            predicates?.map { predicate -> predicate.predicate.toList().associate { it.first to it.second } }
                ?: listOf()
        val realPredicates = predicates?.toMutableList() ?: mutableListOf()
        val iterator = items.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (predicates?.any { it.model == item } == true) continue
            var predicate = incrementor.increment()
            while (predicateMaps.contentEquals(predicate)) {
                predicate = incrementor.increment()
            }
            realPredicates.add(
                ModelOverride(
                    predicate,
                    item
                )
            )
        }
        return realPredicates
    }

    private fun List<Map<String, Any>>.contentEquals(other: Map<String, Any>): Boolean {
        return this.any first@{
            val returned = it.keys.all second@{ key ->
                return@second other[key].toString() == it[key].toString()
            }
            return@first returned
        }
    }

}

class PredicateIncrementor(args: Map<String, PredicateArgument<*>>) {
    private val mapList = args.toList()
    private val digitLimits = args.values.toList().map { it.size() }
    private var value = 0;
    fun setValue(value: Int) {
        this.value = value
    }

    private fun generate(value: Int): Map<String, Any> {
        var current = value;
        val result = mutableListOf<Int>()
        for (limit in digitLimits) {
            if (limit <= 1) {
                throw IllegalArgumentException("Each digit limit must be greater than 1.")
            }
            result.add(current % limit)
            current /= limit

            if (current == 0) {
                break
            }
        }

        // If there are remaining digits, continue dividing by their respective limits
        while (current > 0 && result.size < digitLimits.size) {
            val limit = digitLimits[result.size]
            result.add(current % limit)
            current /= limit
        }

        // If there are remaining digits but no more limits, raise an error
        if (current > 0) {
            throw IllegalArgumentException("The number is too large to be represented with the given digit limits.")
        }
        val toReturn = mutableMapOf<String, Any>()
        mapList.forEach {
            toReturn[it.first] = it.second.of(0) as Any
        }
        result.forEachIndexed { index, chosen ->
            toReturn[mapList[index].first] = mapList[index].second.of(chosen) as Any
        }
        return toReturn
    }

    fun increment(): Map<String, Any> {
        val result = generate(value)
        value++;
        return result;
    }
}