package com.github.yajatkaul.mega_showdown.api.codec.particles;

import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.github.yajatkaul.mega_showdown.MegaShowdown;
import com.github.yajatkaul.mega_showdown.api.codec.Effect;
import com.github.yajatkaul.mega_showdown.api.event.MSDFormChanging;
import com.github.yajatkaul.mega_showdown.utils.AspectUtils;
import com.github.yajatkaul.mega_showdown.utils.PokemonBehaviourHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import kotlin.Unit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public record SnowStormParticle(
        Optional<List<String>> source_apply,
        Optional<List<String>> target_apply,
        Optional<List<String>> source_revert,
        Optional<List<String>> target_revert,
        Optional<String> particle_apply,
        Optional<Float> apply_after,
        Optional<String> particle_revert,
        Optional<Float> revert_after,
        Optional<SoundCodec> sound_apply,
        Optional<SoundCodec> sound_revert,
        Optional<AnimationData> animations
) {
    public static final Codec<SnowStormParticle> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(Codec.STRING).optionalFieldOf("source_apply").forGetter(SnowStormParticle::source_apply),
            Codec.list(Codec.STRING).optionalFieldOf("target_apply").forGetter(SnowStormParticle::target_apply),
            Codec.list(Codec.STRING).optionalFieldOf("source_revert").forGetter(SnowStormParticle::source_revert),
            Codec.list(Codec.STRING).optionalFieldOf("target_revert").forGetter(SnowStormParticle::target_revert),
            Codec.STRING.optionalFieldOf("particle_apply").forGetter(SnowStormParticle::particle_apply),
            Codec.FLOAT.optionalFieldOf("apply_after").forGetter(SnowStormParticle::apply_after),
            Codec.STRING.optionalFieldOf("particle_revert").forGetter(SnowStormParticle::particle_revert),
            Codec.FLOAT.optionalFieldOf("revert_after").forGetter(SnowStormParticle::revert_after),
            SoundCodec.CODEC.optionalFieldOf("sound_apply").forGetter(SnowStormParticle::sound_apply),
            SoundCodec.CODEC.optionalFieldOf("sound_revert").forGetter(SnowStormParticle::sound_revert),
            AnimationData.CODEC.optionalFieldOf("animations").forGetter(SnowStormParticle::animations)
    ).apply(instance, SnowStormParticle::new));

    public void apply(PokemonEntity context, List<String> aspects, Optional<String> properties, PokemonEntity other) {
        processTransformation(context, aspects, properties, other, null, true);
    }

    public void revert(PokemonEntity context, List<String> aspects, Optional<String> properties, PokemonEntity other) {
        processTransformation(context, aspects, properties, other, null, false);
    }

    public void applyBattle(PokemonEntity context, List<String> aspects, Optional<String> properties, PokemonEntity other, BattlePokemon battlePokemon, float battle_pause, boolean loop) {
        battlePokemon.actor.getBattle().dispatchWaitingToFront(battle_pause, () -> Unit.INSTANCE);
        processTransformation(context, aspects, properties, other, battlePokemon, true);
    }

    public void revertBattle(PokemonEntity context, List<String> aspects, Optional<String> properties, PokemonEntity other, BattlePokemon battlePokemon, float battle_pause) {
        battlePokemon.actor.getBattle().dispatchWaitingToFront(battle_pause, () -> Unit.INSTANCE);
        processTransformation(context, aspects, properties, other, battlePokemon, false);
    }

    private void processTransformation(
            PokemonEntity context,
            List<String> aspects,
            Optional<String> properties,
            PokemonEntity other,
            BattlePokemon battlePokemon,
            boolean isApply
    ) {
        MSDFormChanging.FormChangeEvent event =
                new MSDFormChanging.FormChangeEvent(context, battlePokemon, null);

        MSDFormChanging.EVENT.invoker().onFormChange(event);

        if (event.isCancelled()) {
            return;
        }

        boolean wasInBattle = context.isBattling();

        context.setNoAi(true);

        CompoundTag pokemonPersistentData = context.getPokemon().getPersistentData();
        pokemonPersistentData.putBoolean("form_changing", true);

        AspectUtils.appendRevertDataPokemon(
                Effect.empty(),
                aspects,
                properties,
                context.getPokemon(),
                "aspects"
        );

        Optional<String> particle = isApply ? particle_apply : particle_revert;
        Optional<List<String>> sourceParticles = isApply ? source_apply : source_revert;
        Optional<List<String>> targetParticles = isApply ? target_apply : target_revert;
        Optional<Float> delay = isApply ? apply_after : revert_after;

        playSound(context, isApply);

        animations.ifPresent(animation -> {
            if (isApply) {
                animation.applyAnimations(context);
            } else {
                animation.revertAnimations(context);
            }
        });

        Runnable finish = () -> {
            boolean nowInBattle = context.isBattling();

            if (wasInBattle != nowInBattle) {
                pokemonPersistentData.putBoolean("form_changing", false);
                return;
            }

            applyAspectsAndCleanup(
                    context,
                    aspects,
                    properties,
                    pokemonPersistentData,
                    battlePokemon
            );
        };

        particle.ifPresentOrElse(particles -> {
            ResourceLocation particleId = ResourceLocation.tryParse(particles);

            if (particleId == null) {
                MegaShowdown.LOGGER.error(
                        "Invalid snowstorm {} particle{}",
                        isApply ? "apply" : "revert",
                        battlePokemon != null ? " during battle" : ""
                );
                return;
            }

            PokemonBehaviourHelper.Companion.snowStormPartileSpawner(
                    context,
                    particleId,
                    sourceParticles.orElse(null),
                    other,
                    targetParticles.orElse(null)
            );
        }, finish);

        delay.ifPresentOrElse(
                delayValue -> context.after(delayValue, () -> {
                    finish.run();
                    return Unit.INSTANCE;
                }),
                finish
        );
    }

    private void applyAspectsAndCleanup(PokemonEntity context, List<String> aspects, Optional<String> properties,
                                        CompoundTag pokemonPersistentData, BattlePokemon battlePokemon) {
        AspectUtils.applyAspects(context.getPokemon(), aspects);
        AspectUtils.applyProperties(context.getPokemon(), properties);

        if (battlePokemon != null) {
            AspectUtils.updatePackets(battlePokemon);
        }

        resetFormChangingState(context, pokemonPersistentData);
    }

    private void resetFormChangingState(PokemonEntity context, CompoundTag pokemonPersistentData) {
        context.setNoAi(false);

        pokemonPersistentData.remove("form_changing");
        pokemonPersistentData.remove("aspects");
    }

    private void playSound(PokemonEntity context, boolean isApply) {
        Optional<SoundCodec> sound = isApply ? sound_apply : sound_revert;

        sound.ifPresent((soundCodec -> soundCodec.play(context)));
    }
}