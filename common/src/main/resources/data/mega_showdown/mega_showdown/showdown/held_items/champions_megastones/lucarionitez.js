({
  name: "Lucarionite Z",
	spritenum: 502,
  megaStone: { "Lucario": "Lucario-Mega-Z" },
  itemUser: ["Lucario"],
  onTakeItem(item, source) {
    return !item.megaStone?.[source.baseSpecies.baseSpecies];
  },
	num: 2641,
	gen: 9,
  isNonstandard: "Future"
})