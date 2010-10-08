package name.aruniyer.andropulsate;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

public class AndroPulsate extends Activity {
  
  /** 
   * Called when the activity is first created. 
   * */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(new SampleView(this));
  }

  private static class SampleView extends View {
    
    private Paint mPaint;
    private ToneGenerator toneGenerator;
    
    public SampleView(Context context) {
      super(context);

      mPaint = new Paint();
      mPaint.setAntiAlias(true);
      mPaint.setStyle(Paint.Style.STROKE);
      mPaint.setColor(0x88FF0000);
      mPaint.setColor(Color.RED);
      
      toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
    }

    private List<Center> centers = new LinkedList<Center>();
    
    protected void onDraw(Canvas canvas) {
      mPaint.setColor(Color.BLACK);
      draw(centers, canvas);
      
      mPaint.setColor(Color.RED);
      
      // adjust all radii
      List<Center> removeList = new LinkedList<Center>();
      int LIMIT = Math.max(canvas.getWidth(), canvas.getHeight());
      for (int i = 0; i < centers.size(); i++) {
        Center center = centers.get(i);
        center.radius = (center.direction) ? (center.radius + 1) : (center.radius - 1);
        if (center.radius > LIMIT)
          removeList.add(center);
        if (center.radius <= 1)
          center.direction = true;
      }
      
      // remove circles that got moved out of screen
      for (Center center : removeList)
        centers.remove(center);
      
      // compute circles that collide
      List<Pair<Center, Center>> collisionPairs = getCollisionPairs(centers);
      
      // get circles that don't collide
      List<Center> nonColliders = getNonColliders(centers, collisionPairs);
      
      // draw the non colliding circles
      draw(nonColliders, canvas);
      
      // draw the colliding circles
      for (Pair<Center, Center> pair : collisionPairs) {
        canvas.drawCircle(pair.first.x, pair.first.y, pair.first.radius, mPaint);
        canvas.drawCircle(pair.second.x, pair.second.y, pair.second.radius, mPaint);
        // do sound
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_CALL_SIGNAL_ISDN_NORMAL, 10);
      }
      
      invalidate();
    }
    
    private void draw(List<Center> centers, Canvas canvas) {
      for (int i = 0; i < centers.size(); i++) {
        Center center = centers.get(i);
        canvas.drawCircle(center.x, center.y, center.radius, mPaint);
      }
    }
    
    // Naive collision detection
    private static List<Pair<Center, Center>> getCollisionPairs(List<Center> centers) {
      List<Pair<Center, Center>> pairs = new LinkedList<Pair<Center,Center>>();
      
      for (int i = 0; i < centers.size(); i++) {
        for (int j = i + 1; j < centers.size(); j++) {
          Center center1 = centers.get(i);
          Center center2 = centers.get(j);
          if (isCollision(center1, center2)) {
            pairs.add(new Pair<Center, Center>(center1, center2));
          }
        }
      }
      return pairs;
    }
    
    private List<Center> getNonColliders(List<Center> centers,
        List<Pair<Center, Center>> collisionPairs) {
      Set<Center> centerSet = new HashSet<Center>();
      for (Pair<Center, Center> pair : collisionPairs) {
        centerSet.add(pair.first);
        centerSet.add(pair.second);
      }
      List<Center> nonColliders = new LinkedList<Center>();
      for (Center center : centers) {
        if (!centerSet.contains(center))
          nonColliders.add(center);
      }
      return nonColliders;
    }
    
    // Two circle collision detection
    private static boolean isCollision(Center center1, Center center2) {
      int dx = center1.x - center2.x;
      int dy = center1.y - center2.y;
      
      int r1 = center1.radius;
      int r2 = center2.radius;
      int maxRadius = Math.max (r1, r2);
      int centDist = (dx * dx + dy * dy); 
      if (centDist > maxRadius * maxRadius) {
        // external collision
        int a = r1 + r2;
        if (a * a >= (dx * dx + dy * dy)) {
          center1.direction = false;
          center2.direction = false;
          return true;
        } else          
          return false;
      } else {
        // internal collision
        int a = r1 - r2;
        if (a * a <= (dx * dx + dy * dy)) {
          center1.direction = !center1.direction;
          center2.direction = !center2.direction;
          return true;
        } else
          return false;
      }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
      if (event.getAction() != MotionEvent.ACTION_DOWN)
        return false;
      Center center = new Center((int) event.getX(), (int) event.getY(), 1, true);
      centers.add(center);
      return true;
    }
    
    private static class Center {
      
      int x;
      int y;
      int radius;
      boolean direction;
      
      public Center(int x, int y, int radius, boolean direction) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.direction = direction;
      }
             
    }
    
  }
  
}