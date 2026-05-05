{
onStart(pokemon) {
  if (pokemon.baseSpecies.baseSpecies !== 'Darmanitan' || pokemon.transformed) return;
  if (pokemon.hp <= pokemon.maxhp / 2) {
    if (pokemon.species.id === 'darmanitangalar') {
      pokemon.formeChange('Darmanitan-Galar-Zen');
    } else if (pokemon.species.id === 'darmanitan') {
      pokemon.formeChange('Darmanitan-Zen');
    }
  }
  if (pokemon.hp > pokemon.maxhp / 2) {
    if (pokemon.species.id === 'darmanitangalarzen') {
      pokemon.formeChange('Darmanitan-Galar');
    } else if (pokemon.species.id === 'darmanitanzen') {
      pokemon.formeChange('Darmanitan');
    }
  }
},

  onResidualOrder: 29,
  onResidual(pokemon) {
    if (pokemon.baseSpecies.baseSpecies !== 'Darmanitan' || pokemon.transformed) return;
    if (pokemon.hp <= pokemon.maxhp / 2) {
      if (pokemon.species.id === 'darmanitangalar') {
        pokemon.formeChange('Darmanitan-Galar-Zen');
      } else if (pokemon.species.id === 'darmanitan') {
        pokemon.formeChange('Darmanitan-Zen');
      }
    }

    if (pokemon.hp > pokemon.maxhp / 2) {
      if (pokemon.species.id === 'darmanitangalarzen') {
        pokemon.formeChange('Darmanitan-Galar');
      } else if (pokemon.species.id === 'darmanitanzen') {
        pokemon.formeChange('Darmanitan');
      }
    }
  },

  flags: {
    failroleplay: 1,
    noreceiver: 1,
    noentrain: 1,
    notrace: 1,
    failskillswap: 1,
    cantsuppress: 1,
  },

  name: 'Zen Mode',
  rating: 0,
  num: 161
}