package dev.worldrpg.combat.stat;

/**
 * Minimal transform operations for the P3 mechanism layer.
 *
 * <p>Priority controls ordering. P4 determines which priorities/categories
 * real game content should use.</p>
 */
public enum StatModifierOperation {
    ADD {
        @Override
        double apply(double value, double amount) {
            return value + amount;
        }
    },
    MULTIPLY {
        @Override
        double apply(double value, double amount) {
            return value * amount;
        }
    };

    abstract double apply(double value, double amount);
}
