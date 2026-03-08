package com.github.yajatkaul.mega_showdown.plugin;

import com.github.yajatkaul.mega_showdown.block.block_entity.ReassemblyUnitBlockEntity;
import com.github.yajatkaul.mega_showdown.block.custom.ReassemblyUnitBlock;
import com.github.yajatkaul.mega_showdown.item.MegaShowdownItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

public enum ReassemblyComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        if (blockAccessor.getBlock() instanceof ReassemblyUnitBlock reassemblyUnitBlock) {
            ReassemblyUnitBlockEntity blockEntity = reassemblyUnitBlock.getMainPart(blockAccessor.getBlockState(), blockAccessor.getPosition(), blockAccessor.getLevel());

            if (blockEntity != null) {
                int cookTime = blockAccessor.getServerData().getInt("cookTime");
                int maxCookTime = blockAccessor.getServerData().getInt("maxCookTime");
                int state = blockAccessor.getServerData().getInt("state");

                int remainingTicks = maxCookTime - cookTime;

                IElementHelper elements = IElementHelper.get();
                IElement icon = elements.item(new ItemStack(MegaShowdownItems.ZYGARDE_CUBE), 0.5f).size(new Vec2(10, 10)).translate(new Vec2(0, -1));;
                iTooltip.add(icon);
                if (state == 0) {
                    iTooltip.append(Component.translatable("ui.mega_showdown.idle"));
                } else if (state == 1){
                    iTooltip.append(Component.literal(formatTime(remainingTicks)));
                } else {
                    iTooltip.append(Component.translatable("ui.mega_showdown.ready"));
                }
            }
        }
    }

    private static String formatTime(int ticks) {
        int seconds = ticks / 20;

        int days = seconds / 86400;
        seconds %= 86400;

        int hours = seconds / 3600;
        seconds %= 3600;

        int minutes = seconds / 60;
        seconds %= 60;

        StringBuilder time = new StringBuilder();

        if (days > 0) time.append(days).append("d ");
        if (hours > 0) time.append(hours).append("h ");
        if (minutes > 0) time.append(minutes).append("m ");
        if (seconds > 0 || time.isEmpty()) time.append(seconds).append("s");

        return time.toString();
    }

    @Override
    public ResourceLocation getUid() {
        return JadePlugin.JADE;
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        if (blockAccessor.getBlock() instanceof ReassemblyUnitBlock reassemblyUnitBlock) {
            ReassemblyUnitBlockEntity blockEntity = reassemblyUnitBlock.getMainPart(blockAccessor.getBlockState(), blockAccessor.getPosition(), blockAccessor.getLevel());

            if (blockEntity != null) {
                compoundTag.putInt("cookTime", blockEntity.getCookTime());
                compoundTag.putInt("maxCookTime", blockEntity.getMaxCookTime());
                compoundTag.putInt("state", blockEntity.getState());
            }
        }
    }
}
