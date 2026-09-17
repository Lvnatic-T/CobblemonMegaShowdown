package com.github.yajatkaul.mega_showdown.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.ActorType;
import com.cobblemon.mod.common.api.moves.BenchedMove;
import com.cobblemon.mod.common.api.moves.BenchedMoves;
import com.cobblemon.mod.common.api.moves.Move;
import com.cobblemon.mod.common.api.moves.MoveSet;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.feature.FlagSpeciesFeature;
import com.cobblemon.mod.common.api.pokemon.feature.StringSpeciesFeature;
import com.cobblemon.mod.common.api.pokemon.moves.LearnsetQuery;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.net.messages.client.battle.BattleTransformPokemonPacket;
import com.cobblemon.mod.common.net.messages.client.battle.BattleUpdateTeamPokemonPacket;
import com.cobblemon.mod.common.net.messages.client.pokemon.update.AbilityUpdatePacket;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.properties.UnaspectPropertyType;
import com.github.yajatkaul.mega_showdown.api.codec.Effect;
import com.github.yajatkaul.mega_showdown.config.MegaShowdownConfig;
import com.github.yajatkaul.mega_showdown.gimmick.MaxGimmick;
import com.github.yajatkaul.mega_showdown.gimmick.MegaGimmick;
import com.github.yajatkaul.mega_showdown.tag.MegaShowdownTags;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.Biome;

public class AspectUtils {
    public static final Set<UUID> battleDisconnecter = new HashSet<>();

    public static void applyAspects(Pokemon pokemon, List<String> aspects) {
        FormData oldForm = pokemon.getForm();
        for (String aspect : aspects) {
            String[] aspect_split = aspect.split("=");
            if (aspect_split[1].equals("true") || aspect_split[1].equals("false")) {
                new FlagSpeciesFeature(aspect_split[0], Boolean.parseBoolean(aspect_split[1])).apply(pokemon);
            } else {
                new StringSpeciesFeature(aspect_split[0], aspect_split[1]).apply(pokemon);
            }
        }
        updateMovesOnFormChange(pokemon, oldForm);
    }

    public static void applyProperties(Pokemon pokemon, Optional<String> propertyString) {
        FormData oldForm = pokemon.getForm();
        propertyString.ifPresent((property) -> {
                    PokemonProperties properties = PokemonProperties.Companion.parse(property);
                    properties.apply(pokemon);
                }
        );
        updateMovesOnFormChange(pokemon, oldForm);
    }

    private static void updateMovesOnFormChange(Pokemon pokemon, FormData oldForm) {
        FormData newForm = pokemon.getForm();
        if (oldForm.equals(newForm)) return;

        MoveSet moveSet = pokemon.getMoveSet();
        BenchedMoves benchedMoves = pokemon.getBenchedMoves();
        Set<MoveTemplate> oldFormChangeMoves = new HashSet<>(oldForm.getMoves().getFormChangeMoves());

        // Only remove moves that were granted by the OLD form's form-change moves AND are not learnable
        // by the NEW form - a move the new form can still legally have shouldn't be stripped just because
        // it happened to come from the old form's exclusive list.
        for (int i = 0; i < MoveSet.MOVE_COUNT; i++) {
            Move move = moveSet.get(i);
            if (move != null
                    && oldFormChangeMoves.contains(move.getTemplate())
                    && !LearnsetQuery.Companion.getANY().canLearn(move.getTemplate(), newForm.getMoves())) {
                moveSet.setMove(i, null);
            }
        }

        List<MoveTemplate> noLongerLearnable = new ArrayList<>();
        for (BenchedMove benchedMove : benchedMoves) {
            if (!LearnsetQuery.Companion.getANY().canLearn(benchedMove.getMoveTemplate(), newForm.getMoves())) {
                noLongerLearnable.add(benchedMove.getMoveTemplate());
            }
        }
        noLongerLearnable.forEach(benchedMoves::remove);

        // Add the new form's form-change moves, skipping any that are also a level-up move the Pokémon
        // hasn't reached yet. E.g. Glaciate shouldn't be granted by a form change if it's also a level 80
        // move and this Pokémon is below level 80.
        Map<Integer, List<MoveTemplate>> levelUpMoves = newForm.getMoves().getLevelUpMoves();
        for (MoveTemplate move : newForm.getMoves().getFormChangeMoves()) {
            boolean alreadyKnown = false;
            for (Move known : moveSet.getMoves()) {
                if (known.getTemplate().equals(move)) {
                    alreadyKnown = true;
                    break;
                }
            }
            if (alreadyKnown) {
                continue;
            }

            Integer requiredLevel = null;
            for (Map.Entry<Integer, List<MoveTemplate>> entry : levelUpMoves.entrySet()) {
                if (entry.getValue().contains(move)) {
                    requiredLevel = entry.getKey();
                    break;
                }
            }
            if (requiredLevel != null && pokemon.getLevel() < requiredLevel) {
                continue;
            }

            if (moveSet.hasSpace()) {
                moveSet.add(move.create());
            } else {
                benchedMoves.add(new BenchedMove(move, 0));
            }
        }

        // If moveset is empty try to find one valid move to fill it.
        if (moveSet.getMoves().isEmpty()) {
            BenchedMove firstBenched = null;
            for (BenchedMove benchedMove : benchedMoves) {
                firstBenched = benchedMove;
                break;
            }
            // This shouldn't ever be null, but you never know with data driven.
            if (firstBenched != null) {
                moveSet.setMove(0, new Move(firstBenched.getMoveTemplate(), firstBenched.getPpRaisedStages(), 0));
            }
        }
    }

