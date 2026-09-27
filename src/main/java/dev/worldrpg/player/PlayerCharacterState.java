package dev.worldrpg.player;

import dev.worldrpg.api.id.RpgId;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class PlayerCharacterState {
    private int level;
    private long experienceIntoLevel;
    private final EnumMap<EquipmentSlot, RpgId> equipped =
            new EnumMap<>(EquipmentSlot.class);

    public PlayerCharacterState() {
        this(1, 0L, Map.of());
    }

    public PlayerCharacterState(
            int level,
            long experienceIntoLevel,
            Map<EquipmentSlot, RpgId> equipped
    ) {
        if (level < 1 || level > CharacterProgressionCurve.MAX_LEVEL) {
            throw new IllegalArgumentException(
                    "level must be between 1 and "
                            + CharacterProgressionCurve.MAX_LEVEL
            );
        }
        if (experienceIntoLevel < 0L) {
            throw new IllegalArgumentException(
                    "experienceIntoLevel must be >= 0"
            );
        }
        if (level < CharacterProgressionCurve.MAX_LEVEL
                && experienceIntoLevel
                >= CharacterProgressionCurve.experienceToNextLevel(level)) {
            throw new IllegalArgumentException(
                    "experienceIntoLevel must be below the current level threshold"
            );
        }
        if (level == CharacterProgressionCurve.MAX_LEVEL
                && experienceIntoLevel != 0L) {
            throw new IllegalArgumentException(
                    "max-level characters cannot retain pending experience"
            );
        }

        this.level = level;
        this.experienceIntoLevel = experienceIntoLevel;
        Objects.requireNonNull(equipped, "equipped")
                .forEach((slot, itemId) ->
                        this.equipped.put(
                                Objects.requireNonNull(slot, "slot"),
                                Objects.requireNonNull(itemId, "itemId")
                        )
                );
    }

    public int level() {
        return level;
    }

    public long experienceIntoLevel() {
        return experienceIntoLevel;
    }

    public long experienceToNextLevel() {
        return CharacterProgressionCurve.experienceToNextLevel(level);
    }

    public boolean maxLevel() {
        return level >= CharacterProgressionCurve.MAX_LEVEL;
    }

    public ProgressionResult grantExperience(long amount) {
        if (amount < 0L) {
            throw new IllegalArgumentException(
                    "experience grant must be >= 0"
            );
        }

        int startingLevel = level;
        long remaining = amount;

        while (remaining > 0L && !maxLevel()) {
            long threshold =
                    CharacterProgressionCurve.experienceToNextLevel(level);
            long needed = threshold - experienceIntoLevel;
            long applied = Math.min(needed, remaining);

            experienceIntoLevel += applied;
            remaining -= applied;

            if (experienceIntoLevel >= threshold) {
                level++;
                experienceIntoLevel = 0L;
            }
        }

        if (maxLevel()) {
            experienceIntoLevel = 0L;
        }

        return new ProgressionResult(
                amount - remaining,
                startingLevel,
                level
        );
    }

    public Optional<RpgId> equipped(EquipmentSlot slot) {
        return Optional.ofNullable(
                equipped.get(
                        Objects.requireNonNull(slot, "slot")
                )
        );
    }

    public Map<EquipmentSlot, RpgId> equippedItems() {
        return Map.copyOf(equipped);
    }

    public Optional<RpgId> equip(
            EquipmentSlot slot,
            RpgId itemId
    ) {
        return Optional.ofNullable(
                equipped.put(
                        Objects.requireNonNull(slot, "slot"),
                        Objects.requireNonNull(itemId, "itemId")
                )
        );
    }

    public Optional<RpgId> unequip(EquipmentSlot slot) {
        return Optional.ofNullable(
                equipped.remove(
                        Objects.requireNonNull(slot, "slot")
                )
        );
    }

    public record ProgressionResult(
            long appliedExperience,
            int startingLevel,
            int endingLevel
    ) {
        public boolean leveledUp() {
            return endingLevel > startingLevel;
        }

        public int levelsGained() {
            return endingLevel - startingLevel;
        }
    }
}
