package io.github.moulberry.morus.gui;

import io.github.moulberry.morus.gui.componenteditors.GUICEditorText;
import io.github.moulberry.morus.gui.elements.*;
import io.github.moulberry.morus.utils.FontUtils;
import io.github.moulberry.morus.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.Color;
import java.util.*;

public class GUIEditorFloating extends GUIEditor<GuiGroupFloating> {

    private final ResourceLocation icons = new ResourceLocation("morus:editor/floating/icons.png");

    protected final GuiButton guiButtonRemove = new GuiButton(100, 0, 0, "Remove");
    protected final GuiButton guiButtonRotate = new GuiButton(101, 0, 0, "Rotate Anchor");
    protected final GuiButton guiButtonEdit = new GuiButton(102, 0, 0, "Edit");

    private List<GuiElement> selectedElements = new ArrayList<>();
    private int clickedX = -1;
    private int clickedY = -1;
    private HashMap<GuiElement, Vector2f> clickedAnchors = new HashMap<>();

    private int selectStartX = -1;
    private int selectStartY = -1;

    private GuiAnchor.AnchorPoint resizeCorner = null;
    private Vector2f resizeStartSize = null;

    public GUIEditorFloating(String screenName, GuiGroupScreen mainScreen, LinkedList<GuiGroup> editQueue, GuiGroupFloating editing) {
        super(screenName, mainScreen, editQueue, editing);
    }

    @Override
    protected int getYForButtonID(int buttonID) {
        int superY = super.getYForButtonID(buttonID);

        if(superY >= 0) {
            return superY;
        }

        switch(buttonID) {
            case 100:
                return 78;
            case 101:
                return 53;
            case 102:
                return 28;
        }

        return -1;
    }

    @Override
    protected void updateButtons() {
        super.updateButtons();
        updateButton(guiButtonRemove);
        updateButton(guiButtonRotate);
        updateButton(guiButtonEdit);

        if(selectedElements.size() > 1) {
            guiButtonEdit.displayString = "Edit First";
        } else {
            guiButtonEdit.displayString = "Edit";
        }
    }

    @Override
    public void undo() {
        super.undo();
        selectedElements.clear();
    }

    @Override
    public void redo() {
        super.redo();
        selectedElements.clear();
    }

    @Override
    protected void renderButtons(int mouseX, int mouseY) {
        if(selectedElements.isEmpty()) {
            super.renderButtons(mouseX, mouseY);
        } else {
            guiButtonRemove.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
            guiButtonRotate.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
            guiButtonEdit.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
        }
    }

