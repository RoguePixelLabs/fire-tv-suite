package local.pitv;
import android.content.*;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
final class TvStyle {
 static final int BG=0xff101525,PANEL=0xff1a2635,INK=0xfff4f1e8,AMBER=0xffffb347,CYAN=0xff39d9ff,SILVER=0xffb8c2cc,ORANGE=0xffff6534;
 static boolean motion(Context c){return !c.getSharedPreferences("tv",0).getBoolean("reduceMotion",false);}
 static int dp(Context c,int n){return (int)(n*c.getResources().getDisplayMetrics().density);}
 static GradientDrawable shape(Context c,int fill,int border){GradientDrawable d=new GradientDrawable();d.setColor(fill);d.setCornerRadius(dp(c,20));d.setStroke(dp(c,2),border);return d;}
 static void focus(View v){v.setFocusable(true);v.setBackground(shape(v.getContext(),PANEL,0xff52616e));v.setOnFocusChangeListener((w,f)->{w.setBackground(shape(w.getContext(),f?0xff173c4c:PANEL,f?CYAN:0xff52616e));if(motion(w.getContext()))w.animate().scaleX(1f).scaleY(1f).setDuration(130).start();});}
 static TextView text(Context c,String s,int size){TextView t=new TextView(c);t.setText(s);t.setTextSize(size);t.setTextColor(INK);return t;}
 static Button button(Context c,String s,Runnable action){Button b=new Button(c);b.setText(s);b.setTextColor(INK);b.setTextSize(16);b.setAllCaps(false);b.setGravity(Gravity.CENTER);b.setPadding(dp(c,8),dp(c,1),dp(c,8),dp(c,1));b.setMinHeight(0);b.setMinimumHeight(0);b.setMinWidth(0);b.setMinimumWidth(0);b.setIncludeFontPadding(false);b.setStateListAnimator(null);focus(b);b.setOnClickListener(v->action.run());return b;}
 static void dialog(android.app.AlertDialog d){if(d.getWindow()!=null)d.getWindow().setBackgroundDrawable(shape(d.getContext(),BG,0xff52616e));for(int id:new int[]{-1,-2,-3}){Button b=d.getButton(id);if(b!=null)b.setTextColor(AMBER);}}
 static void enter(View v){if(motion(v.getContext())){v.setAlpha(0);v.animate().alpha(1).setDuration(220).start();}}
}
