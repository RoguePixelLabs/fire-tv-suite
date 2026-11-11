package local.firevolume;
import android.content.*;
public class Boot extends BroadcastReceiver { public void onReceive(Context c,Intent i){if((Intent.ACTION_BOOT_COMPLETED.equals(i.getAction())||Intent.ACTION_MY_PACKAGE_REPLACED.equals(i.getAction()))&&c.getSharedPreferences("volume",0).getBoolean("enabled",false))c.startForegroundService(new Intent(c,VolumeService.class));} }
