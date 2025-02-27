package io.github.solid.resourcepack.api.link

import io.github.solid.resourcepack.api.link.legacy.LegacyResourcePackItemLink
import io.github.solid.resourcepack.api.link.modern.ModernResourcePackLink
import io.github.solid.resourcepack.api.util.GenericEnumSerializer
import io.github.solid.resourcepack.api.util.KeySerializer
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.gson.GsonConfigurationLoader
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.objectMapperFactory
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries


class ModelLinkCollector(private val packPath: Path) : ModelLinkHolder {


    private fun toModelPath(key: Key): Path {
        val keyPath = Paths.get("assets", key.namespace(), "models", key.value() + ".json")
        return packPath.resolve(keyPath)
    }

    override fun collect(): List<ModelLink> {
        val result = mutableListOf<ModelLink>()
        result.addAll(collectModern())
        result.combine(collectLegacy()) { first, second ->
            first.key == second.key
        }
        return result
    }

    private fun collectLegacy(type: ModelType): List<ModelLink> {
        val result = mutableListOf<ModelLink>()
        val linkModels = packPath.resolve(Paths.get("assets", "minecraft", "models", type.name.lowercase()))
        if (!linkModels.exists()) return result
        linkModels.listDirectoryEntries("*.json").forEach { definition ->
            if (!definition.exists()) return@forEach
            try {
                val parsed = readModel<LegacyResourcePackItemLink>(definition)
                parsed?.overrides?.forEach overrideForEach@{ override ->
                    if (!packPath.resolve(toModelPath(override.model)).exists()) return@overrideForEach
                    result.add(ModelLink(override.model, parsed.parent, override.predicate, type))
                }
            } catch (_: Exception) {
            }
        }
        return result
    }

    private fun collectLegacy(): List<ModelLink> {
        val result = mutableListOf<ModelLink>()
        ModelType.entries.forEach { type -> result.addAll(collectLegacy(type)) }
        return result
    }

    private fun collectModern(): List<ModelLink> {
        val result = mutableListOf<ModelLink>()
        val namespaces = getNamespaces()
        if (!namespaces.exists()) return result
        namespaces.forEach { namespace ->
            val items = namespace.resolve("items")
            if (!items.exists()) return@forEach
            items.listDirectoryEntries("*.json").forEach itemForEach@{ definition ->
                if (!definition.exists()) return@itemForEach
                try {
                    val parsed = readModel<ModernResourcePackLink>(definition)
                    if (parsed != null && parsed.model.type == Key.key("minecraft", "model")) {
                        result.add(ModelLink(parsed.model.model))
                    }

                } catch (_: Exception) {
                }
            }
        }
        return result
    }

    private fun getNamespaces(): Path {
        return packPath.resolve("assets")
    }


    private fun <T> MutableList<T>.combine(other: List<T>, check: (first: T, second: T) -> Boolean): List<T> {
        other.forEach { element ->
            val found = this.find { check(element, it) }?.let { this.indexOf(it) }
            if (found == null) {
                this.add(element)
                return@forEach
            }
            this.removeAt(found)
            this.add(found, element)
        }
        return this
    }

    private inline fun <reified T> readModel(path: Path): T? {
        val loader = GsonConfigurationLoader.builder().path(path).defaultOptions { options ->
            options.serializers { serializers ->
                serializers.registerAnnotatedObjects(objectMapperFactory())
                serializers.register(Enum::class.java, GenericEnumSerializer)
                serializers.register(Key::class.java, KeySerializer)
            }
        }.build()
        return loader.load().get<T>()
    }

}