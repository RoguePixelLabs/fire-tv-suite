package local.ondemand;
import android.os.*;import android.text.*;import android.view.*;import android.widget.*;import org.json.*;import java.util.concurrent.*;
/** Debounced title lookup; stale responses cannot replace a newer query. */
final class TitleSearch {
 final MainActivity a;final int generation;final Handler clock=new Handler();final ExecutorService network=Executors.newSingleThreadExecutor();Future<?> pending;Runnable lookup;int serial;boolean closed;EditText input;LinearLayout results;String mediaType="movie";JSONArray matches=new JSONArray();
 TitleSearch(MainActivity app){a=app;a.screen("Search","Type a title · suggestions appear after 2 letters");generation=a.generation;a.titleSearch=this;mediaType=a.type;show();}
 LinearLayout keyboard;
 Button firstKey;
 Button allResults;

 void openKeyboard(){
  firstKey.requestFocus();
 }

 void buildKeyboard(){
  keyboard=new LinearLayout(a);
  keyboard.setOrientation(LinearLayout.VERTICAL);
  for(String letters:new String[]{"QWERTYUIOP","ASDFGHJKL","ZXCVBNM'","1234567890"}){
   LinearLayout row=new LinearLayout(a);
   for(int i=0;i<letters.length();i++){
    String letter=letters.substring(i,i+1);
    Button key=addKey(row,letter,()->input.append(letter));
    if(firstKey==null)firstKey=key;
   }
   keyboard.addView(row);
  }
  LinearLayout actions=new LinearLayout(a);
  addKey(actions,"Space",()->input.append(" "));
  addKey(actions,"Delete",()->{
   int end=input.length();
   if(end>0)input.getText().delete(Character.offsetByCodePoints(input.getText(),end,-1),end);
  });
  addKey(actions,"Clear",()->input.setText(""));
  addKey(actions,"Done",()->allResults.requestFocus());
  keyboard.addView(actions);
 }

 void addSearchControls(LinearLayout panel){
  buildKeyboard();
  LinearLayout controls=new LinearLayout(a);
  int gap=a.dp(12);
  controls.addView(keyboard,new LinearLayout.LayoutParams(0,-2,3));

  LinearLayout options=new LinearLayout(a);
  options.setOrientation(LinearLayout.VERTICAL);
  LinearLayout.LayoutParams optionSize=new LinearLayout.LayoutParams(0,-1,1);
  optionSize.leftMargin=gap;
  controls.addView(options,optionSize);
  addSearchOption(options,"Search movies",()->selectMediaType("movie"));
  addSearchOption(options,"Search series",()->selectMediaType("series"));
  allResults=addSearchOption(options,"All results",this::fullSearch);
  panel.addView(controls,new LinearLayout.LayoutParams(-1,(a.dp(24)+a.dp(3))*5));
 }

 void selectMediaType(String type){
  mediaType=type;
  a.type=type;
  changed();
  openKeyboard();
 }

 Button addSearchOption(LinearLayout options,String label,Runnable action){
  Button button=a.button(label,action);
  button.setTextSize(12);
  button.setPadding(a.dp(6),0,a.dp(6),0);
  LinearLayout.LayoutParams size=new LinearLayout.LayoutParams(-1,0,1);
  size.bottomMargin=a.dp(3);
  options.addView(button,size);
  return button;
 }

