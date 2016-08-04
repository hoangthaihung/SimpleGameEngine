package simpleengine.gameschool.com.simplegameengine;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SimpleGameEngine extends Activity {
    GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gameView = new GameView(this);
        setContentView(gameView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameView.onResume();
    }

    public class GameView extends SurfaceView implements Runnable {
        Thread gameThread = null;
        SurfaceHolder ourHolder;    // When we use Paint and Canvas in a thread,
        // we will see it in action in the draw method soon.

        // a boolean which we will set and unset, when the game is running or not
        volatile boolean playing;

        // a Canvas and a Paint object
        Canvas canvas;
        Paint paint;

        // This variable tracks the game frame rate
        long fps;

        // This is used to help calculate the fps
        private long timeThisFrame;

        // Declare an object of type Bitmap
        Bitmap bitmapBob;

        // Bob starts off not moving
        boolean isMoving = false;
        // He can walk at 150 pixels per second
        float walkSpeedPerSecond = 150;

        // He starts 10 pixels from the left
        float bobXPosition = 10;

        /*
         * When we initialize by calling new GameView(...)
         */
        public GameView(Context context) {
            super(context);

            // Init ourHolder and paint objects
            ourHolder = getHolder();
            paint = new Paint();

            // Load Bob from bob.png file
            bitmapBob = BitmapFactory.decodeResource(this.getResources(), R.drawable.bob);
        }

        public void draw() {
            // Make sure our drawing surface is valid or we crash
            if (ourHolder.getSurface().isValid()) {
                // Lock the canvas ready to draw, make the drawing surface our canvas object
                canvas = ourHolder.lockCanvas();

                // Draw the background color
                canvas.drawColor(Color.argb(255,26,128,182));

                // Choose the brush color for drawing
                paint.setColor(Color.argb(255,249,129,0));
                paint.setTextSize(45);

                // Display the current fps on the screen
                canvas.drawText("FPS: "+fps,20,40,paint);

                // Draw Bob at bobXPosition, 200 pixels
                canvas.drawBitmap(bitmapBob,bobXPosition,200,paint);

                // unlock the drawing surface
                ourHolder.unlockCanvasAndPost(canvas);
            }
        }

        @Override
        public void run() {
            while (playing) {
                // Capture the current time in milliseconds in startFrameTime
                long startFrameTime = System.currentTimeMillis();

                // update the frame
                update();

                // draw the frame
                draw();

                // calculate the fps this frame, we can then use the result to time animations and more
                timeThisFrame = System.currentTimeMillis() - startFrameTime;
                if(timeThisFrame > 0) {
                    fps = 1000 / timeThisFrame;
                }
            }
        }

        public void update() {
            // If Bob is moving (player is touching the screen)
            // --> then move him to the right based on his target speed and the current fps.
            if(isMoving){
                bobXPosition = bobXPosition + (walkSpeedPerSecond / fps);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                // player has touched the screen
                case MotionEvent.ACTION_DOWN:
                    // set isMoving so Bob is moved int he update method
                    isMoving = true;
                    break;

                // player has removed finger from screen
                case MotionEvent.ACTION_UP:
                    // set isMoving so Bob does not move
                    isMoving = false;
                    break;
            }
            return true;
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
        }

        protected void onPause() {
            playing = false;
            try{
                gameThread.join();
            } catch (InterruptedException e) {
                Log.e("Error: ", "joining thread.");
            }
        }

        protected void onResume() {
            playing = true;
            gameThread = new Thread(this);
            gameThread.start();
        }
    }
}
