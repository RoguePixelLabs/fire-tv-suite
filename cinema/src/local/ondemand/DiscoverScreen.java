package local.ondemand;
import android.view.*;import android.widget.*;import android.graphics.Color;import android.graphics.drawable.GradientDrawable;import org.json.*;import java.util.*;import java.util.concurrent.*;
/** Collapsible navigation, a focus-driven feature panel and dense horizontal shelves. */
final class DiscoverScreen {
 static final boolean PHONE=false;
 final MainActivity a;final int token;final List<View> recommended=new ArrayList<>(),recent=new ArrayList<>(),shows=new ArrayList<>();Button[] tabs;LinearLayout nav;View contentFocus;LinearLayout movies,history,series;String lastPoster="";int lastTab;boolean enterWhenReady,closed;final ExecutorService previews=Executors.newSingleThreadExecutor();Future<?> pending;Runnable previewDelay;int previewVersion;JSONObject featured;String featuredType="movie";FrameLayout hero,art;TextView title,description,facts,kind;Button play,info,save,rate;ScrollView scroll;JSONArray movieData=new JSONArray(),seriesData=new JSONArray();
 DiscoverScreen(MainActivity app){a=app;a.screen("Cinema","");a.discover=this;token=a.generation;lastPoster=a.homePoster;show();}
 boolean active(){return !closed&&token==a.generation&&!a.isDestroyed();}
 int cardWidth(){return a.dp(PHONE?112:108);}
 int cardHeight(){return a.dp(PHONE?194:179);}
 void show(){
 a.page.removeAllViews();
 a.page.setOrientation(LinearLayout.HORIZONTAL);
 a.page.setPadding(a.dp(8),a.dp(8),a.dp(18),0);
 a.page.setBackgroundColor(0xff080808);
 nav=new LinearLayout(a);
 nav.setOrientation(LinearLayout.VERTICAL);
 nav.setGravity(Gravity.CENTER_VERTICAL);
 nav.setTag("home:navigation");
 a.page.addView(nav,new LinearLayout.LayoutParams(a.dp(46),-1));
 String[] labels={"Home","Movies","Series","Discover","My Cinema","Search","Settings"};
 String[] icons={"⌂","▣","▤","◈","☆","⌕","⚙"};
 Runnable[] navActions={this::returnToContent,()->a.go(()->a.browse("movie")),()->a.go(()->a.browse("series")),()->a.go(()->new DiscoverBrowser(a)),()->a.go(()->CinemaLibrary.hub(a)),a::openSearch,a::settings};
 tabs=new Button[labels.length];
 for(int i=0;i<labels.length;i++){
  final int index=i;
  Button button=a.button(icons[i],navActions[i]);
  tabs[i]=button;
  button.setTag("nav:"+labels[i]);
  button.setContentDescription(labels[i]);
  button.setTextSize(14);
  button.setGravity(Gravity.CENTER);
  button.setPadding(a.dp(4),0,a.dp(4),0);
  nav.addView(button,new LinearLayout.LayoutParams(-1,a.dp(38)));
  button.setOnFocusChangeListener((view,focused)->{
   button.setTextColor(focused?Color.BLACK:Color.WHITE);
   button.setBackground(a.shape(focused?Color.WHITE:0xff151515,0,8));
   if(focused){lastTab=index;updateSidebar(labels,icons,true);}
   else nav.post(()->{if(active()&&!nav.hasFocus())updateSidebar(labels,icons,false);});
  });
  button.setOnKeyListener((view,key,event)->{
   if(key!=21&&key!=22&&key!=19&&key!=20)return false;
   if(event.getAction()==KeyEvent.ACTION_DOWN){
    if(key==22)returnToContent();
    else if(key==19)tabs[Math.max(0,index-1)].requestFocus();
    else if(key==20)tabs[Math.min(tabs.length-1,index+1)].requestFocus();
   }
   return true;
  });
 }
 scroll=new ScrollView(a);
 scroll.setTag("home:scroll");
 scroll.setFillViewport(true);
 scroll.setVerticalScrollBarEnabled(false);
 a.rows=new LinearLayout(a);
 a.rows.setOrientation(LinearLayout.VERTICAL);
 scroll.addView(a.rows);
 LinearLayout.LayoutParams contentSize=new LinearLayout.LayoutParams(0,-1,1);
 contentSize.leftMargin=a.dp(10);
 a.page.addView(scroll,contentSize);
 hero=new FrameLayout(a);hero.setTag("home:hero");hero.setBackgroundColor(0xff141414);a.rows.addView(hero,new LinearLayout.LayoutParams(-1,a.dp(PHONE?410:245)));
 art=new FrameLayout(a);hero.addView(art,new FrameLayout.LayoutParams(-1,-1));
 View shade=new View(a);shade.setBackground(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,new int[]{0xff080808,0xe0080808,0x90080808,0x00080808}));hero.addView(shade,new FrameLayout.LayoutParams(-1,-1));View floor=new View(a);floor.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0x00080808,0x00080808,0xff080808}));hero.addView(floor,new FrameLayout.LayoutParams(-1,-1));
 LinearLayout copy=new LinearLayout(a);copy.setOrientation(1);copy.setPadding(a.dp(PHONE?14:18),a.dp(PHONE?54:14),a.dp(14),a.dp(8));
 kind=a.text("FEATURED ON CINEMA",10);kind.setTextColor(0xffd7d7d7);kind.setLetterSpacing(.16f);copy.addView(kind);
 title=a.text("Find your next favorite",PHONE?27:29);title.setTag("home:feature-title");title.setTypeface(null,1);title.setMaxLines(2);title.setShadowLayer(4,0,1,Color.BLACK);LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(-1,-2);tp.topMargin=a.dp(6);copy.addView(title,tp);
 facts=a.text("",11);facts.setTextColor(0xffcccccc);facts.setSingleLine(true);facts.setEllipsize(android.text.TextUtils.TruncateAt.END);copy.addView(facts);
 description=a.text("Browse movies and series, save your favorites, and pick up your recently watched titles.",PHONE?13:12);description.setTag("home:feature-description");description.setMaxLines(2);description.setEllipsize(android.text.TextUtils.TruncateAt.END);description.setLineSpacing(a.dp(2),1);LinearLayout.LayoutParams dp=new LinearLayout.LayoutParams(-1,-2);dp.topMargin=a.dp(7);dp.bottomMargin=a.dp(10);copy.addView(description,dp);
 LinearLayout actions=new LinearLayout(a);play=action("▶ Play",()->openFeature());play.setTag("home:play");info=action("More info",()->{if(featured!=null)CinemaLibrary.information(a,featured,featuredType);});info.setTag("home:info");actions.addView(play,new LinearLayout.LayoutParams(a.dp(76),a.dp(27)));LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(a.dp(88),a.dp(27));ip.leftMargin=a.dp(8);actions.addView(info,ip);copy.addView(actions);
 LinearLayout choices=new LinearLayout(a);save=action("+ My List",()->{if(featured!=null){CinemaLibrary.toggle(a,featured,featuredType);updateActions();}});save.setTag("home:save");rate=action("Rate",()->{if(featured!=null)CinemaLibrary.ratingMenu(a,featured,featuredType,this::refreshTastes);});rate.setTag("home:rate");choices.addView(save,new LinearLayout.LayoutParams(a.dp(90),a.dp(23)));choices.addView(rate,new LinearLayout.LayoutParams(a.dp(57),a.dp(23)));copy.addView(choices);
 FrameLayout.LayoutParams cp=new FrameLayout.LayoutParams(PHONE?-1:a.dp(440),-1);hero.addView(copy,cp);
 movies=shelf("Recommended movies");movies.addView(a.text("Loading movies…",12));history=shelf("Recently watched");fillRecent();series=shelf("Popular series");series.addView(a.text("Loading series…",12));
 wireHero();if(a.restoreFocus.isEmpty())play.requestFocus();else restore(false);
 loadCatalog("movie");loadCatalog("series");
 a.migrateHistory(()->{if(active()){fillRecent();restore(true);}});
 }
 Button action(String label,Runnable run){Button b=a.button(label,run);b.setTextSize(12);b.setPadding(a.dp(8),0,a.dp(8),0);b.setMinHeight(0);b.setMinimumHeight(0);b.setTextColor(Color.WHITE);b.setBackground(a.shape(0xff333333,0,4));b.setOnFocusChangeListener((v,f)->{b.setTextColor(f?Color.BLACK:Color.WHITE);b.setBackground(a.shape(f?Color.WHITE:0xff333333,0,4));if(f)revealPreview();});return b;}
 void updateSidebar(String[] labels,String[] icons,boolean expanded){
  int width=a.dp(expanded?130:46);
  if(nav.getLayoutParams().width==width)return;
  nav.getLayoutParams().width=width;
  nav.requestLayout();
  for(int i=0;i<tabs.length;i++){
   tabs[i].setText(expanded?icons[i]+"  "+labels[i]:icons[i]);
   tabs[i].setGravity(expanded?Gravity.CENTER_VERTICAL|Gravity.LEFT:Gravity.CENTER);
  }
 }
 void enterSidebar(View from){
  contentFocus=from;
  tabs[lastTab].requestFocus();
 }
 void returnToContent(){
  View target=contentFocus;
  if(target==null||!target.isAttachedToWindow())target=lastPoster.isEmpty()?null:a.page.findViewWithTag(lastPoster);
  if(target==null)target=recommended.isEmpty()?play:recommended.get(0);
  target.requestFocus();
 }
 void loadCatalog(String type){final Api api=a.api;a.worker.submit(()->{try{JSONArray data=api.get(Api.META+"/catalog/"+type+"/top.json",true).optJSONArray("metas");a.clock.post(()->{if(!active())return;if(type.equals("movie")){movieData=data;fill(movies,recommended,CinemaLibrary.ranked(a,data,type),"recommended",type);if(featured==null&&!recommended.isEmpty()){JSONObject first=CinemaLibrary.ranked(a,data,type).optJSONObject(0);feature(first,"movie");}}else{seriesData=data;fill(series,shows,CinemaLibrary.ranked(a,data,type),"popular",type);}restore(true);});}catch(Exception e){a.clock.post(()->{if(!active())return;LinearLayout row=type.equals("movie")?movies:series;row.removeAllViews();row.addView(a.button("Could not load "+(type.equals("movie")?"movies":"series")+" · Retry",()->loadCatalog(type)));});}});}
 LinearLayout shelf(String label){TextView heading=a.text(label,PHONE?18:16);heading.setTypeface(null,1);heading.setPadding(a.dp(5),a.dp(7),0,a.dp(5));a.rows.addView(heading,new LinearLayout.LayoutParams(-1,a.dp(34)));HorizontalScrollView h=new HorizontalScrollView(a);h.setHorizontalScrollBarEnabled(false);h.setFocusable(false);h.setClipToPadding(false);h.setPadding(a.dp(5),a.dp(4),a.dp(5),a.dp(4));LinearLayout row=new LinearLayout(a);row.setClipChildren(false);h.addView(row);a.rows.addView(h,new LinearLayout.LayoutParams(-1,cardHeight()+a.dp(10)));return row;}
 void fillRecent(){JSONArray data=new JSONArray();for(JSONObject m:WatchLibrary.recent(a))data.put(m);fill(history,recent,data,"recent","");if(recent.isEmpty())history.addView(a.text("Your watched movies and series will appear here.",12));}
 void fill(LinearLayout row,List<View> cards,JSONArray data,String label,String media){String focus=a.getCurrentFocus()==null?"":String.valueOf(a.getCurrentFocus().getTag());row.removeAllViews();cards.clear();if(data!=null)for(int i=0;i<Math.min(30,data.length());i++){JSONObject m=data.optJSONObject(i);if(m==null)continue;String type=media.isEmpty()?m.optString("type","movie"):media;View card=PosterCard.create(a,m,"home:"+label+":"+type+":"+m.optString("id"),()->open(m,type));View.OnFocusChangeListener previous=card.getOnFocusChangeListener();card.setOnFocusChangeListener((v,f)->{if(previous!=null)previous.onFocusChange(v,f);if(f){lastPoster=String.valueOf(v.getTag());a.homePoster=lastPoster;feature(m,type);if(cards==recommended)revealPreview();else v.postOnAnimation(()->{if(active()&&v.hasFocus())v.requestRectangleOnScreen(new android.graphics.Rect(0,0,v.getWidth(),v.getHeight()),true);});}});card.setOnLongClickListener(v->{CinemaLibrary.menu(a,m,type,this::refreshTastes);return true;});LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(cardWidth(),cardHeight());p.rightMargin=a.dp(9);row.addView(card,p);cards.add(card);}wire();View keep=a.page.findViewWithTag(focus);if(keep!=null)keep.requestFocus();if(enterWhenReady&&!recommended.isEmpty()){enterWhenReady=false;remembered().requestFocus();}}
 void feature(JSONObject m,String type){if(m==null)return;featured=m;featuredType=type;int version=++previewVersion;if(previewDelay!=null)a.clock.removeCallbacks(previewDelay);if(pending!=null)pending.cancel(true);paintFeature(m,type,true);final Api api=a.api;previewDelay=()->{if(!active()||version!=previewVersion)return;pending=previews.submit(()->{try{JSONObject full=api.meta(type,m.optString("id"));a.clock.post(()->{if(active()&&version==previewVersion){featured=full;paintFeature(full,type,true);}});}catch(Exception ignored){}});};a.clock.postDelayed(previewDelay,300);}
 void paintFeature(JSONObject m,String type,boolean image){title.setText(m.optString("name"));kind.setText(type.equals("series")?"CINEMA SERIES":"CINEMA FILM");String genres=SelectionInfo.value(m,"genres");facts.setText(m.optString("releaseInfo")+(m.optString("runtime").isEmpty()?"":" · "+m.optString("runtime"))+(genres.isEmpty()?"":" · "+genres));description.setText(m.optString("description",type.equals("series")?"Explore seasons and episodes.":"Choose a source to watch, or see more information."));play.setText(type.equals("series")?"Episodes":"▶ Play");updateActions();if(image){String url=m.optString("background",m.optString("fanart",m.optString("poster")));if(!url.isEmpty()){ImageView backdrop=new ImageView(a);backdrop.setTag("home:backdrop");backdrop.setScaleType(ImageView.ScaleType.CENTER_CROP);art.removeAllViews();art.addView(backdrop,new FrameLayout.LayoutParams(-1,-1));a.artwork(backdrop,url,token,1920,1080,"");}}}
 void updateActions(){if(featured!=null){save.setText(CinemaLibrary.saved(a,featured,featuredType)?"✓ My List":"+ My List");rate.setText(CinemaLibrary.rating(a,featured,featuredType)>0?"Liked":"Rate");}}
 void refreshTastes(){fill(movies,recommended,CinemaLibrary.ranked(a,movieData,"movie"),"recommended","movie");fill(series,shows,CinemaLibrary.ranked(a,seriesData,"series"),"popular","series");fillRecent();updateActions();}
 void openFeature(){if(featured!=null)open(featured,featuredType);}
 void open(JSONObject m,String type){View focused=a.getCurrentFocus();if(focused!=null&&String.valueOf(focused.getTag()).startsWith("home:"))a.homePoster=String.valueOf(focused.getTag());a.selected=m;a.type=type;a.go(a::detail);}
 View first(){return !recommended.isEmpty()?recommended.get(0):tabs[0];}
 View remembered(){if(recommended.isEmpty())enterWhenReady=true;View v=lastPoster.isEmpty()?null:a.page.findViewWithTag(lastPoster);return v!=null?v:first();}
 boolean backToMenu(){View f=a.getCurrentFocus();if(f!=null&&f!=tabs[lastTab]&&(String.valueOf(f.getTag()).startsWith("home:"))){enterSidebar(f);return true;}return false;}
 boolean previewFocused(){View focus=a.getCurrentFocus();if(recommended.contains(focus))return true;if(tabs!=null)for(View tab:tabs)if(tab==focus)return true;for(View v=focus;v!=null;){if(v==hero)return true;android.view.ViewParent parent=v.getParent();v=parent instanceof View?(View)parent:null;}return false;}
 void revealPreview(){if(scroll==null)return;scroll.postOnAnimation(()->{if(active()&&previewFocused())scroll.smoothScrollTo(0,0);});}
 void wireHero(){Button[] bs={play,info,save,rate};for(int i=0;i<bs.length;i++){final int at=i;bs[i].setOnKeyListener((v,k,e)->{if((k==19||(k==21&&at==0))){if(e.getAction()==0)enterSidebar(v);return true;}View next=k==20?remembered():k==21?bs[at-1]:k==22?bs[Math.min(bs.length-1,at+1)]:null;if(next==null)return false;if(e.getAction()==0)next.requestFocus();return true;});}}
 View vertical(View from,List<View> target){if(target.isEmpty())return from;int[] p=new int[2];from.getLocationOnScreen(p);int center=p[0]+from.getWidth()/2,best=Integer.MAX_VALUE;View result=target.get(0);for(View v:target){int[] q=new int[2];v.getLocationOnScreen(q);int distance=Math.abs(q[0]+v.getWidth()/2-center);if(distance<best){best=distance;result=v;}}return result;}
 void wire(){wireRow(recommended,null,recent.isEmpty()?shows:recent);wireRow(recent,recommended,shows);wireRow(shows,recent.isEmpty()?recommended:recent,null);}
 void wireRow(List<View> cards,List<View> above,List<View> below){for(int i=0;i<cards.size();i++){final int at=i;cards.get(i).setOnKeyListener((v,k,e)->{if(k==21&&at==0){if(e.getAction()==0)enterSidebar(v);return true;}View next=k==21?cards.get(at-1):k==22?cards.get(Math.min(cards.size()-1,at+1)):k==19?(above==null?play:vertical(v,above)):k==20?(below==null?v:vertical(v,below)):null;if(next==null)return false;if(e.getAction()==0)next.requestFocus();return true;});}}
 void restore(boolean loaded){if(!a.restoreFocus.isEmpty()){View v=a.page.findViewWithTag(a.restoreFocus);if(v!=null){v.requestFocus();a.restoreFocus="";return;}if(!loaded)return;}View f=a.getCurrentFocus();if(f==null||!f.isAttachedToWindow())first().requestFocus();}
 void close(){closed=true;if(previewDelay!=null)a.clock.removeCallbacks(previewDelay);if(pending!=null)pending.cancel(true);previews.shutdownNow();}
}
