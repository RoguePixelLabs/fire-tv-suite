package local.pitv;
/** Pure decision policy: one video/buffer trial and one independent audio recovery trial per tune, with rollback. */
final class PlaybackHealthPolicy {
 enum Action {NONE,SOFTWARE,BUFFER,RETRY,AUDIO,SAVE_AUDIO,SAVE_SOFTWARE,SAVE_BUFFER,ROLLBACK}
 boolean attempted=false,audioAttempted=false,audioFallbackActive=false,retryCooldown=false,videoCooldown=false,bufferCooldown=false;int missingVideo,badVideo,bufferingWindows,stalled,missingAudio,trialWindows,healthyWindows,hotWindows;String trial="";String status="Watching playback";
 Action evaluate(boolean software,boolean network,boolean expectedVideo,boolean expectedAudio,int shown,int lost,int audio,int rebuffers,int corrupt,double cpuCores,boolean buffering){return evaluate(software,network,expectedVideo,expectedAudio,shown,lost,audio,0,rebuffers,corrupt,cpuCores,buffering);}
 Action evaluate(boolean software,boolean network,boolean expectedVideo,boolean expectedAudio,int shown,int lost,int audio,int lostAudio,int rebuffers,int corrupt,double cpuCores,boolean buffering){
  boolean videoOK=!expectedVideo||shown>=30;boolean audioOK=!expectedAudio||(audio>0&&(audio+lostAudio==0||lostAudio/(double)(audio+lostAudio)<.10));
  double loss=shown+lost>0?lost/(double)(shown+lost):0;
  boolean healthy=videoOK&&audioOK&&loss<.10&&!buffering&&rebuffers==0&&corrupt==0;
  if(!trial.isEmpty()){
   int required=trial.equals("audio")?18:6;trialWindows++;if(healthy)healthyWindows++;if(cpuCores>2.5)hotWindows++;
   if(((trial.equals("software")||trial.equals("audio"))&&hotWindows>=2)||(trialWindows>=3&&!videoOK&&expectedVideo)||(trialWindows>=required&&healthyWindows<required-1)){
    trial="";status="Trial did not help; restored previous settings";return Action.ROLLBACK;
   }
   if(trialWindows>=required&&healthyWindows>=required-1){Action save=trial.equals("software")?Action.SAVE_SOFTWARE:trial.equals("audio")?Action.SAVE_AUDIO:Action.SAVE_BUFFER;trial="";status="Verified adjustment saved for this channel";return save;}
   return Action.NONE;
  }
  if(network&&expectedAudio&&!audioOK&&shown>=30&&!buffering&&rebuffers==0&&corrupt==0)missingAudio++;else missingAudio=0;
  if(missingAudio>=3&&audioFallbackActive)status="Audio recovery still failing; no repeated repair loop";
  if(missingAudio>=3&&!audioAttempted&&!audioFallbackActive){audioAttempted=true;trial="audio";trialWindows=healthyWindows=hotWindows=0;status="Testing stereo audio and timestamp repair";return Action.AUDIO;}
  if(attempted)return Action.NONE;
  if(corrupt>0){status="Source errors detected; decoder unchanged";badVideo=0;return Action.NONE;}
  if(network&&(buffering||rebuffers>0))bufferingWindows++;else bufferingWindows=Math.max(0,bufferingWindows-1);
  if(expectedVideo&&shown+lost>=50&&loss>.30&&!buffering&&rebuffers==0&&audioOK)badVideo++;else badVideo=0;
  if(shown==0&&audio==0)stalled++;else stalled=0;
  if(expectedVideo&&shown==0&&audio>0&&!buffering&&rebuffers==0)missingVideo++;else missingVideo=0;
  if(network&&missingVideo>=3&&!retryCooldown){retryCooldown=true;status="Audio is progressing but no pictures are presented; reconnecting once";return Action.RETRY;}
  if(missingVideo>=3&&retryCooldown)status="Missing pictures persist; reconnect limit reached";

  if(!software&&badVideo>=3&&videoCooldown)status="Video frame loss detected; previous decoder trial cooling down";
  if(!software&&badVideo>=3&&!videoCooldown){attempted=true;trial="software";trialWindows=healthyWindows=hotWindows=0;status="Testing software compatibility";return Action.SOFTWARE;}
  if(network&&bufferingWindows>=3&&!bufferCooldown){attempted=true;trial="buffer";trialWindows=healthyWindows=hotWindows=0;status="Testing larger network buffer";return Action.BUFFER;}
  if(network&&!retryCooldown&&(stalled>=3||missingAudio>=3)){retryCooldown=true;status="Retrying stalled stream once";return Action.RETRY;}
  return Action.NONE;
 }
}
