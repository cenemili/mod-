package com.example.watcher;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WatcherMod implements ModInitializer {
    public static final String MODID = "the_watcher";
    public static final Identifier JUMPSCARE_PACKET = new Identifier(MODID, "jumpscare");

    public static final EntityType<WatcherEntity> WATCHER = Registry.register(
            Registries.ENTITY_TYPE, new Identifier(MODID, "watcher"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, WatcherEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6f, 1.95f))
                    .trackRangeBlocks(96)
                    .build());

    @Override
    public void onInitialize() {
        FabricDefaultAttributeRegistry.register(WATCHER, WatcherEntity.createWatcherAttributes());

        // /watcher  -> invoca uno cerca de ti (para probar)
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, env) ->
                dispatcher.register(CommandManager.literal("watcher")
                        .requires(src -> src.hasPermissionLevel(2))
                        .executes(ctx -> {
                            ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                            spawnNear(p);
                            return 1;
                        })));

        // Aparición aleatoria: cada 30 s, de noche o bajo tierra
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (world.getRegistryKey() != World.OVERWORLD || world.getTime() % 600 != 0) return;
            for (ServerPlayerEntity p : world.getPlayers()) {
                if (p.isSpectator() || p.isCreative()) continue;
                boolean exists = !world.getEntitiesByType(WATCHER, p.getBoundingBox().expand(128), e -> true).isEmpty();
                boolean dark = world.isNight() || !world.isSkyVisible(p.getBlockPos());
                if (!exists && dark && world.random.nextFloat() < 0.25f) spawnNear(p);
            }
        });
    }

    public static boolean spawnNear(ServerPlayerEntity p) {
        ServerWorld w = p.getServerWorld();
        for (int tries = 0; tries < 25; tries++) {
            double ang = w.random.nextDouble() * Math.PI * 2;
            double dist = 24 + w.random.nextInt(10);
            int x = (int) (p.getX() + Math.cos(ang) * dist);
            int z = (int) (p.getZ() + Math.sin(ang) * dist);
            for (int dy = -5; dy <= 5; dy++) {
                BlockPos pos = new BlockPos(x, (int) p.getY() + dy, z);
                BlockPos below = pos.down();
                if (w.getBlockState(below).isSolidBlock(w, below) && w.isAir(pos) && w.isAir(pos.up())) {
                    WatcherEntity e = WATCHER.create(w);
                    if (e == null) return false;
                    e.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
                    w.spawnEntity(e);
                    return true;
                }
            }
        }
        return false;
    }
}
