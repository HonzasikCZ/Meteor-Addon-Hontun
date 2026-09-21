package com.mrmelon54.DraggableLists.config;

public enum ThemeMode {
    /** Follow Hontun when it is installed and its restyle is on, otherwise use the built-in palette. */
    AUTO,
    /** Always use the built-in palette. */
    BUILTIN,
    /** Always read colours from Hontun; falls back to the built-in palette if Hontun is missing. */
    HONTUN
}
