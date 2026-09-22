package dev.worldrpg.combat.stat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Runtime stat container with dirty-on-change cached recomputation.
 *
 * <p>No stat is recomputed merely because a game tick occurred.</p>
 */
public final class StatSheet {
    private static final Comparator<StatModifier> MODIFIER_ORDER =
            Comparator.comparingInt(StatModifier::priority)
                    .thenComparing(StatModifier::handle);

    private final Map<StatKey, Double> baseValues = new HashMap<>();
    private final Map<ModifierHandle, StatModifier> modifiersByHandle = new LinkedHashMap<>();
    private final Map<StatKey, List<StatModifier>> modifiersByStat = new HashMap<>();
    private final Map<StatKey, Double> cachedValues = new HashMap<>();
    private long nextHandle = 1L;

    public void setBase(StatKey stat, double value) {
        Objects.requireNonNull(stat, "stat");
        requireFinite(value, "base stat value");
        baseValues.put(stat, value);
        cachedValues.remove(stat);
    }

    public double base(StatKey stat) {
        Objects.requireNonNull(stat, "stat");
        return baseValues.getOrDefault(stat, 0.0);
    }

    public double value(StatKey stat) {
        Objects.requireNonNull(stat, "stat");

        Double cached = cachedValues.get(stat);
        if (cached != null) {
            return cached;
        }

        double result = base(stat);
        List<StatModifier> modifiers = modifiersByStat.get(stat);

        if (modifiers != null && !modifiers.isEmpty()) {
            List<StatModifier> ordered = new ArrayList<>(modifiers);
            ordered.sort(MODIFIER_ORDER);

            for (StatModifier modifier : ordered) {
                result = modifier.operation().apply(result, modifier.amount());
                requireFinite(result, "computed stat value");
            }
        }

        cachedValues.put(stat, result);
        return result;
    }

    public ModifierHandle addModifier(
            StatKey stat,
            ModifierSource source,
            StatModifierOperation operation,
            double amount,
            int priority
    ) {
        Objects.requireNonNull(stat, "stat");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(operation, "operation");
        requireFinite(amount, "modifier amount");

        ModifierHandle handle = new ModifierHandle(nextHandle++);
        StatModifier modifier = new StatModifier(
                handle,
                stat,
                source,
                operation,
                amount,
                priority
        );

        modifiersByHandle.put(handle, modifier);
        modifiersByStat.computeIfAbsent(stat, ignored -> new ArrayList<>()).add(modifier);
        cachedValues.remove(stat);
        return handle;
    }

    public Optional<StatModifier> modifier(ModifierHandle handle) {
        return Optional.ofNullable(modifiersByHandle.get(
                Objects.requireNonNull(handle, "handle")
        ));
    }

    public boolean removeModifier(ModifierHandle handle) {
        Objects.requireNonNull(handle, "handle");

        StatModifier removed = modifiersByHandle.remove(handle);
        if (removed == null) {
            return false;
        }

        List<StatModifier> modifiers = modifiersByStat.get(removed.stat());
        if (modifiers != null) {
            modifiers.removeIf(modifier -> modifier.handle().equals(handle));
            if (modifiers.isEmpty()) {
                modifiersByStat.remove(removed.stat());
            }
        }

        cachedValues.remove(removed.stat());
        return true;
    }

    public int removeModifiersFrom(ModifierSource source) {
        Objects.requireNonNull(source, "source");

        List<ModifierHandle> handles = modifiersByHandle.values().stream()
                .filter(modifier -> modifier.source().equals(source))
                .map(StatModifier::handle)
                .toList();

        handles.forEach(this::removeModifier);
        return handles.size();
    }

    public int modifierCount() {
        return modifiersByHandle.size();
    }

    private static void requireFinite(double value, String label) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(label + " must be finite");
        }
    }
}
