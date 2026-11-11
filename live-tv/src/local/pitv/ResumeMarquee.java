package local.pitv;
import android.content.*;import android.graphics.*;import android.widget.TextView;

/** A visible-only ticker whose position survives navigation and process recreation. */
class ResumeMarquee extends TextView {
 final android.content.SharedPreferences scrollPrefs;final String scrollKey;
 static final class Run {String text;float x;int color;Typeface face;}
 final java.util.ArrayList<Run> runs=new java.util.ArrayList<>();
 CharSequence cachedText;float cachedSize,cachedScale,cachedSpacing,textWidth;int cachedColor;Typeface cachedFace;
 void cacheRuns(CharSequence text,Paint paint){
  Typeface face=paint.getTypeface();int color=getCurrentTextColor();
  if(text==cachedText&&cachedSize==paint.getTextSize()&&cachedScale==paint.getTextScaleX()&&cachedSpacing==getLetterSpacing()&&cachedColor==color&&cachedFace==face)return;
  cachedText=text;cachedSize=paint.getTextSize();cachedScale=paint.getTextScaleX();cachedSpacing=getLetterSpacing();cachedColor=color;cachedFace=face;runs.clear();textWidth=0;
  android.text.Spanned spans=text instanceof android.text.Spanned?(android.text.Spanned)text:null;
  for(int at=0;at<text.length();){
   int end=spans==null?text.length():spans.nextSpanTransition(at,text.length(),android.text.style.CharacterStyle.class);
   Run run=new Run();run.color=color;run.face=face;run.x=textWidth;run.text=text.subSequence(at,end).toString();
   if(spans!=null){for(android.text.style.ForegroundColorSpan span:spans.getSpans(at,end,android.text.style.ForegroundColorSpan.class))run.color=span.getForegroundColor();for(android.text.style.StyleSpan span:spans.getSpans(at,end,android.text.style.StyleSpan.class))run.face=Typeface.create(face,span.getStyle());}
   paint.setTypeface(run.face);textWidth+=paint.measureText(run.text);runs.add(run);at=end;
  }
  paint.setTypeface(face);paint.setColor(color);
 }
 void drawRuns(Canvas canvas,float x,float baseline){Paint paint=getPaint();for(Run run:runs){paint.setColor(run.color);paint.setTypeface(run.face);canvas.drawText(run.text,x+run.x,baseline,paint);}paint.setColor(cachedColor);paint.setTypeface(cachedFace);}
 @Override protected void onTextChanged(CharSequence text,int start,int before,int count){super.onTextChanged(text,start,before,count);cachedText=null;}
 float scrollDp;long lastFrame,lastSave;boolean scrolling;
 ResumeMarquee(Context context,String key){super(context);scrollPrefs=context.getSharedPreferences("display-positions",0);scrollKey=key;scrollDp=Math.max(0,scrollPrefs.getFloat(key,0));}
 void saveScroll(){scrollPrefs.edit().putFloat(scrollKey,scrollDp).apply();lastSave=android.os.SystemClock.uptimeMillis();}
 void setScrolling(boolean value){if(!value)saveScroll();scrolling=value;lastFrame=0;if(value)invalidate();}
 @Override protected void onDraw(Canvas canvas){
  float density=getResources().getDisplayMetrics().density,width=getWidth()-getPaddingLeft()-getPaddingRight();if(width<=0)return;
  CharSequence text=getText();Paint paint=getPaint();paint.setColor(getCurrentTextColor());cacheRuns(text,paint);float gap=TvStyle.dp(getContext(),60),cycle=textWidth+gap;
  boolean animate=scrolling&&isShown()&&getWindowVisibility()==VISIBLE&&textWidth>width;long now=android.os.SystemClock.uptimeMillis();
  if(animate&&lastFrame!=0)scrollDp+=Math.min(100,Math.max(0,now-lastFrame))*24/1000f;
  lastFrame=animate?now:0;if(cycle>0)scrollDp=(scrollDp*density%cycle)/density;
  float offset=textWidth>width?scrollDp*density:0;Paint.FontMetrics metrics=paint.getFontMetrics();float baseline=getPaddingTop()+(getHeight()-getPaddingTop()-getPaddingBottom()-metrics.bottom-metrics.top)/2;
  canvas.save();canvas.clipRect(getPaddingLeft(),getPaddingTop(),getWidth()-getPaddingRight(),getHeight()-getPaddingBottom());
  drawRuns(canvas,getPaddingLeft()-offset,baseline);if(textWidth>width)drawRuns(canvas,getPaddingLeft()-offset+cycle,baseline);canvas.restore();
  if(animate){if(now-lastSave>=5000)saveScroll();postInvalidateDelayed(33);}
 }
 @Override protected void onWindowVisibilityChanged(int visibility){super.onWindowVisibilityChanged(visibility);lastFrame=0;if(scrollPrefs!=null&&visibility!=VISIBLE)saveScroll();if(visibility==VISIBLE)invalidate();}
 @Override protected void onDetachedFromWindow(){saveScroll();lastFrame=0;super.onDetachedFromWindow();}
}
