package local.pitv;
import android.app.*;import android.content.*;import android.view.*;import android.widget.*;
/** Explicitly focused controls; no power action occurs until the second confirmation. */
final class RemotePowerMenu {
 static void show(Context context){
  AlertDialog dialog=new AlertDialog.Builder(context).create();LinearLayout panel=panel(context,"Power options");
  add(panel,"Power off",()->{dialog.dismiss();confirm(context,false);});add(panel,"Restart",()->{dialog.dismiss();confirm(context,true);});Button cancel=add(panel,"Cancel",()->dialog.dismiss());
  dialog.setView(panel);dialog.setOnShowListener(d->{TvStyle.dialog(dialog);cancel.setFocusableInTouchMode(true);cancel.requestFocus();});dialog.show();
 }
 static void confirm(Context context,boolean restart){
  AlertDialog dialog=new AlertDialog.Builder(context).create();LinearLayout panel=panel(context,restart?"Restart the Pi?":"Power off the Pi?");TextView note=TvStyle.text(context,restart?"The Pi will restart.":"Wait for shutdown to finish before unplugging power.",15);panel.addView(note);
  add(panel,restart?"Confirm restart":"Confirm power off",()->{dialog.dismiss();RemoteButtonsService.systemPowerAction(context,restart);});Button cancel=add(panel,"Cancel",()->dialog.dismiss());dialog.setView(panel);dialog.setOnShowListener(d->{TvStyle.dialog(dialog);cancel.setFocusableInTouchMode(true);cancel.requestFocus();});dialog.show();
 }
 static LinearLayout panel(Context context,String title){LinearLayout panel=new LinearLayout(context);panel.setOrientation(1);int pad=TvStyle.dp(context,20);panel.setPadding(pad,pad,pad,pad);panel.addView(TvStyle.text(context,title,24));return panel;}
 static Button add(LinearLayout parent,String label,Runnable action){Button b=TvStyle.button(parent.getContext(),label,action);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,TvStyle.dp(parent.getContext(),42));p.topMargin=TvStyle.dp(parent.getContext(),10);parent.addView(b,p);return b;}
}
