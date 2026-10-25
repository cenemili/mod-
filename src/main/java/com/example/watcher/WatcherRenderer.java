package com.example.watcher;

import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;

public class WatcherRenderer extends BipedEntityRenderer<WatcherEntity, BipedEntityModel<WatcherEntity>> {
    private static final Identifier TEXTURE = new Identifier(WatcherMod.MODID, "textures/entity/watcher.png");

    public WatcherRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new BipedEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE)), 0.5f);
    }

    @Override
    public Identifier getTexture(WatcherEntity entity) { return TEXTURE; }
}
