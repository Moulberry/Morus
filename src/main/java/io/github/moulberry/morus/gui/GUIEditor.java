package io.github.moulberry.morus.gui;

import com.google.gson.JsonObject;
import io.github.moulberry.morus.Morus;
import io.github.moulberry.morus.MorusSerializer;
import io.github.moulberry.morus.gui.componenteditors.GUICEditor;
import io.github.moulberry.morus.gui.componenteditors.GUICEditorCreate;
import io.github.moulberry.morus.gui.elements.GuiElement;
import io.github.moulberry.morus.gui.elements.GuiGroup;
import io.github.moulberry.morus.gui.elements.GuiGroupScreen;
import io.github.moulberry.morus.gui.elements.GuiText;
import io.github.moulberry.morus.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GUIEditor<T extends GuiGroup> extends GuiScreen {

    private static final ResourceLocation backgroundTile = new ResourceLocation("minecraft:textures/blocks/prismarine_dark.png");

    protected final String screenName;
    protected GuiGroupScreen mainScreen;
    protected final LinkedList<GuiGroup> editQueue;
    protected T editing;

    private final List<JsonObject> undoQueue = new ArrayList<>();
    private final List<String> undoTypeQueue = new ArrayList<>();
    private int undoLocation = -1;

    protected GUICEditor activeComponentEditor = null;

    protected final GuiButton guiButtonSave = new GuiButton(0, 0, 0, "Save");
    protected final GuiButton guiButtonExit = new GuiButton(1, 0, 0, "Exit");
    protected final GuiButton guiButtonOverlay = new GuiButton(2, 0, 0, "Overlay Manager");
    protected final GuiButton guiButtonPreset = new GuiButton(3, 0, 0, "Preset Manager");
    protected final GuiButton guiButtonUndo = new GuiButton(4, 0, 0, "Undo");
    protected final GuiButton guiButtonRedo = new GuiButton(5, 0, 0, "Redo");
    protected final GuiButton guiButtonPreview = new GuiButton(6, 0, 0, "Preview");
    protected final GuiButton guiButtonCreate = new GuiButton(7, 0, 0, "Create Element");

    protected boolean preview = false;

    int backgroundTextureCopy = -1;

    public GUIEditor(String screenName, GuiGroupScreen mainScreen, LinkedList<GuiGroup> editQueue, T editing) {
        this.screenName = screenName;
        if(editQueue.isEmpty() && editing instanceof GuiGroupScreen) {
            this.mainScreen = (GuiGroupScreen)mainScreen.clone();
            this.editQueue = editQueue;
            this.editing = (T) this.mainScreen;
        } else {
            this.mainScreen = mainScreen;
            this.editQueue = editQueue;
            this.editing = editing;
        }
    }

    public void closeComponentEditor(GuiElement elementOriginal, GuiElement elementNew) {
        if(editQueue.isEmpty() && editing instanceof GuiGroupScreen && editing == elementOriginal) {
            this.mainScreen = (GuiGroupScreen)elementNew;
            this.editing = (T) this.mainScreen;
        } else if(editing == elementOriginal) {
            GuiGroup previousGroup = mainScreen;
            for(GuiGroup group : editQueue) {
                previousGroup.replaceChild(elementOriginal, elementNew);
                previousGroup = group;
            }
            editing = (T) elementNew;
        } else {
            GuiGroup previousGroup = mainScreen;
            for(GuiGroup group : editQueue) {
                previousGroup.replaceChild(elementOriginal, elementNew);
                previousGroup = group;
            }
            editing.replaceChild(elementOriginal, elementNew);
        }

        if(activeComponentEditor instanceof GUICEditorCreate) {
            addUndoStep("Create");
        } else {
            addUndoStep("Edit");
        }

        activeComponentEditor = null;
    }

    private JsonObject serializeEditing() {
        if(editing instanceof GuiGroupScreen && editQueue.isEmpty()) {
            return MorusSerializer.serializeScreen(mainScreen);
        } else {
            return MorusSerializer.boxType(editing);
        }
    }

    private void deserializeEditing(JsonObject object) {
        if(editing instanceof GuiGroupScreen && editQueue.isEmpty()) {
            this.mainScreen = MorusSerializer.deserializeScreen(object);
            this.editing = (T)mainScreen;
        } else {
            GuiElement newEditing = MorusSerializer.deserializeElement(object);
            if(newEditing != null) {
                this.editQueue.getLast().replaceChild(this.editing, newEditing);
                this.editing = (T)newEditing;
            }
        }
    }

    public void addUndoStep(String type) {
        for(int i=undoLocation+1; i<undoQueue.size(); i++) {
            int index = undoQueue.size()-1;
            if(index >= 0) {
                undoQueue.remove(index);
                undoTypeQueue.remove(index);
            }
        }

        while(undoQueue.size() > 30) {
            undoQueue.remove(0);
            undoTypeQueue.remove(0);
        }

        JsonObject object = serializeEditing();
        if(!undoQueue.isEmpty() && object.equals(undoQueue.get(undoQueue.size()-1))) {
            undoLocation = undoQueue.size()-1;
            return;
        }

        undoQueue.add(object);
        undoTypeQueue.add(type);
        undoLocation = undoQueue.size()-1;
    }

    public void undo() {
        undoLocation--;
        undoLocation = Math.max(0, Math.min(undoQueue.size()-1, undoLocation));
        if(undoLocation < undoQueue.size()) {
            JsonObject object = undoQueue.get(undoLocation);
            deserializeEditing(object);
        }
    }

    public void redo() {
        undoLocation++;
        undoLocation = Math.max(0, Math.min(undoQueue.size()-1, undoLocation));
        if(undoLocation < undoQueue.size()) {
            JsonObject object = undoQueue.get(undoLocation);
            deserializeEditing(object);
        }
    }

    protected void renderBackground(int width, int height,
                                 int subX, int subY, int subWidth, int subHeight,
                                 int x, int y, int scaledWidth, int scaledHeight) {
        float uMin = subX/(float)width;
        float uMax = (subX+subWidth)/(float)width;
        float vMin = 1-subY/(float)height;
        float vMax = 1-(subY+subHeight)/(float)height;

        GlStateManager.bindTexture(backgroundTextureCopy);
        GlStateManager.color(1f, 1f, 1f, 1f);

        Vec3 fog = Minecraft.getMinecraft().theWorld.getFogColor(0);
        //GL11.glClearColor();
        GlStateManager.clearColor((float)fog.xCoord, (float)fog.yCoord, (float)fog.zCoord, 1);

        RenderUtils.drawTexturedRect(x, y, scaledWidth, scaledHeight, uMin, uMax, vMin, vMax);
    }

    protected void initTexture() {
        if(backgroundTextureCopy < 0) {
            backgroundTextureCopy = TextureUtil.glGenTextures();
        }
    }

    protected void copyTexture() {
        Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(false);
        GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, backgroundTextureCopy);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB,
                Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight,
                0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);

        GL11.glClearColor(0, 0, 0, 0);

        GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0,
                Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
    }

    public int getComponentWidth() {
        return editing.getWidth();
    }

    public int getComponentHeight() {
        return editing.getHeight();
    }

    protected int getYForButtonID(int buttonID) {
        switch (buttonID) {
            case 0:
            case 1:
                return 16;
            case 2:
                return 42;
            case 3:
                return 62;
            case 4:
            case 5:
                return 86;
            case 6:
                return 106;
            case 7:
                return 131;
        }
        return -1;
    }

    protected void updateButton(GuiButton button) {
        button.setWidth(156);
        button.xPosition = width/2 - 78;
        button.yPosition = (height-166)/2 + getYForButtonID(button.id);
    }

    protected void updateButtonLeft(GuiButton button) {
        button.setWidth(76);
        button.xPosition = width/2 - 78;
        button.yPosition = (height-166)/2 + getYForButtonID(button.id);
    }

    protected void updateButtonRight(GuiButton button) {
        button.setWidth(76);
        button.xPosition = width/2 + 2;
        button.yPosition = (height-166)/2 + getYForButtonID(button.id);
    }

    protected void updateButtons() {
        updateButtonLeft(guiButtonSave);
        updateButtonRight(guiButtonExit);
        updateButton(guiButtonOverlay);
        updateButton(guiButtonPreset);
        updateButtonLeft(guiButtonUndo);
        updateButtonRight(guiButtonRedo);
        updateButton(guiButtonPreview);
        updateButton(guiButtonCreate);

        guiButtonUndo.displayString = "Undo";
        if(undoLocation >= 0 && undoLocation < undoTypeQueue.size()) {
            String str = undoTypeQueue.get(undoLocation);
            if(str != null) {
                guiButtonUndo.displayString = "Undo " + str;
            }
        }

        guiButtonRedo.displayString = "Redo";
        if(undoLocation+1 >= 0 && undoLocation+1 < undoTypeQueue.size()) {
            String str = undoTypeQueue.get(undoLocation+1);
            if(str != null) {
                guiButtonRedo.displayString = "Redo " + str;
            }
        }
    }

    protected void renderButtons(int mouseX, int mouseY) {
        guiButtonSave.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
        guiButtonExit.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
        guiButtonOverlay.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
        guiButtonPreset.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
        guiButtonUndo.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
        guiButtonRedo.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
        guiButtonPreview.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
        guiButtonCreate.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
    }

    protected void renderBackground() {
        drawDefaultBackground();

        initTexture();
        copyTexture();

        drawRect(0, 0, width, height, new Color(0, 0, 0, 255).getRGB());

        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        this.mc.getTextureManager().bindTexture(backgroundTile);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        float f = 32.0F;
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        worldrenderer.pos(0.0D, this.height, 0.0D)
                .tex(0.0D, (float)this.height / 32.0F + 0)
                .color(64, 64, 64, 255).endVertex();
        worldrenderer.pos(this.width, this.height, 0.0D)
                .tex((float)this.width / 32.0F, (float)this.height / 32.0F + 0)
                .color(64, 64, 64, 255).endVertex();
        worldrenderer.pos(this.width, 0.0D, 0.0D)
                .tex((float)this.width / 32.0F, 0)
                .color(64, 64, 64, 255).endVertex();
        worldrenderer.pos(0.0D, 0.0D, 0.0D)
                .tex(0.0D,0).color(64, 64, 64, 255).endVertex();
        tessellator.draw();

        renderBackground(width, height,
                0, 0, width, height,
                0, 0, width, height);

        drawRect((width-176)/2, (height-166)/2, (width+176)/2, (height+166)/2, new Color(80, 80, 80, 80).getRGB());
    }

    public void renderEditOverlay(int mouseX, int mouseY) {
    }

    public void recalculate() {
        mainScreen.recalculate();
    }

    public void renderComponentEditor(int mouseX, int mouseY, float partialTicks) {
        if(activeComponentEditor != null) {
            activeComponentEditor.drawScreen(mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        //calculateScaleAndPadding();

        renderBackground();

        if(undoQueue.isEmpty()) addUndoStep(null);

        updateButtons();

        recalculate();
        mainScreen.render(0, 0);

        if(!preview) {
            GlStateManager.translate(0, 0, 500);
            renderEditOverlay(mouseX, mouseY);
            GlStateManager.translate(0, 0, -500);
        }

        GlStateManager.disableDepth();

        renderButtons(mouseX, mouseY);

        renderComponentEditor(mouseX, mouseY, partialTicks);
    }

    public Vector2f getPosition() {
        Vector2f position = new Vector2f();
        GuiGroup previousGroup = mainScreen;
        for(GuiGroup group : editQueue) {
            if(group != previousGroup) {
                Vector2f offset = previousGroup.getChildrenPosition().get(group);
                if(offset != null) {
                    Vector2f.add(position, offset, position);
                }
                previousGroup = group;
            }
        }

        Vector2f offset = previousGroup.getChildrenPosition().get(editing);
        if(offset != null) {
            Vector2f.add(position, offset, position);
        }

        return position;
    }

    public boolean handleButtonClicks(int mouseX, int mouseY, int mouseButton) {
        if(guiButtonSave.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY)) {
            Morus.getInstance().getManager().overlayMap.put(screenName, (GuiGroupScreen) mainScreen.clone());
        } else if(guiButtonExit.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY)) {
            if(editQueue.isEmpty()) {
                Minecraft.getMinecraft().displayGuiScreen(null);
            } else {
                LinkedList<GuiGroup> newEditQueue = new LinkedList<>(editQueue);
                Minecraft.getMinecraft().displayGuiScreen(((GuiGroup)newEditQueue.removeLast()).createEditor(screenName, mainScreen, newEditQueue));
            }
        } else if(guiButtonOverlay.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY)) {

        } else if(guiButtonPreset.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY)) {

        } else if(guiButtonUndo.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY)) {
            undo();
        } else if(guiButtonRedo.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY)) {
            redo();
        } else if(guiButtonPreview.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY)) {
            preview = !preview;
        } else if(guiButtonCreate.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY)) {
            if(editing instanceof GuiGroup) {
                activeComponentEditor = new GUICEditorCreate(this, editing);
            }
            //editing.addChild(new GuiText("Hi"));
        } else {
            return false;
        }
        return true;
    }

    protected void mouseClickedEditor(int mouseX, int mouseY, int mouseButton) {
        handleButtonClicks(mouseX, mouseY, mouseButton);
    }

    protected void mouseReleasedEditor(int mouseX, int mouseY, int state) {
    }

    protected void mouseClickMoveEditor(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
    }

    protected void keyTypedEditor(char typedChar, int keyCode) {
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        try { super.mouseClicked(mouseX, mouseY, mouseButton); } catch(IOException e) {}
        if(activeComponentEditor != null) {
            activeComponentEditor.mouseClicked(mouseX, mouseY, mouseButton);
        } else {
            mouseClickedEditor(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        if(activeComponentEditor != null) {
            activeComponentEditor.mouseReleased(mouseX, mouseY, state);
        } else {
            mouseReleasedEditor(mouseX, mouseY, state);
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        if(activeComponentEditor != null) {
            activeComponentEditor.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        } else {
            mouseClickMoveEditor(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if(activeComponentEditor != null) {
            activeComponentEditor.keyTyped(typedChar, keyCode);
        } else {
            try { super.keyTyped(typedChar, keyCode); } catch(IOException e) {}
            keyTypedEditor(typedChar, keyCode);
        }
    }

    /*public List<MousePosition> getMousePositions(int mouseX, int mouseY) {
        ArrayList<MousePosition> mousePositions = new ArrayList<>();

        Vector2f position = getPosition();
        int width = getComponentWidth();
        int height = getComponentHeight();

        if(mouseX > position.x && mouseX < position.x + width) {
            if(mouseY > position.y && mouseY < position.y + height) {
                mousePositions.add(new MousePosition(MousePosition.Location.GROUP,
                        mouseX - (int)position.x, mouseY - (int)position.y));
            }
        }

        mousePositions.add(new MousePosition(MousePosition.Location.SCREEN, mouseX, mouseY));

        return mousePositions;
    }*/

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
