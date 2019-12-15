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

    float[] Columns;
    float[] Rows;
    int screenWidth;
    int screenHeight;

    Bitmap[] snowflake1_bm = new Bitmap[49];
    float[] snowflake1_x = new float[49];
    float[] snowflake1_y = new float[49];

    Bitmap[] snowflake2_bm = new Bitmap[49];
    float[] snowflake2_x = new float[49];
    float[] snowflake2_y = new float[49];

    Bitmap[] snowflake3_bm = new Bitmap[49];
    float[] snowflake3_x = new float[49];
    float[] snowflake3_y = new float[49];

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

        createSnowflakes(snowflake1_bm, snowflake1_x, snowflake1_y, R.drawable.snowflake1);
        createSnowflakes(snowflake2_bm, snowflake2_x, snowflake2_y, R.drawable.snowflake2);
        createSnowflakes(snowflake3_bm, snowflake3_x, snowflake3_y, R.drawable.snowflake3);
    }

    private void createSnowflakes(Bitmap [] bitmap, float [] x, float [] y, int id) {
        int previousRandom = 0;
        for (int i = 0; i < 49; i++) {
            bitmap[i] = BitmapFactory.decodeResource(getResources(), id);
            x[i] = (i + 1) / 20f * screenWidth;

            // prevent using the same y-value for 2 snowflakes in a row
            int random;
            while ((random = rand.nextInt(49)) != previousRandom) {
                y[i] = random / 50f * screenHeight;     // random y-value on screen
            }
            previousRandom = random;
        }
    }

    @Override
    public void run() {
        createPaints();

        while (canDraw) {
            if (!surfaceHolder.getSurface().isValid()) {
                continue;
            }
            canvas = surfaceHolder.lockCanvas();
            canvas.drawRect(background, basePaint);
            characterMotion();

            for (int i = 0; i < 49; i++) {
                canvas.drawBitmap(snowflake1_bm[i], snowflake1_x[i] - (snowflake1_bm[i].getWidth() / 2),
                        snowflake1_y[i] - (snowflake1_bm[i].getHeight() / 2), null);
                canvas.drawBitmap(snowflake2_bm[i], snowflake2_x[i] - (snowflake2_bm[i].getWidth() / 2),
                        snowflake2_y[i] - (snowflake2_bm[i].getHeight() / 2), null);
                canvas.drawBitmap(snowflake3_bm[i], snowflake3_x[i] - (snowflake3_bm[i].getWidth() / 2),
                        snowflake3_y[i] - (snowflake3_bm[i].getHeight() / 2), null);
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
    private void createPaints() {
        // fill in background with purple
        basePaint = new Paint();
        background = new Rect(0, 0, screenWidth, screenHeight);
        basePaint.setStyle(Paint.Style.FILL);
        basePaint.setColor(getResources().getColor(R.color.purple));
    }

    private void characterMotion() {
        snowflakeMotion(2, snowflake1_y);
        snowflakeMotion(3, snowflake2_y);
        snowflakeMotion(4, snowflake3_y);
    }
    private void snowflakeMotion(int speed, float [] y) {
        // if reach top of screen, return to bottom
        for (int i = 0; i < 49; i++) {
            // snowflakes fall until they get to the bottom of the screen
            if (y[i] <= screenHeight - speed) {
                y[i] += speed;
            }
            // then they return to the top
            else {
                y[i] = 0;
            }
        }
    }
}