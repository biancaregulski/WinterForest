package com.example.winterforest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;

public class PlayGameLayout extends SurfaceView implements Runnable {

    Canvas canvas;
    Thread thread = null;
    boolean canDraw = false;

    private Path backgroundPath;
    Region region;
    private Paint basePaint, fillPaint, strokePaint;
    private Rect background;
    private SurfaceHolder surfaceHolder;

    float[] lineColumns, lineRows, avatarColumns, avatarRows;
    int screenWidth, screenHeight;

    // variables for drag and drop
    boolean dragBitmap = false;
    float xInitial, yInitial;
    int xPrev, yPrev;
    int currentObstacle;
    Bitmap current_bm;
    int strokeWidth;

    // TODO: make multiple snowflakes at different times
    Avatar avatar = new Avatar();
    boolean avatar_transparent = false;

    ArrayList<Obstacle> obstacles = new ArrayList<>();
    Bitmap tree_bm;
    Bitmap stone_bm;
    Bitmap avatar_bm;

    private Context context;

    int speed =  7;
    int score;
    int lives;

    private static final int FADE_MILLISECONDS = 3000; // 3 second fade effect
    private static final int FADE_STEP = 120;          // 120ms refresh

    long timeLastDied = -1;     // initial state


    public PlayGameLayout(Context context) {
        super(context);
    }

    public PlayGameLayout(Context context, Display display) {
        super(context);
        this.context = context;

        surfaceHolder = getHolder();
        lives = 2;
        timeLastDied = 0;

        // make bitmap and set its coordinates
        avatar_bm = BitmapFactory.decodeResource(getResources(), R.drawable.snowflake_avatar);
        tree_bm = BitmapFactory.decodeResource(getResources(), R.drawable.tree);
        stone_bm = BitmapFactory.decodeResource(getResources(), R.drawable.stone);

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

        // get avatar path coordinates for screen
        avatarColumns = new float[] {7 / 40f * screenWidth, 33 / 40f * screenWidth};
        avatarRows = new float[] {8 / 40f * screenHeight, 16 / 40f * screenHeight,
                24 / 40f * screenHeight, 32 / 40f * screenHeight, 5 / 40f * screenHeight};

        // set default coordinates for avatar bitmap
        avatar.x = avatarColumns[0];
        avatar.y = screenHeight;

        obstacles.add(new Obstacle());
        obstacles.get(0).x = avatarColumns[0] - (tree_bm.getWidth() / 2);
        obstacles.get(0).y = avatarRows[1] - (tree_bm.getHeight() / 2);
        obstacles.get(0).type = "tree";

        obstacles.add(new Obstacle());
        obstacles.get(1).x = avatarColumns[1] - (tree_bm.getWidth() / 2);
        obstacles.get(1).y = avatarRows[2] - (tree_bm.getHeight() / 2);
        obstacles.get(1).type = "tree";

        obstacles.add(new Obstacle());
        obstacles.get(2).x = avatarColumns[1] - (tree_bm.getWidth() / 2);
        obstacles.get(2).y = avatarRows[0] - (tree_bm.getHeight() / 2);
        obstacles.get(2).type = "tree";

        obstacles.add(new Obstacle());
        obstacles.get(3).x = avatarColumns[0] - (tree_bm.getWidth() / 2);
        obstacles.get(3).y = avatarRows[4] - (tree_bm.getHeight() / 2);
        obstacles.get(3).type = "tree";

        strokeWidth = screenWidth / 40;         // get width of path stroke
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
            avatarMotion(speed);
            canvas.drawBitmap(avatar_bm, avatar.x - (avatar_bm.getWidth() / 2),
            avatar.y - (avatar_bm.getHeight() / 2), null);
            for (int i = 0; i < 4; i++) {
                canvas.drawBitmap(tree_bm, obstacles.get(i).x, obstacles.get(i).y, null);
            }


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

    public boolean onTouchEvent(MotionEvent event) {

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                for (int i = 0; i < obstacles.size(); i++) {
                    if (obstacles.get(i) != null) {
                        // check if obstacle is being clicked
                        if (clickOnBitmap(i, event)) {
                            dragBitmap = true;
                            // make current obstacle the last in the array so that it shows up above other obstacles
                            currentObstacle = obstacles.size() - 1;
                            Collections.swap(obstacles, currentObstacle, i);

                            // set initial values in case illegal drop takes place eventually
                            xInitial = obstacles.get(currentObstacle).x;
                            yInitial = obstacles.get(currentObstacle).y;

                            xPrev = (int)event.getX();
                            yPrev = (int)event.getY();
                            break;
                        }
                    }
                }

            case MotionEvent.ACTION_MOVE:
            if (dragBitmap == true) {
                    obstacles.get(currentObstacle).x += (int)event.getX() - xPrev;
                    obstacles.get(currentObstacle).y += (int)event.getY() - yPrev;
                    xPrev = (int)event.getX();
                    yPrev = (int)event.getY();
                }
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                // if bitmap is not in the path, return it back to initial position
                int checkX = (int) obstacles.get(currentObstacle).x + (current_bm.getWidth() / 2);
                int checkY = (int) obstacles.get(currentObstacle).y + current_bm.getHeight();

                if(!region.contains(checkX, checkY)) {
                    obstacles.get(currentObstacle).x = xInitial;
                    obstacles.get(currentObstacle).y = yInitial;
                    ((PlayGame) context).playSound("fail");
                }
                dragBitmap = false;
                invalidate();
                break;
        }
        return true;
    }

