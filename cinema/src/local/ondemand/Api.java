package local.ondemand;
import java.net.*;import java.io.*;import org.json.*;import android.util.LruCache;
class Api {
 static final String META="https://v3-cinemeta.strem.io";
 final LruCache<String,String> cache=new LruCache<>(40);
 final java.util.concurrent.ConcurrentHashMap<String,Long> cacheTimes=new java.util.concurrent.ConcurrentHashMap<>();
 static String enc(String s)throws Exception{return URLEncoder.encode(s,"UTF-8").replace("+","%20");}
 JSONObject get(String url,boolean cached)throws Exception{return get(url,cached,12000,20000);}
 JSONObject get(String url,boolean cached,int connect,int read)throws Exception{
  String hit=cached&&System.currentTimeMillis()-cacheTimes.getOrDefault(url,0L)<10*60*1000L?cache.get(url):null;if(hit!=null)return new JSONObject(hit);
  HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();c.setConnectTimeout(connect);c.setReadTimeout(read);c.setRequestProperty("User-Agent","PiOnDemand/0.1");
  try{if(c.getResponseCode()!=200)throw new IOException("Service unavailable");try(InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1){if(out.size()+n>4*1024*1024)throw new IOException("Response too large");out.write(b,0,n);}String body=out.toString("UTF-8");JSONObject result=new JSONObject(body);if(cached){cache.put(url,body);cacheTimes.put(url,System.currentTimeMillis());}return result;}}finally{c.disconnect();}
 }
 JSONArray quickSearch(String type,String query)throws Exception{return get(META+"/catalog/"+type+"/top/search="+enc(query)+".json",true,3000,4500).optJSONArray("metas");}
 JSONArray search(String type,String query)throws Exception{return get(META+"/catalog/"+type+"/top/search="+enc(query)+".json",true).optJSONArray("metas");}
 JSONArray similarFilmmaker(String name)throws Exception{return get(META+"/catalog/movie/top/search="+enc(name)+".json",true,3000,4500).optJSONArray("metas");}
 JSONArray similarCatalog(String genre)throws Exception{return get(META+"/catalog/movie/imdbRating/genre="+enc(genre)+".json",true,3000,4500).optJSONArray("metas");}
 JSONArray discoverPage(String type,String genre,int year,int skip)throws Exception{return get(DiscoverFilter.url(type,genre,year,skip),true).optJSONArray("metas");}
 JSONObject meta(String type,String id)throws Exception{return get(META+"/meta/"+type+"/"+enc(id)+".json",true).getJSONObject("meta");}
 JSONArray streams(String base,String type,String id)throws Exception{return get(base+"/stream/"+type+"/"+enc(id)+".json",false).getJSONArray("streams");}
 static boolean playable(JSONObject s){String name=s.optString("name");return s.optString("url").startsWith("https://")&&name.contains("PM+")&&!name.toLowerCase().contains("download");}

 JSONArray similarSeriesFilmmaker(String name)throws Exception{return get(META+"/catalog/series/top/search="+enc(name)+".json",true,3000,4500).optJSONArray("metas");}
 JSONArray similarSeriesCatalog(String genre)throws Exception{return get(META+"/catalog/series/imdbRating/genre="+enc(genre)+".json",true,3000,4500).optJSONArray("metas");}
}
