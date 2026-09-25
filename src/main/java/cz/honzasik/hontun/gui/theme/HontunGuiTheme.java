package cz.honzasik.hontun.gui.theme;

import cz.honzasik.hontun.gui.HontunGui;
import cz.honzasik.hontun.gui.api.animation.Easing;
import cz.honzasik.hontun.gui.api.text.RichText;
import cz.honzasik.hontun.gui.api.text.RichTextSegment;
import cz.honzasik.hontun.gui.screen.HontunModuleScreen;
import cz.honzasik.hontun.gui.screen.HontunModulesScreen;
import cz.honzasik.hontun.gui.theme.colors.HontunColor;
import cz.honzasik.hontun.gui.theme.widgets.*;
import cz.honzasik.hontun.gui.theme.widgets.container.WHontunSection;
import cz.honzasik.hontun.gui.theme.widgets.container.WHontunView;
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
import cz.honzasik.hontun.gui.render.text.RichTextRenderer;
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
import net.minecraft.client.gui.screens.Screen;

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

    private RichTextRenderer textRenderer;
    private RichTextRenderer smogTextRenderer;

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgCorners = settings.createGroup("Corners");
    private final SettingGroup sgAnimations = settings.createGroup("Animations");
    private final SettingGroup sgColors = settings.createGroup("Colors");
    private final SettingGroup sgSnapping = settings.createGroup("Snapping");
    private final SettingGroup sgScreens = settings.createGroup("Screens");
    private final SettingGroup sgStarscript = settings.createGroup("Starscript");

    public final Setting<HontunTheme.UiMode> uiMode = sgGeneral.add(new EnumSetting.Builder<HontunTheme.UiMode>()
            .name("ui-mode")
            .description("Look of the whole addon (ClickGUI + MC menus/buttons/hotbar/containers). Vanilla = no restyle. HVanilla = classic Minecraft shapes. HModern1 = flat and rounded. HModern2 = chamfered corners with an accent glow (default). SmogClient = rounded black and white SmogClientPro look with the SF font.")
            .defaultValue(HontunTheme.UiMode.HModern2)
            .onChanged(m -> HontunTheme.setMode(m))
            .build()
    );

    public final Setting<Boolean> lightMode = sgGeneral.add(new BoolSetting.Builder()
            .name("light-mode")
            .description("Light palette for the Meteor ClickGUI (modules, settings, search, tabs). Minecraft menus, chat, containers and the HUD keep their dark look. Window and background opacity are raised so dark text stays readable over the game.")
            .defaultValue(false)
            .onChanged(v -> refreshPalette())
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
            .onChanged(this::invalidateScreen)
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

    public final Setting<Integer> cornerRadius = sgCorners.add(new IntSetting.Builder()
            .name("corner-radius")
            .description("The radius of corners for large UI elements.")
            .defaultValue(10)
            .sliderRange(1, 25)
            .build()
    );

    public final Setting<Integer> smallCornerRadius = sgCorners.add(new IntSetting.Builder()
            .name("small-corner-radius")
            .description("The radius of corners for small UI elements.")
            .defaultValue(6)
            .sliderRange(1, 25)
            .build()
    );

    public final Setting<Easing> guiAnimation = sgAnimations.add(new EnumSetting.Builder<Easing>()
            .name("gui-animation-easing")
            .description("The easing function used for UI animations.")
            .defaultValue(Easing.QUART_OUT)
            .build()
    );

    public final Setting<Integer> guiAnimationDuration = sgAnimations.add(new IntSetting.Builder()
            .name("gui-animation-duration")
            .description("Duration of the animation in milliseconds.")
            .defaultValue(300)
            .sliderRange(1, 1000)
            .build()
    );

    public final Setting<SettingColor> paletteColor = sgColors.add(new ColorSetting.Builder()
            .name("palette-color")
            .description("Accent color for the whole addon (ClickGUI, chat, menu, buttons, hotbar). Pick one color; the gradient is derived automatically. Backgrounds stay neutral grey.")
            .defaultValue(new SettingColor(62, 140, 255))
            .onChanged(c -> HontunTheme.setAccent(c.getPacked()))
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
            .description("Base color of the animated menu gradient (title/pause/loading). Default dark grey; set it to your accent to make the menu breathe that color. Alpha is ignored.")
            .defaultValue(new SettingColor(20, 21, 25))
            .onChanged(c -> HontunTheme.setMenuColor(c.getPacked()))
            .build()
    );

    public final Setting<Double> windowOpacity = sgColors.add(new DoubleSetting.Builder()
            .name("window-opacity")
            .description("Controls the opacity of the windows.")
            .defaultValue(1)
            .sliderRange(0, 1)
            .decimalPlaces(2)
            .onChanged(v -> {
                if (lightMode.get()) updateCache();
            })
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

        HontunTheme.setMode(uiMode.get());
        HontunTheme.setAccent(paletteColor.get().getPacked());
        HontunTheme.setBgOverlay(containerBackground.get().getPacked());
        HontunTheme.setMenuColor(menuBackground.get().getPacked());
        HontunGeo.setEnabled(serverFlags.get());
        updateCache();

        HontunTheme.addListener(this::updateCache);
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
        if (maxWidth == 0) return w(new WHontunLabel(RichText.of(text).boldIf(title)));
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
        return w(module(module, module.title));
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
        return lightOpacity(windowOpacity.get());
    }

    public double backgroundOpacity() {
        return lightOpacity(backgroundOpacity.get());
    }

    private double lightOpacity(double opacity) {
        return lightMode.get() ? 0.7 + 0.3 * opacity : opacity;
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
        return lightMode.get() && setting.get().equals(setting.getDefaultValue()) ? light : setting.get();
    }

    public boolean light() {
        return lightMode.get();
    }

    public Color shadowColor() {
        return lightMode.get() ? new Color(0, 0, 0, 38) : ColorUtils.withAlpha(crustColor(), 0.4);
    }

    private void refreshPalette() {
        updateCache();
        if (mc != null && mc.gui != null && mc.gui.screen() instanceof WidgetScreen screen) mc.schedule(screen::reload);
    }

    private void updateCache() {
        boolean light = lightMode.get();
        int accent = light ? HontunLightPalette.accent(windowOpacity()) : HontunTheme.accent();
        int accentHi = light ? HontunLightPalette.accentHi(windowOpacity()) : HontunTheme.accentHi();
        int text = light ? HontunLightPalette.text() : HontunTheme.textLight();
        int red = light ? HontunLightPalette.red() : HontunTheme.red();

        put(HontunColor.Crust,    light ? HontunLightPalette.crust()    : HontunTheme.crust());
        put(HontunColor.Mantle,   light ? HontunLightPalette.mantle()   : HontunTheme.mantle());
        put(HontunColor.Base,     light ? HontunLightPalette.base()     : HontunTheme.base());
        put(HontunColor.Surface0, light ? HontunLightPalette.surface0() : HontunTheme.surface0());
        put(HontunColor.Surface1, light ? HontunLightPalette.surface1() : HontunTheme.surface1());
        put(HontunColor.Surface2, light ? HontunLightPalette.surface2() : HontunTheme.surface2());

        put(HontunColor.Overlay0, light ? HontunLightPalette.overlay0() : HontunTheme.overlay0());
        put(HontunColor.Overlay1, light ? HontunLightPalette.overlay1() : HontunTheme.overlay1());
        put(HontunColor.Overlay2, light ? HontunLightPalette.overlay2() : HontunTheme.overlay2());

        put(HontunColor.Text,     text);
        put(HontunColor.Subtext1, light ? HontunLightPalette.subtext1() : HontunTheme.subtext1());
        put(HontunColor.Subtext0, light ? HontunLightPalette.textDim()  : HontunTheme.textDim());

        put(HontunColor.Blue,     accent);
        put(HontunColor.Sapphire, accentHi);
        put(HontunColor.Sky,      accentHi);
        put(HontunColor.Lavender, accent);
        put(HontunColor.Green,    light ? HontunLightPalette.green()  : HontunTheme.green());
        put(HontunColor.Yellow,   light ? HontunLightPalette.yellow() : HontunTheme.yellow());
        put(HontunColor.Red,      red);

        put(HontunColor.Teal,      accentHi);
        put(HontunColor.Peach,     accent);
        put(HontunColor.Maroon,    red);
        put(HontunColor.Pink,      accentHi);
        put(HontunColor.Mauve,     accent);
        put(HontunColor.Flamingo,  accentHi);
        put(HontunColor.Rosewater, text);

        for (HontunColor color : HontunColor.values()) {
            colorCache.putIfAbsent(color, HontunTheme.color(accent).toSetting());
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
        return new HontunModuleScreen(this, module);
    }

    @Override
    public TextRenderer textRenderer() {
        if (HontunTheme.smog()) {
            RichTextRenderer sf = smogRenderer();
            if (sf != null) return sf;
        }
        return Config.get().customFont.get() ? richTextRenderer() : VanillaTextRenderer.INSTANCE;
    }

    public RichTextRenderer richTextRenderer() {
        if (HontunTheme.smog()) {
            RichTextRenderer sf = smogRenderer();
            if (sf != null) return sf;
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

    public double textWidth(RichTextSegment segment) {
        return scale(Config.get().customFont.get()
                ? richTextRenderer().getWidth(segment, segment.getText().length())
                : textRenderer().getWidth(segment.getText()));
    }

    public double textWidth(RichText text) {
        return scale(Config.get().customFont.get()
                ? richTextRenderer().getWidth(text)
                : textRenderer().getWidth(text.getPlainText()));
    }

    @Override
    public double textWidth(String text, int length, boolean title) {
        return scale(Config.get().customFont.get()
                ? richTextRenderer().getWidth(RichText.of(text).boldIf(title), length)
                : textRenderer().getWidth(text, length, title));
    }

    @Override
    public double textWidth(String text) {
        return textWidth(RichText.of(text));
    }

    public double textHeight(RichText text) {
        return scale(Config.get().customFont.get()
                ? richTextRenderer().getHeight(text)
                : textRenderer().getHeight());
    }

    @Override
    public double textHeight(boolean title) {
        return scale(textRenderer().getHeight(title));
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
        double scaled = value * scale.get();

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

    private double modeRadiusFactor() {
        switch (HontunTheme.mode()) {
            case HModern1:
            case HModern2: return 1.0;
            case Vanilla: return 0.0;
            default:      return 0.0;
        }
    }

    public int effCornerRadius() {
        return (int) Math.round(modeRadiusFactor() * cornerRadius.get());
    }

    public int effSmallCornerRadius() {
        return (int) Math.round(modeRadiusFactor() * smallCornerRadius.get());
    }

    public boolean effWindowShadow() {
        return windowShadow.get() && HontunTheme.modern();
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
