package dev.worldrpg.combat.effect;

import dev.worldrpg.combat.actor.CombatActor;

public enum EffectRecipient {
    SOURCE {
        @Override
        public CombatActor resolve(EffectContext context) {
            return context.source();
        }
    },
    TARGET {
        @Override
        public CombatActor resolve(EffectContext context) {
            return context.target();
        }
    };

    public abstract CombatActor resolve(EffectContext context);
}
