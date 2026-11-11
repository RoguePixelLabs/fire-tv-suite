package local.pitv;
import android.content.*;import android.view.*;import android.widget.*;
/** Reads the existing weather cache; no extra network request or polling timer. */
final class HomeWeatherSummary extends TextView implements android.content.SharedPreferences.OnSharedPreferenceChangeListener {
 final android.content.SharedPreferences prefs;
 HomeWeatherSummary(Context c,Runnable action){super(c);prefs=c.getSharedPreferences("home-info",0);setTextColor(0xffedf0f8);setTextSize(11);setSingleLine(true);setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);setPadding(0,0,TvStyle.dp(c,3),0);setFocusable(true);setClickable(true);setContentDescription("Current weather · OK to change location");setOnClickListener(v->action.run());setOnFocusChangeListener((v,f)->setBackground(TvStyle.shape(c,0x00000000,f?0xffe9edff:0x00000000)));update();}
 void update(){String value=prefs.getString("weatherV2Text","").replaceFirst("^NOW\\s+","");int end=value.indexOf(" · Feels");if(end>=0)value=value.substring(0,end);setText(value.isEmpty()?"Weather":value);}
 public void onSharedPreferenceChanged(android.content.SharedPreferences p,String key){if(key.equals("weatherV2Text"))update();}
 protected void onAttachedToWindow(){super.onAttachedToWindow();prefs.registerOnSharedPreferenceChangeListener(this);update();}
 protected void onDetachedFromWindow(){prefs.unregisterOnSharedPreferenceChangeListener(this);super.onDetachedFromWindow();}
}
