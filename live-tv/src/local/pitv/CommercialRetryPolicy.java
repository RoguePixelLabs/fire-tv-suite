package local.pitv;
/** Retry budget counts consecutive failures, reset only when a video renders. */
final class CommercialRetryPolicy {
 static long delay(int failures,int clips){return failures>=Math.min(5,Math.max(1,clips))?-1:Math.min(8000,1000L<<Math.min(3,Math.max(0,failures)));}
}
