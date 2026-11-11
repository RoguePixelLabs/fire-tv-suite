package local.pitv;
import java.util.regex.Pattern;

/** Provider language labels plus streaming-service browsing categories. */
final class SeriesLanguage {
 static final Pattern SERVICE=Pattern.compile("(?i)(^|[^a-z0-9])(netflix|paramount(?:\\s*\\+|\\s+plus)?|disney(?:\\s*\\+|\\s+plus)?|amazon|prime\\s*video|apple\\s*tv|hbo|max|hulu|peacock|discovery(?:\\s*\\+|\\s+plus)?|crunchyroll|starz|showtime|amc(?:\\s*\\+|\\s+plus)?|mgm(?:\\s*\\+|\\s+plus)?|britbox|acorn(?:\\s*tv)?|pluto(?:\\s*tv)?|tubi)([^a-z0-9]|$)");
 static final Pattern ENGLISH=Pattern.compile("(?i)(^|[^a-z])(en|eng|english|us|usa|uk|gb|au|nz|united states|united kingdom|australia|new zealand)([^a-z]|$)");
 static final Pattern FOREIGN_COUNTRY=Pattern.compile("(?i)(^|[^a-z])(fr|france|de|germany|es|spain|mx|mexico|latam|latino|br|brazil|pt|portugal|it|italy|tr|turkey|turkiye|ru|russia|pl|poland|nl|netherlands|be|belgium|in|india|pk|pakistan|bd|bangladesh|cn|china|kr|korea|jp|japan|sa|saudi|ae|uae|eg|egypt|ir|iran|iq|iraq|sy|syria|lb|lebanon|ma|morocco|dz|algeria|tn|tunisia|af|afghanistan|gr|greece|ro|romania|hu|hungary|cz|czech|sk|slovakia|se|sweden|no|norway|dk|denmark|fi|finland|ua|ukraine|rs|serbia|hr|croatia|ba|bosnia|al|albania|bg|bulgaria|id|indonesia|th|thailand|vn|vietnam|ph|philippines)([^a-z]|$)");
 static boolean categoryVisible(String category){
  String group=category==null?"":category;
  for(int i=0;i<group.length();i++)if(Character.isLetter(group.charAt(i))&&Character.UnicodeBlock.of(group.charAt(i))==Character.UnicodeBlock.ARABIC)return false;
  if(FOREIGN_COUNTRY.matcher(group).find())return false;
  for(String code:ChannelLanguage.CODES)if(!code.equals("en")&&!code.equals("all")&&ChannelLanguage.tag(group,code))return false;
  return ENGLISH.matcher(group).find()||SERVICE.matcher(group).find();
 }
}
