package cz.honzasik.hontun.gui.util.search;

import cz.honzasik.hontun.gui.util.search.results.ModuleSearchResult;
import cz.honzasik.hontun.gui.util.search.results.SettingSearchResult;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.Utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SearchUtils {
    private static final int DEFAULT_MAX_SCORE = 40;

    public static List<SearchResult> search(String query) {
        List<SearchResult> results = new ArrayList<>();

        results.addAll(searchModules(query, DEFAULT_MAX_SCORE));
        results.addAll(searchSettings(query, DEFAULT_MAX_SCORE));

        results.sort(Comparator.comparingInt(SearchResult::score));

        return results;
    }

    public static List<ModuleSearchResult> searchModules(String query, int maxScore) {
        List<ModuleSearchResult> results = new ArrayList<>();

        for (Module module : Modules.get().getAll()) {
            int score = Utils.searchLevenshteinDefault(module.title, query, false);
            String matchedAlias = null;

            if (Config.get().moduleAliases.get()) {
                for (String alias : module.aliases) {
                    int aliasScore = Utils.searchLevenshteinDefault(alias, query, false);
                    if (aliasScore < score) {
                        score = aliasScore;
                        matchedAlias = Utils.nameToTitle(alias);
                    }
                }
            }

            if (score <= maxScore) {
                results.add(new ModuleSearchResult(module, matchedAlias, score));
            }
        }

        results.sort(Comparator.comparingInt(SearchResult::score));
        return results;
    }

    public static List<SettingSearchResult> searchSettings(String query, int maxScore) {
        List<SettingSearchResult> results = new ArrayList<>();

        for (Module module : Modules.get().getAll()) {
            for (SettingGroup sg : module.settings) {
                for (Setting<?> setting : sg) {
                    if (setting.title == null || setting.title.isEmpty()) continue;

                    int score = Utils.searchLevenshteinDefault(setting.title, query, false);

                    if (score <= maxScore) {
                        results.add(new SettingSearchResult(setting, sg, score));
                    }
                }
            }
        }

        results.sort(Comparator.comparingInt(SearchResult::score));
        return results;
    }
}
