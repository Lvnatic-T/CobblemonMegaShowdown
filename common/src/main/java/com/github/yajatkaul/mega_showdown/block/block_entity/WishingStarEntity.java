package com.github.yajatkaul.mega_showdown.block.block_entity;

import com.github.yajatkaul.mega_showdown.block.MegaShowdownBlockEntities;
import com.github.yajatkaul.mega_showdown.block.block_entity.renderer.state.MegaStoneStandState;
import com.github.yajatkaul.mega_showdown.block.block_entity.renderer.state.WishingStarState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class WishingStarEntity extends BlockEntity {
    public final WishingStarState state = new WishingStarState();

    public WishingStarEntity(BlockPos blockPos, BlockState blockState) {
        super(MegaShowdownBlockEntities.WISHINGSTAR_BLOCK_ENTITY.get(), blockPos, blockState);
    }
}
