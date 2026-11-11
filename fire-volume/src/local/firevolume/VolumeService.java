package local.firevolume;
import android.app.*;import android.content.*;import android.os.*;import android.media.audiofx.*;import android.view.*;import android.widget.*;import android.graphics.*;import java.io.*;
public class VolumeService extends Service {
 static VolumeService active;
 Equalizer equalizer;
 SharedPreferences preferences;
 String eqStatus="Flat";
 final SharedPreferences.OnSharedPreferenceChangeListener tuningChanged=(prefs,key)->{
  if("boost".equals(key)||"curve".equals(key))apply();
  if("profile".equals(key)||(key!=null&&key.startsWith("band")))applyEqualizer();
 };
 Handler h=new Handler();LoudnessEnhancer effect;java.lang.Process reader;volatile boolean running;LinearLayout panel;TextView label;ProgressBar bar;int level;boolean muted;long lastEvent;
 final Runnable hide=()->{if(panel!=null){try{((WindowManager)getSystemService(WINDOW_SERVICE)).removeView(panel);}catch(Exception e){}panel=null;}};
 public IBinder onBind(Intent i){return null;}
 public void onCreate(){super.onCreate();NotificationManager n=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);n.createNotificationChannel(new NotificationChannel("volume","Volume control",NotificationManager.IMPORTANCE_LOW));startForeground(1,new Notification.Builder(this,"volume").setSmallIcon(android.R.drawable.ic_lock_silent_mode_off).setContentTitle("Fire Volume active").build());
 level=Math.max(0,Math.min(100,getSharedPreferences("volume",0).getInt("level",100)));
 try{effect=new LoudnessEnhancer(0);effect.setEnabled(true);apply();}catch(Exception e){android.util.Log.e("FireVolume","Audio effect unavailable",e);stopSelf();return;}
 preferences=getSharedPreferences("volume",0);
 preferences.registerOnSharedPreferenceChangeListener(tuningChanged);
 active=this;
 applyEqualizer();
 running=true;new Thread(()->{try{reader=new ProcessBuilder("logcat","-b","main","-v","brief","-T",new java.text.SimpleDateFormat("MM-dd HH:mm:ss.SSS",java.util.Locale.US).format(new java.util.Date()),"com_amazon_devicecont..:I","*:S").start();BufferedReader r=new BufferedReader(new InputStreamReader(reader.getInputStream()));String line;while(running&&(line=r.readLine())!=null){if(!line.contains("Received Key name:")||!line.contains("action: 0"))continue;int direction=line.contains("VOLUME_DOWN,")?-1:line.contains("VOLUME_UP,")?1:line.contains("VOLUME_MUTE,")?0:99;if(direction!=99)h.post(()->change(direction));}}catch(Exception e){android.util.Log.e("FireVolume","Input reader failed",e);}} ,"volume-keys").start();}
 void change(int direction){long now=SystemClock.elapsedRealtime();if(now-lastEvent<70)return;lastEvent=now;if(direction==0)muted=!muted;else{muted=false;int step=getSharedPreferences("volume",0).getInt("step",5);level=Math.max(0,Math.min(100,level+direction*step));}getSharedPreferences("volume",0).edit().putInt("level",level).apply();apply();show();}
 void apply(){int gain=AudioTuning.gain(getSharedPreferences("volume",0),muted?0:level);try{effect.setTargetGain(gain);android.util.Log.i("FireVolume","level="+level+" gain="+effect.getTargetGain()+" enabled="+effect.getEnabled()+" control="+effect.hasControl());}catch(Exception e){android.util.Log.e("FireVolume","Gain failed",e);}}
 void applyEqualizer(){
  int profile=AudioTuning.profile(preferences);
  if(profile==0){
   if(equalizer!=null){equalizer.release();equalizer=null;}
   eqStatus="Flat · EQ off";
   return;
  }
  try{
   if(equalizer==null)equalizer=new Equalizer(0,0);
   if(!equalizer.hasControl())throw new IllegalStateException("No EQ control");
   short count=equalizer.getNumberOfBands();
   if(count!=5)throw new IllegalStateException("Unsupported EQ bands");
   short[] range=equalizer.getBandLevelRange();
   int[] bands=AudioTuning.bands(preferences);
   int headroom=0;
   for(int band:bands)headroom=Math.max(headroom,band);
   StringBuilder applied=new StringBuilder();
   for(short i=0;i<count;i++){
    equalizer.setBandLevel(i,(short)AudioTuning.clamp((bands[i]-headroom)*100,range[0],range[1]));
    applied.append(" ").append(equalizer.getCenterFreq(i)/1000).append("Hz=").append(equalizer.getBandLevel(i));
   }
   if(equalizer.setEnabled(true)!=AudioEffect.SUCCESS||!equalizer.getEnabled())throw new IllegalStateException("EQ could not start");
   eqStatus=AudioTuning.PROFILES[profile]+" · EQ active";
   android.util.Log.i("FireVolume",eqStatus+applied);
  }catch(RuntimeException e){
   if(equalizer!=null){equalizer.release();equalizer=null;}
   eqStatus="EQ unavailable on this output";
   android.util.Log.w("FireVolume",eqStatus,e);
  }
 }
 void show(){if(panel==null){float d=getResources().getDisplayMetrics().density;int p=(int)(10*d);panel=new LinearLayout(this);panel.setOrientation(1);panel.setPadding(p,p,p,p);android.graphics.drawable.GradientDrawable bg=new android.graphics.drawable.GradientDrawable();bg.setColor(0xee202632);bg.setCornerRadius(p);panel.setBackground(bg);label=new TextView(this);label.setTextColor(Color.WHITE);label.setTextSize(14);panel.addView(label);bar=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);bar.setMax(100);panel.addView(bar,new LinearLayout.LayoutParams(-1,p));WindowManager.LayoutParams lp=new WindowManager.LayoutParams((int)(170*d),-2,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,PixelFormat.TRANSLUCENT);lp.gravity=Gravity.TOP|Gravity.RIGHT;lp.x=2*p;lp.y=2*p;try{((WindowManager)getSystemService(WINDOW_SERVICE)).addView(panel,lp);}catch(Exception e){panel=null;return;}}label.setText(muted?"Volume · Muted":"Volume · "+level);bar.setProgress(muted?0:level);h.removeCallbacks(hide);h.postDelayed(hide,1500);}
 public int onStartCommand(Intent i,int f,int id){return START_STICKY;}
 public void onDestroy(){if(active==this)active=null;if(preferences!=null)preferences.unregisterOnSharedPreferenceChangeListener(tuningChanged);if(equalizer!=null)equalizer.release();running=false;if(reader!=null)reader.destroy();h.removeCallbacksAndMessages(null);hide.run();if(effect!=null)effect.release();super.onDestroy();}
}
