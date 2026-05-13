package com.github.yajatkaul.mega_showdown.utils;

import com.cobblemon.mod.common.api.types.ElementalType;
import com.github.yajatkaul.mega_showdown.item.MegaShowdownItems;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.item.Item;

import java.util.Locale;

public class TeraHelper {
    public static Item getTeraShardForType(ElementalType type) {
        String typeName = type.getName().toLowerCase(Locale.ROOT);
        RegistrySupplier<Item> shard = switch (typeName) {
            case "bug" -> MegaShowdownItems.BUG_TERA_SHARD;
            case "dark" -> MegaShowdownItems.DARK_TERA_SHARD;
            case "dragon" -> MegaShowdownItems.DRAGON_TERA_SHARD;
            case "electric" -> MegaShowdownItems.ELECTRIC_TERA_SHARD;
            case "fairy" -> MegaShowdownItems.FAIRY_TERA_SHARD;
            case "fighting" -> MegaShowdownItems.FIGHTING_TERA_SHARD;
            case "fire" -> MegaShowdownItems.FIRE_TERA_SHARD;
            case "flying" -> MegaShowdownItems.FLYING_TERA_SHARD;
            case "ghost" -> MegaShowdownItems.GHOST_TERA_SHARD;
            case "grass" -> MegaShowdownItems.GRASS_TERA_SHARD;
            case "ground" -> MegaShowdownItems.GROUND_TERA_SHARD;
            case "ice" -> MegaShowdownItems.ICE_TERA_SHARD;
            case "poison" -> MegaShowdownItems.POISON_TERA_SHARD;
            case "psychic" -> MegaShowdownItems.PSYCHIC_TERA_SHARD;
            case "rock" -> MegaShowdownItems.ROCK_TERA_SHARD;
            case "steel" -> MegaShowdownItems.STEEL_TERA_SHARD;
            case "water" -> MegaShowdownItems.WATER_TERA_SHARD;
            default -> MegaShowdownItems.NORMAL_TERA_SHARD;
        };

        return shard.get();
    }

    public static String getTeraAnimationFromAspect(String aspect) {
        if (aspect == null) {
            return "cobblemon:tera_normal";
        }
        return aspect.startsWith("msd:")
                ? aspect.replaceFirst("msd:", "cobblemon:")
                : "cobblemon:tera_normal";
    }
}
