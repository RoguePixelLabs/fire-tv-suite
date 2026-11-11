package local.ondemand;
/** One decoder trial, with rollback when software cannot sustain playback. */
final class VodHealthPolicy {
 enum Action { NONE, SOFTWARE, ROLLBACK, KEEP }
 boolean attempted,trial;int badVideo,hotWindows,trialWindows,healthyWindows;
 Action evaluate(boolean software,boolean active,int shown,int lost,double seconds,double cpuCores,boolean oversized,boolean corrupt){
  if(!active||seconds<=0){badVideo=0;return Action.NONE;}
  double fps=shown/seconds,loss=shown+lost>0?lost/(double)(shown+lost):0;
  if(trial){trialWindows++;if(cpuCores>2.5||fps<12)hotWindows++;else hotWindows=0;if(fps>=20&&loss<.10&&cpuCores<2.5)healthyWindows++;
   if(hotWindows>=2||(trialWindows>=6&&healthyWindows<5)){trial=false;return Action.ROLLBACK;}
   if(trialWindows>=6){trial=false;return Action.KEEP;}return Action.NONE;}
  if(attempted||software||oversized||corrupt)return Action.NONE;
  if(shown+lost>=50&&loss>.30)badVideo++;else badVideo=0;
  if(badVideo>=3){attempted=true;trial=true;badVideo=0;return Action.SOFTWARE;}return Action.NONE;
 }
}
