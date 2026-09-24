package dev.worldrpg.api.id;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Stable namespaced identity used by World RPG definitions and persisted references.
 *
 * <p>IDs are deliberately independent from Minecraft classes so the same identity
 * model can be reused by standalone validators and simulators.</p>
 */
public record RpgId(String namespace, String path) implements Comparable<RpgId> {
    private static final Pattern NAMESPACE_PATTERN = Pattern.compile("[a-z0-9_.-]+");
    private static final Pattern PATH_PATTERN =
            Pattern.compile("[a-z0-9._-]+(?:/[a-z0-9._-]+)*");

    public RpgId {
        Objects.requireNonNull(namespace, "namespace");
        Objects.requireNonNull(path, "path");

        if (!NAMESPACE_PATTERN.matcher(namespace).matches()) {
            throw new IllegalArgumentException("Invalid RPG ID namespace: " + namespace);
        }
        if (!PATH_PATTERN.matcher(path).matches()) {
            throw new IllegalArgumentException("Invalid RPG ID path: " + path);
        }
    }

    public static RpgId of(String namespace, String path) {
        return new RpgId(namespace, path);
    }

    public static RpgId parse(String value) {
        Objects.requireNonNull(value, "value");

        int separator = value.indexOf(':');
        if (separator <= 0 || separator != value.lastIndexOf(':') || separator == value.length() - 1) {
            throw new IllegalArgumentException("RPG ID must be exactly namespace:path: " + value);
        }

        return new RpgId(value.substring(0, separator), value.substring(separator + 1));
    }

    @Override
    public int compareTo(RpgId other) {
        int namespaceOrder = namespace.compareTo(other.namespace);
        return namespaceOrder != 0 ? namespaceOrder : path.compareTo(other.path);
    }

    @Override
    public String toString() {
        return namespace + ":" + path;
    }
}
