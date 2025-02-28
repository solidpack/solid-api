package io.github.solid.resourcepack.api.link

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.nio.file.Path

class ModelLinkCollectorTest {

    @Test
    fun collect() {
        val path = Path.of("src", "test", "kotlin", "pack")
        println(path.toAbsolutePath().toString())
        val collector = ModelLinkCollector(path)
        val result = collector.collect()
        printResult(result)
        assert(result.isNotEmpty())
    }

    private fun printResult(result: List<ModelLink>) {
        result.forEach {
            println("Key: " + it.key)
            println("Type: " + it.modelType)
            println("Item Model: " + it.itemModel)
            println("Parent: " + it.parent)
            println("Predicates: " + it.predicates)
            println("---")
        }
    }
}