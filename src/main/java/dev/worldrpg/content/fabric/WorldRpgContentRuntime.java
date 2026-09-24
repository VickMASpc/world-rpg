package dev.worldrpg.content.fabric;

import dev.worldrpg.api.registry.RegistryPublisher;
import dev.worldrpg.content.load.ContentLoadResult;
import dev.worldrpg.content.load.ContentLoader;
import dev.worldrpg.content.load.DefinitionDomainCatalog;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;

import java.util.Objects;
import java.util.Optional;

/**
 * One-time bootstrap owner for the server-data content runtime.
 *
 * <p>The domain catalog is frozen at initialization. Later phases must compose
 * their domains before calling initialize rather than mutating a live catalog.</p>
 */
public final class WorldRpgContentRuntime {
    private static RegistryPublisher publisher;
    private static ContentLoader loader;
    private static ContentLoadResult lastResult;
    private static boolean initialized;

    private WorldRpgContentRuntime() {
    }

    public static synchronized void initialize(
            DefinitionDomainCatalog catalog
    ) {
        Objects.requireNonNull(catalog, "catalog");

        if (initialized) {
            throw new IllegalStateException(
                    "World RPG content runtime is already initialized"
            );
        }

        publisher = new RegistryPublisher();
        loader = new ContentLoader(catalog, publisher);

        ResourceManagerHelper.get(ResourceType.SERVER_DATA)
                .registerReloadListener(
                        new WorldRpgContentReloadListener(
                                loader,
                                WorldRpgContentRuntime::acceptResult
                        )
                );

        initialized = true;
    }

    public static boolean initialized() {
        return initialized;
    }

    public static RegistryPublisher publisher() {
        requireInitialized();
        return publisher;
    }

    public static ContentLoader loader() {
        requireInitialized();
        return loader;
    }

    public static Optional<ContentLoadResult> lastResult() {
        requireInitialized();
        return Optional.ofNullable(lastResult);
    }

    private static synchronized void acceptResult(
            ContentLoadResult result
    ) {
        lastResult = Objects.requireNonNull(result, "result");
    }

    private static void requireInitialized() {
        if (!initialized) {
            throw new IllegalStateException(
                    "World RPG content runtime is not initialized"
            );
        }
    }
}
