package local.ondemand;
import android.view.*;import android.widget.*;import org.json.*;
/** End-of-episode handoff, cancelled whenever playback is left or backgrounded. */
final class AutoNext {
 final MainActivity a;final VodPlayer owner;final JSONObject series,next;final Button cancel;int seconds=6;boolean closed;final Runnable tick=new Runnable(){public void run(){if(closed||a.player!=owner||!a.foreground)return;if(a.playbackUi!=null){a.playbackUi.loadingTitle.setText("Up next · S"+next.optInt("season")+" E"+next.optInt("episode"));a.playbackUi.loadingNote.setText(next.optString("title",next.optString("name","Next episode"))+" · starts in "+seconds+" seconds");}if(seconds--<=0){advance();return;}a.clock.postDelayed(this,1000);}};
 AutoNext(MainActivity app,VodPlayer p,JSONObject full,JSONObject episode){a=app;owner=p;series=full;next=episode;cancel=a.button("Cancel autoplay",()->{close();a.autoNext=null;if(a.playbackUi!=null){a.playbackUi.refresh();if(a.playbackFirst!=null)a.playbackFirst.requestFocus();}});cancel.setTag("autoplay:cancel");if(a.playbackUi!=null){a.playbackUi.loading.addView(cancel);cancel.requestFocus();}a.clock.post(tick);}
 void advance(){if(closed||a.player!=owner||!a.foreground)return;close();a.autoNext=null;a.release();while(a.back.size()>Math.max(0,a.episodeReturnDepth)){a.back.pop();if(!a.focusBack.isEmpty())a.focusBack.pop();}a.restoreFocus="";a.autoPlayEpisodeId=next.optString("id");a.seriesSeasons.put(series.optString("id"),next.optInt("season"));a.current=()->TitlePages.episode(a,series,next,next.optInt("season"));a.current.run();}
 void close(){if(closed)return;closed=true;a.clock.removeCallbacks(tick);if(cancel.getParent() instanceof ViewGroup)((ViewGroup)cancel.getParent()).removeView(cancel);}
}
