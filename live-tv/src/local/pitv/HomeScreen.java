package local.pitv;

import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.widget.*;

/** Everyday home: one compact header, dominant reel, and two quiet side panels. */
final class HomeScreen {
 final MainActivity app; final LinearLayout nav; final View logo; final Button[] apps=new Button[4];
 final ImageButton settings; final RoundedVideoPanel video; final LinearLayout side;
 int dp(int n){return app.dp(n);}
 HomeScreen(MainActivity a){
  app=a;app.root=new LinearLayout(a);app.root.setOrientation(1);app.root.setPadding(dp(38),dp(18),dp(38),dp(18));
  app.root.setBackgroundColor(0xff080a10);a.setContentView(app.root);
  nav=new LinearLayout(a);nav.setGravity(Gravity.CENTER_VERTICAL);nav.setClipChildren(false);nav.setClipToPadding(false);app.root.addView(nav,new LinearLayout.LayoutParams(-1,dp(42)));
  LinearLayout brand=new LinearLayout(a);brand.setGravity(Gravity.CENTER_VERTICAL);brand.setPadding(dp(3),0,dp(4),0);brand.setContentDescription("Fire TV settings");identify(brand);brand.setFocusable(true);brand.setClickable(true);brand.setOnClickListener(v->a.settings());brand.setOnLongClickListener(v->{a.settings();return true;});brand.setOnFocusChangeListener((v,f)->brand.setBackground(panel(0x00000000,f?0xffe9edff:0x00000000,8)));logo=brand;
  ImageView image=new ImageView(a);image.setScaleType(ImageView.ScaleType.FIT_CENTER);image.setImageDrawable(new FireTvIcon());brand.addView(image,new LinearLayout.LayoutParams(dp(24),dp(28)));TextView name=a.text("Fire TV",13);name.setPadding(dp(6),0,0,0);brand.addView(name);nav.addView(brand,new LinearLayout.LayoutParams(dp(90),-1));
  LinearLayout dock=new LinearLayout(a);dock.setGravity(Gravity.CENTER_VERTICAL);dock.setPadding(dp(5),dp(4),dp(5),dp(4));dock.setBackground(panel(0xff242733,0x00000000,10));LinearLayout.LayoutParams dockParams=new LinearLayout.LayoutParams(0,dp(38),1);dockParams.setMargins(dp(12),0,dp(16),0);nav.addView(dock,dockParams);
  String[] labels={"Cinema","Live TV","YouTube","Open Game Stream"};int[] colors={0xff29374d,0xff2462c6,0xffdc2943,0xff218f85};
  Runnable[] actions={()->a.launch("local.ondemand"),()->a.iptv("home"),()->a.launch("com.amazon.firetv.youtube","com.google.android.youtube.tv","com.google.android.youtube"),()->a.launch("com.opengamestream.firetv","com.opengamestream.firetv.debug")};
  for(int i=0;i<apps.length;i++){final int color=colors[i];Button b=TvStyle.button(a,labels[i],actions[i]);identify(b);b.setTextSize(10);b.setSingleLine(true);b.setPadding(dp(6),0,dp(6),0);b.setCompoundDrawablePadding(dp(5));Drawable icon=new AppIcon(i);icon.setBounds(0,0,dp(14),dp(14));b.setCompoundDrawables(icon,null,null,null);b.setBackground(tile(color,false));b.setOnFocusChangeListener((v,f)->b.setBackground(tile(color,f)));apps[i]=b;LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(30),1);p.setMargins(dp(2),0,dp(2),0);dock.addView(b,p);}
  settings=new ImageButton(a);identify(settings);settings.setContentDescription("Settings");settings.setTag("home:settings");settings.setImageDrawable(new SettingsIcon());settings.setScaleType(ImageView.ScaleType.FIT_CENTER);settings.setPadding(dp(9),dp(9),dp(9),dp(9));settings.setFocusable(true);settings.setFocusableInTouchMode(true);settings.setBackground(panel(0xff1c1e28,0x00000000,9));settings.setOnFocusChangeListener((v,f)->settings.setBackground(panel(0xff1c1e28,f?0xffe9edff:0x00000000,9)));settings.setOnClickListener(v->a.settings());nav.addView(settings,new LinearLayout.LayoutParams(dp(34),dp(34)));
  LinearLayout content=new LinearLayout(a);LinearLayout.LayoutParams contentParams=new LinearLayout.LayoutParams(-1,0,1);contentParams.topMargin=dp(12);app.root.addView(content,contentParams);
  LinearLayout reel=new LinearLayout(a);reel.setOrientation(1);LinearLayout.LayoutParams reelParams=new LinearLayout.LayoutParams(0,-1,1);reelParams.rightMargin=dp(12);content.addView(reel,reelParams);
  app.promo=new VlcVideoView(a);app.promo.commercialMode=true;app.promo.setVolume(!a.getPreferences(0).getBoolean("promoSound",true));app.promo.setBackgroundColor(0xff050708);app.promo.setFocusable(false);app.promo.setFocusableInTouchMode(false);
  video=new RoundedVideoPanel(a);video.setMaskColor(0xff080a10);identify(video);video.setPadding(dp(2),dp(2),dp(2),dp(2));video.setBackgroundColor(0xff050708);video.addView(app.promo,new FrameLayout.LayoutParams(-1,-1,Gravity.CENTER));reel.addView(video,new LinearLayout.LayoutParams(-1,0,1));video.setFocusable(true);video.setClickable(true);video.setContentDescription("Latest trailers · OK for next clip");video.setOnFocusChangeListener((v,f)->video.setBorderColor(f?0xffe9edff:0xff242c39));video.setOnClickListener(v->a.skipPromo());video.setOnKeyListener((v,key,e)->{if(key==KeyEvent.KEYCODE_DPAD_CENTER||key==KeyEvent.KEYCODE_ENTER||key==KeyEvent.KEYCODE_BUTTON_A){if(e.getAction()==KeyEvent.ACTION_UP)video.performClick();return true;}return false;});
  app.promoTitle=a.text("",12);app.promoTitle.setSingleLine(true);app.promoTitle.setEllipsize(android.text.TextUtils.TruncateAt.END);app.promoTitle.setPadding(dp(12),dp(6),dp(12),dp(6));app.promoTitle.setBackgroundColor(0xb0080a10);app.promoTitle.setVisibility(View.GONE);video.addView(app.promoTitle,new FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM));
  app.promoStatus=a.text("Retro reel unavailable · select to retry",13);app.promoStatus.setGravity(Gravity.CENTER);app.promoStatus.setPadding(dp(20),dp(16),dp(20),dp(16));app.promoStatus.setBackgroundColor(0xe6080a10);app.promoStatus.setVisibility(View.GONE);video.addView(app.promoStatus,new FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM));
  app.weatherTicker=new HomeInfoTicker(a,true);identify(app.weatherTicker);app.weatherTicker.setBackgroundColor(Color.TRANSPARENT);app.weatherTicker.setTextSize(11);LinearLayout.LayoutParams forecast=new LinearLayout.LayoutParams(-1,dp(22));forecast.topMargin=dp(4);app.root.addView(app.weatherTicker,forecast);
  app.stockTicker=new HomeInfoTicker(a,false);app.stockTicker.setBackgroundColor(Color.TRANSPARENT);app.stockTicker.setTextSize(11);app.root.addView(app.stockTicker,new LinearLayout.LayoutParams(-1,dp(22)));
  side=new LinearLayout(a);side.setOrientation(1);content.addView(side,new LinearLayout.LayoutParams(dp(240),-1));
  app.movieTrivia=new MovieTriviaView(a);identify(app.movieTrivia);app.movieTrivia.setPadding(dp(12),dp(8),dp(12),dp(6));app.movieTrivia.question.setTextSize(16);app.movieTrivia.answer.setTextSize(13);app.movieTrivia.phase.setTextSize(10);app.movieTrivia.phase.setLayoutParams(new LinearLayout.LayoutParams(-1,dp(18)));app.movieTrivia.answer.setLayoutParams(new LinearLayout.LayoutParams(-1,dp(32)));app.movieTrivia.phase.setTextColor(0xffcbd2e5);app.movieTrivia.timer.setProgressTintList(android.content.res.ColorStateList.valueOf(0xffbac8ff));app.movieTrivia.setBackground(panel(0xff29243c,0x00000000,18));app.movieTrivia.setOnFocusChangeListener((v,f)->app.movieTrivia.setBackground(panel(0xff29243c,f?0xffe9edff:0x00000000,18)));side.addView(app.movieTrivia,new LinearLayout.LayoutParams(-1,dp(166)));
  LinearLayout spotlight=new LinearLayout(a);spotlight.setOrientation(1);spotlight.setPadding(dp(10),dp(8),dp(10),dp(4));spotlight.setBackground(panel(0xff1c1e28,0x00000000,18));LinearLayout.LayoutParams spotParams=new LinearLayout.LayoutParams(-1,0,1);spotParams.topMargin=dp(8);side.addView(spotlight,spotParams);TextView label=a.text("MOVIE SPOTLIGHT",10);label.setLetterSpacing(.13f);label.setTextColor(0xffcbd2e5);spotlight.addView(label,new LinearLayout.LayoutParams(-1,dp(20)));app.moviePhotos=new MoviePhotoReel(a);identify(app.moviePhotos);app.moviePhotos.caption.setTextSize(11);spotlight.addView(app.moviePhotos,new LinearLayout.LayoutParams(-1,0,1));
  wireFocus();if(a.active){app.movieTrivia.resume();app.moviePhotos.resume();app.weatherTicker.resume();app.stockTicker.resume();}apps[0].requestFocus();
 }
 void identify(View v){v.setId(View.generateViewId());}
 GradientDrawable panel(int color,int border,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));d.setStroke(dp(1),border);return d;}
 GradientDrawable tile(int color,boolean focused){GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{color,darken(color)});d.setCornerRadius(dp(6));d.setStroke(dp(1),focused?0xffedf0ff:0x00000000);return d;}
 int darken(int c){return Color.rgb((int)(Color.red(c)*.75),(int)(Color.green(c)*.75),(int)(Color.blue(c)*.75));}
 void wireFocus(){
  logo.setNextFocusRightId(apps[0].getId());logo.setNextFocusDownId(video.getId());logo.setNextFocusUpId(logo.getId());
  for(int i=0;i<apps.length;i++){apps[i].setNextFocusLeftId(i==0?logo.getId():apps[i-1].getId());apps[i].setNextFocusRightId(i==apps.length-1?settings.getId():apps[i+1].getId());apps[i].setNextFocusUpId(apps[i].getId());apps[i].setNextFocusDownId(i<4?video.getId():app.movieTrivia.getId());}
  settings.setNextFocusLeftId(apps[apps.length-1].getId());settings.setNextFocusRightId(settings.getId());settings.setNextFocusDownId(app.movieTrivia.getId());settings.setNextFocusUpId(settings.getId());
  video.setNextFocusUpId(apps[1].getId());video.setNextFocusLeftId(video.getId());video.setNextFocusRightId(app.movieTrivia.getId());video.setNextFocusDownId(app.weatherTicker.getId());
  app.movieTrivia.setNextFocusUpId(apps[apps.length-1].getId());app.movieTrivia.setNextFocusLeftId(video.getId());app.movieTrivia.setNextFocusRightId(app.movieTrivia.getId());app.movieTrivia.setNextFocusDownId(app.moviePhotos.getId());
  app.moviePhotos.setNextFocusUpId(app.movieTrivia.getId());app.moviePhotos.setNextFocusDownId(app.moviePhotos.getId());
  app.weatherTicker.setNextFocusUpId(video.getId());app.weatherTicker.setNextFocusDownId(app.weatherTicker.getId());app.weatherTicker.setNextFocusLeftId(app.weatherTicker.getId());app.weatherTicker.setNextFocusRightId(app.moviePhotos.getId());
 }
 static final class SettingsIcon extends Drawable {
  final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
  public void draw(Canvas c){c.save();c.translate(getBounds().left,getBounds().top);c.scale(getBounds().width()/24f,getBounds().height()/24f);p.setColor(0xffedf0f8);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.8f);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);Path gear=new Path();for(int i=0;i<32;i++){double angle=i*Math.PI/16-Math.PI/2;float r=i%4==0||i%4==3?10:8;float x=12+(float)Math.cos(angle)*r,y=12+(float)Math.sin(angle)*r;if(i==0)gear.moveTo(x,y);else gear.lineTo(x,y);}gear.close();c.drawPath(gear,p);c.drawCircle(12,12,3.3f,p);c.restore();}
  public void setAlpha(int alpha){p.setAlpha(alpha);}public void setColorFilter(ColorFilter f){p.setColorFilter(f);}public int getOpacity(){return PixelFormat.TRANSLUCENT;}
 }
 final class AppIcon extends Drawable {
  final int kind;final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);AppIcon(int k){kind=k;}
  public void draw(Canvas c){c.save();c.translate(getBounds().left,getBounds().top);c.scale(getBounds().width()/24f,getBounds().height()/24f);p.setColor(0xfff7f7fa);p.setStrokeWidth(1.7f);p.setStrokeJoin(Paint.Join.ROUND);p.setStrokeCap(Paint.Cap.ROUND);p.setStyle(Paint.Style.STROKE);
   if(kind==0||kind==1){c.drawRoundRect(3,6,21,19,2,2,p);if(kind==0){c.drawLine(3,10,21,10,p);c.drawLine(7,6,10,10,p);c.drawLine(14,6,17,10,p);}else{c.drawLine(8,2,12,6,p);c.drawLine(16,2,12,6,p);c.drawLine(9,22,15,22,p);}}
   else if(kind==2){Path t=new Path();t.moveTo(7,3);t.lineTo(21,12);t.lineTo(7,21);t.close();c.drawPath(t,p);}
   else if(kind==3){c.drawRoundRect(2,6,22,19,5,5,p);c.drawLine(5,12,11,12,p);c.drawLine(8,9,8,15,p);c.drawCircle(16,11,.7f,p);c.drawCircle(19,14,.7f,p);}
   else{for(int i=0;i<3;i++){int y=5+i*7,x=i==1?15:8;c.drawLine(2,y,x-2,y,p);c.drawLine(x+2,y,22,y,p);c.drawCircle(x,y,2,p);}}
   c.restore();}
  public void setAlpha(int alpha){}public void setColorFilter(ColorFilter filter){}public int getOpacity(){return PixelFormat.TRANSLUCENT;}
 }
}
