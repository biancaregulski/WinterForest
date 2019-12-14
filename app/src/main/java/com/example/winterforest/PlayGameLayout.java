package com.example.winterforest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.ImageView;

public class PlayGameLayout extends SurfaceView implements Runnable {

    Canvas canvas;
    Thread thread = null;
    boolean canDraw = false;

    private Path backgroundPath;
    private Paint basePaint, fillPaint, strokePaint;
    private Rect background;
    private SurfaceHolder surfaceHolder;

    float[] lineColumns;
    float[] lineRows;
    float[] characterColumns;
    float[] characterRows;
    int screenWidth;
    int screenHeight;

    Bitmap character_bm;
    float character_x, character_y;

    // maximum of 10 trees
    Bitmap[] tree_bm = new Bitmap[10];
    float[] tree_x = new float[10];
    float[] tree_y = new float[10];

    // maximum of 10 stones
    Bitmap[] stone_bm = new Bitmap[10];
    float[] stone_x = new float[10];
    float[] stone_y = new float[10];

    private ImageView tree;
    private ViewGroup rootLayout;
    private Context context;

    public PlayGameLayout(Context context) {
        super(context);
    }

    public PlayGameLayout(Context context, Display display) {
        super(context);
        this.context = context;

        surfaceHolder = getHolder();

        // make bitmap and set its coordinates
        character_bm = BitmapFactory.decodeResource(getResources(), R.drawable.snowflake);
        for (int i = 0; i < 3; i++) {
            tree_bm[i] = BitmapFactory.decodeResource(getResources(), R.drawable.tree);
        }

        stone_bm[0] = BitmapFactory.decodeResource(getResources(), R.drawable.stone);

        // get custom dimensions of screen
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

        // get line path coordinates for screen
        lineColumns = new float[] {4 / 40f * screenWidth, 10 / 40f * screenWidth,
                30 / 40f * screenWidth, 36 / 40f * screenWidth};
        lineRows = new float[] {6 / 40f * screenHeight, 10 / 40f * screenHeight,
                14 / 40f * screenHeight, 18 / 40f * screenHeight, 22 / 40f * screenHeight,
                26 / 40f * screenHeight, 30 / 40f * screenHeight, 34 / 40f * screenHeight};

        // get character path coordinates for screen
        characterColumns = new float[] {7 / 40f * screenWidth, 33 / 40f * screenWidth};
        characterRows = new float[] {8 / 40f * screenHeight, 16 / 40f * screenHeight,
                24 / 40f * screenHeight, 32 / 40f * screenHeight};

        // set default coordinates for character bitmap
        character_x = characterColumns[0];
        character_y = screenHeight;

        tree_x[0] = characterColumns[0];
        tree_y[0] = characterRows[1];

        tree_x[1] = characterColumns[1];
        tree_y[1] = characterRows[2];

        tree_x[2] = characterColumns[1];
        tree_y[2] = characterRows[0];

        stone_x[0] = lineColumns[1];
        stone_y[0] = characterRows[0];
    }


