({
  name: "Garchompite Z",
  spritenum: 501,
  megaStone: { "Garchomp": "Garchomp-Mega-Z" },
  itemUser: ["Garchomp"],
  onTakeItem(item, source) {
    return !item.megaStone?.[source.baseSpecies.baseSpecies];
  },
  num: 2640,
	gen: 9,
  isNonstandard: "Future"
})