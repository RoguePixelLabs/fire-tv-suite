package local.pitv;
import android.app.*;import android.content.*;import android.media.*;import android.media.audiofx.*;import android.os.*;
/** Single post-mix processor. Player effects, master volume and media engines stay unchanged. */
public class AudioLevelService extends Service {
 static final int NOTIFICATION=4421;static volatile AudioLevelService instance;
 DynamicsProcessing effect;final Handler handler=new Handler();AudioManager audio;
 static SharedPreferences prefs(Context c){return c.getSharedPreferences("audio-leveling",0);}
 static String mode(Context c){String value=prefs(c).getString("mode","off");return AudioLevelProfiles.valid(value)?value:"off";}
 static void ensure(Context c){if(mode(c).equals("off")){c.stopService(new Intent(c,AudioLevelService.class));return;}try{c.startForegroundService(new Intent(c,AudioLevelService.class));}catch(Exception e){prefs(c).edit().putBoolean("active",false).putString("error",e.getClass().getSimpleName()).apply();}}
 static void select(Context c,String mode){if(!AudioLevelProfiles.valid(mode))throw new IllegalArgumentException("Unknown audio mode");prefs(c).edit().putString("mode",mode).apply();ensure(c);}
 final Runnable health=new Runnable(){public void run(){try{if(effect==null||!effect.getEnabled()||!effect.hasControl())apply();}catch(Exception e){release();apply();}handler.postDelayed(this,30000);}};
 final AudioDeviceCallback devices=new AudioDeviceCallback(){public void onAudioDevicesAdded(AudioDeviceInfo[] d){routeChanged();}public void onAudioDevicesRemoved(AudioDeviceInfo[] d){routeChanged();}};
 final BroadcastReceiver volume=new BroadcastReceiver(){public void onReceive(Context c,Intent i){if(i.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE",-1)==AudioManager.STREAM_MUSIC)apply();}};
 float volumeDb(){try{float value=audio.getStreamVolumeDb(AudioManager.STREAM_MUSIC,audio.getStreamVolume(AudioManager.STREAM_MUSIC),AudioDeviceInfo.TYPE_BUILTIN_SPEAKER);return Float.isFinite(value)?Math.max(-60,Math.min(0,value)):-60;}catch(Exception e){return 0;}}
 final Runnable reattach=()->{release();apply();};
 void routeChanged(){handler.removeCallbacks(reattach);handler.postDelayed(reattach,500);}
 public void onCreate(){super.onCreate();instance=this;audio=(AudioManager)getSystemService(AUDIO_SERVICE);
  NotificationChannel channel=new NotificationChannel("audio-leveling","Volume leveling",NotificationManager.IMPORTANCE_LOW);channel.setSound(null,null);channel.enableVibration(false);((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(channel);
  audio.registerAudioDeviceCallback(devices,handler);if(Build.VERSION.SDK_INT>=33)registerReceiver(volume,new IntentFilter("android.media.VOLUME_CHANGED_ACTION"),Context.RECEIVER_NOT_EXPORTED);else registerReceiver(volume,new IntentFilter("android.media.VOLUME_CHANGED_ACTION"));handler.postDelayed(health,30000);
 }
 Notification notification(){String current=mode(this);PendingIntent open=PendingIntent.getActivity(this,0,new Intent(this,AudioLevelSettingsActivity.class),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);return new Notification.Builder(this,"audio-leveling").setSmallIcon(android.R.drawable.ic_lock_silent_mode_off).setContentTitle("Volume leveling").setContentText(current.equals("night")?"Night · stronger leveling":"Normal · gentler volume changes").setContentIntent(open).setOngoing(true).setOnlyAlertOnce(true).build();}
 public int onStartCommand(Intent intent,int flags,int startId){startForeground(NOTIFICATION,notification());if(mode(this).equals("off")){stopSelf();return START_NOT_STICKY;}apply();return START_STICKY;}
 void apply(){if(mode(this).equals("off"))return;try{
  float reference=volumeDb();DynamicsProcessing.Config config=AudioLevelProfiles.config(mode(this),reference);prefs(this).edit().putFloat("reference_volume_db",reference).apply();
  if(effect==null){effect=new DynamicsProcessing(100,0,config);effect.setControlStatusListener((fx,control)->{if(!control)prefs(this).edit().putBoolean("active",false).putString("error","Another audio effect has control").apply();else handler.post(()->{if(effect!=null)apply();});});}
  else{effect.setMbcAllChannelsTo(config.getMbcByChannelIndex(0));effect.setLimiterAllChannelsTo(config.getLimiterByChannelIndex(0));}
  int status=effect.setEnabled(true);boolean active=status==AudioEffect.SUCCESS&&effect.hasControl()&&effect.getEnabled();prefs(this).edit().putBoolean("active",active).putString("error",active?"":"Output processor unavailable").apply();
 }catch(Exception e){prefs(this).edit().putBoolean("active",false).putString("error",e.getClass().getSimpleName()).apply();}}
 void release(){if(effect!=null){effect.release();effect=null;}prefs(this).edit().putBoolean("active",false).apply();}
 public void onDestroy(){handler.removeCallbacksAndMessages(null);if(audio!=null)audio.unregisterAudioDeviceCallback(devices);unregisterReceiver(volume);release();instance=null;stopForeground(STOP_FOREGROUND_REMOVE);super.onDestroy();}
 public IBinder onBind(Intent intent){return null;}
}
