package dev.worldrpg.entity;

import dev.worldrpg.WorldRpg;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class WorldRpgEntities {
    public static final EntityType<AshwoodWolfEntity> ASHWOOD_WOLF =
            Registry.register(
                    Registries.ENTITY_TYPE,
                    Identifier.of(
                            WorldRpg.MOD_ID,
                            "ashwood_wolf"
                    ),
                    EntityType.Builder.create(
                                    AshwoodWolfEntity::new,
                                    SpawnGroup.CREATURE
                            )
                            .dimensions(0.95F, 1.15F)
                            .maxTrackingRange(80)
                            .build()
            );

    private WorldRpgEntities() {
    }

    public static void register() {
        FabricDefaultAttributeRegistry.register(
                ASHWOOD_WOLF,
                MobEntity.createMobAttributes()
                        .add(
                                EntityAttributes.GENERIC_MAX_HEALTH,
                                60.0
                        )
                        .add(
                                EntityAttributes.GENERIC_MOVEMENT_SPEED,
                                0.32
                        )
                        .add(
                                EntityAttributes.GENERIC_FOLLOW_RANGE,
                                32.0
                        )
                        .add(
                                EntityAttributes.GENERIC_ATTACK_DAMAGE,
                                0.0
                        )
        );
    }
}
