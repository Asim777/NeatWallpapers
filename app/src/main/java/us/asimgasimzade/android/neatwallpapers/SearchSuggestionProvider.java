package us.asimgasimzade.android.neatwallpapers;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Content provider to serve search suggestions in search bar
 */

public class SearchSuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "us.asimgasimzade.android.neatwallpapers.SearchSuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public SearchSuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
