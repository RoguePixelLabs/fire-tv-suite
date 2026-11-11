package local.ondemand;
import org.json.*;import java.util.regex.*;
/** Catalog parameters are checked locally too: provider lists may contain mismatches. */
final class DiscoverFilter {
 static final String[] GENRES={"All genres","Action","Adventure","Animation","Biography","Comedy","Crime","Documentary","Drama","Family","Fantasy","History","Horror","Mystery","Romance","Sci-Fi","Sport","Thriller","War","Western","Reality-TV","Talk-Show","Game-Show"};
 static String url(String type,String genre,int year,int skip)throws Exception{String extra=year>0?"genre="+year:genre.isEmpty()?"":"genre="+Api.enc(genre);if(skip>0)extra+=(extra.isEmpty()?"":"&")+"skip="+skip;return Api.META+"/catalog/"+type+"/"+(year>0?"year":"top")+(extra.isEmpty()?"":"/"+extra)+".json";}
 static int year(JSONObject m){String value=m.optString("releaseInfo",m.optString("year",""));Matcher match=Pattern.compile("(?:19|20)[0-9]{2}").matcher(value);return match.find()?Integer.parseInt(match.group()):0;}
 static boolean accepts(JSONObject m,String genre,int year){if(year>0&&year(m)!=year)return false;if(genre.isEmpty())return true;JSONArray genres=m.optJSONArray("genres");if(genres!=null)for(int i=0;i<genres.length();i++)if(genre.equalsIgnoreCase(genres.optString(i)))return true;return false;}
}
