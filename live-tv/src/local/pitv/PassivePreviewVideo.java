package local.pitv;
import android.content.Context;import android.graphics.Rect;import android.view.View;import android.widget.VideoView;import java.util.ArrayList;
/** Decorative preview surfaces must never become remote-navigation targets. */
final class PassivePreviewVideo extends VideoView {
 PassivePreviewVideo(Context context){super(context);setFocusable(false);setFocusableInTouchMode(false);setClickable(false);setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);}
 @Override public boolean requestFocus(int direction,Rect previous){return false;}
 @Override public void addFocusables(ArrayList<View> views,int direction,int mode){}
}
