package local.pitv;
import java.util.*;
import java.util.regex.*;
import java.net.URI;
public final class Playlist {
 public static final class Entry {
  public final String name,group,url,epgId,language;
  Entry(String n,String g,String u){this(n,g,u,"");} Entry(String n,String g,String u,String id){this(n,g,u,id,"");} Entry(String n,String g,String u,String id,String lang){name=n;group=g;url=u;epgId=id;language=lang;}
 }
 public static List<Entry> parse(String input,String base) throws Exception {
  List<Entry> out=new ArrayList<>();String name="",group="Other",epgId="",language="";
  Pattern attr=Pattern.compile("group-title=\"([^\"]*)\"");
  String[] lines=input.replace("\uFEFF","").split("\\r?\\n");
  boolean header=false;for(String raw:lines){String line=raw.trim();if(line.isEmpty())continue;
   if(!header){if(!line.startsWith("#EXTM3U"))throw new Exception("Not an M3U playlist");header=true;}
   if(line.startsWith("#EXT-X-"))throw new Exception("This is a single HLS stream, not a channel playlist");
   if(line.startsWith("#EXTINF:")){int comma=-1;boolean quote=false;for(int j=0;j<line.length();j++){char c=line.charAt(j);if(c=='\"')quote=!quote;if(c==','&&!quote){comma=j;break;}}Matcher id=Pattern.compile("tvg-id=\"([^\"]*)\"").matcher(line);epgId=id.find()?id.group(1):"";Matcher lang=Pattern.compile("tvg-language=\"([^\"]*)\"",Pattern.CASE_INSENSITIVE).matcher(line);language=lang.find()?lang.group(1):"";name=comma>=0?line.substring(comma+1).trim():"";Matcher m=attr.matcher(line);group=m.find()?m.group(1):"Other";}
   else if(line.startsWith("#EXTGRP:"))group=line.substring(8).trim();
   else if(!line.startsWith("#")){URI u=new URI(line);if(!u.isAbsolute()&&base!=null)u=new URI(base).resolve(u);String s=u.getScheme();if("https".equalsIgnoreCase(s)||"http".equalsIgnoreCase(s)){out.add(new Entry(name.isEmpty()?"Channel "+(out.size()+1):name,group.isEmpty()?"Other":group,u.toString(),epgId,language));}name="";group="Other";epgId="";language="";}
  }
  if(out.isEmpty())throw new Exception("No HTTP or HTTPS channels found");return out;
 }
}
