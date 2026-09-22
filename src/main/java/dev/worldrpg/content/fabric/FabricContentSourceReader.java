package dev.worldrpg.content.fabric;

import dev.worldrpg.api.validation.SourceRef;
import dev.worldrpg.api.validation.ValidationReport;
import dev.worldrpg.content.source.ContentSource;
import dev.worldrpg.content.source.ContentSourceBatch;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Adapts Minecraft server-data resources into the Minecraft-independent P2
 * content source model.
 */
public final class FabricContentSourceReader {
    public static final String ROOT = "world_rpg/definitions";

    private FabricContentSourceReader() {
    }

    public static ContentSourceBatch read(
            ResourceManager resourceManager
    ) {
        Objects.requireNonNull(resourceManager, "resourceManager");

        ValidationReport diagnostics = new ValidationReport();
        List<ContentSource> sources = new ArrayList<>();

        Map<Identifier, Resource> resources =
                resourceManager.findResources(
                        ROOT,
                        id -> id.getPath().endsWith(".json")
                );

        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier id = entry.getKey();
            Resource resource = entry.getValue();

            SourceRef sourceRef = SourceRef.of(
                    "pack=" + resource.getPackId()
                            + " resource=" + id
            );

            try (BufferedReader reader = resource.getReader()) {
                String content = reader.lines()
                        .collect(Collectors.joining("\n"));

                sources.add(new ContentSource(
                        id.toString(),
                        content,
                        sourceRef
                ));
            } catch (IOException exception) {
                diagnostics.error(
                        "source.io",
                        "Failed to read UTF-8 JSON resource: "
                                + exception.getMessage(),
                        sourceRef
                );
            } catch (RuntimeException exception) {
                diagnostics.error(
                        "source.read_failure",
                        "Unexpected resource read failure: "
                                + exception.getMessage(),
                        sourceRef
                );
            }
        }

        return new ContentSourceBatch(
                sources,
                diagnostics
        );
    }
}
