package local.pitv;

import android.content.Context;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;

/** Bundled cinema trivia: one timer, silent reveals, and no background work. */
final class MovieTriviaView extends LinearLayout {
 static final String[][] CARDS={
  {"What car becomes a time machine in Back to the Future?","A DeLorean"},
  {"Who plays Indiana Jones in Raiders of the Lost Ark?","Harrison Ford"},
  {"What is the name of the boy left Home Alone?","Kevin McCallister"},
  {"Who directed Jurassic Park?","Steven Spielberg"},
  {"What is the name of Simba's father in The Lion King?","Mufasa"},
  {"Which film features the song \"Ghostbusters\"?","Ghostbusters · 1984"},
  {"What is the name of the cowboy toy in Toy Story?","Woody"},
  {"Who provides Woody's voice in Toy Story?","Tom Hanks"},
  {"What kind of fish is Nemo?","A clownfish"},
  {"What is the name of the ogre voiced by Mike Myers?","Shrek"},
  {"Which 1999 movie offers a choice between a red pill and a blue pill?","The Matrix"},
  {"Who plays the title character in Mrs. Doubtfire?","Robin Williams"},
  {"What is the name of Marty McFly's hometown?","Hill Valley"},
  {"Who is the scientist who builds the time machine in Back to the Future?","Dr. Emmett Brown"},
  {"Which film follows a shark terrorizing Amity Island?","Jaws · 1975"},
  {"What is the name of the friendly alien in E.T. the Extra-Terrestrial?","E.T."},
  {"Which actor plays the Terminator in the original 1984 film?","Arnold Schwarzenegger"},
  {"Who plays Ferris Bueller?","Matthew Broderick"},
  {"What is the name of the princess in The Princess Bride?","Buttercup"},
  {"Which film features a magical board game called Jumanji?","Jumanji · 1995"},
  {"Who plays Jack Sparrow in Pirates of the Caribbean?","Johnny Depp"},
  {"What is the name of the green Jedi master who trains Luke Skywalker?","Yoda"},
  {"Which movie features a robot named WALL-E?","WALL-E · 2008"},
  {"Which 1980s film follows a group of kids searching for One-Eyed Willy's treasure?","The Goonies · 1985"},
  {"Who plays the title character in Forrest Gump?","Tom Hanks"},
  {"Which film has a character named Willy Wonka and a chocolate factory?","Willy Wonka & the Chocolate Factory · 1971"},
  {"Who plays the police officer Axel Foley in Beverly Hills Cop?","Eddie Murphy"},
  {"Which movie follows Bill Murray reliving the same day in Punxsutawney?","Groundhog Day · 1993"},
  {"What is the name of the space ranger in Toy Story?","Buzz Lightyear"},
  {"Which 1988 film mixes live action with a cartoon rabbit named Roger?","Who Framed Roger Rabbit"}
 };
 String[][] cards=CARDS;String[] categories;
 final Handler handler=new Handler();
 final TextView phase,question,answer,hint;
 final ProgressBar timer;
 final ArrayList<Integer> order=new ArrayList<>();
 boolean active=false,closed=false,revealed=false;
 int index=0,seconds=0;
 final Runnable tick=()->advance();
 MovieTriviaView(Context context){
  super(context);setOrientation(VERTICAL);setPadding(TvStyle.dp(context,8),TvStyle.dp(context,6),TvStyle.dp(context,8),TvStyle.dp(context,8));setFocusable(true);setClickable(true);
  phase=TvStyle.text(context,"MOVIE TRIVIA",11);phase.setTextColor(TvStyle.AMBER);phase.setLetterSpacing(.15f);addView(phase,new LayoutParams(-1,TvStyle.dp(context,22)));
  question=TvStyle.text(context,"",21);question.setMaxLines(3);question.setGravity(Gravity.CENTER_VERTICAL);addView(question,new LayoutParams(-1,0,1));
  answer=TvStyle.text(context,"",17);answer.setTextColor(TvStyle.CYAN);answer.setMaxLines(2);answer.setGravity(Gravity.CENTER_VERTICAL);addView(answer,new LayoutParams(-1,TvStyle.dp(context,43)));
  timer=new ProgressBar(context,null,android.R.attr.progressBarStyleHorizontal);timer.setMax(6);timer.setProgressTintList(android.content.res.ColorStateList.valueOf(TvStyle.AMBER));addView(timer,new LayoutParams(-1,TvStyle.dp(context,3)));
  hint=TvStyle.text(context,"OK · Reveal answer",10);hint.setTextColor(TvStyle.SILVER);hint.setPadding(0,TvStyle.dp(context,5),0,0);addView(hint);
  setOnFocusChangeListener((v,focused)->setBackground(TvStyle.shape(context,0x00000000,focused?TvStyle.CYAN:0x00000000)));
  setOnClickListener(v->{if(revealed)next();else reveal();});
  loadDeck();showQuestion();
 }
 String category(){return categories==null?"Movies":categories[order.get(index)];}
 void loadDeck(){try(java.io.InputStream in=getContext().getAssets().open("entertainment-trivia.json");java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream()){byte[] b=new byte[4096];int n;while((n=in.read(b))!=-1)out.write(b,0,n);org.json.JSONArray list=new org.json.JSONArray(out.toString("UTF-8"));cards=new String[list.length()][2];categories=new String[list.length()];for(int i=0;i<list.length();i++){org.json.JSONObject c=list.getJSONObject(i);cards[i][0]=c.getString("question");cards[i][1]=c.getString("answer");categories[i]=c.getString("category");}}catch(Exception ignored){cards=CARDS;categories=null;}
  android.content.SharedPreferences p=getContext().getSharedPreferences("trivia",0);try{org.json.JSONArray saved=new org.json.JSONArray(p.getString("order","[]"));if(saved.length()!=cards.length)throw new Exception();java.util.HashSet<Integer> unique=new java.util.HashSet<>();for(int i=0;i<saved.length();i++){int id=saved.getInt(i);if(id<0||id>=cards.length||!unique.add(id))throw new Exception();order.add(id);}index=Math.max(0,Math.min(order.size()-1,p.getInt("index",0)));}catch(Exception e){order.clear();for(int i=0;i<cards.length;i++)order.add(i);Collections.shuffle(order);index=0;}saveDeck();
 }
 void saveDeck(){getContext().getSharedPreferences("trivia",0).edit().putString("order",new org.json.JSONArray(order).toString()).putInt("index",index).apply();}
 boolean motion(){return !getContext().getSharedPreferences("tv",0).getBoolean("reduceMotion",false);}
 void showQuestion(){
  revealed=false;seconds=0;phase.setText(category().toUpperCase(java.util.Locale.ROOT)+" TRIVIA");question.setText(cards[order.get(index)][0]);answer.setText("");hint.setText("OK · Reveal answer");timer.setProgress(6);setContentDescription(category()+" trivia. "+question.getText()+". Press OK to reveal the answer.");
  question.animate().cancel();answer.animate().cancel();answer.setAlpha(1);answer.setScaleX(1);answer.setScaleY(1);question.setAlpha(1);question.setTranslationY(0);
  if(active&&motion()){question.setAlpha(0);question.setTranslationY(TvStyle.dp(getContext(),7));question.animate().alpha(1).translationY(0).setDuration(650).start();}
 }
 void reveal(){
  if(revealed)return;revealed=true;seconds=0;phase.setText("THE ANSWER");answer.setText(cards[order.get(index)][1]);hint.setText("OK · Next question");timer.setProgress(6);setContentDescription(category()+" trivia. "+question.getText()+". Answer: "+answer.getText());
  if(active&&motion()){answer.setAlpha(0);answer.setScaleX(.97f);answer.setScaleY(.97f);answer.setShadowLayer(TvStyle.dp(getContext(),8),0,0,0x8839d9ff);answer.animate().alpha(1).scaleX(1).scaleY(1).setDuration(700).withEndAction(()->answer.setShadowLayer(0,0,0,0)).start();}
 }
 void next(){index=(index+1)%order.size();if(index==0){int gap=Math.min(50,order.size()/3);HashSet<Integer> recent=new HashSet<>(order.subList(order.size()-gap,order.size()));ArrayList<Integer> eligible=new ArrayList<>(),tail=new ArrayList<>();for(int id:order)if(recent.contains(id))tail.add(id);else eligible.add(id);Collections.shuffle(eligible);ArrayList<Integer> shuffled=new ArrayList<>(eligible.subList(0,gap));tail.addAll(eligible.subList(gap,eligible.size()));Collections.shuffle(tail);shuffled.addAll(tail);order.clear();order.addAll(shuffled);}saveDeck();showQuestion();}
 void advance(){if(!active||closed)return;seconds++;if(!revealed){timer.setProgress(Math.max(0,6-seconds));if(seconds>=6)reveal();}else{timer.setProgress(Math.max(0,6-seconds*6/4));if(seconds>=4)next();}handler.postDelayed(tick,1000);}
 void resume(){if(closed)return;active=true;handler.removeCallbacks(tick);handler.postDelayed(tick,1000);}
 void pause(){active=false;handler.removeCallbacks(tick);question.animate().cancel();answer.animate().cancel();question.setAlpha(1);question.setTranslationY(0);answer.setAlpha(1);answer.setScaleX(1);answer.setScaleY(1);answer.setShadowLayer(0,0,0,0);}
 void close(){pause();closed=true;handler.removeCallbacksAndMessages(null);}
 protected void onDetachedFromWindow(){pause();super.onDetachedFromWindow();}
}
