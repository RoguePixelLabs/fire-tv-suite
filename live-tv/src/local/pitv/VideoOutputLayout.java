package local.pitv;
import android.view.*;import android.widget.*;import android.hardware.display.DisplayManager;import android.content.Context;import android.os.Handler;
/** Fit the source into the actual window, never substitute the TV panel size for a guide preview. */
final class VideoOutputLayout implements DisplayManager.DisplayListener {
 final View owner;final Runnable changed;final DisplayManager manager;
 VideoOutputLayout(View v,Runnable r){owner=v;changed=r;manager=(DisplayManager)v.getContext().getSystemService(Context.DISPLAY_SERVICE);}
 void start(){if(manager!=null)manager.registerDisplayListener(this,new Handler(android.os.Looper.getMainLooper()));changed.run();}
 void stop(){if(manager!=null)manager.unregisterDisplayListener(this);}
 public void onDisplayAdded(int id){changed.run();}public void onDisplayRemoved(int id){changed.run();}public void onDisplayChanged(int id){changed.run();}
 static int[] fit(int windowW,int windowH,int videoW,int videoH,int sarN,int sarD){
  if(windowW<=0||windowH<=0)return new int[]{0,0};
  if(videoW<=0||videoH<=0)return new int[]{windowW,windowH};
  double ratio=videoW/(double)videoH*(sarN>0&&sarD>0?sarN/(double)sarD:1);
  int w=windowW,h=(int)Math.round(w/ratio);
  if(h>windowH){h=windowH;w=(int)Math.round(h*ratio);}
  return new int[]{Math.max(1,Math.min(windowW,w)),Math.max(1,Math.min(windowH,h))};
 }
 static void size(SurfaceView view,int w,int h){FrameLayout.LayoutParams p=(FrameLayout.LayoutParams)view.getLayoutParams();if(p.width!=w||p.height!=h)view.setLayoutParams(new FrameLayout.LayoutParams(w,h,Gravity.CENTER));}
 String describe(){Display d=owner.getDisplay();String mode="Display unavailable";if(d!=null){Display.Mode m=d.getMode();mode="TV output "+m.getPhysicalWidth()+" × "+m.getPhysicalHeight()+" · "+Math.round(m.getRefreshRate())+" Hz";}return mode+"\nPlayer window "+owner.getWidth()+" × "+owner.getHeight()+" · fit, original proportions";}
}
