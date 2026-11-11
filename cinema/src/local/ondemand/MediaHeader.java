package local.ondemand;
import java.util.*;
import java.nio.*;
/** Bounded, non-decoding inspection of MP4/Matroska track metadata. */
final class MediaHeader {
 static class Track {int type,id;String lang="",name="",codec="";boolean forced,full;byte[] codecPrivate;}
 static class Info {String codec="";int width,height,bits;boolean hdr,dv,complete;double seconds,fps;List<Track> tracks=new ArrayList<>();
  boolean englishAudio(){for(Track t:tracks)if(t.type==2&&english(t.lang))return true;return false;}
  boolean englishSubs(){for(Track t:tracks)if(t.type==17&&english(t.lang)&&t.full&&!t.forced)return true;return false;}
  boolean audioKnown(){boolean seen=false;for(Track t:tracks)if(t.type==2){seen=true;if(!known(t.lang))return false;}return complete&&seen;}
 }
 static boolean english(String s){s=s==null?"":s.toLowerCase(Locale.ROOT);return s.equals("en")||s.equals("eng")||s.equals("english")||s.startsWith("en-");}
 static boolean known(String s){return s!=null&&!s.isEmpty()&&!s.equals("und")&&!s.equals("zxx");}
 static Info parse(byte[] b){Info i=new Info();try{if(b.length>4&&(b[0]&255)==0x1a&&(b[1]&255)==0x45)ebml(b,0,b.length,i,null,0);else mp4(b,0,b.length,i,null,0);}catch(RuntimeException ignored){}return i;}
 static long u(byte[] b,int p,int n){if(n<0||n>8||p<0||p+n>b.length)return -1;long x=0;for(int j=0;j<n;j++)x=(x<<8)|(b[p+j]&255L);return x;}
 static String str(byte[] b,int p,int n){if(p<0||n<0||p+n>b.length)return "";return new String(b,p,n,java.nio.charset.StandardCharsets.UTF_8).replace("\u0000","").trim();}
 static long[] vint(byte[] b,int p,boolean id){if(p>=b.length)return null;int n=1,mask=128;while(n<=8&&(b[p]&mask)==0){n++;mask>>=1;}if(n>8||p+n>b.length||(id&&n>4))return null;long v=id?b[p]&255:b[p]&(mask-1);for(int j=1;j<n;j++)v=(v<<8)|(b[p+j]&255L);if(!id&&v==((1L<<(7*n))-1))v=-1;return new long[]{v,n};}
 static void ebml(byte[] b,int p,int end,Info i,Track t,int depth){if(depth>12)return;while(p<end){long[] id=vint(b,p,true);if(id==null)return;p+=(int)id[1];long[] len=vint(b,p,false);if(len==null)return;p+=(int)len[1];long l=len[0];int stop=l<0?end:(int)Math.min(end,Math.min((long)b.length,p+l));long x=id[0];if(x==0x1f43b675L)return;
   if(x==0x18538067L||x==0x1654ae6bL||x==0x1549a966L||x==0xe0||x==0xe1||x==0x55b0){ebml(b,p,stop,i,t,depth+1);if(x==0x1654ae6bL&&l>=0&&p+l<=b.length)i.complete=true;}
   else if(x==0xae){Track nt=new Track();ebml(b,p,stop,i,nt,depth+1);nt.forced|=nt.name.toLowerCase(Locale.ROOT).contains("forced");nt.full=nt.type==17&&!nt.forced;if(nt.type==1&&nt.codecPrivate!=null){if(nt.codec.equals("V_MPEG4/ISO/AVC")&&nt.codecPrivate.length>1){int profile=nt.codecPrivate[1]&255;i.bits=profile==110||profile==122||profile==244?10:8;}else if(nt.codec.equals("V_MPEGH/ISO/HEVC")&&nt.codecPrivate.length>17)i.bits=8+(nt.codecPrivate[17]&7);}i.tracks.add(nt);}
   else if(t!=null){int n=stop-p;if(x==0x83)t.type=(int)u(b,p,n);else if(x==0xd7)t.id=(int)u(b,p,n);else if(x==0x22b59cL||x==0x22b59dL)t.lang=str(b,p,n);else if(x==0x536e)t.name=str(b,p,n);else if(x==0x55aa)t.forced=u(b,p,n)==1;else if(x==0x63a2)t.codecPrivate=Arrays.copyOfRange(b,p,Math.min(stop,p+32));else if(x==0x86){t.codec=str(b,p,n);if(t.codec.equals("V_MPEG4/ISO/AVC"))i.codec="H.264";else if(t.codec.equals("V_MPEGH/ISO/HEVC"))i.codec="HEVC";else if(t.codec.equals("V_AV1"))i.codec="AV1";else if(t.codec.equals("V_VP9"))i.codec="VP9";}
    else if(x==0xb0)i.width=(int)u(b,p,n);else if(x==0xba)i.height=(int)u(b,p,n);else if(x==0x55b2)i.bits=(int)u(b,p,n);else if(x==0x55ba){long v=u(b,p,n);i.hdr|=v==16||v==18;}else if(x==0x23e383L){long ns=u(b,p,n);if(ns>0)i.fps=1e9/ns;}
   }
   if(l<0||p+l>end)return;p=(int)(p+l);
  }}
 static void mp4(byte[] b,int p,int end,Info i,Track t,int depth){if(depth>12)return;while(p+8<=end){long size=u(b,p,4);String kind=str(b,p+4,4);int head=8;if(size==1){size=u(b,p+8,8);head=16;}if(size==0)size=end-p;if(size<head||size>Integer.MAX_VALUE)return;int a=p+head,stop=(int)Math.min(end,p+size);boolean full=p+size<=end;
   if(kind.equals("moov")||kind.equals("mdia")||kind.equals("minf")||kind.equals("stbl")||kind.equals("udta")){mp4(b,a,stop,i,t,depth+1);if(kind.equals("moov")&&full)i.complete=true;}
   else if(kind.equals("trak")){Track nt=new Track();mp4(b,a,stop,i,nt,depth+1);nt.forced|=nt.name.toLowerCase(Locale.ROOT).contains("forced");if(nt.forced)nt.full=false;i.tracks.add(nt);}
   else if(t!=null){if(kind.equals("hdlr")){String h=str(b,a+8,4);t.type=h.equals("soun")?2:h.equals("vide")?1:(h.equals("subt")||h.equals("sbtl")||h.equals("text"))?17:0;}
    else if(kind.equals("mdhd")){int offset=(b[a]&255)==1?32:20;int bits=(int)u(b,a+offset,2);if(bits>0&&bits!=0x7fff)t.lang=""+(char)(((bits>>10)&31)+96)+(char)(((bits>>5)&31)+96)+(char)((bits&31)+96);int tsOff=(b[a]&255)==1?20:12,duOff=(b[a]&255)==1?24:16;long ts=u(b,a+tsOff,4),dur=u(b,a+duOff,(b[a]&255)==1?8:4);if(ts>0&&dur>0)i.seconds=Math.max(i.seconds,dur/(double)ts);}
    else if(kind.equals("tkhd"))t.id=(int)u(b,a+((b[a]&255)==1?20:12),4);
    else if(kind.equals("name"))t.name=str(b,a,stop-a);
    else if(kind.equals("stsd")){int q=a+8;if(q+16<=stop){int box=(int)u(b,q,4);String code=str(b,q+4,4);t.codec=code;if(code.equals("avc1")||code.equals("avc3")||code.equals("hvc1")||code.equals("hev1")||code.equals("av01")||code.equals("vp09")){i.codec=code.startsWith("avc")?"H.264":(code.equals("hvc1")||code.equals("hev1"))?"HEVC":code.equals("av01")?"AV1":"VP9";i.width=(int)u(b,q+32,2);i.height=(int)u(b,q+34,2);int v=q+86;while(v+8<=stop&&v<q+box){int vs=(int)u(b,v,4);if(vs<8||v+vs>stop)break;String vk=str(b,v+4,4);if(vk.equals("avcC")&&vs>=12){int profile=b[v+9]&255;i.bits=profile==110||profile==122||profile==244?10:8;}if(vk.equals("hvcC")&&vs>=27)i.bits=8+(b[v+25]&7);if(vk.equals("dvcC")||vk.equals("dvvC"))i.dv=true;if(vk.equals("colr")&&vs>=18&&str(b,v+8,4).equals("nclx")){int tr=(int)u(b,v+14,2);i.hdr|=tr==16||tr==18;}v+=vs;}}
     else if(code.equals("tx3g")){long flags=u(b,q+16,4);t.forced=(flags&0x40000000L)!=0;t.full=!t.forced;}else if(code.equals("wvtt")||code.equals("stpp"))t.full=false;
    }}
   }if(!full)return;p+=(int)size;
  }}
}
