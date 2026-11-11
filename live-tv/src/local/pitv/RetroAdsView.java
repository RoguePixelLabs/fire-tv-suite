package local.pitv;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** Offline print ads, scrolling at a constant speed without taking remote focus. */
public final class RetroAdsView extends View {
 private final Bitmap[] pages=new Bitmap[70];
 private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
 private float offset=0;
 private long previous=0,lastSave=0;
 private boolean running=true;
 public RetroAdsView(Context context){
  super(context);offset=context.getSharedPreferences("display-positions",0).getFloat("gameAdsDp",0)*getResources().getDisplayMetrics().density;setFocusable(false);setContentDescription("Retro game advertisements and Game Informer magazine covers");
  for(int i=0;i<pages.length;i++)try{
   BitmapFactory.Options opts=new BitmapFactory.Options();opts.inJustDecodeBounds=true;
   try(java.io.InputStream in=context.getAssets().open("game-ads/"+i+".jpg")){BitmapFactory.decodeStream(in,null,opts);}
   opts.inSampleSize=1;while(opts.outWidth/opts.inSampleSize>360)opts.inSampleSize*=2;opts.inJustDecodeBounds=false;
   try(java.io.InputStream in=context.getAssets().open("game-ads/"+i+".jpg")){pages[i]=BitmapFactory.decodeStream(in,null,opts);}
  }catch(Exception ignored){}
 }
 void savePosition(){getContext().getSharedPreferences("display-positions",0).edit().putFloat("gameAdsDp",offset/getResources().getDisplayMetrics().density).apply();lastSave=android.os.SystemClock.uptimeMillis();}
 public void setRunning(boolean value){if(!value)savePosition();running=value;previous=0;if(value)invalidate();}
 protected void onDraw(Canvas canvas){
  super.onDraw(canvas);float width=getWidth(),gap=TvStyle.dp(getContext(),16),total=0;
  for(Bitmap b:pages)if(b!=null)total+=width*b.getHeight()/b.getWidth()+gap;
  if(total==0||width==0)return;offset%=total;
  long now=android.os.SystemClock.uptimeMillis();
  if(running&&previous!=0)offset=(offset+Math.min(50,now-previous)*getResources().getDisplayMetrics().density*12/1000f)%total;
  previous=now;if(now-lastSave>=5000)savePosition();
  for(float start=-offset;start<getHeight();start+=total){float y=start;
   for(Bitmap b:pages)if(b!=null){float h=width*b.getHeight()/b.getWidth();if(y+h>=0&&y<getHeight())canvas.drawBitmap(b,null,new RectF(0,y,width,y+h),paint);y+=h+gap;}
  }
  if(running&&isShown())postInvalidateOnAnimation();
 }
 protected void onWindowVisibilityChanged(int visibility){super.onWindowVisibilityChanged(visibility);if(visibility!=VISIBLE&&paint!=null)savePosition();previous=0;if(visibility==VISIBLE&&running)invalidate();}
 protected void onDetachedFromWindow(){savePosition();previous=0;super.onDetachedFromWindow();}
}
