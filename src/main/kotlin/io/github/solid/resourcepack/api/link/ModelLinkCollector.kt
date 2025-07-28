package io.github.solid.resourcepack.api.link

import io.github.solid.resourcepack.api.link.legacy.LegacyResourcePackItemLink
import io.github.solid.resourcepack.api.link.modern.ModernResourcePackLink
import io.github.solid.resourcepack.api.meta.PackMeta
import io.github.solid.resourcepack.api.util.*
import io.leangen.geantyref.TypeToken
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.gson.GsonConfigurationLoader
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.objectMapperFactory
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.relativeTo


class ModelLinkCollector(private val packPath: Path) : ModelLinkHolder {

    private fun toModelPath(key: Key): Path {
        return Paths.get(key.namespace(), "models", key.value() + ".json")
    }

    private fun toModelKey(path: Path): Key {
        return Key.key(
            path.subpath(0, 1).toString(),
            path.subpath(2, path.nameCount).toString().replace("\\", "/").replace(".json", "")
        )
    }

    override fun collect(): List<ModelLink> {
        val result = mutableListOf<ModelLink>()
        result.addAll(collectModern())
        result.combine(collectLegacy()) { first, second ->
            first.key == second.key
        }
        result.combine(collectOverlays()) { first, second ->
            first.key == second.key
        }
        return result
    }

    private fun collectOverlays(): List<ModelLink> {
        val returned = mutableListOf<ModelLink>()
        val metaPath = packPath.resolve("pack.mcmeta")
        if (!metaPath.exists()) return returned
        val meta = readModel<PackMeta>(metaPath) ?: return returned
        meta.overlays.entries.forEach { overlay ->
            if (overlay.formats == null || overlay.directory == null) return@forEach
            val version = overlay.formats.start
            val results: List<ModelLink> = if (version >= 46) {
                collectModern(
                    listOf(
                        Paths.get("assets"),
                        Paths.get(overlay.directory, "assets")
                    )
                )
            } else {
                collectLegacy(
                    listOf(Paths.get("assets"), Paths.get(overlay.directory, "assets")),
                    Path.of(overlay.directory, "assets", "minecraft", "models")
                )
            }
            returned.addAll(results)
        }
        return returned
    }

    private fun collectLegacy(basePaths: List<Path>, modelPath: Path, type: ModelType): List<ModelLink> {
        val result = mutableListOf<ModelLink>()
        val linkModels = packPath.resolve(Paths.get(modelPath.toString(), type.name.lowercase()))
        if (!linkModels.exists()) return result
        linkModels.listDirectoryEntries("*.json").forEach { definition ->
            if (!definition.exists()) return@forEach
            try {
                val parsed = readModel<LegacyResourcePackItemLink>(definition)
                parsed?.collect()?.map {
                    ModelLink(
                        it.key,
                        toModelKey(definition.relativeTo(packPath.resolve(basePaths[0]))),
                        predicates = it.predicates
                    )
                }?.let {
                    result.addAll(it.filter { m ->
                        basePaths.any { path -> packPath.resolve(path).resolve(toModelPath(m.key)).exists() }
                    })
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return result
    }

    private fun collectLegacy(
        basePath: List<Path> = listOf(Paths.get("assets")),
        modelPath: Path = Paths.get("assets", "minecraft", "models")
    ): List<ModelLink> {
        val result = mutableListOf<ModelLink>()
        ModelType.entries.forEach { type -> result.addAll(collectLegacy(basePath, modelPath, type)) }
        return result
    }

    private fun collectModern(basePaths: List<Path> = listOf(Paths.get("assets"))): List<ModelLink> {
        val result = mutableListOf<ModelLink>()
        basePaths.map { packPath.resolve(it) }.forEach { baseDir ->
            if (!baseDir.exists()) return@forEach
            result.addAll(collectModernRecursively(basePaths, baseDir))
        }
        return result
    }

    private fun collectModernRecursively(basePaths: List<Path>, currentDir: Path): List<ModelLink> {
        val result = mutableListOf<ModelLink>()

        val itemsDir = currentDir.resolve("items")
        if (itemsDir.exists()) {
            itemsDir.listDirectoryEntries("*.json").forEach itemForEach@{ definition ->
                if (!definition.exists()) return@itemForEach
                try {
                    val parsed = readModel<ModernResourcePackLink>(definition)
                    parsed?.collect()?.let {
                        result.addAll(it.filter { m ->
                            basePaths.any { basePath ->
                                packPath.resolve(basePath).resolve(toModelPath(m.key)).exists() &&
                                        m.key.namespace() == currentDir.last().toString()
                            }
                        })
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // Recurse into subdirectories
        currentDir.listDirectoryEntries()
            .filter { it.isDirectory() }
            .forEach { subDir ->
                result.addAll(collectModernRecursively(basePaths, subDir))
            }

        return result
    }



    private fun MutableList<ModelLink>.combine(other: List<ModelLink>, check: (first: ModelLink, second: ModelLink) -> Boolean): List<ModelLink> {
        other.forEach { element ->
            val found = this.find { check(element, it) }?.let { this.indexOf(it) }
            if (found == null) {
                this.add(element)
                return@forEach
            }
            val toReplace = this.elementAt(found)
            val result = ModelLink(
                key = toReplace.key,
                parent = element.parent ?: toReplace.parent,
                itemModel = element.itemModel ?: toReplace.itemModel,
                modelType = toReplace.modelType,
                predicates = element.predicates ?: toReplace.predicates,
            )
            this.removeAt(found)
            this.add(found, result)
        }
        return this
    }

    private inline fun <reified T> readModel(path: Path): T? {
        val loader = GsonConfigurationLoader.builder().path(path).defaultOptions { options ->
            options.serializers { serializers ->
                serializers.registerAnnotatedObjects(objectMapperFactory())
                serializers.register(Enum::class.java, GenericEnumSerializer)
                serializers.register(Key::class.java, KeySerializer)
                serializers.register(Range::class.java, RangeSerializer)
                serializers.register(object : TypeToken<Map<String, Any>>() {}, MapSerializer)
            }
        }.build()
        return loader.load().get<T>()
    }

}