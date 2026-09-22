package dev.worldrpg.content.fabric;

import dev.worldrpg.api.registry.RegistryPublisher;
import dev.worldrpg.content.load.ContentLoadResult;
import dev.worldrpg.content.load.ContentLoader;
import dev.worldrpg.content.load.DefinitionDomainCatalog;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public final class WorldRpgContentRuntime {
    private static final List<Consumer<ContentLoadResult>> RESULT_LISTENERS =
            new ArrayList<>();

    private static RegistryPublisher publisher;
    private static ContentLoader loader;
    private static ContentLoadResult lastResult;
    private static boolean initialized;

    private WorldRpgContentRuntime() {
    }

    public static synchronized void addResultListener(
            Consumer<ContentLoadResult> listener
    ) {
        RESULT_LISTENERS.add(
                Objects.requireNonNull(listener, "listener")
        );
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

    private static void acceptResult(
            ContentLoadResult result
    ) {
        List<Consumer<ContentLoadResult>> listeners;

        synchronized (WorldRpgContentRuntime.class) {
            lastResult = Objects.requireNonNull(result, "result");
            listeners = List.copyOf(RESULT_LISTENERS);
        }

        for (Consumer<ContentLoadResult> listener : listeners) {
            listener.accept(result);
        }
    }

    private static void requireInitialized() {
        if (!initialized) {
            throw new IllegalStateException(
                    "World RPG content runtime is not initialized"
            );
        }
    }
}
