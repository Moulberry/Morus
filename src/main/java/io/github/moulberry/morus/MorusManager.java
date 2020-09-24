package io.github.moulberry.morus;

import io.github.moulberry.morus.gui.GUIEditor;
import io.github.moulberry.morus.gui.GuiAnchor;
import io.github.moulberry.morus.gui.elements.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.util.vector.Vector2f;

import java.util.*;
import java.util.List;

public class MorusManager {

    private final Morus morus;
    private final Map<OverlayType, LinkedHashSet<String>> overlayTypeMap = new HashMap<>();
    public final Map<String, GuiGroupScreen> overlayMap = new HashMap<>();

    public MorusManager(Morus morus) {
        this.morus = morus;

        LinkedHashMap<GuiElement, GuiAnchor> map = new LinkedHashMap<>();
        map.put(new GuiText("{AAAAA}"), new GuiAnchor(GuiAnchor.AnchorPoint.BOTMID, new Vector2f()));

        LinkedHashMap<GuiElement, GuiAnchor> mapFloating = new LinkedHashMap<>();
        mapFloating.put(new GuiText("BBBBBBBBBBBBBB"), new GuiAnchor(GuiAnchor.AnchorPoint.MIDLEFT, new Vector2f()));
        GuiGroupFloating floating = new GuiGroupFloating(100, 20, mapFloating);

        map.put(floating, new GuiAnchor(GuiAnchor.AnchorPoint.BOTMID, new Vector2f()));

        List<GuiElement> children = new ArrayList<>();
        children.add(new GuiText("1111111111"));
        children.add(new GuiText("2222222222"));
        children.add(new GuiText("333{AAAAA}33"));
        children.add(new GuiText("4444444444"));
        children.add(new GuiText("55{AAAAA}555"));
        GuiGroupAligned aligned = new GuiGroupAligned(children, true, 3);

        map.put(aligned, new GuiAnchor(GuiAnchor.AnchorPoint.BOTMID, new Vector2f()));

        overlayTypeMap.computeIfAbsent(OverlayType.GAME_LAST, k->new LinkedHashSet<>()).add("main");
        overlayMap.put("main", new GuiGroupScreen(map));
    }

    public void renderOverlay(OverlayType overlayType) {
        if(Minecraft.getMinecraft().theWorld == null || Minecraft.getMinecraft().currentScreen instanceof GUIEditor<?>) return;

        LinkedHashSet<String> list = overlayTypeMap.get(overlayType);
        if(list != null) {
            for(String str : list) {
                if(overlayMap.containsKey(str)) {
                    GuiGroupScreen screen = overlayMap.get(str);
                    if(screen != null) {
                        screen.recalculate();

                        GlStateManager.color(1, 1, 1, 1);
                        screen.render(0, 0);

                        return;
                    }
                }
            }
        }
    }

}
