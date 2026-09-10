({
  name: "Baxcalibrite",
  spritenum: 514,
  megaStone: { "Baxcalibur": "Baxcalibur-Mega" },
  itemUser: ["Baxcalibur"],
  onTakeItem(item, source) {
    return !item.megaStone?.[source.baseSpecies.baseSpecies];
  },
  num: 2648,
	gen: 9,
  isNonstandard: "Future"
})