package com.github.yajatkaul.mega_showdown.render;

import com.github.yajatkaul.mega_showdown.MegaShowdown;
import com.github.yajatkaul.mega_showdown.api.codec.item.ItemRenderingCodec;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ItemRenderingLoader implements ResourceManagerReloadListener {
    private static volatile Map<ResourceLocation, ItemRenderingCodec> registry = Map.of();
    private static final String DIRECTORY = "item_rendering";

    public static ItemRenderingCodec get(ResourceLocation itemId) {
        return registry.get(itemId);
    }

    public static Collection<ItemRenderingCodec> entries() {
        return registry.values();
    }

    private static void load(ResourceManager resourceManager) {
        HashMap<ResourceLocation, ItemRenderingCodec> loadedRegistry = new HashMap<>();

        Collection<ResourceLocation> resources =
                resourceManager.listResources(DIRECTORY, path -> path.getPath().endsWith(".json")).keySet();

        for (ResourceLocation id : resources) {
            try (var stream = resourceManager.getResource(id).get().open()) {
                ItemRenderingCodec codec = ItemRenderingCodec.CODEC.parse(
                        JsonOps.INSTANCE,
                        JsonParser.parseReader(new InputStreamReader(stream))
                ).result().orElseThrow();
                loadedRegistry.put(codec.itemId(), codec);
            } catch (Exception e) {
                MegaShowdown.LOGGER.error("Failed loading item_rendering JSON: {}", id, e);
            }
        }

        registry = Map.copyOf(loadedRegistry);
        MegaShowdown.LOGGER.info("Loaded {} custom rendering entries", registry.size());
    }

    @Override
    public @NotNull CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller profilerFiller, ProfilerFiller profilerFiller2, Executor executor, Executor executor2) {
        load(resourceManager);
        return ResourceManagerReloadListener.super.reload(preparationBarrier, resourceManager, profilerFiller, profilerFiller2, executor, executor2);
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {

    }

    @Override
    public @NotNull String getName() {
        return "mega_showdown";
    }
}