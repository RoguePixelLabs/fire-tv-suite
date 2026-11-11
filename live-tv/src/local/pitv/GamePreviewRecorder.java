package local.pitv;
import android.app.*;import android.graphics.*;import android.os.*;import java.io.*;
final class GamePreviewRecorder {
 static void record(UiAutomation ui,File file)throws Exception{File folder=new File(file.getPath()+".frames");folder.mkdirs();long start=SystemClock.elapsedRealtime();int count=0;try(PrintWriter times=new PrintWriter(new File(folder,"times.txt"))){while(SystemClock.elapsedRealtime()-start<18000){Bitmap full=ui.takeScreenshot();if(full==null)throw new IOException("Screenshot unavailable");Bitmap frame=Bitmap.createScaledBitmap(full,480,270,true);String name=String.format(java.util.Locale.US,"frame-%04d.jpg",count++);try(FileOutputStream out=new FileOutputStream(new File(folder,name))){frame.compress(Bitmap.CompressFormat.JPEG,80,out);}times.println(name+" "+(SystemClock.elapsedRealtime()-start));frame.recycle();full.recycle();SystemClock.sleep(80);}}if(count<3)throw new IOException("Not enough preview frames");}
}
