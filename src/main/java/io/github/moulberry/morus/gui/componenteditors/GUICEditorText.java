package io.github.moulberry.morus.gui.componenteditors;

import io.github.moulberry.morus.gui.GUIEditor;
import io.github.moulberry.morus.gui.elements.GuiGroup;
import io.github.moulberry.morus.gui.elements.GuiText;
import io.github.moulberry.morus.utils.FontUtils;
import io.github.moulberry.morus.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class GUICEditorText extends GUICEditor<GuiText> {

    private static final ResourceLocation textEditorResource = new ResourceLocation("morus:editor/text/texteditor.png");

    private final GuiTextField textField;

    private final int xSize = 352;
    private final int ySize = 249;

    public GUICEditorText(GUIEditor editor, GuiText editingOriginal) {
        super(editor, editingOriginal);
        textField = new GuiTextField(0, Minecraft.getMinecraft().fontRendererObj, 0, 0, 1000, 50);
        textField.setMaxStringLength(999);
        textField.setText(editingOriginal.getTextRaw());
        textField.setFocused(true);
        textField.setCanLoseFocus(false);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        int sF = scaledResolution.getScaleFactor();
        int width = scaledResolution.getScaledWidth();
        int height = scaledResolution.getScaledHeight();

        int guiLeft = (width - xSize)/2;
        int guiTop = (height - ySize)/2;

        Minecraft.getMinecraft().getTextureManager().bindTexture(textEditorResource);
        GlStateManager.color(1f, 1f, 1f, 1f);
        RenderUtils.drawTexturedRect(guiLeft, guiTop, xSize, ySize, GL11.GL_NEAREST);

        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

        fr.drawString("Edit Text", guiLeft+13, guiTop+13, new Color(80, 80, 80).getRGB());

        int componentWidth = editingObject.getWidth();
        int componentHeight = editingObject.getHeight();

        float horizontalScale = 318f/componentWidth;
        float verticalScale = 16f/componentHeight;

        float textScale = Math.min(horizontalScale, verticalScale);

        GL11.glScalef(textScale, textScale, 1);
        editingObject.render( (guiLeft + xSize/2f)/textScale - componentWidth/2f, (guiTop + 46f)/textScale - componentHeight/2f);
        GL11.glScalef(1/textScale, 1/textScale, 1);

        String rawText = editingObject.getTextRaw();

        int rawWidth = fr.getStringWidth(rawText);
        int rawHeight = 8;

        float rawHorizontalScale = 318f/rawWidth;
        float rawVerticalScale = 16f/rawHeight;

        float rawTextScale = Math.min(rawHorizontalScale, rawVerticalScale);

        GL11.glScalef(rawTextScale, rawTextScale, 1);
        FontUtils.drawStringF(rawText, fr, (guiLeft + xSize/2f)/rawTextScale - rawWidth/2f, (guiTop + 70f)/rawTextScale - rawHeight/2f, false, -1);
        GL11.glScalef(1/rawTextScale, 1/rawTextScale, 1);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);
        textField.textboxKeyTyped(typedChar, keyCode);
        editingObject.setText(textField.getText());
    }
}