    private boolean clickOnBitmap(int index, MotionEvent event) {
        if (obstacles.get(index).type == "tree") {
            current_bm = tree_bm;
        }
        else {
            current_bm = stone_bm;
        }
        float xStart = obstacles.get(index).x;
        float yStart = obstacles.get(index).y;
        float xEnd = xStart + current_bm.getWidth();
        float yEnd = yStart + current_bm.getHeight();

        // check if clicked on the bitmap
        if ((event.getX() >= xStart && event.getX() <= xEnd)
                && (event.getY() >= yStart && event.getY() <= yEnd) ) {
            int pixelX = (int) (event.getX() - xStart);
            int pixelY = (int) (event.getY() - yStart);
            if (current_bm.getPixel(pixelX, pixelY) != 0) {
                return true;
            }
            else {
                return false;
            }
        }
        return false;
    }

    private void createBackgroundPath() {
        // create shape of backgroundPath
        backgroundPath = new Path();
        backgroundPath.moveTo(lineColumns[0], 0 - strokeWidth);
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
        backgroundPath.lineTo(lineColumns[1], 0 - strokeWidth);
        backgroundPath.close();

        fillPaint = new Paint();
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(android.graphics.Color.WHITE);

        RectF rectF = new RectF();
        backgroundPath.computeBounds(rectF, true);
        region = new Region();
        region.setPath(backgroundPath, new Region((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom));

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
        strokePaint.setStrokeWidth(strokeWidth);
    }

    private void avatarMotion(int speed) {

        long currentTime = SystemClock.elapsedRealtime();
        // check if 3000ms or more have passed since last life lost
        if (currentTime == -1 || currentTime - timeLastDied > 3000) {

            if (avatar_transparent == true) {
                // change avatar back to blue
                avatar_bm = BitmapFactory.decodeResource(getResources(), R.drawable.snowflake_avatar);
                avatar_transparent = true;
            }

            // check if snowflake hits any obstacle
            for (int i = 0; i < 4; i++) {
                if (isCollisionDetected(avatar_bm, (int) avatar.x, (int) avatar.y,
                        tree_bm, (int) obstacles.get(i).x, (int) obstacles.get(i).y)) {
                    // lost a life
                    lives -= 1;
                    timeLastDied = currentTime;
                    if (lives == 0) {
                        // GAME OVER
                        Intent intent = new Intent().setClass(getContext(), GameOverActivity.class);
                        intent.putExtra("score", score);
                        (getContext()).startActivity(intent);
                        break;
                    }
                    else {
                        ((PlayGame) context).playSound("lose");
                        // temporarily change transparency of avatar
                        avatar_bm = BitmapFactory.decodeResource(getResources(), R.drawable.snowflake_avatar_transparent);
                        avatar_transparent = true;
                    }
                }
            }
        }

        // if reach top of screen, return to bottom
        if (avatar.y <= speed){
            avatar.y = screenHeight;
            updatePoints(10);
        }

        // move up
        else if (avatar.x <= avatarColumns[0] &&  (avatar.y <= avatarRows[0])) {
            avatar.x = avatarColumns[0];
            avatar.y -= speed;
        }

        // move left
        else if (avatar.x <= avatarColumns[1] && avatar.y <= avatarRows[0]) {
            avatar.x -= speed;
            avatar.y = avatarRows[0];
        }

        // move up
        else if (avatar.x >= avatarColumns[1] &&
                (avatar.y <= avatarRows[1])) {
            avatar.x = avatarColumns[1];
            avatar.y -= speed;
        }

        // move right
        else if (avatar.x >= avatarColumns[0] && avatar.y <= avatarRows[1]) {
            avatar.x += speed;
            avatar.y = avatarRows[1];
        }

        // move up
        else if (avatar.x <= avatarColumns[0] && avatar.y <= avatarRows[2]) {
            avatar.x = avatarColumns[0];
            avatar.y -= speed;
        }

        // move left
        else if (avatar.x <= avatarColumns[1] && avatar.y <= avatarRows[2]) {
            avatar.x -= speed;
            avatar.y = avatarRows[2];
        }

        // move up
        else if (avatar.x >= avatarColumns[1] && avatar.y <= avatarRows[3]) {
            avatar.x = avatarColumns[1];
            avatar.y -= speed;
        }

        //  move right
        else if (avatar.x >= avatarColumns[0] && avatar.y <= avatarRows[3]) {
            avatar.x += speed;
            avatar.y = avatarRows[3];
        }

        // move up
        else {
            avatar.y -= speed;
        }
    }
    private void updatePoints(final int num) {
        ((PlayGame)context).runOnUiThread(new Runnable() {

            @Override
            public void run() {
                ((PlayGame) context).playSound("points");
                ((PlayGame) context).addPoints(num);
                score = ((PlayGame) context).getPoints();
            }
        });
    }

     // check if two bitmaps are colliding
    public boolean isCollisionDetected(Bitmap avatarBitmap, int avatarX, int avatarY,
                                       Bitmap obstacleBitmap, int obstacleX, int obstacleY) {
        if (obstacleBitmap == null || avatarBitmap == null) {
            throw new IllegalArgumentException("Error: null bitmap");
        }

        if (obstacleX <= avatarX && (obstacleX + tree_bm.getWidth()) >= avatarX
            && obstacleY <= avatarY && (obstacleY + tree_bm.getHeight()) >= avatarY) {
            return true;
        }
        return false;
    }
}