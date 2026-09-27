package dev.worldrpg.client.entity;

import dev.worldrpg.WorldRpg;
import dev.worldrpg.entity.AshwoodWolfEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public final class AshwoodWolfModel
        extends GeoModel<AshwoodWolfEntity> {
    @Override
    public Identifier getModelResource(
            AshwoodWolfEntity entity
    ) {
        return Identifier.of(
                WorldRpg.MOD_ID,
                "geo/ashwood_wolf.geo.json"
        );
    }

    @Override
    public Identifier getTextureResource(
            AshwoodWolfEntity entity
    ) {
        return Identifier.of(
                WorldRpg.MOD_ID,
                "textures/entity/ashwood_wolf.png"
        );
    }

    @Override
    public Identifier getAnimationResource(
            AshwoodWolfEntity entity
    ) {
        return Identifier.of(
                WorldRpg.MOD_ID,
                "animations/ashwood_wolf.animation.json"
        );
    }
}