    public static void appendRevertDataPokemon(Effect effect, List<String> aspects, Optional<String> properties, Pokemon pokemon, String tagName) {
        EffectPair effectPair = new EffectPair(effect, aspects, properties);

        List<EffectPair> existing = getRevertDataPokemon(pokemon, tagName);

        existing.add(effectPair);

        Tag encoded =
                EffectPair.CODEC.listOf()
                        .encodeStart(NbtOps.INSTANCE, existing)
                        .getOrThrow();

        pokemon.getPersistentData().put(tagName, encoded);
        pokemon.onChange(null);
    }

    public static List<EffectPair> getRevertDataPokemon(Pokemon pokemon, String tagName) {
        Tag raw = pokemon.getPersistentData().get(tagName);

        if (raw == null) return new ArrayList<>();

        return EffectPair.CODEC.listOf()
                .parse(NbtOps.INSTANCE, raw)
                .result()
                .map(ArrayList::new)
                .orElseGet(ArrayList::new);
    }

    public static void revertPokemonsIfRequiredBattleEnd(ServerPlayer player) {
        if (player == null || battleDisconnecter.contains(player.getUUID())) {
            if (player != null) {
                battleDisconnecter.remove(player.getUUID());
            }
            return;
        }

        PlayerPartyStore playerPartyStore = Cobblemon.INSTANCE.getStorage().getParty(player);
        for (Pokemon pokemon : playerPartyStore) {
            AspectUtils.revertPokemonsIfRequired(pokemon, false);

            if (pokemon.getSpecies().getName().equals("Burmy")) {
                Holder<Biome> biomeHolder = player.serverLevel().getBiome(player.blockPosition());

                ServerLevel serverLevel = (ServerLevel) player.level();
                boolean isTrash = serverLevel.structureManager()
                        .getStructureWithPieceAt(player.blockPosition(), MegaShowdownTags.Biomes.VILLAGE)
                        .isValid();


                boolean isPlant = false;
                boolean isSandy = false;

                if (!isTrash) {
                    isSandy = MegaShowdownTags.Biomes.SANDY_BIOMES
                            .stream().anyMatch(biomeHolder::is);

                    isPlant = MegaShowdownTags.Biomes.PLANT_BIOMES
                            .stream().anyMatch(biomeHolder::is);
                }

                if (isTrash) {
                    new StringSpeciesFeature("bagworm_cloak", "trash").apply(pokemon);
                } else if (isPlant) {
                    new StringSpeciesFeature("bagworm_cloak", "plant").apply(pokemon);
                } else if (isSandy) {
                    new StringSpeciesFeature("bagworm_cloak", "sandy").apply(pokemon);
                }
            }
        }
    }

    public static void revertPokemonsIfRequired(PlayerPartyStore playerPartyStore) {
        for (Pokemon pokemon : playerPartyStore) {
            AspectUtils.revertPokemonsIfRequired(pokemon, false);
        }
    }

    public static void revertPokemonsIfRequiredBattleStart(PlayerPartyStore playerPartyStore) {
        for (Pokemon pokemon : playerPartyStore) {
            AspectUtils.revertPokemonsIfRequired(pokemon, true);
        }
    }

