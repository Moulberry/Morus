package io.github.moulberry.morus.gui.componenteditors;

import com.google.gson.JsonObject;
import io.github.moulberry.morus.gui.GUIEditor;
import io.github.moulberry.morus.gui.elements.*;
import io.github.moulberry.morus.utils.FontUtils;
import io.github.moulberry.morus.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class GUICEditorCreate extends GUICEditor<GuiGroup> {

    private static final ResourceLocation createElementBoxResource = new ResourceLocation("morus:editor/createelement/createelementbox.png");
    private static final ResourceLocation createElementResource = new ResourceLocation("morus:editor/createelement/createelement.png");

    private static final List<Creatable> creatables = new ArrayList<>();

    private final int xSize = 352;
    private final int ySize = 249;

    private static class Creatable {
        private String displayName;
        private String descLine1;
        private String descLine2;
        private GuiElement element;

        public Creatable(String displayName, String descLine1, String descLine2, GuiElement element) {
            this.displayName = displayName;
            this.descLine1 = descLine1;
            this.descLine2 = descLine2;
            this.element = element;
        }
    }

    static {
        creatables.add(new Creatable("Text Element",
                "Some very cool text on line 1",
                "Some slightly less cool text on line 2",
                new GuiText("Text")));
        creatables.add(new Creatable("Floating Group",
                "Some very cool text on line 1",
                "Some slightly less cool text on line 2",
                new GuiGroupFloating(50, 50, new LinkedHashMap<>())));
        creatables.add(new Creatable("Aligned Group",
                "Some very cool text on line 1",
                "Some slightly less cool text on line 2",
                new GuiGroupAligned(new ArrayList<>(), false, 5)));
    }

    public GUICEditorCreate(GUIEditor editor, GuiGroup editingOriginal) {
        super(editor, editingOriginal);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        int sF = scaledResolution.getScaleFactor();
        int width = scaledResolution.getScaledWidth();
        int height = scaledResolution.getScaledHeight();

        int guiLeft = (width - xSize)/2;
        int guiTop = (height - ySize)/2;

        Minecraft.getMinecraft().getTextureManager().bindTexture(createElementResource);
        GlStateManager.color(1f, 1f, 1f, 1f);
        RenderUtils.drawTexturedRect(guiLeft, guiTop, xSize, ySize, GL11.GL_NEAREST);

        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

        fr.drawString("Create Element", guiLeft+13, guiTop+13, new Color(80, 80, 80).getRGB());

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((guiLeft+14)*sF, (height-198-guiTop-35)*sF, 304*sF, 198*sF);
        for(int i=0; i<creatables.size(); i++) {
            Minecraft.getMinecraft().getTextureManager().bindTexture(createElementBoxResource);
            GlStateManager.color(1f, 1f, 1f, 1f);
            RenderUtils.drawTexturedRect(guiLeft+14, guiTop+35+i*66, 304, 60, GL11.GL_NEAREST);

            drawRect(guiLeft+14+4, guiTop+35+i*66+4, guiLeft+14+36, guiTop+35+i*66+36, -1);
            FontUtils.drawStringScaled(creatables.get(i).displayName, fr,
                    guiLeft+54, guiTop+35+i*66+12, false, new Color(80, 80, 80).getRGB(), 2f);

            FontUtils.drawStringF(creatables.get(i).descLine1, fr,
                    guiLeft+18, guiTop+35+i*66+39, false, new Color(80, 80, 80).getRGB());
            FontUtils.drawStringF(creatables.get(i).descLine2, fr,
                    guiLeft+18, guiTop+35+i*66+49, false, new Color(80, 80, 80).getRGB());

            FontUtils.drawStringCentered("Add To GUI", fr,
                    guiLeft+14+304-34, guiTop+35+i*66+11, false, new Color(80, 80, 80).getRGB());

        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);

        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        int width = scaledResolution.getScaledWidth();
        int height = scaledResolution.getScaledHeight();

        int guiLeft = (width - xSize)/2;
        int guiTop = (height - ySize)/2;

        for(int i=0; i<creatables.size(); i++) {
            if(mouseX > guiLeft+14+304-4-60 && mouseX < guiLeft+14+304-4) {
                if(mouseY > guiTop+35+i*66+4 && mouseY < guiTop+35+i*66+16) {
                    editingObject.addChild(creatables.get(i).element.clone());
                    editor.closeComponentEditor(editingOriginal, editingObject);
                }
            }
        }
    }
}
