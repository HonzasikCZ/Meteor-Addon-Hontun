package cz.honzasik.hontun.gui.theme;

import cz.honzasik.hontun.gui.HontunGui;
import cz.honzasik.hontun.gui.api.animation.Easing;
import cz.honzasik.hontun.gui.api.text.RichText;
import cz.honzasik.hontun.gui.api.text.RichTextSegment;
import cz.honzasik.hontun.gui.screen.HontunModuleScreen;
import cz.honzasik.hontun.gui.screen.HontunModulesScreen;
import cz.honzasik.hontun.gui.theme.colors.HontunColor;
import cz.honzasik.hontun.gui.theme.style.ClickStyle;
import cz.honzasik.hontun.gui.theme.style.ClickStyles;
import cz.honzasik.hontun.gui.theme.style.FontChoice;
import cz.honzasik.hontun.gui.theme.style.Knob;
import cz.honzasik.hontun.gui.theme.style.Pipeline;
import cz.honzasik.hontun.gui.theme.style.StylePalette;
import cz.honzasik.hontun.gui.theme.style.TabGlide;
import cz.honzasik.hontun.gui.theme.style.terminal.TerminalMarker;
import cz.honzasik.hontun.gui.theme.style.terminal.TerminalNames;
import cz.honzasik.hontun.gui.render.MatrixRain;
import cz.honzasik.hontun.gui.render.route.PrimitiveRouter;
import cz.honzasik.hontun.gui.render.route.Routers;
import cz.honzasik.hontun.gui.theme.widgets.*;
import cz.honzasik.hontun.gui.theme.widgets.container.WHontunSection;
import cz.honzasik.hontun.gui.theme.widgets.container.WHontunView;
import cz.honzasik.hontun.gui.mixin.meteorclient.WindowScreenAccessor;
import cz.honzasik.hontun.gui.theme.widgets.container.WHontunWindow;
import cz.honzasik.hontun.gui.theme.widgets.input.*;
import cz.honzasik.hontun.gui.theme.widgets.pressable.*;
import cz.honzasik.hontun.gui.theme.widgets.settings.WHontunDoubleEdit;
import cz.honzasik.hontun.gui.theme.widgets.settings.WHontunIntEdit;
import cz.honzasik.hontun.gui.theme.widgets.settings.WHontunKeybind;
import cz.honzasik.hontun.gui.widget.WGuiTexture;
import cz.honzasik.hontun.gui.widget.container.WTreeTable;
import cz.honzasik.hontun.gui.widget.input.WMultiSelect;
import cz.honzasik.hontun.gui.widget.input.WSearch;
import cz.honzasik.hontun.gui.widget.pressable.WColorPicker;
import cz.honzasik.hontun.gui.render.HontunRenderer;
import cz.honzasik.hontun.gui.render.text.GlyphSupport;
import cz.honzasik.hontun.gui.render.text.RichTextRenderer;
import cz.honzasik.hontun.gui.transition.TransitionStyle;
import cz.honzasik.hontun.gui.util.ColorUtils;
import cz.honzasik.hontun.utils.HontunGeo;
import cz.honzasik.hontun.utils.HontunTheme;
import meteordevelopment.meteorclient.renderer.Fonts;
import meteordevelopment.meteorclient.renderer.text.FontFace;
import meteordevelopment.meteorclient.renderer.text.FontFamily;
import meteordevelopment.meteorclient.renderer.text.FontInfo;
import meteordevelopment.meteorclient.renderer.text.SystemFontFace;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.utils.AlignmentX;
import meteordevelopment.meteorclient.gui.utils.CharFilter;
import meteordevelopment.meteorclient.gui.utils.WindowConfig;
import meteordevelopment.meteorclient.gui.widgets.*;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.containers.WView;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import meteordevelopment.meteorclient.gui.widgets.input.WDropdown;
import meteordevelopment.meteorclient.gui.widgets.input.WSlider;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.*;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.renderer.text.VanillaTextRenderer;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.accounts.Account;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static meteordevelopment.meteorclient.MeteorClient.mc;
import net.minecraft.util.Util;

public class HontunGuiTheme extends GuiTheme {
    private final Map<HontunColor, Color> colorCache;
    private final StylePalette palette = new StylePalette();

    private volatile HontunTheme.UiMode activeUiMode = HontunTheme.UiMode.Modern2;
    private volatile HontunTheme.UiMode activeClickGui = HontunTheme.UiMode.Modern2;

    private RichTextRenderer textRenderer;
    private RichTextRenderer smogTextRenderer;
    private RichTextRenderer monoTextRenderer;

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgTerminal = settings.createGroup("Terminal");
    private final SettingGroup sgRain = settings.createGroup("Rain");
    private final SettingGroup sgCorners = settings.createGroup("Corners");
    private final SettingGroup sgAnimations = settings.createGroup("Animations");
    private final SettingGroup sgColors = settings.createGroup("Colors");
    private final SettingGroup sgSnapping = settings.createGroup("Snapping");
    private final SettingGroup sgScreens = settings.createGroup("Screens");
    private final SettingGroup sgStarscript = settings.createGroup("Starscript");

    public final Setting<HontunTheme.UiMode> uiMode = sgGeneral.add(new EnumSetting.Builder<HontunTheme.UiMode>()
            .name("ui-mode")
            .description("Look of the whole addon (ClickGUI + MC menus/buttons/hotbar/containers). Vanilla = no restyle. HVanilla = classic Minecraft shapes. Modern1 = flat and rounded. Modern2 = chamfered corners with an accent glow (default). SmogClient = rounded black and white SmogClientPro look with the SF font. Terminal = square black console look with a forced monospace font; palette-color still works and tints the text, borders and caret. Also sets click-gui to the matching style.")
            .defaultValue(HontunTheme.UiMode.Modern2)
            .onChanged(this::onUiModeChanged)
            .build()
    );

    public final Setting<HontunTheme.UiMode> clickGui = sgGeneral.add(new EnumSetting.Builder<HontunTheme.UiMode>()
            .name("click-gui")
            .description("Look of the Meteor ClickGUI only. Changing ui-mode switches this to the matching style; you can pick any other style afterwards. Vanilla and HVanilla always use the Minecraft font, SmogClient always uses the SF font, Terminal always uses its monospace font. Vanilla and HVanilla snap scale to half steps and ignore palette-color (Vanilla) or custom font (Vanilla, HVanilla). SmogClient maps BACK easings to QUART_OUT. Low window-opacity triggers readability floors.")
            .defaultValue(HontunTheme.UiMode.Modern2)
            .onChanged(this::onClickGuiChanged)
            .build()
    );

    public final Setting<Boolean> lightMode = sgGeneral.add(new BoolSetting.Builder()
            .name("light-mode")
            .description("Light palette for the Meteor ClickGUI (modules, settings, search, tabs). Minecraft menus, chat, containers and the HUD keep their dark look. Window and background opacity are raised so dark text stays readable over the game.")
            .defaultValue(false)
            .onChanged(v -> refreshPalette())
            .visible(this::lightModeVisible)
            .build()
    );

