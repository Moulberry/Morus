package io.github.moulberry.morus;

import com.google.common.collect.Lists;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

import java.util.List;
import java.util.TreeSet;

public class MorusSubstitutorListener {

    private TreeSet<Long> clickTicksLeft = new TreeSet<>();
    private TreeSet<Long> clickTicksRight = new TreeSet<>();

    @SubscribeEvent
    public void onMouse(MouseEvent event) {
        long currentTime = System.currentTimeMillis();
        if(event.buttonstate) {
            if(event.button == 0) {
                clickTicksLeft.add(currentTime);
            }
            if(event.button == 1) {
                clickTicksRight.add(currentTime);
            }
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        long currentTime = System.currentTimeMillis();

        Long old = 0L;
        while((old = clickTicksLeft.floor(currentTime - 1000)) != null) {
            clickTicksLeft.remove(old);
        }
        while((old = clickTicksRight.floor(currentTime - 1000)) != null) {
            clickTicksRight.remove(old);
        }

        MorusSubstitutor.putSubstiution("morus", "cps.cpsleft", ""+clickTicksLeft.size());
        MorusSubstitutor.putSubstiution("morus", "cps.cpsright", ""+clickTicksRight.size());

        Minecraft mc = Minecraft.getMinecraft();

        MorusSubstitutor.putSubstiution("morus", "f3.fps", ""+Minecraft.getDebugFPS());
        MorusSubstitutor.putSubstiution("morus", "f3.chunkupdates", ""+RenderChunk.renderChunksUpdated);
        MorusSubstitutor.putSubstiution("morus", "f3.t",
                mc.gameSettings.limitFramerate == GameSettings.Options.FRAMERATE_LIMIT.getValueMax() ?
                        "inf" : ""+mc.gameSettings.limitFramerate);
        MorusSubstitutor.putSubstiution("morus", "f3.vsync", mc.gameSettings.enableVsync ? " vsync" : "");
        MorusSubstitutor.putSubstiution("morus", "f3.fast", mc.gameSettings.fancyGraphics ? "" : " fast");
        MorusSubstitutor.putSubstiution("morus", "f3.clouds", mc.gameSettings.clouds == 0 ? "" : (mc.gameSettings.clouds == 1 ? " fast-clouds" : " fancy-clouds"));
        MorusSubstitutor.putSubstiution("morus", "f3.vbo", OpenGlHelper.useVbo() ? " vbo" : "");
        MorusSubstitutor.putSubstiution("morus", "f3.debug", mc.debug);

        /*mc.renderGlobal.getDebugInfoRenders();
        mc.renderGlobal.getDebugInfoEntities();

        List<String> list = Lists.newArrayList(new String[] {
                "Minecraft 1.8.9 (" + this.mc.getVersion() + "/" + ClientBrandRetriever.getClientModName() + ")",
                this.mc.debug,
                this.mc.renderGlobal.getDebugInfoRenders(),
                this.mc.renderGlobal.getDebugInfoEntities(),
                "P: " + this.mc.effectRenderer.getStatistics() + ". T: " + this.mc.theWorld.getDebugLoadedEntities(),
                 this.mc.theWorld.getProviderName(),
                 "",
                 String.format("XYZ: %.3f / %.5f / %.3f", Double.valueOf(this.mc.getRenderViewEntity().posX),
                         Double.valueOf(this.mc.getRenderViewEntity().getEntityBoundingBox().minY),
                         Double.valueOf(this.mc.getRenderViewEntity().posZ)),
                 String.format("Block: %d %d %d", Integer.valueOf(blockpos.getX()), Integer.valueOf(blockpos.getY()), Integer.valueOf(blockpos.getZ())),
                 String.format("Chunk: %d %d %d in %d %d %d", Integer.valueOf(blockpos.getX() & 15), Integer.valueOf(blockpos.getY() & 15), Integer.valueOf(blockpos.getZ() & 15), Integer.valueOf(blockpos.getX() >> 4), Integer.valueOf(blockpos.getY() >> 4), Integer.valueOf(blockpos.getZ() >> 4)),
                 String.format("Facing: %s (%s) (%.1f / %.1f)", new Object[]{enumfacing, s, MathHelper.wrapAngleTo180_float(entity.rotationYaw), MathHelper.wrapAngleTo180_float(entity.rotationPitch)})});
        */



    }

}