    @Override
    public void run() {
        createPaints();

        while(canDraw) {
            if (!surfaceHolder.getSurface().isValid()) {
                continue;
            }

            canvas = surfaceHolder.lockCanvas();
            createBackgroundPath();
            int speed =  10;
            characterMotion(speed);

            canvas.drawBitmap(character_bm, character_x - (character_bm.getWidth() / 2),
                    character_y - (character_bm.getHeight() / 2), null);
            for (int i = 0; i < 3; i++) {
                canvas.drawBitmap(tree_bm[i], tree_x[i] - (tree_bm[i].getWidth() / 2),
                        tree_y[i] - (tree_bm[i].getHeight() / 2), null);
            }
            canvas.drawBitmap(stone_bm[0], stone_x[0] - (stone_bm[0].getWidth() / 2),
                    stone_y[0] - (stone_bm[0].getHeight() / 2), null);

            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    public void pause() {
        canDraw = false;
        while (true) {
            try {
                if (thread != null)
                    {
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

    private void createBackgroundPath() {
        // create shape of backgroundPath
        backgroundPath = new Path();
        backgroundPath.moveTo(lineColumns[0], 0);
        backgroundPath.lineTo(lineColumns[0], lineRows[1]);
        backgroundPath.lineTo(lineColumns[2], lineRows[1]);
        backgroundPath.lineTo(lineColumns[2], lineRows[2]);
        backgroundPath.lineTo(lineColumns[0], lineRows[2]);
        backgroundPath.lineTo(lineColumns[0], lineRows[5]);
        backgroundPath.lineTo(lineColumns[2], lineRows[5]);
        backgroundPath.lineTo(lineColumns[2], lineRows[6]);
        backgroundPath.lineTo(lineColumns[0], lineRows[6]);
        backgroundPath.lineTo(lineColumns[0], screenHeight);
        backgroundPath.lineTo(lineColumns[1], screenHeight);
        backgroundPath.lineTo(lineColumns[1], lineRows[7]);
        backgroundPath.lineTo(lineColumns[3], lineRows[7]);
        backgroundPath.lineTo(lineColumns[3], lineRows[4]);
        backgroundPath.lineTo(lineColumns[1], lineRows[4]);
        backgroundPath.lineTo(lineColumns[1], lineRows[3]);
        backgroundPath.lineTo(lineColumns[3], lineRows[3]);
        backgroundPath.lineTo(lineColumns[3], lineRows[0]);
        backgroundPath.lineTo(lineColumns[1], lineRows[0]);
        backgroundPath.lineTo(lineColumns[1], 0);
        backgroundPath.close();


        fillPaint = new Paint();
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(android.graphics.Color.WHITE);

        this.canvas.drawRect(background, basePaint);
        this.canvas.drawPath(backgroundPath, fillPaint);
        this.canvas.drawPath(backgroundPath, strokePaint);
    }

    private void createPaints() {
        // fill in background with purple
        basePaint = new Paint();
        background = new Rect(0, 0, screenWidth, screenHeight);
        basePaint.setStyle(Paint.Style.FILL);
        basePaint.setColor(getResources().getColor(R.color.purple));
        // fill in backgroundPath with white
        fillPaint = new Paint();
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(android.graphics.Color.WHITE);
        // stroke backgroundPath with black
        strokePaint = new Paint();
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(android.graphics.Color.BLACK);
        strokePaint.setStrokeWidth(screenWidth / 40);
    }

    private void characterMotion(int speed) {
        //Log.v("DEBUG", "x = " + character_x + "and y = " + character_y);

        // if reach top of screen, return to bottom
        if (character_y <= speed){
            character_y = screenHeight;
            updatePoints(10);
        }

        // move up
        else if (character_x <= characterColumns[0] &&  (character_y <= characterRows[0])) {
            character_x = characterColumns[0];
            character_y -= speed;
        }

        // move left
        else if (character_x <= characterColumns[1] && character_y <= characterRows[0]) {
            character_x -= speed;
            character_y = characterRows[0];
        }

        // move up
        else if (character_x >= characterColumns[1] &&
                (character_y <= characterRows[1])) {
            character_x = characterColumns[1];
            character_y -= speed;
        }

        // move right
        else if (character_x >= characterColumns[0] && character_y <= characterRows[1]) {
            character_x += speed;
            character_y = characterRows[1];
        }

        // move up
        else if (character_x <= characterColumns[0] && character_y <= characterRows[2]) {
            character_x = characterColumns[0];
            character_y -= speed;
        }

        // move left
        else if (character_x <= characterColumns[1] && character_y <= characterRows[2]) {
            character_x -= speed;
            character_y = characterRows[2];
        }

        // move up
        else if (character_x >= characterColumns[1] && character_y <= characterRows[3]) {
            character_x = characterColumns[1];
            character_y -= speed;
        }

        //  move right
        else if (character_x >= characterColumns[0] && character_y <= characterRows[3]) {
            character_x += speed;
            character_y = characterRows[3];
        }

        // move up
        else {
            character_y -= speed;
        }
    }
    private void updatePoints(final int num) {
        ((PlayGame)context).runOnUiThread(new Runnable() {

            @Override
            public void run() {

                ((PlayGame) context).addPoints(num);

            }
        });
    }
}