package dev.worldrpg.combat.resource;

public record ResourceChange(
        double before,
        double after,
        double requested,
        double applied,
        boolean fullyApplied
) {
}
