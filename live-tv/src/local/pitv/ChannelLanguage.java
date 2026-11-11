package local.pitv;
import java.util.*;import java.util.regex.*;
final class ChannelLanguage {
 static boolean english(String value){if(value==null)return false;return Pattern.compile("(?i)(^|[^a-z])(en|eng|english)([^a-z]|$)").matcher(value).find();}
 static boolean foreign(String value){return Pattern.compile("(?i)(^|[^a-z])(es|esp|spa|spanish|espanol|español|latino|fr|fra|fre|french|francais|français|de|deu|ger|german|ar|ara|arabic|hi|hin|hindi|ur|urdu|pt|por|portuguese|it|ita|italian|tr|turkish|ru|rus|russian|pl|pol|polish|nl|nld|dutch|zh|zho|chinese|ko|kor|korean|ja|jpn|japanese|tamil|telugu|punjabi|bengali|kurdish|farsi|persian)([^a-z]|$)").matcher(value==null?"":value).find();}
 static int classify(String name,String group,String language){
 if(language!=null&&!language.trim().isEmpty()&&!language.equalsIgnoreCase("und")){if(english(language))return 1;if(foreign(language))return -1;}
 String n=name==null?"":name,g=group==null?"":group;
 if(english(n))return 1;if(foreign(n))return -1;if(english(g))return 1;if(foreign(g))return -1;
 String both=n+" "+g;if(both.toUpperCase(Locale.ROOT).contains("AT&T"))return 1;
 if(Pattern.compile("(?i)(^|[ |:;\\[\\]/])(US|USA|UK|GB|UNITED STATES|UNITED KINGDOM|AU|AUSTRALIA|NZ|NEW ZEALAND)(?=$|[ |:;\\]\\[/])").matcher(both).find())return 1;
 return 0;
 }
 static final String[] CODES={"en","es","fr","de","ar","hi","pt","it","tr","ru","pl","nl","zh","ko","ja","ta","te","pa","bn","ku","fa","ur","all"};
 static final String[] NAMES={"English","Spanish","French","German","Arabic","Hindi","Portuguese","Italian","Turkish","Russian","Polish","Dutch","Chinese","Korean","Japanese","Tamil","Telugu","Punjabi","Bengali","Kurdish","Persian","Urdu","All channels"};
 static final String[] WORDS={"en|eng|english","es|esp|spa|spanish|espanol|español|latino|telemundo|univision|unimas|galavision|azteca","fr|fra|fre|french|francais|français","de|deu|ger|german","ar|ara|arabic","hi|hin|hindi|aajtak","pt|por|portuguese","it|ita|italian","tr|tur|turkish","ru|rus|russian","pl|pol|polish","nl|nld|dut|dutch","zh|zho|chi|chinese","ko|kor|korean","ja|jpn|japanese","ta|tam|tamil","te|tel|telugu","pa|pan|punjabi","bn|ben|bengali","ku|kur|kurdish","fa|fas|per|farsi|persian","ur|urd|urdu"};
 static String label(String code){for(int i=0;i<CODES.length;i++)if(CODES[i].equals(code))return NAMES[i];return "English";}
 static final Pattern[] TAGS=makeTags();static Pattern[] makeTags(){Pattern[] out=new Pattern[WORDS.length];for(int i=0;i<out.length;i++)out[i]=Pattern.compile("(?i)(^|[^a-z])("+WORDS[i]+")([^a-z]|$)");return out;}
 static boolean tag(String value,String code){if(value==null||value.isEmpty())return false;for(int i=0;i<WORDS.length;i++)if(CODES[i].equals(code))return TAGS[i].matcher(value).find();return false;}
 static boolean labeled(String value){if(value==null||value.isEmpty())return false;for(int i=0;i<WORDS.length;i++)if(tag(value,CODES[i]))return true;return false;}
 static boolean matches(String name,String group,String language,String code){if(code.equals("all"))return true;if(tag(language,code))return true;if(labeled(language))return false;if(tag(name,code))return true;if(labeled(name))return false;if(tag(group,code))return true;if(labeled(group))return false;if(code.equals("en"))return classify(name,group,language)==1;String regions=code.equals("es")?"ES|MX|LATAM|SPAIN|MEXICO":code.equals("fr")?"FR|FRANCE":code.equals("de")?"DE|GERMANY":code.equals("pt")?"PT|BR|PORTUGAL|BRAZIL":code.equals("it")?"IT|ITALY":code.equals("tr")?"TR|TURKEY":code.equals("ru")?"RU|RUSSIA":code.equals("pl")?"PL|POLAND":code.equals("nl")?"NL|NETHERLANDS":code.equals("zh")?"CN|CHINA":code.equals("ko")?"KR|KOREA":code.equals("ja")?"JP|JAPAN":"";return !regions.isEmpty()&&Pattern.compile("(?i)(^|[ |:])("+regions+")(?=$|[ |:])").matcher((name==null?"":name)+" "+(group==null?"":group)).find();}
}
