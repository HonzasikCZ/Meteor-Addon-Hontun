package cz.honzasik.hontun.gui.theme.widgets.input;

import cz.honzasik.hontun.gui.api.text.RichText;
import cz.honzasik.hontun.gui.api.text.TextScale;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.theme.HontunWidget;
import cz.honzasik.hontun.gui.theme.icons.HontunBuiltinIcons;
import cz.honzasik.hontun.gui.theme.style.ChipKind;
import cz.honzasik.hontun.gui.theme.style.HoverTarget;
import cz.honzasik.hontun.gui.theme.style.Metrics;
import cz.honzasik.hontun.gui.widget.input.WSearch;
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
        style().paintSearchPanel(this, renderer, mouseX, mouseY);
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

    public static class WHontunHeader extends WSearchHeader implements HontunWidget {
        public WHontunHeader(WSearch search) {
            super(search);
        }

        @Override
        public void init() {
            HontunGuiTheme theme = theme();

            WHorizontalList row = add(theme.horizontalList()).expandX().pad(metrics().searchHeaderPad).widget();

            row.add(theme.texture(HontunBuiltinIcons.SEARCH.texture(), theme.textHeight())).center();

            WHontunTextBox textBox = (WHontunTextBox) theme.textBox("", "Search for modules...");
            textBox.shouldRenderBackground(false);

            row.add(textBox).expandX();
            search.initTextBox(textBox);

            row.add(theme.label("ESC to close")).padLeft(metrics().searchHintPadL).padRight(metrics().searchHintPadR);
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            style().paintSearchHeader(this, renderer, mouseX, mouseY);
        }
    }

    public static class WHontunResultsContainer extends WResultsContainer implements HontunWidget {
        @Override
        public void init() {
            super.init();

            addDirect(theme.label("Left click to toggle module; Right click to open the module's settings.").color(theme().textSecondaryColor())).pad(metrics().gap).centerX();
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            style().paintSearchBody(this, renderer, mouseX, mouseY);
        }
    }

    public static class WHontunResult extends WSearchResult implements HontunWidget, HoverTarget {
        private HontunGuiTheme theme;

        public WHontunResult(SearchResult result) {
            super(result);
        }

        @Override
        public void init() {
            theme = theme();
            Metrics m = metrics();

            WHorizontalList row = add(theme.horizontalList()).expandX().pad(m.searchRowPad).widget();

            row.add(new WResultType(result)).pad(m.gap).center();

            WVerticalList infoColumn = row.add(theme.verticalList()).expandX().widget();

            infoColumn.add(theme.label(RichText.of(result.title())));

            RichText desc = RichText.of(result.description()).scale(TextScale.SMALL.get());
            WLabel descLabel = infoColumn.add(theme.label(desc)).widget();
            descLabel.color = theme.textSecondaryColor();
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            style().paintSearchRow(this, renderer, mouseX, mouseY);
        }

        public SearchResult result() {
            return result;
        }

        public boolean isPressed() {
            return pressed;
        }

        @Override
        public boolean hoverLit() {
            return mouseOver;
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

                add(theme().texture(getIcon(), theme.textHeight()).color(color)).pad(metrics().gap).center();
            }

            @Override
            protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
                style().paintChip(renderer, x, y, width, height, color, kind());
            }

            public ChipKind kind() {
                return switch (result) {
                    case ModuleSearchResult r -> r.hasAlias() ? ChipKind.ALIAS : ChipKind.MODULE;
                    case SettingSearchResult ignored -> ChipKind.SETTING;
                    default -> ChipKind.MODULE;
                };
            }

            public Color chipColor() {
                return color;
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
