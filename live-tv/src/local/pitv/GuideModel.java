package local.pitv;
import java.util.*;
final class GuideModel {
 static final long MINUTE=60000L,HOUR=60*MINUTE;
 static final class Program {long start,end;String title,description;Program(long s,long e,String t,String d){start=s;end=e;title=t;description=d;}}
 static final class Channel {String name,logo;int sourceIndex;List<Program> programs=new ArrayList<>();}
 static Program at(Channel channel,long time){for(Program p:channel.programs)if(p.start<=time&&p.end>time)return p;return null;}
 static long move(Channel channel,long time,int direction){Program current=at(channel,time);if(direction>0){if(current!=null)return current.end;for(Program p:channel.programs)if(p.start>time)return Math.min(time+30*MINUTE,p.start);return time+30*MINUTE;}if(current!=null&&time>current.start)return current.start-1;return time-30*MINUTE;}
 static float fraction(long time,long start,long duration){return Math.max(0,Math.min(1,(time-start)/(float)duration));}
}
