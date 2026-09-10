package com.github.yajatkaul.mega_showdown.block;

import com.github.yajatkaul.mega_showdown.MegaShowdown;
import com.github.yajatkaul.mega_showdown.block.block_entity.MegaStoneStandEntity;
import com.github.yajatkaul.mega_showdown.block.block_entity.PedestalBlockEntity;
import com.github.yajatkaul.mega_showdown.block.block_entity.ReassemblyUnitBlockEntity;
import com.github.yajatkaul.mega_showdown.block.block_entity.WishingStarEntity;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class MegaShowdownBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(MegaShowdown.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    public static void register() {
        BLOCK_ENTITIES.register();
    }

    public static final RegistrySupplier<BlockEntityType<ReassemblyUnitBlockEntity>> REASSEMBLY_UNIT_ENTITY =
            BLOCK_ENTITIES.register("reassembly_unit_entity", () ->
                    BlockEntityType.Builder.of(ReassemblyUnitBlockEntity::new, MegaShowdownBlocks.REASSEMBLY_UNIT.get()).build(null)
            );

    public static final RegistrySupplier<BlockEntityType<PedestalBlockEntity>> PEDESTAL_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("pedestal_block_entity", () ->
                    BlockEntityType.Builder.of(PedestalBlockEntity::new, MegaShowdownBlocks.PEDESTAL.get()).build(null)
            );

    public static final RegistrySupplier<BlockEntityType<MegaStoneStandEntity>> MEGA_STONE_STAND_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("mega_stone_stand_be", () ->
                    BlockEntityType.Builder.of(MegaStoneStandEntity::new, MegaShowdownBlocks.MEGA_STONE_CRYSTAL.get()).build(null)
            );

    public static final RegistrySupplier<BlockEntityType<WishingStarEntity>> WISHINGSTAR_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("wishingstar_be", () ->
                    BlockEntityType.Builder.of(WishingStarEntity::new, MegaShowdownBlocks.WISHING_STAR_CRYSTAL.get()).build(null)
            );
}
