package local.pitv;
public class AudioLevelBootReceiver extends android.content.BroadcastReceiver {
 public void onReceive(android.content.Context c,android.content.Intent i){if(android.content.Intent.ACTION_BOOT_COMPLETED.equals(i.getAction()))try{c.startActivity(new android.content.Intent(c,MainActivity.class).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK));}catch(Exception ignored){}}
}
