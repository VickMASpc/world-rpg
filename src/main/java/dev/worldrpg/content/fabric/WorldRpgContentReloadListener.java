package dev.worldrpg.content.fabric;

import dev.worldrpg.WorldRpg;
import dev.worldrpg.api.validation.ValidationMessage;
import dev.worldrpg.api.validation.ValidationSeverity;
import dev.worldrpg.content.load.ContentLoadResult;
import dev.worldrpg.content.load.ContentLoader;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Main-thread Fabric bridge for the P2 atomic content transaction.
 */
public final class WorldRpgContentReloadListener
        implements SimpleSynchronousResourceReloadListener {
    public static final Identifier ID = Identifier.of(
            WorldRpg.MOD_ID,
            "rpg_definitions"
    );

    private final ContentLoader loader;
    private final Consumer<ContentLoadResult> resultConsumer;

    public WorldRpgContentReloadListener(
            ContentLoader loader,
            Consumer<ContentLoadResult> resultConsumer
    ) {
        this.loader = Objects.requireNonNull(loader, "loader");
        this.resultConsumer =
                Objects.requireNonNull(resultConsumer, "resultConsumer");
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public void reload(ResourceManager manager) {
        ContentLoadResult result = loader.loadAndPublish(
                FabricContentSourceReader.read(manager)
        );

        resultConsumer.accept(result);

        for (ValidationMessage message : result.report().messages()) {
            String rendered = "["
                    + message.code()
                    + "] "
                    + message.message()
                    + " @ "
                    + message.source().display()
                    + message.definitionId()
                            .map(id -> " definition=" + id)
                            .orElse("");

            if (message.severity() == ValidationSeverity.ERROR) {
                WorldRpg.LOGGER.error(rendered);
            } else {
                WorldRpg.LOGGER.warn(rendered);
            }
        }

        if (result.published()) {
            int definitions = result.candidate()
                    .registries()
                    .values()
                    .stream()
                    .mapToInt(registry -> registry.size())
                    .sum();

            WorldRpg.LOGGER.info(
                    "Published World RPG content snapshot: {} registries, {} definitions, {} warnings.",
                    result.candidate().registryCount(),
                    definitions,
                    result.report().warningCount()
            );
        } else {
            WorldRpg.LOGGER.error(
                    "Rejected World RPG content candidate: {} errors, {} warnings. Last-known-good snapshot remains active.",
                    result.report().errorCount(),
                    result.report().warningCount()
            );
        }
    }
}
