package local.pitv;

import android.content.Context;
import org.json.*;
import java.io.*;
import java.net.*;
import java.text.Normalizer;
import java.util.*;

/** Public latest trailer feed. Legacy theater matching remains available for checks only. */
final class TheatricalTrailers {
 static final long DAY=24*60*60*1000L;
 static final String FEED="https://www.moviefone.com/feeds/trailers.json";
 static String normalize(String value){return Normalizer.normalize(value,Normalizer.Form.NFKD).replaceAll("\\p{M}","").toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]","");}
 static String title(String label){java.util.regex.Matcher m=java.util.regex.Pattern.compile("^['\"‘](.+)['\"’]\\s+(?:Official\\s+)?(?:Trailer|Teaser|Red Band).*",java.util.regex.Pattern.CASE_INSENSITIVE).matcher(label);return m.matches()?m.group(1):label.replaceFirst("(?i)\\s+(?:Official\\s+)?(?:Trailer|Teaser|Red Band).*$","").trim();}
 static JSONArray select(JSONArray candidates,JSONArray nowPlaying)throws JSONException{
  java.util.Calendar cutoff=java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));cutoff.add(java.util.Calendar.DAY_OF_YEAR,-180);String earliest=new java.text.SimpleDateFormat("yyyy-MM-dd",Locale.US).format(cutoff.getTime());String today=new java.text.SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new java.util.Date());HashMap<String,JSONObject> titles=new HashMap<>();for(int i=0;i<nowPlaying.length();i++){JSONObject movie=nowPlaying.optJSONObject(i);if(movie==null||movie.optBoolean("adult")||movie.optString("release_date").compareTo(earliest)<0||movie.optString("release_date").compareTo(today)>0)continue;String name=normalize(movie.optString("title"));if(!name.isEmpty())titles.put(name,movie);String original=normalize(movie.optString("original_title"));if(!original.isEmpty())titles.put(original,movie);}
  JSONArray out=new JSONArray();HashSet<Integer> used=new HashSet<>();
  for(int i=0;i<candidates.length();i++){JSONObject item=candidates.optJSONObject(i);if(item==null||item.optJSONObject("_moviefone")==null||!item.optJSONObject("_moviefone").optString("category").equals("movie"))continue;String label=item.optString("title");if(!label.toLowerCase(Locale.ROOT).matches(".*(?:trailer|teaser).*"))continue;JSONObject movie=titles.get(normalize(title(label)));if(movie==null||used.contains(movie.optInt("id")))continue;JSONArray attachments=item.optJSONArray("attachments");if(attachments==null)continue;for(int n=0;n<attachments.length();n++){JSONObject media=attachments.optJSONObject(n);if(media==null)continue;String url=media.optString("url");if(!url.startsWith("https://cdn.jwplayer.com/")||!(url.contains(".m3u8")||url.contains(".mp4")))continue;out.put(new JSONObject().put("title",movie.optString("title")).put("releaseDate",movie.optString("release_date")).put("movieId",movie.optInt("id")).put("url",url).put("durationMs",media.optInt("duration_in_seconds")*1000).put("source","Moviefone · TMDB US theaters"));used.add(movie.optInt("id"));break;}}
  return out;
 }
 static JSONArray read(Context c,String name){try(FileInputStream in=c.openFileInput(name)){return new JSONArray(readText(in,2*1024*1024));}catch(Exception ignored){return new JSONArray();}}
 static void write(Context c,String name,JSONArray items)throws Exception{File target=new File(c.getFilesDir(),name);android.util.AtomicFile file=new android.util.AtomicFile(target);FileOutputStream out=null;try{out=file.startWrite();out.write(items.toString().getBytes("UTF-8"));file.finishWrite(out);}catch(Exception e){if(out!=null)file.failWrite(out);throw e;}}
 static String readText(InputStream in,int max)throws Exception{ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buffer=new byte[8192];int count;while((count=in.read(buffer))!=-1){if(Thread.currentThread().isInterrupted())throw new InterruptedIOException();if(out.size()+count>max)throw new IOException("Response too large");out.write(buffer,0,count);}return out.toString("UTF-8");}
 static String get(String url,String bearer)throws Exception{HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();c.setConnectTimeout(10000);c.setReadTimeout(15000);c.setRequestProperty("User-Agent","PiTV/0.4");c.setRequestProperty("Accept","application/json");if(!bearer.isEmpty())c.setRequestProperty("Authorization","Bearer "+bearer);try{if(c.getResponseCode()!=200)throw new IOException("Feed unavailable");try(InputStream in=c.getInputStream()){return readText(in,2*1024*1024);}}finally{c.disconnect();}}
 static final String CACHE="latest-trailers.json",UPDATED="latestUpdatedAt";
 static JSONArray unfiltered(JSONArray items)throws JSONException{
  JSONArray out=new JSONArray();HashSet<String> used=new HashSet<>();for(int i=0;i<items.length();i++){JSONObject item=items.optJSONObject(i);if(item==null)continue;JSONArray media=item.optJSONArray("attachments");if(media==null)continue;for(int n=0;n<media.length();n++){JSONObject attachment=media.optJSONObject(n);if(attachment==null)continue;String url=attachment.optString("url");if(!url.startsWith("https://cdn.jwplayer.com/")||!(url.contains(".m3u8")||url.contains(".mp4"))||used.contains(url))continue;out.put(new JSONObject().put("title",item.optString("title","Movie trailer")).put("published",item.optString("date_published")).put("url",url).put("durationMs",attachment.optInt("duration_in_seconds")*1000).put("source","Moviefone Latest Trailers"));used.add(url);break;}}return out;
 }
 static JSONArray refresh(Context c)throws Exception{
  JSONArray items=new JSONObject(get(FEED,"")).getJSONArray("items"),selected=unfiltered(items);if(selected.length()==0)throw new IOException("No playable feed entries");write(c,CACHE,selected);c.getSharedPreferences("trailer-feed",0).edit().putLong(UPDATED,System.currentTimeMillis()).putString("status","Latest trailer feed updated").apply();return selected;
 }
 static boolean due(Context c){long age=System.currentTimeMillis()-c.getSharedPreferences("trailer-feed",0).getLong(UPDATED,0);return age<0||age>=DAY;}
}
