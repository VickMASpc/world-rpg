package dev.worldrpg.content.combat;

import dev.worldrpg.api.id.RpgId;
import dev.worldrpg.api.registry.RegistryPublisher;
import dev.worldrpg.content.load.ContentLoader;
import dev.worldrpg.content.source.ContentSource;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombatContentDomainsTest {
    @Test
    void authoredAuraAndAbilityCompileThroughP2Pipeline() {
        RegistryPublisher publisher = new RegistryPublisher();
        ContentLoader loader = new ContentLoader(
                CombatContentDomains.catalog(),
                publisher
        );

        var result = loader.loadAndPublish(List.of(
                aura(),
                ability("world_rpg:aura/test/focus")
        ));

        assertTrue(result.published());
        assertFalse(result.report().hasErrors());

        P3CombatContentSnapshot compiled =
                P3CombatContentCompiler.compile(publisher.active());

        assertEquals(1, compiled.auras().size());
        assertEquals(1, compiled.abilities().size());
        assertEquals(
                20,
                compiled.requireAbility(
                        RpgId.parse("world_rpg:ability/test/bolt")
                ).castDurationTicks()
        );
    }

    @Test
    void missingAuraReferenceRejectsWholeCandidate() {
        RegistryPublisher publisher = new RegistryPublisher();
        ContentLoader loader = new ContentLoader(
                CombatContentDomains.catalog(),
                publisher
        );

        var result = loader.loadAndPublish(List.of(
                ability("world_rpg:aura/test/missing")
        ));

        assertFalse(result.published());
        assertTrue(result.report().hasErrors());
        assertEquals(0, publisher.active().registryCount());
    }

    private static ContentSource aura() {
        return ContentSource.of(
                "aura.json",
                """
                {
                  "schema": 1,
                  "registry": "world_rpg:registry/auras",
                  "id": "world_rpg:aura/test/focus",
                  "max_stacks": 1,
                  "duration_ticks": 200,
                  "uniqueness": "per_target",
                  "refresh_policy": "reset_duration",
                  "stat_modifiers": [
                    {
                      "stat": "world_rpg:stat/test_power",
                      "operation": "add",
                      "amount_per_stack": 5.0,
                      "priority": 1
                    }
                  ]
                }
                """
        );
    }

    private static ContentSource ability(String auraId) {
        return ContentSource.of(
                "ability.json",
                """
                {
                  "schema": 1,
                  "registry": "world_rpg:registry/abilities",
                  "id": "world_rpg:ability/test/bolt",
                  "cast_kind": "timed",
                  "cast_duration_ticks": 20,
                  "cooldown_ticks": 10,
                  "global_cooldown_ticks": 5,
                  "movement_policy": "interrupt",
                  "costs": [],
                  "conditions": [
                    {"type": "max_range", "blocks": 12.0}
                  ],
                  "effects": [
                    {
                      "type": "apply_aura",
                      "recipient": "target",
                      "aura": "%s"
                    }
                  ]
                }
                """.formatted(auraId)
        );
    }
}
