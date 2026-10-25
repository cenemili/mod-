package com.example.watcher;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

import java.util.Random;

public class WatcherClient implements ClientModInitializer {
    private static final Identifier SCARE = new Identifier(WatcherMod.MODID, "textures/gui/jumpscare.png");
    private static final int DURATION = 40; // ticks (2 s)
    private static int ticks = 0;
    private static final Random RNG = new Random();

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(WatcherMod.WATCHER, WatcherRenderer::new);

        ClientPlayNetworking.registerGlobalReceiver(WatcherMod.JUMPSCARE_PACKET,
                (client, handler, buf, sender) -> client.execute(() -> ticks = DURATION));

        ClientTickEvents.END_CLIENT_TICK.register(c -> { if (ticks > 0) ticks--; });

        HudRenderCallback.EVENT.register((ctx, tickDelta) -> {
            if (ticks <= 0) return;
            MinecraftClient mc = MinecraftClient.getInstance();
            int w = mc.getWindow().getScaledWidth();
            int h = mc.getWindow().getScaledHeight();

            // fondo negro
            ctx.fill(0, 0, w, h, 0xFF000000);

            // imagen con temblor y fundido de salida
            float alpha = Math.min(1f, ticks / 10f);
            int shake = 6;
            int ox = RNG.nextInt(shake * 2 + 1) - shake;
            int oy = RNG.nextInt(shake * 2 + 1) - shake;
            ctx.setShaderColor(1f, 1f, 1f, alpha);
            ctx.drawTexture(SCARE, ox - shake, oy - shake, 0, 0, w + shake * 2, h + shake * 2,
                    w + shake * 2, h + shake * 2);
            ctx.setShaderColor(1f, 1f, 1f, 1f);
        });
    }
}