    public final Setting<Boolean> serverFlags = sgGeneral.add(new BoolSetting.Builder()
            .name("server-country-flags")
            .description("Shows the hosting country's flag next to servers in the multiplayer list. Looks the country up online, which sends the server address to a third-party service (ipwho.is).")
            .defaultValue(true)
            .onChanged(HontunGeo::setEnabled)
            .build()
    );

    public final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
            .name("scale")
            .description("Scale of the GUI.")
            .defaultValue(1)
            .min(0.75)
            .sliderRange(0.75, 4)
            .onSliderRelease()
            .onChanged(this::onScaleChanged)
            .build()
    );

    public final Setting<AlignmentX> moduleAlignment = sgGeneral.add(new EnumSetting.Builder<AlignmentX>()
            .name("module-alignment")
            .description("How module titles are aligned.")
            .defaultValue(AlignmentX.Center)
            .build()
    );

    public final Setting<Boolean> categoryIcons = sgGeneral.add(new BoolSetting.Builder()
            .name("category-icons")
            .description("Displays icons next to module categories.")
            .defaultValue(true)
            .build()
    );

    public final Setting<Boolean> tabIcons = sgGeneral.add(new BoolSetting.Builder()
            .name("tab-icons")
            .description("Displays icons next to tabs in the top bar.")
            .defaultValue(true)
            .onChanged(this::invalidateScreen)
            .visible(this::tabIconsVisible)
            .build()
    );

    public final Setting<Boolean> hideHUD = sgGeneral.add(new BoolSetting.Builder()
            .name("hide-HUD")
            .description("Hide HUD when in GUI.")
            .defaultValue(false)
            .onChanged(v -> {
                if (mc.gui.screen() instanceof WidgetScreen) mc.gameRenderer.gameRenderState().guiRenderState.isHudHidden = v;
            })
            .build()
    );

    public final Setting<Boolean> windowShadow = sgGeneral.add(new BoolSetting.Builder()
            .name("window-shadow")
            .description("Render a subtle shadow under windows.")
            .defaultValue(true)
            .visible(this::windowShadowVisible)
            .build()
    );

    public final Setting<Boolean> indentSettings = sgGeneral.add(new BoolSetting.Builder()
            .name("indent-settings")
            .description("Indents setting that have conditional visibility like in a Tree View.")
            .defaultValue(true)
            .build()
    );

    public final Setting<Boolean> modulesHelpText = sgGeneral.add(new BoolSetting.Builder()
            .name("modules-help-text")
            .description("Shows help text in the modules screen.")
            .defaultValue(true)
            .build()
    );

    public final Setting<TerminalMarker> terminalMarker = sgTerminal.add(new EnumSetting.Builder<TerminalMarker>()
            .name("marker")
            .description("How enabled modules are marked in click-gui Terminal: Block = a solid palette-color block in front of the name, Brackets = [x].")
            .defaultValue(TerminalMarker.Block)
            .onChanged(this::invalidateScreen)
            .visible(this::terminalVisible)
            .build()
    );

    public final Setting<TerminalNames> terminalNames = sgTerminal.add(new EnumSetting.Builder<TerminalNames>()
            .name("module-names")
            .description("How click-gui Terminal prints modules: Id = command ids like kill-aura, Title = titles like Kill Aura.")
            .defaultValue(TerminalNames.Id)
            .onChanged(v -> scheduleRebuild())
            .visible(this::terminalVisible)
            .build()
    );

    public final Setting<String> terminalPrompt = sgTerminal.add(new StringSetting.Builder()
            .name("prompt")
            .description("Prompt printed before commands in click-gui Terminal, up to 3 characters.")
            .defaultValue("$")
            .onChanged(this::invalidateScreen)
            .visible(this::terminalVisible)
            .build()
    );

    public final Setting<Boolean> terminalCursorBlink = sgTerminal.add(new BoolSetting.Builder()
            .name("cursor-blink")
            .description("Blinking block cursor in click-gui Terminal.")
            .defaultValue(true)
            .visible(this::terminalVisible)
            .build()
    );

    public final Setting<Boolean> terminalLineNumbers = sgTerminal.add(new BoolSetting.Builder()
            .name("line-numbers")
            .description("Line numbers in the click-gui Terminal config views.")
            .defaultValue(true)
            .visible(this::terminalVisible)
            .build()
    );

    public final Setting<Boolean> terminalPrintAnimation = sgTerminal.add(new BoolSetting.Builder()
            .name("print-animation")
            .description("Lines print in one after another when a click-gui Terminal screen opens.")
            .defaultValue(true)
            .visible(this::terminalVisible)
            .build()
    );

    public final Setting<Boolean> terminalScanlines = sgTerminal.add(new BoolSetting.Builder()
            .name("scanlines")
            .description("Faint CRT scanlines over the whole screen, only while no world is loaded: over the menus with ui-mode Terminal and over the ClickGUI with click-gui Terminal. Never drawn over a world.")
            .defaultValue(false)
            .onChanged(MatrixRain::setScanlines)
            .visible(this::terminalAnyVisible)
            .build()
    );

    public final Setting<Boolean> rain = sgRain.add(new BoolSetting.Builder()
            .name("rain")
            .description("Digital rain of hex digits and terminal symbols behind the click-gui Terminal ClickGUI and, with ui-mode Terminal, behind the menus shown without a world. It replaces menu-scene there; turn it off to get menu-scene back. Tinted by palette-color.")
            .defaultValue(true)
            .onChanged(MatrixRain::setEnabled)
            .visible(this::terminalAnyVisible)
            .build()
    );

    public final Setting<MatrixRain.Charset> rainCharset = sgRain.add(new EnumSetting.Builder<MatrixRain.Charset>()
            .name("charset")
            .description("Glyphs that fall, all from the bundled monospace font. Hex = 0-9 A-F and a few terminal symbols, Ascii = letters, digits and symbols, Binary = 0 and 1.")
            .defaultValue(MatrixRain.Charset.Hex)
            .onChanged(MatrixRain::setCharset)
            .visible(this::rainTuningVisible)
            .build()
    );

    public final Setting<Boolean> rainMirror = sgRain.add(new BoolSetting.Builder()
            .name("mirror-glyphs")
            .description("Mirror half of the glyphs for a less readable, more alien look.")
            .defaultValue(true)
            .onChanged(MatrixRain::setMirror)
            .visible(this::rainTuningVisible)
            .build()
    );

    public final Setting<Double> rainSpeed = sgRain.add(new DoubleSetting.Builder()
            .name("speed")
            .description("Fall speed of the streams.")
            .defaultValue(1)
            .range(0.2, 3)
            .sliderRange(0.2, 3)
            .decimalPlaces(2)
            .onChanged(MatrixRain::setSpeed)
            .visible(this::rainTuningVisible)
            .build()
    );

    public final Setting<Double> rainDensity = sgRain.add(new DoubleSetting.Builder()
            .name("density")
            .description("Share of columns that carry a stream.")
            .defaultValue(0.7)
            .range(0.1, 1)
            .sliderRange(0.1, 1)
            .decimalPlaces(2)
            .onChanged(MatrixRain::setDensity)
            .visible(this::rainTuningVisible)
            .build()
    );

    public final Setting<Double> rainBrightness = sgRain.add(new DoubleSetting.Builder()
            .name("brightness")
            .description("Brightness of the trails.")
            .defaultValue(0.7)
            .range(0.1, 1)
            .sliderRange(0.1, 1)
            .decimalPlaces(2)
            .onChanged(MatrixRain::setBrightness)
            .visible(this::rainTuningVisible)
            .build()
    );

    public final Setting<Integer> cornerRadius = sgCorners.add(new IntSetting.Builder()
            .name("corner-radius")
            .description("The radius of corners for large UI elements.")
            .defaultValue(10)
            .sliderRange(1, 25)
            .visible(this::cornerRadiusVisible)
            .build()
    );

    public final Setting<Integer> smallCornerRadius = sgCorners.add(new IntSetting.Builder()
            .name("small-corner-radius")
            .description("The radius of corners for small UI elements.")
            .defaultValue(6)
            .sliderRange(1, 25)
            .visible(this::cornerRadiusVisible)
            .build()
    );

    public final Setting<Easing> guiAnimation = sgAnimations.add(new EnumSetting.Builder<Easing>()
            .name("gui-animation-easing")
            .description("The easing function used for UI animations.")
            .defaultValue(Easing.QUART_OUT)
            .visible(this::animationVisible)
            .build()
    );

    public final Setting<Integer> guiAnimationDuration = sgAnimations.add(new IntSetting.Builder()
            .name("gui-animation-duration")
            .description("Duration of the animation in milliseconds.")
            .defaultValue(300)
            .sliderRange(1, 1000)
            .visible(this::animationVisible)
            .build()
    );

    public final Setting<SettingColor> paletteColor = sgColors.add(new ColorSetting.Builder()
            .name("palette-color")
            .description("Accent color for the whole addon (ClickGUI, chat, menu, buttons, hotbar). Pick one color; the gradient is derived automatically. Backgrounds stay neutral grey.")
            .defaultValue(new SettingColor(62, 140, 255))
            .onChanged(c -> HontunTheme.setAccent(c.getPacked()))
            .visible(this::paletteColorVisible)
            .build()
    );

    public final Setting<SettingColor> containerBackground = sgColors.add(new ColorSetting.Builder()
            .name("container-background")
            .description("Color of the backdrop behind inventory, chests and other containers. Alpha controls transparency (lower it if you want the world to show through).")
            .defaultValue(new SettingColor(16, 17, 21, 200))
            .onChanged(c -> HontunTheme.setBgOverlay(c.getPacked()))
            .build()
    );

    public final Setting<SettingColor> menuBackground = sgColors.add(new ColorSetting.Builder()
            .name("menu-background")
            .description("Base color of the menu-scene backdrop (title/pause/loading): the BlockyParticles gradient, the RoundedParticles glow and the base of BlockyHorizon. Default dark grey; set it to your accent to make the menu breathe that color. Alpha is ignored. ui-mode SmogClient ignores it and keeps its fixed black and white backdrop in every scene.")
            .defaultValue(new SettingColor(20, 21, 25))
            .onChanged(c -> HontunTheme.setMenuColor(c.getPacked()))
            .build()
    );

    public final Setting<HontunTheme.MenuScene> menuScene = sgColors.add(new EnumSetting.Builder<HontunTheme.MenuScene>()
            .name("menu-scene")
            .description("Scene behind the title screen and every menu shown without a world (also the ClickGUI opened from the title screen and the Hontun screens). BlockyParticles = animated menu-background gradient with square palette-color particles (default). RoundedParticles = soft round palette-color particles joined by faint lines over a menu-background glow, the SmogClient backdrop in your colors. BlockyHorizon = shader-drawn blocky night landscape with a moon, stars, clouds and fireflies, tinted by palette-color on a menu-background base. Matrix = falling digital rain of hex digits and terminal symbols on black, tinted by palette-color. ui-mode SmogClient keeps its black and white colors in every scene and draws RoundedParticles for both particle scenes. ui-mode Terminal (and the click-gui Terminal ClickGUI) draws Matrix while its rain setting is on. Menus over a world keep a translucent particle backdrop (round with RoundedParticles, square otherwise) and ui-mode Vanilla keeps the panorama. Falls back to the particle backdrop if the shader cannot run.")
            .defaultValue(HontunTheme.MenuScene.BlockyParticles)
            .onChanged(this::onMenuSceneChanged)
            .build()
    );

    public final Setting<Boolean> customCursor = sgScreens.add(new BoolSetting.Builder()
            .name("custom-cursor")
            .description("Replaces the mouse cursor with one drawn in the current ui-mode style and palette-color, including the hand over buttons, the text cursor and the resize cursors. ui-mode Vanilla keeps the system cursor.")
            .defaultValue(true)
            .build()
    );

    public final Setting<Boolean> customCrosshair = sgScreens.add(new BoolSetting.Builder()
            .name("custom-crosshair")
            .description("Draws the in-game crosshair in the current ui-mode style and palette-color. The attack indicator stays vanilla. ui-mode Vanilla keeps the vanilla crosshair.")
            .defaultValue(true)
            .build()
    );

    public final Setting<TransitionStyle> screenTransition = sgScreens.add(new EnumSetting.Builder<TransitionStyle>()
            .name("screen-transition")
            .description("How screens change: the previous screen fades out over the new one instead of switching at once. Covers the title screen and the other menus, the Hontun screens and the Meteor ClickGUI (opening it, closing it back to the game, every tab and module screen). Fade = plain crossfade. FadeZoom = the old screen also grows slightly while it fades (default); over a world, between ClickGUI tabs and when a menu reopens itself it uses the plain crossfade. On ClickGUI tab switches the top bar stays live and only the content fades. With ui-mode Terminal and no world loaded, FadeZoom slides the old screen 8 px down while it fades instead of growing, ClickGUI tab switches included; the Terminal top bar and key bar stay in place. Off = instant. Chat, inventories and containers, sign, book and command block editing, the death screen and the credits always switch instantly, and so does the first title screen after the game starts. ui-mode Vanilla keeps instant switching.")
            .defaultValue(TransitionStyle.FadeZoom)
            .build()
    );

    public final Setting<Integer> screenTransitionDuration = sgScreens.add(new IntSetting.Builder()
            .name("screen-transition-duration")
            .description("Length of the screen transition in milliseconds.")
            .defaultValue(220)
            .range(80, 600)
            .sliderRange(80, 600)
            .visible(() -> screenTransition.get() != TransitionStyle.Off)
            .build()
    );

    public final Setting<Double> windowOpacity = sgColors.add(new DoubleSetting.Builder()
            .name("window-opacity")
            .description("Controls the opacity of the windows.")
            .defaultValue(1)
            .sliderRange(0, 1)
            .decimalPlaces(2)
            .onChanged(v -> updateCache())
            .build()
    );

    public final Setting<Double> backgroundOpacity = sgColors.add(new DoubleSetting.Builder()
            .name("background-opacity")
            .description("Controls the opacity of the backgrounds of UI elements.")
            .defaultValue(1)
            .sliderRange(0, 1)
            .decimalPlaces(2)
            .build()
    );

    public final Setting<Boolean> snapModuleCategories = sgSnapping.add(new BoolSetting.Builder()
            .name("snap-module-categories")
            .description("Snaps category windows to the grid.")
            .defaultValue(true)
            .build()
    );

    public final Setting<Integer> snappingGridSize = sgSnapping.add(new IntSetting.Builder()
            .name("grid-size")
            .description("The size of the snapping grid.")
            .defaultValue(10)
            .sliderRange(5, 50)
            .visible(snapModuleCategories::get)
            .build()
    );

    public final Setting<Boolean> hontunSearchScreen = sgScreens.add(new BoolSetting.Builder()
            .name("search-screen")
            .description("Replaces Meteor's search window with Hontun's search screen.")
            .defaultValue(true)
            .build()
    );

    public final Setting<Boolean> hontunEntityTypeListScreen = sgScreens.add(new BoolSetting.Builder()
            .name("entity-type-list-screen")
            .description("Replaces Meteor's entity selection screen with the Hontun version.")
            .defaultValue(true)
            .build()
    );

    public final ThreeStateColor backgroundColor = new ThreeStateColor(
            this::surface0Color,
            this::surface1Color,
            this::surface2Color
    );

    public final ThreeStateColor outlineColor = new ThreeStateColor(
            this::overlay0Color,
            this::overlay1Color,
            this::overlay2Color
    );

    public final ThreeStateColor scrollbarColor = new ThreeStateColor(
            this::surface0Color,
            this::surface1Color,
            this::surface2Color
    );

    private final Setting<SettingColor> starscriptText = color(sgStarscript, "starscript-text", "Color of text in Starscript code.", new SettingColor(169, 183, 198));
    private final Setting<SettingColor> starscriptBraces = color(sgStarscript, "starscript-braces", "Color of braces in Starscript code.", new SettingColor(150, 150, 150));
    private final Setting<SettingColor> starscriptParenthesis = color(sgStarscript, "starscript-parenthesis", "Color of parenthesis in Starscript code.", new SettingColor(169, 183, 198));
    private final Setting<SettingColor> starscriptDots = color(sgStarscript, "starscript-dots", "Color of dots in starscript code.", new SettingColor(169, 183, 198));
    private final Setting<SettingColor> starscriptCommas = color(sgStarscript, "starscript-commas", "Color of commas in starscript code.", new SettingColor(169, 183, 198));
    private final Setting<SettingColor> starscriptOperators = color(sgStarscript, "starscript-operators", "Color of operators in Starscript code.", new SettingColor(169, 183, 198));
    private final Setting<SettingColor> starscriptStrings = color(sgStarscript, "starscript-strings", "Color of strings in Starscript code.", new SettingColor(106, 135, 89));
    private final Setting<SettingColor> starscriptNumbers = color(sgStarscript, "starscript-numbers", "Color of numbers in Starscript code.", new SettingColor(104, 141, 187));
    private final Setting<SettingColor> starscriptKeywords = color(sgStarscript, "starscript-keywords", "Color of keywords in Starscript code.", new SettingColor(204, 120, 50));
    private final Setting<SettingColor> starscriptAccessedObjects = color(sgStarscript, "starscript-accessed-objects", "Color of accessed objects (before a dot) in Starscript code.", new SettingColor(152, 118, 170));

    public HontunGuiTheme() {
        super("Hontun");

        settingsFactory = new HontunSettingsWidgetFactory(this);
        colorCache = new EnumMap<>(HontunColor.class);

        activeUiMode = uiMode.get();
        activeClickGui = clickGui.get();
        HontunTheme.setMode(uiMode.get());
        HontunTheme.setAccent(paletteColor.get().getPacked());
        HontunTheme.setBgOverlay(containerBackground.get().getPacked());
        HontunTheme.setMenuColor(menuBackground.get().getPacked());
        HontunTheme.setMenuScene(menuScene.get());
        HontunGeo.setEnabled(serverFlags.get());
        applyRain();
        updateCache();

        HontunTheme.addListener(this::updateCache);
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = super.toTag();
        tag.putString("click-gui", activeClickGui.name());
        return tag;
    }

    private static final Map<String, String> LEGACY_VALUES = Map.of(
            "HModern1", HontunTheme.UiMode.Modern1.name(),
            "HModern2", HontunTheme.UiMode.Modern2.name(),
            "Particles", HontunTheme.MenuScene.BlockyParticles.name());

    private static final Set<String> LEGACY_SETTINGS = Set.of("ui-mode", "click-gui", "menu-scene");

    private static void migrateLegacy(CompoundTag tag) {
        tag.getString("click-gui").map(LEGACY_VALUES::get).ifPresent(value -> tag.putString("click-gui", value));
        tag.getCompound("settings").ifPresent(saved -> {
            for (Tag group : saved.getListOrEmpty("groups")) {
                if (!(group instanceof CompoundTag groupTag)) continue;
                for (Tag setting : groupTag.getListOrEmpty("settings")) {
                    if (!(setting instanceof CompoundTag settingTag)) continue;
                    if (!LEGACY_SETTINGS.contains(settingTag.getStringOr("name", ""))) continue;
                    String value = LEGACY_VALUES.get(settingTag.getStringOr("value", ""));
                    if (value != null) settingTag.putString("value", value);
                }
            }
        });
    }

    @Override
    public GuiTheme fromTag(CompoundTag tag) {
        migrateLegacy(tag);
        super.fromTag(tag);
        tag.getString("click-gui").ifPresent(value -> {
            for (HontunTheme.UiMode mode : HontunTheme.UiMode.values()) {
                if (mode.name().equals(value)) clickGui.set(mode);
            }
        });
        applyRain();
        return this;
    }

    private void applyRain() {
        MatrixRain.setEnabled(rain.get());
        MatrixRain.setCharset(rainCharset.get());
        MatrixRain.setMirror(rainMirror.get());
        MatrixRain.setSpeed(rainSpeed.get());
        MatrixRain.setDensity(rainDensity.get());
        MatrixRain.setBrightness(rainBrightness.get());
        MatrixRain.setScanlines(terminalScanlines.get());
    }

    private Setting<SettingColor> color(SettingGroup group, String name, String description, SettingColor color) {
        return group.add(new ColorSetting.Builder()
                .name(name + "-color")
                .description(description)
                .defaultValue(color)
                .build());
    }

    @Override
    public WWindow window(WWidget icon, String title) {
        return w(new WHontunWindow(icon, title));
    }

    public WLabel label(RichText text, double maxWidth) {
        if (maxWidth == 0) return w(new WHontunLabel(text));
        return w(new WHontunMultiLabel(text, maxWidth));
    }

    public WLabel label(RichText text) {
        return label(text, 0);
    }

    public WLabel swatchLabel(RichText text) {
        return w(new WHontunSwatchLabel(text));
    }

    @Override
    public WLabel label(String text, boolean title, double maxWidth) {
        if (maxWidth == 0) {
            WHontunLabel label = w(new WHontunLabel(RichText.of(text).boldIf(title)));
            label.titleRole = title;
            return label;
        }
        return w(new WHontunMultiLabel(RichText.of(text).boldIf(title), maxWidth));
    }

    @Override
    public WHorizontalSeparator horizontalSeparator(String text) {
        return w(new WHontunHorizontalSeparator(text));
    }

    @Override
    public WVerticalSeparator verticalSeparator() {
        return w(new WHontunVerticalSeparator());
    }

    public WHontunButton button(RichText text, GuiTexture texture) {
        return w(new WHontunButton(text, texture));
    }

    public WHontunButton button(RichText text) {
        return button(text, null);
    }

    @Override
    public WButton button(String text, GuiTexture texture) {
        return button(RichText.of(text), texture);
    }

    @Override
    public WButton button(GuiTexture texture) {
        return w(new WHontunButton(texture));
    }

    @Override
    protected WConfirmedButton confirmedButton(String text, String confirmText, GuiTexture texture) {
        return w(new WHontunConfirmedButton(text, confirmText, texture));
    }

    @Override
    public WMinus minus() {
        return w(new WHontunMinus());
    }

    @Override
    public WConfirmedMinus confirmedMinus() {
        return w(new WHontunConfirmedMinus());
    }

    @Override
    public WPlus plus() {
        return w(new WHontunPlus());
    }

    @Override
    public WCheckbox checkbox(boolean checked) {
        return w(new WHontunCheckbox(checked));
    }

    @Override
    public WSlider slider(double value, double min, double max) {
        return w(new WHontunSlider(value, min, max));
    }

    public WTextBox textBox(String text, String placeholder, String title, double padding, CharFilter filter, Class<? extends WTextBox.Renderer> renderer) {
        return w(new WHontunTextBox(text, placeholder, title, padding, filter, renderer));
    }

    public WTextBox textBox(String text, String placeholder, String title, CharFilter filter, Class<? extends WTextBox.Renderer> renderer) {
        return textBox(text, placeholder, title, pad(), filter, renderer);
    }

    public WTextBox textBox(String text, CharFilter filter, double padding) {
        return textBox(text, null, "", padding, filter, null);
    }

    @Override
    public WTextBox textBox(String text, String placeholder, CharFilter filter, Class<? extends WTextBox.Renderer> renderer) {
        return textBox(text, placeholder, "", filter, renderer);
    }

    public <T> WDropdown<T> dropdown(String title, T[] values, T value) {
        return w(new WHontunDropdown<>(title, values, value));
    }

    @SuppressWarnings("unchecked")
    public <T extends Enum<?>> WDropdown<T> dropdown(String title, T value) {
        Class<?> klass = value.getDeclaringClass();
        T[] values = (T[]) klass.getEnumConstants();
        return dropdown(title, values, value);
    }

    @Override
    public <T> WDropdown<T> dropdown(T[] values, T value) {
        return dropdown(null, values, value);
    }

    @Override
    public WTriangle triangle() {
        return w(new WHontunTriangle());
    }

    @Override
    public WTooltip tooltip(String text) {
        return w(new WHontunTooltip(text));
    }

    @Override
    public WView view() {
        return w(new WHontunView());
    }

    @Override
    public WSection section(String title, boolean expanded, WWidget headerWidget) {
        return w(new WHontunSection(title, expanded, headerWidget));
    }

    @Override
    public WAccount account(WidgetScreen screen, Account<?> account) {
        return w(new WHontunAccount(screen, account));
    }

    @Override
    public WWidget module(Module module) {
        return w(module(module, style().moduleTitle(module)));
    }

    @Override
    public WWidget module(Module module, String title) {
        return w(new WHontunModule(module, title));
    }

    @Override
    public WQuad quad(Color color) {
        return w(new WHontunQuad(color));
    }

    @Override
    public WTopBar topBar() {
        return w(new WHontunTopBar());
    }

    @Override
    public WFavorite favorite(boolean checked) {
        return w(new WHontunFavorite(checked));
    }

    public WHontunKeybind hontunKeybind(Keybind keybind) {
        return hontunKeybind(keybind, Keybind.none());
    }

    public WHontunKeybind hontunKeybind(Keybind keybind, Keybind defaultValue) {
        return hontunKeybind(null, keybind, defaultValue);
    }

    public WHontunKeybind hontunKeybind(String title, Keybind keybind, Keybind defaultValue) {
        return w(new WHontunKeybind(title, keybind, defaultValue));
    }

    public WGuiTexture texture(GuiTexture texture, double size) {
        return w(new WHontunGuiTexture(texture, size));
    }

    public WColorPicker colorPicker(Color color, GuiTexture overlayTexture) {
        return w(new WHontunColorPicker(color, overlayTexture));
    }

    public <T> WMultiSelect<T> multiSelect(String title, List<T> items) {
        return w(new WHontunMultiSelect<>(title, items));
    }

    public WSearch search() {
        return w(new WHontunSearch());
    }

    public <T> WTreeTable<T> treeTable(List<T> items, Function<T, Set<T>> dependencyResolver, Predicate<T> visibility, BiConsumer<WTable, T> factoryCreator) {
        return w(new WTreeTable<>(items, dependencyResolver, visibility, factoryCreator));
    }

    public WHontunIntEdit hontunIntEdit(IntSetting setting) {
        return w(new WHontunIntEdit(setting));
    }

    public WHontunDoubleEdit hontunDoubleEdit(String title, String description, double value, double min, double max, int decimalPlaces, double sliderMin, double sliderMax, boolean noSlider) {
        return w(new WHontunDoubleEdit(title, description, null, value, min, max, decimalPlaces, sliderMin, sliderMax, noSlider));
    }

    public WHontunDoubleEdit hontunDoubleEdit(DoubleSetting setting) {
        return w(new WHontunDoubleEdit(setting));
    }

    public Easing guiAnimationEasing() {
        return guiAnimation.get();
    }

    public int guiAnimationDuration() {
        return guiAnimationDuration.get();
    }

    public Color accentColor() {
        return colorCache.get(HontunColor.Blue);
    }

    public Color accentHiColor() {
        return colorCache.get(HontunColor.Sapphire);
    }

    public Color accentLoColor() {
        return colorCache.get(HontunColor.AccentLo);
    }

    public Color accentTextColor() {
        return colorCache.get(HontunColor.AccentText);
    }

    public Color accentHiTextColor() {
        return colorCache.get(HontunColor.AccentHiText);
    }

    public Color accentLineColor() {
        return colorCache.get(HontunColor.AccentLine);
    }

    public Color accentHiLineColor() {
        return colorCache.get(HontunColor.AccentHiLine);
    }

    public Color accentLoLineColor() {
        return colorCache.get(HontunColor.AccentLoLine);
    }

    public Color accentBarColor() {
        return colorCache.get(HontunColor.AccentBar);
    }

    public Color accentHiBarColor() {
        return colorCache.get(HontunColor.AccentHiBar);
    }

    public Color accentLoBarColor() {
        return colorCache.get(HontunColor.AccentLoBar);
    }

    public Color onAccentColor() {
        return colorCache.get(HontunColor.OnAccent);
    }

    public Color greenColor() {
        return colorCache.get(HontunColor.Green);
    }

    public Color yellowColor() {
        return colorCache.get(HontunColor.Yellow);
    }

    public Color redColor() {
        return colorCache.get(HontunColor.Red);
    }

    public Color blueColor() {
        return colorCache.get(HontunColor.Blue);
    }

    public Color overlay2Color() {
        return colorCache.get(HontunColor.Overlay2);
    }

    public Color overlay1Color() {
        return colorCache.get(HontunColor.Overlay1);
    }

    public Color overlay0Color() {
        return colorCache.get(HontunColor.Overlay0);
    }

    public Color surface2Color() {
        return colorCache.get(HontunColor.Surface2);
    }

    public Color surface1Color() {
        return colorCache.get(HontunColor.Surface1);
    }

    public Color surface0Color() {
        return colorCache.get(HontunColor.Surface0);
    }

    public Color baseColor() {
        return colorCache.get(HontunColor.Base);
    }

    public Color mantleColor() {
        return colorCache.get(HontunColor.Mantle);
    }

    public Color crustColor() {
        return colorCache.get(HontunColor.Crust);
    }

    public Color textColor() {
        return colorCache.get(HontunColor.Text);
    }

    public Color textSecondaryColor() {
        return colorCache.get(HontunColor.Subtext0);
    }

    public Color textHighlightColor() {
        return colorCache.get(HontunColor.Blue);
    }

    public double windowOpacity() {
        return style().windowAlpha(windowOpacity.get(), lightMode.get());
    }

    public double backgroundOpacity() {
        return style().controlAlpha(backgroundOpacity.get(), lightMode.get());
    }

    @Override
    public Color starscriptTextColor() {
        return starscript(starscriptText, textColor());
    }

    @Override
    public Color starscriptBraceColor() {
        return starscript(starscriptBraces, textSecondaryColor());
    }

    @Override
    public Color starscriptParenthesisColor() {
        return starscript(starscriptParenthesis, textColor());
    }

    @Override
    public Color starscriptDotColor() {
        return starscript(starscriptDots, textColor());
    }

    @Override
    public Color starscriptCommaColor() {
        return starscript(starscriptCommas, textColor());
    }

    @Override
    public Color starscriptOperatorColor() {
        return starscript(starscriptOperators, textColor());
    }

    @Override
    public Color starscriptStringColor() {
        return starscript(starscriptStrings, LIGHT_STARSCRIPT_STRING);
    }

    @Override
    public Color starscriptNumberColor() {
        return starscript(starscriptNumbers, LIGHT_STARSCRIPT_NUMBER);
    }

    @Override
    public Color starscriptKeywordColor() {
        return starscript(starscriptKeywords, LIGHT_STARSCRIPT_KEYWORD);
    }

    @Override
    public Color starscriptAccessedObjectColor() {
        return starscript(starscriptAccessedObjects, LIGHT_STARSCRIPT_OBJECT);
    }

    private static final Color LIGHT_STARSCRIPT_STRING = new Color(6, 125, 23);
    private static final Color LIGHT_STARSCRIPT_NUMBER = new Color(23, 80, 235);
    private static final Color LIGHT_STARSCRIPT_KEYWORD = new Color(0, 51, 179);
    private static final Color LIGHT_STARSCRIPT_OBJECT = new Color(135, 16, 148);

    private Color starscript(Setting<SettingColor> setting, Color light) {
        return light() && setting.get().equals(setting.getDefaultValue()) ? light : setting.get();
    }

    public boolean light() {
        return lightMode.get() && style().supportsLightMode();
    }

    public Color shadowColor() {
        return light() ? new Color(0, 0, 0, 38) : ColorUtils.withAlpha(crustColor(), 0.4);
    }

    private void refreshPalette() {
        updateCache();
        if (mc != null && mc.gui != null && mc.gui.screen() instanceof WidgetScreen screen) mc.schedule(screen::reload);
    }

    private void updateCache() {
        StylePalette p = palette;
        style().palette(p, lightMode.get(), this);

        put(HontunColor.Crust,    p.crust);
        put(HontunColor.Mantle,   p.mantle);
        put(HontunColor.Base,     p.base);
        put(HontunColor.Surface0, p.surface0);
        put(HontunColor.Surface1, p.surface1);
        put(HontunColor.Surface2, p.surface2);

        put(HontunColor.Overlay0, p.overlay0);
        put(HontunColor.Overlay1, p.overlay1);
        put(HontunColor.Overlay2, p.overlay2);

        put(HontunColor.Text,     p.text);
        put(HontunColor.Subtext1, p.subtext1);
        put(HontunColor.Subtext0, p.subtext0);

        put(HontunColor.Blue,     p.accent);
        put(HontunColor.Sapphire, p.accentHi);
        put(HontunColor.Sky,      p.accentHi);
        put(HontunColor.Lavender, p.accent);
        put(HontunColor.Green,    p.green);
        put(HontunColor.Yellow,   p.yellow);
        put(HontunColor.Red,      p.red);

        put(HontunColor.Teal,      p.accentHi);
        put(HontunColor.Peach,     p.accent);
        put(HontunColor.Maroon,    p.red);
        put(HontunColor.Pink,      p.accentHi);
        put(HontunColor.Mauve,     p.accent);
        put(HontunColor.Flamingo,  p.accentHi);
        put(HontunColor.Rosewater, p.text);
        put(HontunColor.AccentLo,  p.accentLo);
        put(HontunColor.AccentText,   p.accentText);
        put(HontunColor.AccentHiText, p.accentHiText);
        put(HontunColor.AccentLine,   p.accentLine);
        put(HontunColor.AccentHiLine, p.accentHiLine);
        put(HontunColor.AccentLoLine, p.accentLoLine);
        put(HontunColor.AccentBar,    p.accentBar);
        put(HontunColor.AccentHiBar,  p.accentHiBar);
        put(HontunColor.AccentLoBar,  p.accentLoBar);
        put(HontunColor.OnAccent,     p.onAccent);

        for (HontunColor color : HontunColor.values()) {
            colorCache.putIfAbsent(color, HontunTheme.color(p.accent).toSetting());
        }
    }

    private void put(HontunColor role, int rgb) {
        int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
        Color existing = colorCache.get(role);
        if (existing != null) existing.set(r, g, b, 255);
        else colorCache.put(role, new SettingColor(r, g, b));
    }

    @Override
    public TabScreen modulesScreen() {
        return new HontunModulesScreen(this);
    }

    @Override
    public boolean isModulesScreen(Screen screen) {
        return screen instanceof HontunModulesScreen;
    }

    @Override
    public WidgetScreen moduleScreen(Module module) {
        WidgetScreen custom = style().moduleScreen(this, module);
        return custom != null ? custom : new HontunModuleScreen(this, module);
    }

    @Override
    public WWidget settings(Settings settings, String filter) {
        WWidget custom = style().settingsView(this, settings, filter);
        return custom != null ? custom : super.settings(settings, filter);
    }

    public WWidget classicSettings(Settings settings, String filter) {
        return super.settings(settings, filter);
    }

    public String terminalPromptText() {
        String p = terminalPrompt.get();
        if (p == null || p.isBlank()) return "$";
        p = p.strip();
        return p.length() > 3 ? p.substring(0, 3) : p;
    }

    @Override
    public TextRenderer textRenderer() {
        return switch (style().font()) {
            case SF -> {
                RichTextRenderer sf = smogRenderer();
                yield sf != null ? sf : richTextRenderer();
            }
            case MONO -> {
                RichTextRenderer mono = monoRenderer();
                yield mono != null ? mono : richTextRenderer();
            }
            case MC -> VanillaTextRenderer.INSTANCE;
            default -> Config.get().customFont.get() ? richTextRenderer() : VanillaTextRenderer.INSTANCE;
        };
    }

    public boolean richText() {
        return textRenderer() instanceof RichTextRenderer;
    }

    public double mcTextScale() {
        return Math.max(0.5, Math.round(2 * scale(1)) / 2.0);
    }

    private double mcFactor() {
        return mcTextScale() / scale(1);
    }

    public RichTextRenderer richTextRenderer() {
        if (style().font() == FontChoice.SF) {
            RichTextRenderer sf = smogRenderer();
            if (sf != null) return sf;
        }

        if (style().font() == FontChoice.MONO) {
            RichTextRenderer mono = monoRenderer();
            if (mono != null) return mono;
        }

        if (textRenderer == null) {
            try {
                setTextRenderer(new RichTextRenderer(Config.get().font.get()));
            } catch (Exception e) {
                HontunGui.LOG.error("Failed to load TextRenderer: ", e);
            }
        }

        return textRenderer;
    }

    public void setTextRenderer(RichTextRenderer renderer) {
        if (textRenderer != null) textRenderer.destroy();
        this.textRenderer = renderer;
    }

    private RichTextRenderer smogRenderer() {
        if (smogTextRenderer == null) {
            try {
                smogTextRenderer = new RichTextRenderer(smogFontFace());
            } catch (Exception e) {
                HontunGui.LOG.error("Failed to load SmogClient SF TextRenderer: ", e);
                return null;
            }
        }

        return smogTextRenderer;
    }

    private static FontFace smogFontFace() throws IOException {
        Path path = Files.createTempFile("hontun-smog-sf", ".ttf");
        path.toFile().deleteOnExit();

        try (InputStream in = HontunGuiTheme.class.getResourceAsStream("/assets/hontun/font/sf.ttf")) {
            if (in == null) throw new IOException("Bundled SmogClient font hontun:font/sf.ttf not found on classpath");
            Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
        }

        FontFace face = new SystemFontFace(new FontInfo("SF", FontInfo.Type.Regular), path);

        if (Fonts.getFamily(face.info.family()) == null) {
            FontFamily family = new FontFamily(face.info.family());
            family.addFont(face);
            Fonts.FONT_FAMILIES.add(family);
        }

        return face;
    }

    private RichTextRenderer monoRenderer() {
        if (monoTextRenderer == null) {
            try {
                monoTextRenderer = new RichTextRenderer(monoFontFace());
            } catch (Exception e) {
                HontunGui.LOG.error("Failed to load Terminal mono TextRenderer: ", e);
                return null;
            }
        }

        return monoTextRenderer;
    }

    private static FontFace monoFontFace() throws IOException {
        Path path = Files.createTempFile("hontun-terminal-mono", ".ttf");
        path.toFile().deleteOnExit();

        try (InputStream in = HontunGuiTheme.class.getResourceAsStream("/assets/hontun/font/mono.ttf")) {
            if (in == null) throw new IOException("Bundled Terminal font hontun:font/mono.ttf not found on classpath");
            Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
        }

        FontFace face = new SystemFontFace(new FontInfo("Hontun Mono", FontInfo.Type.Regular), path);

        if (Fonts.getFamily(face.info.family()) == null) {
            FontFamily family = new FontFamily(face.info.family());
            family.addFont(face);
            Fonts.FONT_FAMILIES.add(family);
        }

        return face;
    }

    public double vanillaTextScale(double segmentScale) {
        return Math.max(0.5, Math.round(2 * mcTextScale() * segmentScale) / 2.0);
    }

    public double vanillaUnit(double segmentScale) {
        return 2 * vanillaTextScale(segmentScale);
    }

    public double vanillaHeight(double segmentScale) {
        return 9 * vanillaUnit(segmentScale);
    }

    public double vanillaTextOffset(double segmentScale) {
        return -vanillaUnit(segmentScale);
    }

    public double vanillaWidth(String text, int length, double segmentScale) {
        if (text.isEmpty() || length <= 0) return 0;
        String s = length < text.length() ? text.substring(0, length) : text;
        return Minecraft.getInstance().font.width(s) * vanillaUnit(segmentScale);
    }

    public double runWidth(RichTextSegment segment, int start, int end, boolean vanilla) {
        if (end <= start) return 0;
        if (vanilla) return vanillaWidth(segment.getText().substring(start, end), end - start, segment.getScale());
        return scale(richTextRenderer().getRunWidth(segment, start, end));
    }

    private double richSegmentWidth(RichTextSegment segment, int length) {
        String text = segment.getText();
        int end = Math.min(length, text.length());
        if (!GlyphSupport.needsVanilla(text, end)) return scale(richTextRenderer().getWidth(segment, length));

        double total = 0;
        int start = 0;
        while (start < end) {
            int stop = GlyphSupport.runEnd(text, start, end);
            total += runWidth(segment, start, stop, GlyphSupport.vanillaRun(text, start));
            start = stop;
        }

        return total + scale(richTextRenderer().getShadowWidth(segment));
    }

    private double richTextWidth(RichText text, int length) {
        double total = 0;
        int remaining = length;

        for (RichTextSegment segment : text.getSegments()) {
            if (remaining <= 0) break;
            String s = segment.getText();
            if (s.isEmpty()) continue;

            int n = Math.min(s.length(), remaining);
            total += richSegmentWidth(segment, n);
            remaining -= n;
        }

        return total;
    }

    public double textWidth(RichTextSegment segment) {
        return richText()
                ? richSegmentWidth(segment, segment.getText().length())
                : scale(textRenderer().getWidth(segment.getText())) * mcFactor();
    }

    public double textWidth(RichText text) {
        double routed = router().measure(this, text);
        if (routed >= 0) return routed;

        return richText()
                ? richTextWidth(text, text.length())
                : scale(textRenderer().getWidth(text.getPlainText())) * mcFactor();
    }

    @Override
    public double textWidth(String text, int length, boolean title) {
        if (!richText()) return scale(textRenderer().getWidth(text, length, title)) * mcFactor();
        return richTextWidth(RichText.of(text).boldIf(title), length);
    }

    @Override
    public double textWidth(String text) {
        return textWidth(RichText.of(text));
    }

    public double textHeight(RichText text) {
        if (!richText()) return scale(textRenderer().getHeight()) * mcFactor();

        double h = scale(richTextRenderer().getHeight(text));
        for (RichTextSegment segment : text.getSegments()) {
            if (GlyphSupport.needsVanilla(segment.getText()))
                h = Math.max(h, vanillaHeight(segment.getScale()));
        }
        return h;
    }

    @Override
    public double textHeight(boolean title) {
        TextRenderer renderer = textRenderer();
        double h = scale(renderer.getHeight(title));
        return renderer instanceof RichTextRenderer ? h : h * mcFactor();
    }

    @Override
    public double textHeight() {
        return textHeight(false);
    }

    @Override
    public void beforeRender() {
        super.beforeRender();
        HontunRenderer.get().setTheme(this);
    }

    @Override
    public double scale(double value) {
        double scaled = value * style().effectiveScale(scale.get());

        if (Util.getPlatform() == Util.OS.OSX) {
            scaled /= (double) mc.getWindow().getWidth() / mc.getWindow().getWidth();
        }

        return scaled;
    }

    @Override
    public boolean categoryIcons() {
        return categoryIcons.get();
    }

    @Override
    public boolean hideHUD() {
        return hideHUD.get();
    }

    @Override
    public boolean modulesHelpText() {
        return modulesHelpText.get();
    }

    private void invalidateScreen(Object ignored) {
        if (mc.gui.screen() instanceof WidgetScreen)
            ((WidgetScreen) mc.gui.screen()).invalidate();
    }

    private void onScaleChanged(Object value) {
        invalidateScreen(value);
        if (style().pipeline() == Pipeline.PIXEL) scheduleRebuild();
    }

    private void onUiModeChanged(HontunTheme.UiMode m) {
        if (m != null) activeUiMode = m;
        HontunTheme.setMode(m);
        if (clickGui != null) clickGui.set(m);
    }

    private void onClickGuiChanged(HontunTheme.UiMode m) {
        if (m != null) activeClickGui = m;
        updateCache();
        HontunRenderer.get().styleChanged();
        TabGlide.reset();
        scheduleRebuild();
    }

    private void scheduleRebuild() {
        if (mc == null || mc.gui == null || !(mc.gui.screen() instanceof WidgetScreen)) return;

        mc.schedule(() -> {
            Screen current = mc.gui.screen();
            if (current instanceof TabScreen ts) ts.tab.openScreen(this);
            else if (current instanceof WidgetScreen ws) {
                if (ws instanceof WindowScreenAccessor accessor && accessor.hontun$window() instanceof WHontunWindow window) window.rebuildChrome();
                ws.reload();
                ws.invalidate();
            }
        });
    }

    private boolean cornerRadiusVisible() {
        return style().uses(Knob.CORNER_RADIUS);
    }

    private boolean paletteColorVisible() {
        HontunTheme.UiMode ui = activeUiMode;
        return style().uses(Knob.PALETTE_COLOR) || (ui != HontunTheme.UiMode.Vanilla && ui != HontunTheme.UiMode.SmogClient);
    }

    private boolean animationVisible() {
        return style().uses(Knob.ANIMATION);
    }

    private boolean windowShadowVisible() {
        return style().uses(Knob.WINDOW_SHADOW);
    }

    private boolean terminalVisible() {
        return clickGui.get() == HontunTheme.UiMode.Terminal;
    }

    private boolean terminalAnyVisible() {
        return terminalVisible() || uiMode.get() == HontunTheme.UiMode.Terminal;
    }

    private boolean rainTuningVisible() {
        return (terminalAnyVisible() && rain.get()) || menuScene.get() == HontunTheme.MenuScene.Matrix;
    }

    private void onMenuSceneChanged(HontunTheme.MenuScene scene) {
        boolean wasMatrix = HontunTheme.menuScene() == HontunTheme.MenuScene.Matrix;
        HontunTheme.setMenuScene(scene);
        if (wasMatrix != (scene == HontunTheme.MenuScene.Matrix) && !terminalAnyVisible()) scheduleRebuild();
    }

    private boolean lightModeVisible() {
        return style().supportsLightMode();
    }

    private boolean tabIconsVisible() {
        return style().uses(Knob.TAB_ICONS);
    }

    public ClickStyle style() {
        return ClickStyles.of(activeClickGui);
    }

    public PrimitiveRouter router() {
        return Routers.of(style().pipeline());
    }

    public double rawScale() {
        return scale.get();
    }

    public void seedWindowConfig(String base, String id) {
        if (base == null || id == null || base.equals(id) || windowConfigs.containsKey(id)) return;

        WindowConfig source = windowConfigs.get(base);
        WindowConfig config = getWindowConfig(id);
        if (source != null) config.expanded = source.expanded;
    }

    public int effCornerRadius() {
        return (int) Math.round(style().radiusFactor() * cornerRadius.get());
    }

    public int effSmallCornerRadius() {
        return (int) Math.round(style().radiusFactor() * smallCornerRadius.get());
    }

    public boolean effWindowShadow() {
        return style().windowShadow(this);
    }

    public class ThreeStateColor {
        private final Supplier<Color> normal, hovered, pressed;

        public ThreeStateColor(Supplier<Color> normal, Supplier<Color> hovered, Supplier<Color> pressed) {
            this.normal = normal;
            this.hovered = hovered;
            this.pressed = pressed;
        }

        public Color get() {
            return normal.get();
        }

        public Color get(float alpha) {
            return withAlpha(normal.get(), alpha);
        }

        public Color get(boolean pressed, boolean hovered, boolean bypassDisableHoverColor) {
            if (pressed) return this.pressed.get();
            return (hovered && (bypassDisableHoverColor || !disableHoverColor)) ? this.hovered.get() : this.normal.get();
        }

        public Color get(boolean pressed, boolean hovered, boolean bypassDisableHoverColor, float alpha) {
            Color color = get(pressed, hovered, bypassDisableHoverColor);
            return withAlpha(color, alpha);
        }

        public Color get(boolean pressed, boolean hovered) {
            return get(pressed, hovered, false);
        }

        public Color get(boolean pressed, boolean hovered, float alpha) {
            return get(pressed, hovered, false, alpha);
        }

        public Color get(boolean hovered) {
            return get(false, hovered, false);
        }

        public Color get(boolean hovered, float alpha) {
            return get(false, hovered, false, alpha);
        }

        private Color withAlpha(Color color, float alpha) {
            Color result = color.copy().a((int) (255 * alpha));
            result.validate();
            return result;
        }
    }
}
