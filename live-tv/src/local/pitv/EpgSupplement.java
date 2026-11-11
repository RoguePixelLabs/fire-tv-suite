package local.pitv;
import android.content.*;import org.json.*;import org.xmlpull.v1.*;import java.io.*;import java.net.*;import java.util.*;import java.util.concurrent.*;import java.util.function.*;import java.util.zip.*;
/** Account-bound, cached XMLTV gap filling. Never opens a video stream. */
final class EpgSupplement {
 static final long DAY=86400000L;
 final Context context;final String scope;final File directory;final SharedPreferences prefs;
 final ExecutorService updater=Executors.newSingleThreadExecutor();
 volatile Map<String,List<XmlGuide.Programme>> listings=Collections.emptyMap();
 volatile JSONObject channels=new JSONObject();final Map<String,Set<String>> sources=new TreeMap<>();final Map<String,Map<String,String>> sourceKeys=new HashMap<>();
 volatile boolean closed=false;boolean loaded=false;volatile boolean updating=false;
 EpgSupplement(Context c,String key){context=c.getApplicationContext();scope=key;directory=new File(context.getFilesDir(),"epg-supplement");prefs=context.getSharedPreferences("epg-supplement",0);}
 static boolean usable(List<XmlGuide.Programme> values,long now){if(values!=null)for(XmlGuide.Programme p:values)if(p.stop>now&&p.start<now+DAY&&!p.title.trim().isEmpty())return true;return false;}
 boolean enabled(){return scope.equals(prefs.getString("scope",""))&&prefs.getBoolean("enabled",true);}
 static String read(InputStream in)throws Exception{try(InputStream input=in;ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[8192];int n;while((n=input.read(b))!=-1){if(out.size()+n>8*1024*1024)throw new IOException("Mapping too large");out.write(b,0,n);}return out.toString("UTF-8");}}
 void loadCached()throws Exception{
  if(loaded||!enabled())return;
  JSONObject config=new JSONObject(read(context.getAssets().open("epg-mapping.json")));channels=config.getJSONObject("channels");
  for(Iterator<String> it=channels.keys();it.hasNext();){JSONObject ch=channels.getJSONObject(it.next());String source=ch.getString("source");if(!source.equals("provider")&&validSource(source)){sources.computeIfAbsent(source,k->new HashSet<>()).add(ch.getString("guide_id"));sourceKeys.computeIfAbsent(source,k->new HashMap<>()).put(ch.getString("guide_id"),ch.optString("cache_id",ch.getString("guide_id")));}}
  Set<String> wanted=new HashSet<>();for(Map<String,String> ids:sourceKeys.values())wanted.addAll(ids.values());
  Map<String,List<XmlGuide.Programme>> data;
  try(InputStream in=new GZIPInputStream(context.getAssets().open("epg-seed.xml.gz"))){data=XmlGuide.parse(in,id->"UTC",wanted);}
  for(Map.Entry<String,Set<String>> entry:sources.entrySet()){
   File file=file(entry.getKey());if(!file.isFile())continue;
   try(InputStream in=new GZIPInputStream(new FileInputStream(file))){Map<String,List<XmlGuide.Programme>> cached=XmlGuide.parse(in,id->"UTC",entry.getValue());for(String id:entry.getValue())if(usable(cached.get(id),System.currentTimeMillis()))data.put(sourceKeys.get(entry.getKey()).get(id),cached.get(id));}catch(Exception ignored){}
  }
  listings=data;loaded=true;
 }
 static boolean validSource(String url){return (url.matches("https://epgshare01\\.online/epgshare01/epg_ripper_[A-Za-z0-9_]+\\.xml\\.gz")&&!url.contains("ALL_SOURCES")&&!url.contains("DUMMY"))||url.matches("https://epg\\.pw/xmltv/epg_[A-Z]{2}\\.xml\\.gz")||url.matches("https://raw\\.githubusercontent\\.com/matthuisman/i\\.mjh\\.nz/master/(?:Plex|PlutoTV|SamsungTVPlus)/[a-z]{2}\\.xml\\.gz");}
 File file(String source){try{StringBuilder name=new StringBuilder();for(byte b:java.security.MessageDigest.getInstance("SHA-256").digest(source.getBytes("UTF-8")))name.append(String.format(Locale.ROOT,"%02x",b&255));return new File(directory,name+".xml.gz");}catch(Exception e){throw new IllegalStateException(e);}}
 boolean overrides(JSONObject channel){if(!enabled())return false;JSONObject match=channels.optJSONObject(channel.optString("stream_id"));return match!=null&&match.optString("name").equals(channel.optString("name"))&&match.optString("category_id").equals(channel.optString("category_id"))&&!match.optString("override_epg_id").isEmpty()&&match.optString("override_epg_id").equals(channel.optString("epg_channel_id",channel.optString("epg_id")));}
 List<XmlGuide.Programme> get(JSONObject channel,Map<String,List<XmlGuide.Programme>> provider){
  if(!enabled())return null;
  JSONObject match=channels.optJSONObject(channel.optString("stream_id"));
  if(match==null||!match.optString("name").equals(channel.optString("name"))||!match.optString("category_id").equals(channel.optString("category_id")))return null;
  String id=match.optString("guide_id");List<XmlGuide.Programme> result=match.optString("source").equals("provider")?provider.get(id):listings.get(match.optString("cache_id",id));
  return usable(result,System.currentTimeMillis())?result:null;
 }
 void refresh(BooleanSupplier idle,Runnable changed){
  if(closed||updating||!loaded||!enabled())return;updating=true;
  updater.submit(()->{try{
   long generated=0;try{generated=new JSONObject(read(context.getAssets().open("epg-mapping.json"))).optLong("generated_at");}catch(Exception ignored){}
   for(Map.Entry<String,Set<String>> entry:sources.entrySet()){
    if(closed||!idle.getAsBoolean()||!enabled())break;
    String url=entry.getKey(),check="check_"+url;long now=System.currentTimeMillis(),last=prefs.getLong(check,generated);
    if(last>0&&now>=last&&now-last<DAY)continue;
    HttpURLConnection c=null;
    try{
     c=(HttpURLConnection)new URL(url).openConnection();c.setConnectTimeout(10000);c.setReadTimeout(15000);c.setInstanceFollowRedirects(false);c.setRequestProperty("User-Agent","PiTV-EPG/1.0");c.setRequestProperty("Accept-Encoding","identity");
     if(c.getResponseCode()!=200)throw new IOException("EPG source unavailable");
     Map<String,List<XmlGuide.Programme>> updated;
     java.io.PushbackInputStream raw=new java.io.PushbackInputStream(new BufferedInputStream(new Limited(c.getInputStream(),128L*1024*1024,()->!closed&&idle.getAsBoolean())),2);int a=raw.read(),b=raw.read();if(b>=0)raw.unread(b);if(a>=0)raw.unread(a);try(InputStream in=new Limited(a==31&&b==139?new GZIPInputStream(raw):raw,1024L*1024*1024,()->!closed&&idle.getAsBoolean())){updated=XmlGuide.parse(in,id->"UTC",entry.getValue());}
     boolean useful=false;for(List<XmlGuide.Programme> list:updated.values())if(usable(list,now)){useful=true;break;}
     if(!useful)throw new IOException("No upcoming listings");
     if(!directory.isDirectory()&&!directory.mkdirs())throw new IOException("Storage unavailable");save(file(url),updated);
     Map<String,List<XmlGuide.Programme>> next=new HashMap<>(listings);
     for(String id:entry.getValue())if(usable(updated.get(id),now))next.put(sourceKeys.get(url).get(id),updated.get(id));listings=next;
     prefs.edit().putLong(check,now).putLong("last_success",now).apply();if(!closed)changed.run();
    }catch(Exception ignored){if(!closed&&idle.getAsBoolean())prefs.edit().putLong(check,System.currentTimeMillis()-DAY+6*3600000L).apply();}
    finally{if(c!=null)c.disconnect();}
   }
  }finally{updating=false;}});
 }
 static void save(File file,Map<String,List<XmlGuide.Programme>> data)throws Exception{
  File temp=new File(file.getPath()+".tmp");try{
   try(OutputStream out=new GZIPOutputStream(new FileOutputStream(temp))){
    XmlSerializer x=XmlPullParserFactory.newInstance().newSerializer();x.setOutput(out,"UTF-8");x.startDocument("UTF-8",true);x.startTag(null,"tv");
    java.text.SimpleDateFormat fmt=new java.text.SimpleDateFormat("yyyyMMddHHmmss Z",Locale.ROOT);fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
    for(Map.Entry<String,List<XmlGuide.Programme>> entry:data.entrySet())for(XmlGuide.Programme p:entry.getValue()){
     x.startTag(null,"programme");x.attribute(null,"channel",entry.getKey());x.attribute(null,"start",fmt.format(new Date(p.start)));x.attribute(null,"stop",fmt.format(new Date(p.stop)));x.startTag(null,"title");x.text(p.title);x.endTag(null,"title");x.startTag(null,"desc");x.text(p.description);x.endTag(null,"desc");x.endTag(null,"programme");
    }x.endTag(null,"tv");x.endDocument();x.flush();
   }if(!temp.renameTo(file))throw new IOException("Guide save failed");
  }finally{if(temp.isFile())temp.delete();}
 }
 static final class Limited extends FilterInputStream {
  long remaining;final BooleanSupplier allowed;Limited(InputStream in,long limit,BooleanSupplier ok){super(in);remaining=limit;allowed=ok;}
  void check()throws IOException{if(!allowed.getAsBoolean())throw new IOException("Guide update paused");}
  public int read()throws IOException{check();int n=in.read();if(n>=0&&--remaining<0)throw new IOException("Guide too large");return n;}
  public int read(byte[] b,int off,int len)throws IOException{check();int n=in.read(b,off,len);if(n>0&&(remaining-=n)<0)throw new IOException("Guide too large");return n;}
 }
 void close(){closed=true;updater.shutdownNow();}
}
