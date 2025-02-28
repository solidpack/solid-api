package io.github.solid.resourcepack.api.meta

import io.github.solid.resourcepack.api.util.Range
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class PackMeta(
    val pack: PackDescription,
    val overlays: Overlays,
)

@ConfigSerializable
data class PackDescription(
    @Setting("pack_format")
    val format: Int = -1,
    @Setting("supported_formats")
    val supportedFormats: Range = Range(-1, -1),
    val description: String = "",
)

@ConfigSerializable
data class Overlays(
    val entries: List<Overlay> = listOf(),
)

@ConfigSerializable
data class Overlay(
    val formats: Range? = null,
    val directory: String? = null,
)
