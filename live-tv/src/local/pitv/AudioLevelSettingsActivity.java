package local.pitv;
import android.app.*;import android.os.*;import android.content.*;import android.widget.*;import android.graphics.Color;
public class AudioLevelSettingsActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener {
 AlertDialog dialog;TextView state;
 static void open(Context c){c.startActivity(new Intent(c,AudioLevelSettingsActivity.class));}
 public void onCreate(Bundle saved){super.onCreate(saved);getWindow().getDecorView().setSystemUiVisibility(5894);getWindow().getDecorView().setBackgroundColor(0xff101518);
  LinearLayout title=new LinearLayout(this);title.setOrientation(1);title.setPadding(32,24,32,12);TextView heading=new TextView(this);heading.setText("Volume leveling");heading.setTextSize(22);heading.setTextColor(Color.WHITE);title.addView(heading);state=new TextView(this);state.setTextSize(14);state.setTextColor(0xffbbc5cf);title.addView(state);
  String[] modes={"off","normal","night"},labels={"Off · original sound","Normal · gentler volume changes","Night · stronger leveling and lower peaks"};int selected=java.util.Arrays.asList(modes).indexOf(AudioLevelService.mode(this));
  dialog=new AlertDialog.Builder(this).setCustomTitle(title).setSingleChoiceItems(labels,selected,(d,n)->{AudioLevelService.select(this,modes[n]);status();}).setNegativeButton("Close",(d,n)->finish()).setOnCancelListener(d->finish()).create();dialog.setOnDismissListener(d->{if(!isFinishing())finish();});dialog.show();dialog.getListView().requestFocus();status();AudioLevelService.prefs(this).registerOnSharedPreferenceChangeListener(this);
 }
 void status(){if(state==null)return;SharedPreferences p=AudioLevelService.prefs(this);String mode=AudioLevelService.mode(this);state.setText(mode.equals("off")?"Shared audio output · Off":p.getBoolean("active",false)?"Shared audio output · Active":"Shared audio output · Starting / "+p.getString("error","waiting"));}public void onSharedPreferenceChanged(SharedPreferences p,String key){status();}
 protected void onDestroy(){AudioLevelService.prefs(this).unregisterOnSharedPreferenceChangeListener(this);super.onDestroy();}
}
