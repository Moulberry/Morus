package io.github.moulberry.morus.gui.elements;

import com.google.gson.JsonObject;
import io.github.moulberry.morus.gui.GuiAnchor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.util.vector.Vector2f;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public class GuiGroupScreen extends GuiGroupFloating {

    private GuiScreen lastScreen = null;

    private HashMap<GuiElement, Vector2f> childrenPositionOffset = new HashMap<>();

    public GuiGroupScreen(LinkedHashMap<GuiElement, GuiAnchor> children) {
        super(0, 0, children);
    }

    @Override
    public GuiElement clone() {
        LinkedHashMap<GuiElement, GuiAnchor> newChildren = new LinkedHashMap<>();
        for(Map.Entry<GuiElement, GuiAnchor> childEntry : getChildrenMap().entrySet()) {
            newChildren.put(childEntry.getKey().clone(), childEntry.getValue().clone());
        }
        return new GuiGroupScreen(newChildren);
    }

    @Override
    public JsonObject serialize() {
        return null;
    }

    @Override
    public void recalculate() {
        lastScreen = null;

        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        width = scaledResolution.getScaledWidth();
        height = scaledResolution.getScaledHeight();

        for(GuiElement child : getChildren()) {
            child.recalculate();
        }

        for(Map.Entry<GuiElement, GuiAnchor> entry : getChildrenMap().entrySet()) {
            GuiElement child = entry.getKey();
            GuiAnchor guiAnchor = entry.getValue();
            float x = guiAnchor.anchorPoint.x * width - guiAnchor.anchorPoint.x * child.getWidth() + guiAnchor.offset.x/scaledResolution.getScaleFactor();
            float y = guiAnchor.anchorPoint.y * height - guiAnchor.anchorPoint.y * child.getHeight() + guiAnchor.offset.y/scaledResolution.getScaleFactor();

            if(guiAnchor.inventoryRelative) {
                x = width*0.5f + guiAnchor.offset.x/scaledResolution.getScaleFactor();
                y = height*0.5f + guiAnchor.offset.y/scaledResolution.getScaleFactor();
            }

            if(childrenPosition.get(child) != null) {
                childrenPosition.get(child).x = x;
                childrenPosition.get(child).y = y;
            } else {
                childrenPosition.put(child, new Vector2f(x, y));
            }
        }
    }

    @Override
    public Map<GuiElement, Vector2f> getChildrenPosition() {
        GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;

        /*if(currentScreen instanceof GuiContainer || currentScreen instanceof GuiItemRecipe
            || currentScreen instanceof CustomAHGui || NotEnoughUpdates.INSTANCE.manager.auctionManager.customAH.isRenderOverAuctionView()) {

            if(lastScreen != currentScreen) {
                lastScreen = currentScreen;

                ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
                int screenWidth = scaledResolution.getScaledWidth();
                int screenHeight = scaledResolution.getScaledHeight();

                int xSize = -1;
                int ySize = -1;
                int guiLeft = -1;
                int guiTop = -1;

                if(currentScreen instanceof GuiContainer) {
                    GuiContainer currentContainer = (GuiContainer) currentScreen;

                    try {
                        xSize = (int) Utils.getField(GuiContainer.class, currentContainer, "xSize", "field_146999_f");
                        ySize = (int) Utils.getField(GuiContainer.class, currentContainer, "ySize", "field_147000_g");
                        guiLeft = (int) Utils.getField(GuiContainer.class, currentContainer, "guiLeft", "field_147003_i");
                        guiTop = (int) Utils.getField(GuiContainer.class, currentContainer, "guiTop", "field_147009_r");
                    } catch(Exception ignored) {
                    }
                } else if(currentScreen instanceof GuiItemRecipe) {
                    xSize = ((GuiItemRecipe)currentScreen).xSize;
                    ySize = ((GuiItemRecipe)currentScreen).ySize;
                    guiLeft = ((GuiItemRecipe)currentScreen).guiLeft;
                    guiTop = ((GuiItemRecipe)currentScreen).guiTop;
                } else if(currentScreen instanceof CustomAHGui ||
                        NotEnoughUpdates.INSTANCE.manager.auctionManager.customAH.isRenderOverAuctionView()) {
                    xSize = NotEnoughUpdates.INSTANCE.manager.auctionManager.customAH.getXSize();
                    ySize = NotEnoughUpdates.INSTANCE.manager.auctionManager.customAH.getYSize();
                    guiLeft = NotEnoughUpdates.INSTANCE.manager.auctionManager.customAH.guiLeft;
                    guiTop = NotEnoughUpdates.INSTANCE.manager.auctionManager.customAH.guiTop;
                }

                if(xSize <= 0 && ySize <= 0 && guiLeft <= 0 && guiTop <= 0) {
                    lastScreen = null;
                    return Collections.unmodifiableMap(childrenPosition);
                }

                for(Map.Entry<MBGuiElement, MBAnchorPoint> entry : children.entrySet()) {
                    MBGuiElement child = entry.getKey();
                    MBAnchorPoint anchorPoint = entry.getValue();

                    Vector2f childPos;
                    if(childrenPosition.containsKey(child)) {
                        childPos = new Vector2f(childrenPosition.get(child));
                    } else {
                        childPos = new Vector2f();
                    }

                    if(anchorPoint.inventoryRelative) {
                        int defGuiLeft = (screenWidth - xSize) / 2;
                        int defGuiTop = (screenHeight - ySize) / 2;

                        childPos.x += guiLeft-defGuiLeft + (0.5f-anchorPoint.anchorPoint.x)*xSize;
                        childPos.y += guiTop-defGuiTop + (0.5f-anchorPoint.anchorPoint.y)*ySize;
                    }

                    childrenPositionOffset.put(child, childPos);
                }
            }
            return Collections.unmodifiableMap(childrenPositionOffset);
        } else {*/
            return super.getChildrenPosition();
        //}
    }
}
