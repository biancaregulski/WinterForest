package com.example.winterforest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.Random;

public class SnowLayout extends SurfaceView implements Runnable {

    Canvas canvas;
    Thread thread = null;
    boolean canDraw = false;


    private Paint basePaint;
    private Rect background;
    private SurfaceHolder surfaceHolder;

    int screenWidth;
    int screenHeight;

    Snowflake [] snowflakes = new Snowflake[60];
    Bitmap [] bitmap = new Bitmap[3];

    private ImageView tree;
    private ViewGroup rootLayout;
    private Context context;
    Random rand;

    public SnowLayout(Context context) {
        super(context);
    }

    public SnowLayout(Context context, Display display) {
        super(context);
        this.context = context;

        surfaceHolder = getHolder();

        // get custom dimensions of screen
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

        rand = new Random();

        bitmap[0] = BitmapFactory.decodeResource(getResources(), R.drawable.snowflake1);
        bitmap[1] = BitmapFactory.decodeResource(getResources(), R.drawable.snowflake2);
        bitmap[2] = BitmapFactory.decodeResource(getResources(), R.drawable.snowflake3);
        createSnowflakes();
    }

    // TODO: make snowflakes start at top
    private void createSnowflakes() {
        int previousRandom = 0;


        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 20; j++) {
                snowflakes[j + (20 * i)] = new Snowflake();
                snowflakes[j + (20 * i)].bitmapIndex = i;            // assign one of three bitmap indexes
                snowflakes[j + (20 * i)].x = (j + 1) / 21f * screenWidth;

                // prevent using the same y-value for 2 snowflakes in a row
                int random;
                while ((random = rand.nextInt(19)) != previousRandom) {
                    snowflakes[j + (20 * i)].y = random / 20f * screenHeight;   // random y-value on screen
                }
                previousRandom = random;
            }
        }

    }

    @Override
    public void run() {
        createPaint();

        while (canDraw) {
            if (!surfaceHolder.getSurface().isValid()) {
                continue;
            }
            canvas = surfaceHolder.lockCanvas();
            canvas.drawRect(background, basePaint);
            avatarMotion();

            for (int i = 0; i < 60; i++) {
                canvas.drawBitmap(bitmap[snowflakes[i].bitmapIndex], snowflakes[i].x - (bitmap[snowflakes[i].bitmapIndex].getWidth() / 2),
                        snowflakes[i].y - (bitmap[snowflakes[i].bitmapIndex].getHeight() / 2), null);
            }

            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    public void pause() {
        canDraw = false;
        while (true) {
            try {
                if (thread != null) {
                    thread.join();
                }
                break;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        thread = null;
    }

    public void resume() {
        canDraw = true;
        thread = new Thread(this);
        thread.start();
    }
    private void createPaint() {
        // fill in background with purple
        basePaint = new Paint();
        background = new Rect(0, 0, screenWidth, screenHeight);
        basePaint.setStyle(Paint.Style.FILL);
        basePaint.setColor(getResources().getColor(R.color.purple));
    }

private void avatarMotion() {
        int speed;
        for (int i = 0; i < 60; i++) {
            speed = snowflakes[i].bitmapIndex + 2;
            if (snowflakes[i].y <= screenHeight - speed) {
                snowflakes[i].y += speed;
            }
            // if reach bottom of screen, return to top
            else {
                snowflakes[i].y = 0;
            }
        }
    }
}