    @Override
    public boolean handleButtonClicks(int mouseX, int mouseY, int mouseButton) {
        if(selectedElements.isEmpty()) {
            return super.handleButtonClicks(mouseX, mouseY, mouseButton);
        } else {
            int scaleFactor = (editing instanceof GuiGroupScreen) ? new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor() : 1;
            if(guiButtonEdit.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY)) {
                GuiElement selectedElement = selectedElements.get(0);

                if(selectedElement instanceof GuiGroup) {
                    LinkedList<GuiGroup> newEditQueue = new LinkedList<>(editQueue);
                    newEditQueue.addLast(editing);
                    Minecraft.getMinecraft().displayGuiScreen(((GuiGroup)selectedElement).createEditor(screenName, mainScreen, newEditQueue));
                } else if(selectedElement instanceof GuiText) {
                    activeComponentEditor = new GUICEditorText(this, (GuiText) selectedElement);
                }
                return true;
            } else if(guiButtonRotate.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY)) {
                for(GuiElement element : selectedElements) {
                    recalculate();

                    Vector2f position = new Vector2f(editing.getChildrenPosition().get(element)); //copy
                    GuiAnchor anchor = editing.getChildrenMap().get(element);
                    if(anchor == null) return true;

                    GuiAnchor.AnchorPoint[] vals = GuiAnchor.AnchorPoint.values();
                    anchor.anchorPoint = vals[(anchor.anchorPoint.ordinal()+1)%vals.length];

                    recalculate();

                    Vector2f positionNew = editing.getChildrenPosition().get(element);
                    anchor.offset.x += (position.x - positionNew.x)*scaleFactor;
                    anchor.offset.y += (position.y - positionNew.y)*scaleFactor;

                    recalculate();
                }
                addUndoStep("Anchor");
                return true;
            } else if(guiButtonRemove.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY)) {
                for(GuiElement element : selectedElements) {
                    editing.removeChild(element);
                }
                selectedElements.clear();
                addUndoStep("Delete");
                return true;
            }
        }
        return false;
    }

    @Override
    protected void mouseClickedEditor(int mouseX, int mouseY, int mouseButton) {
        if(handleButtonClicks(mouseX, mouseY, mouseButton)) return;

        super.mouseClickedEditor(mouseX, mouseY, mouseButton);

        clickedX = clickedY = -1;
        resizeCorner = null;

        Vector2f groupPosition = getPosition();
        int mouseXGroup = mouseX - (int)groupPosition.x;
        int mouseYGroup = mouseY - (int)groupPosition.y;

        for(GuiElement element : editing.getChildren()) {
            if(selectedElements.contains(element)) {
                Vector2f position = editing.getChildrenPosition().get(element);
                int elementWidth = element.getWidth();
                int elementHeight = element.getHeight();

                if(mouseXGroup > position.x-4 && mouseXGroup < position.x+2 &&
                        mouseYGroup > position.y-4 && mouseYGroup < position.y+2) {
                    resizeCorner = GuiAnchor.AnchorPoint.TOPLEFT;
                } else if(mouseXGroup > position.x+elementWidth-2 && mouseXGroup < position.x+elementWidth+4 &&
                        mouseYGroup > position.y-4 && mouseYGroup < position.y+2) {
                    resizeCorner = GuiAnchor.AnchorPoint.TOPRIGHT;
                } else if(mouseXGroup > position.x-4 && mouseXGroup < position.x+2 &&
                        mouseYGroup > position.y+elementHeight-2 && mouseYGroup < position.y+elementHeight+4) {
                    resizeCorner = GuiAnchor.AnchorPoint.BOTLEFT;
                } else if(mouseXGroup > position.x+elementWidth-2 && mouseXGroup < position.x+elementWidth+4 &&
                        mouseYGroup > position.y+elementHeight-2 && mouseYGroup < position.y+elementHeight+4) {
                    resizeCorner = GuiAnchor.AnchorPoint.BOTRIGHT;
                } else {
                    continue;
                }

                resizeStartSize = new Vector2f(elementWidth, elementHeight);

                selectedElements.clear();
                selectedElements.add(element);

                clickedX = mouseXGroup;
                clickedY = mouseYGroup;
                GuiAnchor guiAnchor = editing.getChildrenMap().get(element);
                clickedAnchors.put(element, new Vector2f(guiAnchor.offset));

                return;
            }
        }

        for(GuiElement element : editing.getChildren()) {
            Vector2f position = editing.getChildrenPosition().get(element);

            if(mouseXGroup > position.x-4 && mouseXGroup < position.x + element.getWidth()+4) {
                if(mouseYGroup > position.y-4 && mouseYGroup < position.y + element.getHeight()+4) {
                    if(selectedElements.size() <= 1 || !selectedElements.contains(element)) {
                        selectedElements.clear();
                        selectedElements.add(element);
                    }

                    clickedX = mouseXGroup;
                    clickedY = mouseYGroup;
                }
            }
        }

        if(clickedX < 0 || clickedY < 0) selectedElements.clear();

        for(GuiElement selectedElement : selectedElements) {
            GuiAnchor guiAnchor = editing.getChildrenMap().get(selectedElement);
            clickedAnchors.put(selectedElement, new Vector2f(guiAnchor.offset));
        }
    }

    @Override
    protected void mouseClickMoveEditor(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMoveEditor(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);

        Vector2f position = getPosition();

        int mouseXGroup = mouseX - (int)position.x;
        int mouseYGroup = mouseY - (int)position.y;

        int scaleFactor = (editing instanceof GuiGroupScreen) ? new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor() : 1;

        if(resizeCorner != null && selectedElements.size() == 1 && resizeStartSize != null) {
            GuiElement element = selectedElements.get(0);
            int elementWidth = element.getWidth();
            int elementHeight = element.getHeight();
            int editingWidth = getComponentWidth();
            int editingHeight = getComponentHeight();
            if(element instanceof Resizable && clickedAnchors.containsKey(element)) {
                GuiAnchor guiAnchor = editing.getChildrenMap().get(element);

                float scaleFactorX = 1;
                float scaleFactorY = 1;
                float deltaX = (resizeCorner.x*2-1) * (mouseXGroup - clickedX);
                float deltaY = (resizeCorner.y*2-1) * (mouseYGroup - clickedY);

                ((Resizable)element).setSize(
                        Math.max(1, (int)(deltaX*scaleFactorX+resizeStartSize.x)),
                        Math.max(1, (int)(deltaY*scaleFactorY+resizeStartSize.y))
                );

                float deltaMouseX = (element.getWidth()-resizeStartSize.x)/scaleFactorX/(resizeCorner.x*2-1);
                float deltaMouseY = (element.getHeight()-resizeStartSize.y)/scaleFactorY/(resizeCorner.y*2-1);

                float xMult = 1 - Math.abs(resizeCorner.x - guiAnchor.anchorPoint.x);
                float yMult = 1 - Math.abs(resizeCorner.y - guiAnchor.anchorPoint.y);

                guiAnchor.offset.x = deltaMouseX*scaleFactor*xMult + (int)clickedAnchors.get(element).x;
                guiAnchor.offset.y = deltaMouseY*scaleFactor*yMult + (int)clickedAnchors.get(element).y;
            }
        } else if((selectStartY < 0 || selectStartX < 0) && !selectedElements.isEmpty() && clickedX >= 0 && clickedY >= 0) {
            for(GuiElement selectedElement : selectedElements) {
                if(!clickedAnchors.containsKey(selectedElement)) continue;
                GuiAnchor guiAnchor = editing.getChildrenMap().get(selectedElement);

                guiAnchor.offset.x = (mouseXGroup - clickedX)*scaleFactor + (int)clickedAnchors.get(selectedElement).x;
                guiAnchor.offset.y = (mouseYGroup - clickedY)*scaleFactor + (int)clickedAnchors.get(selectedElement).y;

                if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                    int dX = mouseXGroup - clickedX;
                    int dY = mouseYGroup - clickedY;
                    if(Math.abs(dX) > Math.abs(dY)) {
                        guiAnchor.offset.y = (int)clickedAnchors.get(selectedElement).y;
                    } else {
                        guiAnchor.offset.x = (int)clickedAnchors.get(selectedElement).x;
                    }
                }
            }
            recalculate();
            int editingWidth = getComponentWidth();
            int editingHeight = getComponentHeight();

            for(GuiElement selectedElement : selectedElements) {
                GuiAnchor guiAnchor = editing.getChildrenMap().get(selectedElement);
                Vector2f elementPosition = editing.getChildrenPosition().get(selectedElement);

                int elementWidth = selectedElement.getWidth();
                int elementHeight = selectedElement.getHeight();

                if(elementPosition != null) {
                    int dX = 0;
                    int dY = 0;

                    int x = (int)(position.x + elementPosition.x);
                    int y = (int)(position.y + elementPosition.y);

                    if(x < position.x) dX = x-(int)position.x;
                    if(x+elementWidth > editingWidth+position.x) dX = x+elementWidth-editingWidth-(int)position.x;
                    if(y < position.y) dY = y-(int)position.y;
                    if(y+elementHeight > editingHeight+position.y) dY = y+elementHeight-editingHeight-(int)position.y;

                    guiAnchor.offset.x -= dX*scaleFactor;
                    guiAnchor.offset.y -= dY*scaleFactor;
                }
            }
        } else {
            if(selectStartX < 0 || selectStartY < 0) {
                selectStartX = mouseX;
                selectStartY = mouseY;
            }

            selectedElements.clear();
            int minX = Math.min(selectStartX, mouseX);
            int maxX = Math.max(selectStartX, mouseX);
            int minY = Math.min(selectStartY, mouseY);
            int maxY = Math.max(selectStartY, mouseY);

            for(Map.Entry<GuiElement, Vector2f> elementEntry : editing.getChildrenPosition().entrySet()) {
                int elementWidth = elementEntry.getKey().getWidth();
                int elementHeight = elementEntry.getKey().getHeight();

                if(minX < position.x+elementEntry.getValue().x+elementWidth && maxX > position.x+elementEntry.getValue().x) {
                    if(minY < position.y+elementEntry.getValue().y+elementHeight && maxY > position.y+elementEntry.getValue().y) {
                        selectedElements.add(elementEntry.getKey());
                    }
                }
            }
        }
    }

    @Override
    protected void mouseReleasedEditor(int mouseX, int mouseY, int state) {
        super.mouseReleasedEditor(mouseX, mouseY, state);

        if(resizeCorner != null && selectedElements.size() == 1 && resizeStartSize != null) {
            addUndoStep("Resize");
        } else if((selectStartY < 0 || selectStartX < 0) && !selectedElements.isEmpty() && clickedX >= 0 && clickedY >= 0) {
            addUndoStep("Move");
        }

        selectStartX = selectStartY = -1;
        resizeCorner = null;
        clickedX = clickedY = -1;
    }

    @Override
    public void renderEditOverlay(int mouseX, int mouseY) {
        super.renderEditOverlay(mouseX, mouseY);

        long currentTime = System.currentTimeMillis();
        int bgColour = new Color(40, 40, 40, 150).getRGB();
        int blackColour = new Color(10, 10, 10, 255).getRGB();

        Vector2f editingPosition = getPosition();
        int editingWidth = getComponentWidth();
        int editingHeight = getComponentHeight();

        //dark overlay over stuff outside of the group we are editing
        //left
        drawRect(0, 0, (int)(editingPosition.x), height, bgColour);
        //right
        drawRect((int)(editingPosition.x+editingWidth), 0, width, height, bgColour);
        //top
        drawRect((int)(editingPosition.x), 0,
                (int)(editingPosition.x+editingWidth), (int)(editingPosition.y), bgColour);
        //bottom
        drawRect((int)(editingPosition.x), (int)(editingPosition.y+editingHeight),
                (int)(editingPosition.x+editingWidth), height, bgColour);

        //black border around editing
        //left
        drawRect((int)(editingPosition.x)-1, (int)(editingPosition.y)-1,
                (int)(editingPosition.x), (int)(editingPosition.y+editingHeight)+1, blackColour);
        //right
        drawRect((int)(editingPosition.x+editingWidth), (int)(editingPosition.y)-1,
                (int)(editingPosition.x+editingWidth)+1, (int)(editingPosition.y+editingHeight)+1, blackColour);
        //top
        drawRect((int)(editingPosition.x), (int)(editingPosition.y)-1,
                (int)(editingPosition.x+editingWidth), (int)(editingPosition.y), blackColour);
        //bottom
        drawRect((int)(editingPosition.x), (int)(editingPosition.y+editingHeight),
                (int)(editingPosition.x+editingWidth), (int)(editingPosition.y+editingHeight)+1, blackColour);

        Vector2f groupPosition = getPosition();
        int mouseXGroup = mouseX - (int)groupPosition.x;
        int mouseYGroup = mouseY - (int)groupPosition.y;

        GuiElement hoveredElement = null;
        for(GuiElement element : editing.getChildren()) {
            Vector2f position = editing.getChildrenPosition().get(element);

            if(mouseXGroup > position.x-4 && mouseXGroup < position.x + element.getWidth()+4) {
                if(mouseYGroup > position.y-4 && mouseYGroup < position.y + element.getHeight()+4) {
                    hoveredElement = element;
                }
            }
        }

        GL11.glTranslatef(editingPosition.x, editingPosition.y, 0);
        for(GuiElement element : editing.getChildren()) {
            GuiAnchor guiAnchor = editing.getChildrenMap().get(element);
            Vector2f position = editing.getChildrenPosition().get(element);

            if(element == hoveredElement && !selectedElements.contains(element)) {
                drawRect((int)position.x, (int)position.y,
                        (int)position.x+element.getWidth(), (int)position.y+element.getHeight(), new Color(200, 200, 200, 100).getRGB());
            } else {
                drawRect((int)position.x, (int)position.y,
                        (int)position.x+element.getWidth(), (int)position.y+element.getHeight(), new Color(100, 100, 100, 100).getRGB());
            }

            if(guiAnchor.inventoryRelative) {
                FontUtils.drawStringCentered(EnumChatFormatting.GOLD+"Inv-Relative", Minecraft.getMinecraft().fontRendererObj,
                        position.x+element.getWidth()*0.5f, position.y+element.getHeight()*0.5f, true, 0);
            }

            GlStateManager.color(1, 1, 1, 1);
            Minecraft.getMinecraft().getTextureManager().bindTexture(icons);
            RenderUtils.drawTexturedRect(position.x+element.getWidth()*guiAnchor.anchorPoint.x-4,
                    position.y+element.getHeight()*guiAnchor.anchorPoint.y-4.5f, 8, 9,
                    0, 16/256f, 18/256f, 36/256f, GL11.GL_NEAREST);

            if(selectedElements.contains(element)) {
                GlStateManager.color(1, 1, 1, 1);

                int elementWidth = element.getWidth();
                int elementHeight = element.getHeight();

                int cornerColour = element instanceof Resizable ? -1 : new Color(150, 150, 150, 255).getRGB();

                //topleft
                if(resizeCorner == GuiAnchor.AnchorPoint.TOPLEFT) {
                    drawRect((int)(position.x-2), (int)(position.y-2),
                            (int)(position.x+2), (int)(position.y+2), cornerColour);
                } else {
                    drawRect((int)(position.x-1), (int)(position.y-1),
                            (int)(position.x+1), (int)(position.y+1), cornerColour);
                }
                //topright
                if(resizeCorner == GuiAnchor.AnchorPoint.TOPRIGHT) {
                    drawRect((int)(position.x+elementWidth-2), (int)(position.y-2),
                            (int)(position.x+elementWidth+2), (int)(position.y+2), cornerColour);
                } else {
                    drawRect((int)(position.x+elementWidth-1), (int)(position.y-1),
                            (int)(position.x+elementWidth+1), (int)(position.y+1), cornerColour);
                }
                //botleft
                if(resizeCorner == GuiAnchor.AnchorPoint.BOTLEFT) {
                    drawRect((int)(position.x-2), (int)(position.y+elementHeight-2),
                            (int)(position.x+2), (int)(position.y+elementHeight+2), cornerColour);
                } else {
                    drawRect((int)(position.x-1), (int)(position.y+elementHeight-1),
                            (int)(position.x+1), (int)(position.y+elementHeight+1), cornerColour);
                }
                //botright
                if(resizeCorner == GuiAnchor.AnchorPoint.BOTRIGHT) {
                    drawRect((int)(position.x+elementWidth-2), (int)(position.y+elementHeight-2),
                            (int)(position.x+elementWidth+2), (int)(position.y+elementHeight+2), cornerColour);
                } else {
                    drawRect((int)(position.x+elementWidth-1), (int)(position.y+elementHeight-1),
                            (int)(position.x+elementWidth+1), (int)(position.y+elementHeight+1), cornerColour);
                }
            }
        }
        GL11.glTranslatef(-editingPosition.x, -editingPosition.y, 0);

        GL11.glScalef(5/4f, 5/4f, 1);
        if(selectStartX >= 0 && selectStartY >= 0) {
            int minX = Math.round(Math.min(selectStartX, mouseX)*4/5f);
            int maxX = Math.round(Math.max(selectStartX, mouseX)*4/5f);
            int minY = Math.round(Math.min(selectStartY, mouseY)*4/5f);
            int maxY = Math.round(Math.max(selectStartY, mouseY)*4/5f);

            minX = Math.max(minX, 0);
            maxX = Math.min(maxX, width*4/5);
            minY = Math.max(minY, 0);
            maxY = Math.min(maxY, height*4/5);

            drawRect(minX, minY, maxX, maxY, new Color(0, 100, 200, 70).getRGB());

            drawRect(maxX-1, minY, maxX, maxY, new Color(0, 120, 215, 255).getRGB());
            drawRect(minX, maxY-1, maxX, maxY, new Color(0, 120, 215, 255).getRGB());
            drawRect(minX, minY, minX+1, maxY, new Color(0, 120, 215, 255).getRGB());
            drawRect(minX, minY, maxX, minY+1, new Color(0, 120, 215, 255).getRGB());
        }
        GL11.glScalef(4/5f, 4/5f, 1);
    }
}