 Button addKey(LinearLayout row,String label,Runnable action){
  Button key=a.button(label,action);
  key.setPadding(0,0,0,0);key.setTextSize(12);
  LinearLayout.LayoutParams size=new LinearLayout.LayoutParams(0,a.dp(24),1);
  size.setMargins(0,0,a.dp(4),a.dp(3));
  row.addView(key,size);
  return key;
 }
 void show(){input=new EditText(a);input.setId(View.generateViewId());input.setTag("title-query");input.setSingleLine(true);input.setTextColor(0xfff4f1e8);input.setTextSize(16);input.setHint("Search movies or series");input.setHintTextColor(0xffb8c2cc);input.setPadding(a.dp(12),a.dp(5),a.dp(12),a.dp(5));input.setBackground(a.shape(0xff1c1e28,0xff465064,10));input.setShowSoftInputOnFocus(false);input.setInputType(android.text.InputType.TYPE_CLASS_TEXT);input.setOnClickListener(v->openKeyboard());input.setOnEditorActionListener((v,action,event)->{fullSearch();return true;});LinearLayout panel=new LinearLayout(a);panel.setOrientation(LinearLayout.VERTICAL);panel.setPadding(0,a.dp(5),0,0);a.page.addView(panel,new LinearLayout.LayoutParams(-1,-2));panel.addView(input,new LinearLayout.LayoutParams(-1,a.dp(32)));
  addSearchControls(panel);results=new LinearLayout(a);results.setOrientation(1);a.rows.addView(results);input.setText(a.query);input.setSelection(input.length());input.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int af){}public void onTextChanged(CharSequence s,int st,int before,int count){a.query=s.toString();changed();}public void afterTextChanged(Editable e){}});changed();openKeyboard();if(a.restoreFocus.equals("title-query"))a.restoreFocus="";}
 void linkResults(){
  View last=results.getChildCount()==0?null:results.getChildAt(results.getChildCount()-1);
  int target=last==null?input.getId():last.getId();
  LinearLayout topRow=(LinearLayout)keyboard.getChildAt(0);
  for(int i=0;i<topRow.getChildCount();i++)topRow.getChildAt(i).setNextFocusUpId(target);
  input.setNextFocusUpId(last==null?View.NO_ID:target);
  if(last!=null)last.setNextFocusDownId(firstKey.getId());
 }
 void fullSearch(){String q=input.getText().toString().trim();if(q.isEmpty())return;a.query=q;a.type=mediaType;a.go(a::search);}
 void choose(int i){if(closed||i<0||i>=matches.length())return;JSONObject selected=matches.optJSONObject(i);if(selected==null)return;a.query=input.getText().toString();a.selected=selected;a.type=mediaType;a.go(a::detail);}
 boolean active(int version){return !closed&&a.generation==generation&&serial==version;}
 void changed(){if(closed)return;final int version=++serial;if(lookup!=null)clock.removeCallbacks(lookup);if(pending!=null)pending.cancel(true);matches=new JSONArray();results.removeAllViews();linkResults();final String q=input.getText().toString().trim(),t=mediaType;a.status.setText((t.equals("movie")?"Movies":"Series")+" · "+(q.length()<2?"Type at least 2 letters":"Finding matching titles…"));if(q.length()<2)return;lookup=()->{if(!active(version))return;pending=network.submit(()->{try{JSONArray data=new Api().quickSearch(t,q);clock.post(()->{if(!active(version))return;matches=data==null?new JSONArray():data;render();});}catch(Exception e){clock.post(()->{if(active(version))a.status.setText("Title suggestions unavailable · try All results");});}});};clock.postDelayed(lookup,500);}
 void render(){results.removeAllViews();a.status.setText((mediaType.equals("movie")?"Movies":"Series")+" · "+(matches.length()==0?"No matching titles yet":"Choose a title, or keep typing"));for(int i=0;i<Math.min(4,matches.length());i++){JSONObject m=matches.optJSONObject(i);if(m==null)continue;final int index=i;Button b=a.button(m.optString("name")+"  ·  "+m.optString("releaseInfo",""),()->choose(index));b.setTextSize(14);b.setPadding(a.dp(10),0,a.dp(10),0);b.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);b.setTag("suggestion:"+i);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,a.dp(34));p.bottomMargin=a.dp(4);results.addView(b,p);}linkResults();}
 void close(){closed=true;serial++;clock.removeCallbacksAndMessages(null);if(pending!=null)pending.cancel(true);network.shutdownNow();}
}
