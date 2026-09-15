package cz.honzasik.hontun;

import cz.honzasik.hontun.commands.CommandExample;
import cz.honzasik.hontun.commands.CenterCommand;
import cz.honzasik.hontun.commands.CwProbe;
import cz.honzasik.hontun.commands.ForEach;
import cz.honzasik.hontun.commands.PluginScanner;
import cz.honzasik.hontun.commands.Reconnect;
import cz.honzasik.hontun.modules.AntiExploit;
import cz.honzasik.hontun.modules.ChannelFetcher;
import cz.honzasik.hontun.modules.ChannelSender;
import cz.honzasik.hontun.modules.FreeInteract;
import cz.honzasik.hontun.modules.GamemodeNotify;
import cz.honzasik.hontun.modules.NoWorldBorder;
import cz.honzasik.hontun.modules.WorldGuardBypass;
import cz.honzasik.hontun.gui.api.render.RoundedRect;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.render.HontunRenderer;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;

public class Hontun extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("Hontun");

    public static HontunGuiTheme THEME;

    @Override
    public void onInitialize() {
        addCommands();
        addModules();

        MeteorClient.EVENT_BUS.subscribe(new ChannelFetcher.Cleaner());

        MeteorClient.EVENT_BUS.subscribe(PluginScanner.INSTANCE);

        THEME = new HontunGuiTheme();
        GuiThemes.add(THEME);
        selectThemeByDefault();
        RoundedRect.get().registerRenderer(HontunRenderer.get());
    }

    private static void selectThemeByDefault() {
        try {
            GuiTheme active = GuiThemes.get();
            if (active == null || "Meteor".equals(active.name)) {
                GuiThemes.select(THEME.name);
                GuiThemes.save();
                LOG.info("[Hontun] Hontun GUI theme selected automatically (was Meteor default).");
            }
        } catch (Throwable t) {
            LOG.warn("[Hontun] Could not select the Hontun GUI theme automatically: {}", t.toString());
        }
    }

    private void addCommands() {
        Commands.add(new CommandExample());
        Commands.add(new ForEach());
        Commands.add(new Reconnect());
        Commands.add(new CwProbe());
        Commands.add(new CenterCommand());
    }

    private void addModules() {
        Modules modules = Modules.get();

        modules.add(new ChannelSender());
        modules.add(new ChannelFetcher());
        modules.add(new AntiExploit());
        modules.add(new FreeInteract());
        modules.add(new GamemodeNotify());
        modules.add(new NoWorldBorder());
        modules.add(new WorldGuardBypass());
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "cz.honzasik.hontun";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("HonzasikCZ", "Hontun");
    }
}
