package com.github.yajatkaul.mega_showdown.api.event;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;

public interface MSDFormChanging {
    Event<MSDFormChanging> EVENT = EventFactory.createLoop(MSDFormChanging.class);

    void onFormChange(FormChangeEvent event);

    class FormChangeEvent {
        public final PokemonEntity context;
        public final BattlePokemon battlePokemon;
        public final PokemonBattle battle;

        private boolean cancelled = false;

        public FormChangeEvent(PokemonEntity context, BattlePokemon battlePokemon, PokemonBattle battle) {
            this.context = context;
            this.battlePokemon = battlePokemon;
            this.battle = battle;
        }

        public void cancel() {
            this.cancelled = true;
        }

        public boolean isCancelled() {
            return cancelled;
        }
    }
}