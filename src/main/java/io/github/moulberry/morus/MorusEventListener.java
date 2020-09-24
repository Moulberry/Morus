package io.github.moulberry.morus;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MorusEventListener {

    private final Morus morus;

    public MorusEventListener(Morus morus) {
        this.morus = morus;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onOverlayRenderHigh(RenderGameOverlayEvent event) {
        if(event.type == RenderGameOverlayEvent.ElementType.ALL) {
            morus.getManager().renderOverlay(OverlayType.GAME_FIRST);
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onOverlayRender(RenderGameOverlayEvent event) {
        if(event.type == RenderGameOverlayEvent.ElementType.ALL) {
            morus.getManager().renderOverlay(OverlayType.GAME);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onOverlayRenderLow(RenderGameOverlayEvent event) {
        if(event.type == RenderGameOverlayEvent.ElementType.ALL) {
            morus.getManager().renderOverlay(OverlayType.GAME_LAST);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onGuiPreRenderHigh(GuiScreenEvent.BackgroundDrawnEvent event) {
        morus.getManager().renderOverlay(OverlayType.PREGUI_FIRST);
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onGuiPreRender(GuiScreenEvent.BackgroundDrawnEvent event) {
        morus.getManager().renderOverlay(OverlayType.PREGUI);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onGuiPreRenderLow(GuiScreenEvent.BackgroundDrawnEvent event) {
        morus.getManager().renderOverlay(OverlayType.PREGUI_LAST);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onGuiPostRenderHigh(GuiScreenEvent.DrawScreenEvent.Post event) {
        morus.getManager().renderOverlay(OverlayType.POSTGUI_FIRST);
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onGuiPostRender(GuiScreenEvent.DrawScreenEvent.Post event) {
        morus.getManager().renderOverlay(OverlayType.POSTGUI);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onGuiPostRenderLow(GuiScreenEvent.DrawScreenEvent.Post event) {
        morus.getManager().renderOverlay(OverlayType.POSTGUI_LAST);
    }

}
