package dev.worldrpg.client.entity;

import dev.worldrpg.entity.AshwoodWolfEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class AshwoodWolfRenderer
        extends GeoEntityRenderer<AshwoodWolfEntity> {
    public AshwoodWolfRenderer(
            EntityRendererFactory.Context context
    ) {
        super(context, new AshwoodWolfModel());
        shadowRadius = 0.55F;
    }
}
