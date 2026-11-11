package local.pitv;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

/** Stream receiver for TV apps; Back returns to the calling application's task. */
public class ExternalPlayerActivity extends Activity {
 TvPlayer player;
 @Override public void onCreate(Bundle state){super.onCreate(state);getWindow().getDecorView().setSystemUiVisibility(5894);getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);open(getIntent());}
 void open(Intent intent){
  Uri uri=intent.getData();String scheme=uri==null?"":uri.getScheme();
  if(!"http".equalsIgnoreCase(scheme)&&!"https".equalsIgnoreCase(scheme)&&!"content".equalsIgnoreCase(scheme)){Toast.makeText(this,"Unsupported video address",Toast.LENGTH_LONG).show();finish();return;}
  String title=intent.getStringExtra("title");if(title==null||title.trim().isEmpty())title="Television";
  boolean live=uri.getPath()!=null&&uri.getPath().contains("/live/");
  player=new TvPlayer(this,title,uri.toString(),live,false,new TvPlayer.Host(){public void previous(){}public void next(){}public void recall(){}public void guide(){finish();}public void back(){finish();}public void completed(){}public void nextEpisode(){}});
  if(live){hideChannelControls(player);player.setSchedule("Programme details are available in the Television guide.");}
  setContentView(player);
 }
 void hideChannelControls(View view){if(view instanceof Button){String label=((Button)view).getText().toString();if(label.equals("Previous")||label.equals("Next")||label.equals("Last channel")||label.equals("Guide"))view.setVisibility(View.GONE);}if(view instanceof TextView&&((TextView)view).getText().toString().contains("Channel +/−"))((TextView)view).setText("OK controls  ·  Back returns to TiviMate");if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++)hideChannelControls(((ViewGroup)view).getChildAt(i));}
 @Override public boolean dispatchKeyEvent(KeyEvent event){if(player!=null&&player.live&&event.getAction()==KeyEvent.ACTION_DOWN&&(event.getKeyCode()==KeyEvent.KEYCODE_DPAD_UP||event.getKeyCode()==KeyEvent.KEYCODE_DPAD_DOWN)&&player.overlay.getVisibility()!=View.VISIBLE){player.showInfo();return true;}return super.dispatchKeyEvent(event);}
 @Override public void onBackPressed(){finish();}
 @Override protected void onPause(){if(player!=null)player.suspend();super.onPause();}
 @Override protected void onResume(){super.onResume();if(player!=null)player.resume();}
 @Override protected void onDestroy(){if(player!=null)player.handler.removeCallbacksAndMessages(null);super.onDestroy();}
}
