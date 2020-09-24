package io.github.moulberry.morus;

import io.github.moulberry.morus.commands.SimpleCommand;
import io.github.moulberry.morus.gui.GUIEditorFloating;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.LinkedList;

@Mod(modid = Morus.MODID, version = Morus.VERSION, clientSideOnly = true)
public class Morus {
    public static final String MODID = "morus";
    public static final String VERSION = "1.0";

    private static Morus instance;

    public GuiScreen openGui = null;
    private MorusManager manager;

    SimpleCommand morusCommand = new SimpleCommand("morus", new SimpleCommand.ProcessCommandRunnable() {
        public void processCommand(ICommandSender sender, String[] args) {
            openGui = new GUIEditorFloating("main", manager.overlayMap.get("main"), new LinkedList<>(), manager.overlayMap.get("main"));
        }
    });

    public static Morus getInstance() {
        return instance;
    }

    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event) {
        this.instance = this;
        this.manager = new MorusManager(this);

        ClientCommandHandler.instance.registerCommand(morusCommand);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new MorusEventListener(this));
        MinecraftForge.EVENT_BUS.register(new MorusSubstitutorListener());
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if(Minecraft.getMinecraft().currentScreen == null &&
            openGui != null) {
            Minecraft.getMinecraft().displayGuiScreen(openGui);
            openGui = null;
        }
    }

    public MorusManager getManager() {
        return manager;
    }
}
