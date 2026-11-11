package local.pitv;
import android.media.audiofx.DynamicsProcessing;
final class AudioLevelProfiles {
 static boolean valid(String mode){return mode.equals("off")||mode.equals("normal")||mode.equals("night");}
 static DynamicsProcessing.Config config(String mode){return config(mode,0);}
 static DynamicsProcessing.Config config(String mode,float volumeDb){
  boolean night=mode.equals("night");
  DynamicsProcessing.Mbc mbc=new DynamicsProcessing.Mbc(true,true,1);
  mbc.setBand(0,new DynamicsProcessing.MbcBand(true,22000,8,night?450:300,night?6:3,(night?-34:-28)+volumeDb,8,(night?-58:-56)+volumeDb,2,0,night?10:6));
  return new DynamicsProcessing.Config.Builder(DynamicsProcessing.VARIANT_FAVOR_TIME_RESOLUTION,2,false,0,true,1,false,0,true)
   .setPreferredFrameDuration(5).setMbcAllChannelsTo(mbc)
   .setLimiterAllChannelsTo(new DynamicsProcessing.Limiter(true,true,0,1,80,100,(night?-8:-2)+volumeDb,0)).build();
 }
}
