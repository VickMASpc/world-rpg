package dev.worldrpg.combat.resource;

import java.util.Objects;

/**
 * Mutable runtime pool.
 *
 * <p>Regeneration policy deliberately lives outside the pool. P3 owns storage
 * and mutation semantics; P4/later systems own actual recovery rates.</p>
 */
public final class ResourcePool {
    private final ResourceKey key;
    private double maximum;
    private double current;

    public ResourcePool(ResourceKey key, double maximum, double current) {
        this.key = Objects.requireNonNull(key, "key");
        requireNonNegativeFinite(maximum, "maximum");
        requireNonNegativeFinite(current, "current");

        if (current > maximum) {
            throw new IllegalArgumentException("current resource cannot exceed maximum");
        }

        this.maximum = maximum;
        this.current = current;
    }

    public ResourceKey key() {
        return key;
    }

    public double maximum() {
        return maximum;
    }

    public double current() {
        return current;
    }

    public double missing() {
        return maximum - current;
    }

    public boolean isEmpty() {
        return current == 0.0;
    }

    public boolean isFull() {
        return current == maximum;
    }

    /**
     * Strict spend: insufficient resource spends nothing.
     */
    public ResourceChange spend(double amount) {
        requireNonNegativeFinite(amount, "spend amount");
        double before = current;

        if (amount > current) {
            return new ResourceChange(before, before, amount, 0.0, false);
        }

        current -= amount;
        return new ResourceChange(before, current, amount, amount, true);
    }

    /**
     * Partial drain: removes as much as available.
     */
    public ResourceChange drainUpTo(double amount) {
        requireNonNegativeFinite(amount, "drain amount");
        double before = current;
        double applied = Math.min(amount, current);
        current -= applied;

        return new ResourceChange(
                before,
                current,
                amount,
                applied,
                applied == amount
        );
    }

    public ResourceChange gain(double amount) {
        requireNonNegativeFinite(amount, "gain amount");
        double before = current;
        double applied = Math.min(amount, maximum - current);
        current += applied;

        return new ResourceChange(
                before,
                current,
                amount,
                applied,
                applied == amount
        );
    }

    public void setCurrent(double value) {
        requireNonNegativeFinite(value, "current");
        current = Math.min(value, maximum);
    }

    public void setMaximum(double newMaximum, ResourceMaximumPolicy policy) {
        requireNonNegativeFinite(newMaximum, "maximum");
        Objects.requireNonNull(policy, "policy");

        double oldMaximum = maximum;
        double oldCurrent = current;
        double oldMissing = oldMaximum - oldCurrent;

        maximum = newMaximum;

        current = switch (policy) {
            case KEEP_CURRENT -> Math.min(oldCurrent, newMaximum);
            case PRESERVE_RATIO -> oldMaximum == 0.0
                    ? 0.0
                    : Math.min(newMaximum, newMaximum * (oldCurrent / oldMaximum));
            case PRESERVE_MISSING_AMOUNT ->
                    Math.max(0.0, Math.min(newMaximum, newMaximum - oldMissing));
        };
    }

    private static void requireNonNegativeFinite(double value, String label) {
        if (!Double.isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException(label + " must be finite and >= 0");
        }
    }
}
