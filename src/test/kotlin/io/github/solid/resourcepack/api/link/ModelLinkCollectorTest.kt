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
        assert(collector.collect().isNotEmpty())
    }
}