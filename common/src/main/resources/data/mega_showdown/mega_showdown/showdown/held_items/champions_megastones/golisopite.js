({
  name: "Golisopite",
  spritenum: 508,
  megaStone: { "Golisopod": "Golisopod-Mega" },
  itemUser: ["Golisopod"],
  onTakeItem(item, source) {
    return !item.megaStone?.[source.baseSpecies.baseSpecies];
  },
	num: 2645,
	gen: 9,
  isNonstandard: "Future"
})