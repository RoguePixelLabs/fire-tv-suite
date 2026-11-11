package local.pitv;
import android.accessibilityservice.*;import android.view.*;import android.view.accessibility.*;import android.content.*;
/** Firestick volume indicator. Native HOME selection is managed by Fire OS. */
public class RemoteButtonsService extends AccessibilityService {
 static RemoteButtonsService connected;final android.os.Handler handler=new android.os.Handler();long lastHome;
 final Runnable home=()->openHome();
 android.widget.LinearLayout volumePanel;android.widget.TextView volumeLabel;android.widget.ProgressBar volumeBar;
 final Runnable hideVolume=()->{if(volumePanel!=null){try{((WindowManager)getSystemService(WINDOW_SERVICE)).removeView(volumePanel);}catch(Exception ignored){}volumePanel=null;}};
 final Runnable volumeUpdate=()->showVolume();
 void showVolume(){
  android.media.AudioManager audio=(android.media.AudioManager)getSystemService(AUDIO_SERVICE);
  int max=audio.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC),value=audio.getStreamVolume(android.media.AudioManager.STREAM_MUSIC);
  boolean mute=audio.isStreamMute(android.media.AudioManager.STREAM_MUSIC);int percent=max>0?Math.round(100f*value/max):0;
  if(volumePanel==null){
   volumePanel=new android.widget.LinearLayout(this);volumePanel.setOrientation(1);int pad=(int)(10*getResources().getDisplayMetrics().density);volumePanel.setPadding(pad,pad,pad,pad);
   android.graphics.drawable.GradientDrawable bg=new android.graphics.drawable.GradientDrawable();bg.setColor(0xee202632);bg.setCornerRadius(pad);volumePanel.setBackground(bg);
   volumeLabel=new android.widget.TextView(this);volumeLabel.setTextColor(android.graphics.Color.WHITE);volumeLabel.setTextSize(13);volumePanel.addView(volumeLabel);
   volumeBar=new android.widget.ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);volumeBar.setMax(100);volumePanel.addView(volumeBar,new android.widget.LinearLayout.LayoutParams(-1,pad));
   WindowManager.LayoutParams lp=new WindowManager.LayoutParams((int)(180*getResources().getDisplayMetrics().density),-2,WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,android.graphics.PixelFormat.TRANSLUCENT);lp.gravity=Gravity.TOP|Gravity.RIGHT;lp.x=pad*2;lp.y=pad*2;
   try{((WindowManager)getSystemService(WINDOW_SERVICE)).addView(volumePanel,lp);}catch(Exception e){volumePanel=null;android.widget.Toast.makeText(this,"Volume · "+percent+"%",0).show();return;}
  }
  volumeLabel.setText(mute?"Volume · Muted":"Volume · "+percent+"%");volumeBar.setProgress(mute?0:percent);handler.removeCallbacks(hideVolume);handler.postDelayed(hideVolume,1800);
 }
 int lastVolume=-1;boolean lastMute;
 final Runnable audioWatch=new Runnable(){public void run(){android.media.AudioManager a=(android.media.AudioManager)getSystemService(AUDIO_SERVICE);int value=a.getStreamVolume(android.media.AudioManager.STREAM_MUSIC);boolean mute=a.isStreamMute(android.media.AudioManager.STREAM_MUSIC);if(lastVolume>=0&&(value!=lastVolume||mute!=lastMute))showVolume();lastVolume=value;lastMute=mute;handler.postDelayed(this,250);}};
 @Override protected void onServiceConnected(){super.onServiceConnected();connected=this;AccessibilityServiceInfo info=getServiceInfo();info.flags|=AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS;setServiceInfo(info);handler.removeCallbacks(audioWatch);handler.post(audioWatch);}
 @Override public void onDestroy(){handler.removeCallbacksAndMessages(null);hideVolume.run();connected=null;super.onDestroy();}
 void openHome(){long now=android.os.SystemClock.elapsedRealtime();if(now-lastHome<800)return;lastHome=now;try{startActivity(new Intent(this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP));}catch(Exception ignored){}}
 public void onAccessibilityEvent(AccessibilityEvent e){}
 protected boolean onKeyEvent(KeyEvent e){return false;}
 public void onInterrupt(){}
 static void reportVisibleApp(String pkg){}
 static void powerMenu(Context c){if(connected!=null)connected.handler.removeCallbacks(connected.home);try{c.startActivity(new Intent().setComponent(new ComponentName("com.amazon.tv.launcher","com.amazon.tv.launcher.ui.MainSettingsActivity")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));}catch(Exception e){c.startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));}}
 static void systemPowerAction(Context c,boolean restart){powerMenu(c);}
}
