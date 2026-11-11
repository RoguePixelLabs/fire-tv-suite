package local.firevolume;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;

public class MainActivity extends Activity {
    private SharedPreferences preferences;
    private TextView summary;
    private Button profileChoice;
    private final SeekBar[] bandSliders = new SeekBar[5];

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        preferences = getSharedPreferences("volume", MODE_PRIVATE);
        ScrollView scroll = new ScrollView(this);
        LinearLayout page = column();
        page.setPadding(dp(24), dp(16), dp(24), dp(16));
        scroll.addView(page);
        setContentView(scroll);
        text(page, "Audio settings", 25);
        summary = text(page, "", 14);

        LinearLayout columns = new LinearLayout(this);
        page.addView(columns);
        LinearLayout curve = column();
        LinearLayout tone = column();
        columns.addView(curve, new LinearLayout.LayoutParams(0, -2, 1));
        LinearLayout.LayoutParams right = new LinearLayout.LayoutParams(0, -2, 1);
        right.leftMargin = dp(20);
        columns.addView(tone, right);

        text(curve, "Volume curve", 20);
        text(curve, "A higher curve value makes the middle quieter.", 13);
        slider(curve, "Maximum boost", -6, 6, preferences.getInt("boost", 6), " dB",
            value -> preferences.edit().putInt("boost", value).apply());
        slider(curve, "Curve strength", 4, 12, preferences.getInt("curve", 8), "",
            value -> preferences.edit().putInt("curve", value).apply());
        choices(curve, "Remote step size", new String[]{"1", "2", "5", "10"},
            preferences.getInt("step", 5) == 1 ? 0 : preferences.getInt("step", 5) == 2 ? 1 : preferences.getInt("step", 5) == 10 ? 3 : 2,
            index -> preferences.edit().putInt("step", new int[]{1,2,5,10}[index]).apply());
        button(curve, "Reset volume curve", () -> {
            preferences.edit().remove("boost").remove("curve").apply();
            recreate();
        });
        button(curve, "Start volume control", () -> {
            preferences.edit().putBoolean("enabled", true).apply();
            startForegroundService(new Intent(this, VolumeService.class));
            summary.postDelayed(this::refresh, 300);
        });
        button(curve, "Stop volume control", () -> {
            preferences.edit().putBoolean("enabled", false).apply();
            stopService(new Intent(this, VolumeService.class));
            summary.postDelayed(this::refresh, 300);
        });
        button(curve, "Use Fire TV audio baseline", this::useFireTvBaseline);

        text(tone, "Sound profile", 20);
        profileChoice = choices(tone, "Profile", AudioTuning.PROFILES, AudioTuning.profile(preferences), index -> {
            preferences.edit().putInt("profile", index).apply();
            refresh();
        });
        text(tone, "Music adds warmth. Movies emphasize impact and dialogue. Talk brings voices forward.", 13);
        String[] names = {"Bass", "Low mids", "Mids", "Upper mids", "Treble"};
        int[] bands = AudioTuning.bands(preferences);
        for (int i = 0; i < names.length; i++) {
            final int band = i;
            bandSliders[i] = slider(tone, names[i], -6, 6, bands[i], " dB", value -> adjustBand(band, value));
        }
        refresh();
    }

    @Override protected void onResume() {
        super.onResume();
        if (summary != null) refresh();
    }

    private void useFireTvBaseline() {
        preferences.edit().putInt("boost", 0).putInt("curve", 4)
            .putInt("profile", 0).putBoolean("enabled", true).apply();
        if (VolumeService.active == null) startForegroundService(new Intent(this, VolumeService.class));
        recreate();
    }

    private void refresh() {
        VolumeService service = VolumeService.active;
        String state = service == null ? "Volume control off" : service.eqStatus;
        summary.setText(String.format(java.util.Locale.US,
            "%s    ·    25: %+.1f dB    50: %+.1f dB    75: %+.1f dB    100: %+.1f dB",
            state, AudioTuning.gain(preferences, 25)/100f, AudioTuning.gain(preferences, 50)/100f,
            AudioTuning.gain(preferences, 75)/100f, AudioTuning.gain(preferences, 100)/100f));
        profileChoice.setText("Profile · " + AudioTuning.PROFILES[AudioTuning.profile(preferences)]);
        int[] bands = AudioTuning.bands(preferences);
        for (int i = 0; i < bandSliders.length; i++) bandSliders[i].setProgress(bands[i] + 6);
    }

    private void adjustBand(int band, int value) {
        SharedPreferences.Editor edit = preferences.edit();
        if (AudioTuning.profile(preferences) < AudioTuning.PRESETS.length) {
            int[] startingBands = AudioTuning.bands(preferences);
            for (int i = 0; i < startingBands.length; i++) edit.putInt("band" + i, startingBands[i]);
            edit.putInt("profile", AudioTuning.PRESETS.length);
        }
        edit.putInt("band" + band, value).apply();
    }

    private SeekBar slider(LinearLayout parent, String title, int min, int max, int initial, String unit,
                           java.util.function.IntConsumer action) {
        TextView label = text(parent, "", 14);
        SeekBar slider = new SeekBar(this);
        slider.setContentDescription(title);
        slider.setMax(max - min);
        slider.setKeyProgressIncrement(1);
        int value = AudioTuning.clamp(initial, min, max);
        slider.setProgress(value - min);
        label.setText(title + "  " + value + unit);
        parent.addView(slider);
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar bar, int progress, boolean user) {
                int value = min + progress;
                label.setText(title + "  " + value + unit);
                if (user) {
                    action.accept(value);
                    refresh();
                }
            }
            public void onStartTrackingTouch(SeekBar bar) {}
            public void onStopTrackingTouch(SeekBar bar) {}
        });
        return slider;
    }

    private Button choices(LinearLayout parent, String title, String[] values, int selected,
                         java.util.function.IntConsumer action) {
        Button choice = new Button(this);
        choice.setAllCaps(false);
        choice.setText(title + " · " + values[selected]);
        parent.addView(choice);
        choice.setOnClickListener(view -> new android.app.AlertDialog.Builder(this)
            .setTitle(title).setItems(values, (dialog, index) -> {
                action.accept(index);
                choice.setText(title + " · " + values[index]);
            }).show());
        return choice;
    }

    private void button(LinearLayout parent, String label, Runnable action) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setText(label);
        button.setOnClickListener(view -> action.run());
        parent.addView(button);
    }

    private LinearLayout column() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private TextView text(LinearLayout parent, String label, int size) {
        TextView view = new TextView(this);
        view.setText(label);
        view.setTextSize(size);
        view.setPadding(0, dp(4), 0, dp(4));
        parent.addView(view);
        return view;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
