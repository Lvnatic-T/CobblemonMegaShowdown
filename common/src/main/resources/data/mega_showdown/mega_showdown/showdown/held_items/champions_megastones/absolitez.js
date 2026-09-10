({
  name: "Absolite Z",
  spritenum: 499,
  megaStone: { "Absol": "Absol-Mega-Z" },
  itemUser: ["Absol"],
  onTakeItem(item, source) {
    return !item.megaStone?.[source.baseSpecies.baseSpecies];
  },
  num: 2638,
	gen: 9,
  isNonstandard: "Future",
})