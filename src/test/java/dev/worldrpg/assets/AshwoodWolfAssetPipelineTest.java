package dev.worldrpg.assets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AshwoodWolfAssetPipelineTest {
    private static final Path SOURCE = Path.of(
            "assets-source",
            "creatures",
            "ashwood_wolf.bbmodel"
    );

    @Test
    void runtimeExportMatchesCanonicalBlockbenchSource()
            throws IOException {
        JsonObject source = JsonParser.parseString(
                Files.readString(SOURCE)
        ).getAsJsonObject();

        JsonObject geometry = resourceJson(
                "assets/world_rpg/geo/ashwood_wolf.geo.json"
        );
        JsonObject animation = resourceJson(
                "assets/world_rpg/animations/ashwood_wolf.animation.json"
        );

        int sourceElements =
                source.getAsJsonArray("elements").size();

        var runtimeGeometry =
                geometry.getAsJsonArray("minecraft:geometry")
                        .get(0)
                        .getAsJsonObject();
        var bones = runtimeGeometry.getAsJsonArray("bones");

        int runtimeCubes = 0;
        for (var boneElement : bones) {
            JsonObject bone = boneElement.getAsJsonObject();
            if (bone.has("cubes")) {
                runtimeCubes += bone.getAsJsonArray("cubes").size();
            }
        }

        assertEquals(92, sourceElements);
        assertEquals(sourceElements, runtimeCubes);
        assertTrue(bones.size() >= 20);

        Set<String> sourceAnimations =
                new LinkedHashSet<>();
        for (var value : source.getAsJsonArray("animations")) {
            sourceAnimations.add(
                    value.getAsJsonObject()
                            .get("name")
                            .getAsString()
            );
        }

        Set<String> runtimeAnimations =
                animation.getAsJsonObject("animations")
                        .keySet();

        assertEquals(
                sourceAnimations,
                runtimeAnimations
        );

        String embedded =
                source.getAsJsonArray("textures")
                        .get(0)
                        .getAsJsonObject()
                        .get("source")
                        .getAsString();
        String prefix = "data:image/png;base64,";
        assertTrue(embedded.startsWith(prefix));

        byte[] sourceTexture = Base64.getDecoder()
                .decode(embedded.substring(prefix.length()));
        byte[] runtimeTexture = resourceBytes(
                "assets/world_rpg/textures/entity/ashwood_wolf.png"
        );

        assertArrayEquals(
                sourceTexture,
                runtimeTexture
        );
    }

    private static JsonObject resourceJson(
            String path
    ) throws IOException {
        return JsonParser.parseString(
                new String(
                        resourceBytes(path),
                        StandardCharsets.UTF_8
                )
        ).getAsJsonObject();
    }

    private static byte[] resourceBytes(
            String path
    ) throws IOException {
        try (InputStream input =
                     AshwoodWolfAssetPipelineTest.class
                             .getClassLoader()
                             .getResourceAsStream(path)) {
            if (input == null) {
                throw new IllegalStateException(
                        "missing packaged resource: " + path
                );
            }
            return input.readAllBytes();
        }
    }
}
