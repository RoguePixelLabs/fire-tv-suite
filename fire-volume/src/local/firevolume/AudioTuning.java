package local.firevolume;

import android.content.SharedPreferences;

final class AudioTuning {
    static final String[] PROFILES = {"Flat", "Music", "Movies", "Talk", "Custom"};
    static final int[][] PRESETS = {{0,0,0,0,0}, {3,1,0,1,2}, {3,0,2,1,0}, {-5,-2,3,2,-2}};

    static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    static int gain(SharedPreferences prefs, int level) {
        if (level <= 0) return -9600;
        int ceiling = clamp(prefs.getInt("boost", 6), -6, 6);
        int shape = clamp(prefs.getInt("curve", 8), 4, 12);
        return Math.max(-9600, (int) Math.round(ceiling * 100 + 500 * shape * Math.log10(level / 100.0)));
    }

    static int profile(SharedPreferences prefs) {
        return clamp(prefs.getInt("profile", 0), 0, PROFILES.length - 1);
    }

    static int[] bands(SharedPreferences prefs) {
        int selected = profile(prefs);
        if (selected < PRESETS.length) return PRESETS[selected];
        int[] bands = new int[5];
        for (int i = 0; i < bands.length; i++) bands[i] = clamp(prefs.getInt("band" + i, 0), -6, 6);
        return bands;
    }
}