    public static void revertPokemonsIfRequired(Pokemon pokemon, boolean battleStart) {
        if (battleStart) {
            if (pokemon.getPersistentData().getBoolean(MegaGimmick.IS_MEGA_TAG)) {
                pokemon.getPersistentData().remove(MegaGimmick.IS_MEGA_TAG);
                MegaGimmick.unmegaEvolve(pokemon);
            }
        }

        if (pokemon.getPersistentData().contains("battle_end_revert")) {
            List<EffectPair> aspects = AspectUtils.getRevertDataPokemon(
                    pokemon,
                    "battle_end_revert"
            );

            for (EffectPair effectPair : aspects) {
                effectPair.effect.revertEffects(pokemon, effectPair.aspects, effectPair.pokemonProperties, null);
            }

            pokemon.getPersistentData().remove("battle_end_revert");
        }

        if (pokemon.getPersistentData().contains("aspects")) {
            List<EffectPair> aspects = AspectUtils.getRevertDataPokemon(
                    pokemon,
                    "aspects"
            );

            for (EffectPair effectPair : aspects) {
                effectPair.effect.revertEffects(pokemon, effectPair.aspects, effectPair.pokemonProperties, null);
            }

            pokemon.getPersistentData().remove("aspects");
        }

        if (pokemon.getPersistentData().contains("revert_aspects")) {
            List<EffectPair> aspects = AspectUtils.getRevertDataPokemon(
                    pokemon,
                    "revert_aspects"
            );

            for (EffectPair effectPair : aspects) {
                effectPair.effect.revertEffects(pokemon, effectPair.aspects, effectPair.pokemonProperties, null);
            }

            pokemon.getPersistentData().remove("revert_aspects");
        }

        if (pokemon.getPersistentData().getBoolean("is_tera") || pokemon.getAspects().stream().anyMatch((s) -> s.startsWith("msd:tera_"))) {
            pokemon.getAspects().stream().filter(a -> a.startsWith("msd:tera_")).forEach(name -> {
                UnaspectPropertyType.INSTANCE.fromString(name).apply(pokemon);
            });
            UnaspectPropertyType.INSTANCE.fromString("play_tera").apply(pokemon);
            if (pokemon.getEntity() instanceof PokemonEntity pokemonEntity) {
                if (MegaShowdownConfig.legacyTeraEffect) {
                    pokemon.getEntity().setGlowingTag(false);
                    if (pokemonEntity.level() instanceof ServerLevel serverLevel) {
                        ServerScoreboard scoreboard = serverLevel.getScoreboard();
                        scoreboard.removePlayerFromTeam(pokemonEntity.getScoreboardName());
                    }
                }
            }
            pokemon.getPersistentData().remove("is_tera");
        }

        if (pokemon.getPersistentData().getBoolean("is_max")) {
            if (pokemon.getAspects().contains("gmax")) {
                Effect.getEffect("mega_showdown:dynamax").revertEffects(pokemon, List.of("dynamax_form=none"), Optional.empty(), null);
            } else {
                UnaspectPropertyType.INSTANCE.fromString("msd:dmax").apply(pokemon);
                Effect.getEffect("mega_showdown:dynamax").revertEffects(pokemon, List.of(), Optional.empty(), null);
            }
            if (pokemon.getEntity() != null) {
                MaxGimmick.startGradualScalingDown(pokemon);
            } else {
                pokemon.setScaleModifier(pokemon.getPersistentData().getFloat("orignal_size"));
            }
            pokemon.getPersistentData().remove("is_max");
        }

        if (pokemon.getPersistentData().getBoolean("form_changing")) {
            pokemon.getPersistentData().remove("form_changing");
        }
    }

    public static void updatePackets(BattlePokemon battlePokemon) {
        Pokemon pokemon = battlePokemon.getEffectedPokemon();
        PokemonBattle battle = battlePokemon.getActor().getBattle();

        if (battlePokemon.getActor().getType() == ActorType.PLAYER) {
            battle.sendUpdate(new AbilityUpdatePacket(
                    battlePokemon::getEffectedPokemon,
                    pokemon.getAbility().getTemplate()
            ));

            battle.sendUpdate(new BattleUpdateTeamPokemonPacket(pokemon));
        }

        for (ActiveBattlePokemon active : battle.getActivePokemon()) {
            if (active.getBattlePokemon() == null) continue;
            if (active.getBattlePokemon() != battlePokemon) continue;

            battle.sendSidedUpdate(
                    active.getActor(),
                    new BattleTransformPokemonPacket(active.getPNX(), battlePokemon, true),
                    new BattleTransformPokemonPacket(active.getPNX(), battlePokemon, false),
                    false
            );
        }
    }

    public record EffectPair(
            Effect effect,
            List<String> aspects,
            Optional<String> pokemonProperties
    ) {
        public static final Codec<EffectPair> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Effect.CODEC.fieldOf("effect").forGetter(EffectPair::effect),
                Codec.STRING.listOf().fieldOf("aspects").forGetter(EffectPair::aspects),
                Codec.STRING.optionalFieldOf("pokemon_properties").forGetter(EffectPair::pokemonProperties)
        ).apply(instance, EffectPair::new));
    }
}
