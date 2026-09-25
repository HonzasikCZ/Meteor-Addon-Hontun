package cz.honzasik.hontun.gui.theme.widgets.input;

import cz.honzasik.hontun.gui.api.render.Corners;
import cz.honzasik.hontun.gui.api.text.RichText;
import cz.honzasik.hontun.gui.api.text.TextScale;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.theme.HontunWidget;
import cz.honzasik.hontun.gui.theme.icons.HontunBuiltinIcons;
import cz.honzasik.hontun.gui.widget.input.WSearch;
import cz.honzasik.hontun.gui.util.ColorUtils;
import cz.honzasik.hontun.gui.util.search.SearchResult;
import cz.honzasik.hontun.gui.util.search.results.ModuleSearchResult;
import cz.honzasik.hontun.gui.util.search.results.SettingSearchResult;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class WHontunSearch extends WSearch implements HontunWidget {
    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        HontunGuiTheme theme = theme();
        Color shadowColor = theme.shadowColor();

        int shadowOffset = 2;
        roundedRect().pos(x - shadowOffset, y - shadowOffset)
                     .size(width + shadowOffset * 2, height + shadowOffset * 2)
                     .radius(radius() + shadowOffset)
                     .color(shadowColor)
                     .render();
    }

    @Override
    protected WSearchHeader createHeader(WSearch search) {
        return new WHontunHeader(search);
    }

    @Override
    protected WResultsContainer createResultsContainer() {
        return new WHontunResultsContainer();
    }

    @Override
    protected WSearchResult createSearchResult(SearchResult result) {
        return new WHontunResult(result);
    }

    private static class WHontunHeader extends WSearchHeader implements HontunWidget {
        public WHontunHeader(WSearch search) {
            super(search);
        }

        @Override
        public void init() {
            HontunGuiTheme theme = theme();

            WHorizontalList row = add(theme.horizontalList()).expandX().pad(theme.scale(12)).widget();

            row.add(theme.texture(HontunBuiltinIcons.SEARCH.texture(), theme.textHeight())).center();

            WHontunTextBox textBox = (WHontunTextBox) theme.textBox("", "Search for modules...");
            textBox.shouldRenderBackground(false);

            row.add(textBox).expandX();
            search.initTextBox(textBox);

            row.add(theme.label("ESC to close"));
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            roundedRect().bounds(this)
                         .color(theme().crustColor())
                         .radius(radius(), Corners.TOP)
                         .render();
        }
    }

    private static class WHontunResultsContainer extends WResultsContainer implements HontunWidget {
        @Override
        public void init() {
            super.init();

            addDirect(theme.label("Left click to toggle module; Right click to open the module's settings.").color(theme().textSecondaryColor())).pad(theme.pad()).centerX();
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            HontunGuiTheme theme = theme();

            roundedRect().bounds(this)
                         .color(ColorUtils.withAlpha(theme.baseColor(), theme.windowOpacity()))
                         .radius(radius(), Corners.BOTTOM)
                         .render();
        }
    }

    private static class WHontunResult extends WSearchResult implements HontunWidget {
        private HontunGuiTheme theme;

        public WHontunResult(SearchResult result) {
            super(result);
        }

        @Override
        public void init() {
            theme = theme();

            WHorizontalList row = add(theme.horizontalList()).expandX().pad(6).widget();

            row.add(new WResultType(result)).pad(theme.pad()).center();

            WVerticalList infoColumn = row.add(theme.verticalList()).expandX().widget();

            infoColumn.add(theme.label(RichText.of(result.title())));

            RichText desc = RichText.of(result.description()).scale(TextScale.SMALL.get());
            WLabel descLabel = infoColumn.add(theme.label(desc)).widget();
            descLabel.color = theme.textSecondaryColor();
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            if (!mouseOver) return;

            Color outlineColor = ColorUtils.withAlpha(
                    theme.accentColor(),
                    theme.backgroundOpacity() * 0.5
            );

            background(getBackgroundColor(pressed, false), outlineColor).render();
        }

        public static class WResultType extends WContainer implements HontunWidget {
            private final SearchResult result;
            private Color color;

            public WResultType(SearchResult result) {
                this.result = result;
            }

            @Override
            public void init() {
                color = getColor();

                add(theme().texture(getIcon(), theme.textHeight()).color(color)).pad(theme.pad()).center();
            }

            @Override
            protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
                roundedRect().bounds(this)
                             .color(ColorUtils.withAlpha(color, 60))
                             .radius(smallRadius())
                             .render();
            }

            private Color getColor() {
                return switch (result) {
                    case ModuleSearchResult r -> r.hasAlias() ? theme().yellowColor() : theme().greenColor();
                    case SettingSearchResult ignored -> theme().blueColor();
                    default -> theme().textSecondaryColor();
                };
            }

            private GuiTexture getIcon() {
                return switch (result) {
                    case ModuleSearchResult ignored -> HontunBuiltinIcons.CUBE.texture();
                    case SettingSearchResult ignored -> HontunBuiltinIcons.SETTING.texture();
                    default -> HontunBuiltinIcons.QUESTION_MARK.texture();
                };
            }
        }
    }
}
