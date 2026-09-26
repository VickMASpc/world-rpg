package dev.worldrpg.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Rendering/movement shell for authored Ashwood canids.
 *
 * <p>Combat stats, aggro policy, leash behavior, abilities, loot, and spawn
 * population are owned by authored runtime data rather than this entity
 * class. The entity exists so creature presentation can be replaced and
 * animated without coupling content to a vanilla wolf renderer.</p>
 */
public final class AshwoodWolfEntity
        extends PathAwareEntity
        implements GeoEntity {
    private static final RawAnimation IDLE =
            RawAnimation.begin()
                    .then("idle", Animation.LoopType.LOOP);
    private static final RawAnimation WALK =
            RawAnimation.begin()
                    .then("walk", Animation.LoopType.LOOP);
    private static final RawAnimation ATTACK =
            RawAnimation.begin()
                    .then(
                            "attack",
                            Animation.LoopType.PLAY_ONCE
                    );
    private static final RawAnimation HOWL =
            RawAnimation.begin()
                    .then(
                            "howl",
                            Animation.LoopType.PLAY_ONCE
                    );

    private final AnimatableInstanceCache animationCache =
            GeckoLibUtil.createInstanceCache(this);

    public AshwoodWolfEntity(
            EntityType<? extends PathAwareEntity> type,
            World world
    ) {
        super(type, world);
    }

    @Override
    protected void initGoals() {
        // World RPG owns pursuit, assistance, leash, and combat scheduling
        // in AuthoredMobRuntime. Do not install vanilla combat goals here.
    }

    @Override
    public void registerControllers(
            AnimatableManager.ControllerRegistrar controllers
    ) {
        controllers.add(
                new AnimationController<>(
                        this,
                        "locomotion",
                        3,
                        this::locomotion
                )
        );
        controllers.add(
                new AnimationController<>(
                        this,
                        "action",
                        0,
                        state -> PlayState.STOP
                )
                        .triggerableAnim("attack", ATTACK)
                        .triggerableAnim("howl", HOWL)
        );
    }

    private PlayState locomotion(
            AnimationState<AshwoodWolfEntity> state
    ) {
        if (state.isMoving()) {
            state.getController().setAnimation(WALK);
        } else {
            state.getController().setAnimation(IDLE);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }
}
