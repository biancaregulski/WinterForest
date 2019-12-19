package com.example.winterforest;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

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

    Avatar avatar = new Avatar();

    Obstacle [] obstacles = new Obstacle[30];
    Bitmap tree_bm;
    Bitmap stone_bm;
    Bitmap avatar_bm;

    private ImageView tree;
    private ViewGroup rootLayout;
    private Context context;

    int speed =  7;

    public PlayGameLayout(Context context) {
        super(context);
    }

    public PlayGameLayout(Context context, Display display) {
        super(context);
        this.context = context;

        surfaceHolder = getHolder();

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
                24 / 40f * screenHeight, 32 / 40f * screenHeight};

        // set default coordinates for avatar bitmap
        avatar.x = avatarColumns[0];
        avatar.y = screenHeight;

        obstacles[0] = new Obstacle();
        obstacles[0].x = avatarColumns[0] - (tree_bm.getWidth() / 2);
        obstacles[0].y = avatarRows[1] - (tree_bm.getHeight() / 2);
        obstacles[0].type = "tree";

        obstacles[1] = new Obstacle();
        obstacles[1].x = avatarColumns[1] - (tree_bm.getWidth() / 2);
        obstacles[1].y = avatarRows[2] - (tree_bm.getHeight() / 2);
        obstacles[1].type = "tree";

        obstacles[2] = new Obstacle();
        obstacles[2].x = avatarColumns[1] - (tree_bm.getWidth() / 2);
        obstacles[2].y = avatarRows[0] - (tree_bm.getHeight() / 2);
        obstacles[2].type = "tree";

        obstacles[3] = new Obstacle();
        obstacles[3].x = lineColumns[1] - (stone_bm.getWidth() / 2);
        obstacles[3].y = avatarRows[0] - (stone_bm.getHeight() / 2);
        obstacles[3].type = "stone";

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
            for (int i = 0; i < 3; i++) {
                canvas.drawBitmap(tree_bm, obstacles[i].x, obstacles[i].y, null);
            }
            canvas.drawBitmap(stone_bm, obstacles[3].x, obstacles[3].y, null);

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
                for (int i = 0; i < 30; i++) {
                    if (obstacles[i] != null) {
                        // check if obstacle is being clicked
                        if (clickOnBitmap(i, event)) {
                            dragBitmap = true;

                            // set initial values in case illegal drop takes place eventually
                            xInitial = obstacles[i].x;
                            yInitial = obstacles[i].y;

                            xPrev = (int)event.getX();
                            yPrev = (int)event.getY();
                            currentObstacle = i;
                            break;
                        }
                    }
                }

            // TODO: make dragged bitmap show on top
            case MotionEvent.ACTION_MOVE:
            if (dragBitmap == true) {
                    obstacles[currentObstacle].x += (int)event.getX() - xPrev;
                    obstacles[currentObstacle].y += (int)event.getY() - yPrev;
                    xPrev = (int)event.getX();
                    yPrev = (int)event.getY();
                }
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                // if bitmap is not in the path, return it back to initial position
                float slope;
                // check if bottom of tree stump is within path
                // TODO: combine path and stroke
                int checkX = (int) obstacles[currentObstacle].x + (current_bm.getWidth() / 2);
                int checkY = (int) obstacles[currentObstacle].y + current_bm.getHeight();
                // TODO: make different check values for stone

                if(!region.contains(checkX, checkY)) {
                    slope = (yInitial - checkY) / (xInitial - checkX);
                    obstacles[currentObstacle].x = xInitial;
                    obstacles[currentObstacle].y = yInitial;
                }
                dragBitmap = false;
                invalidate();
                break;
        }
        return true;
    }

    // TODO: make motion interesting
    private void obstacleMotion() {
        //if (obstacles[0].x != xInitial && obstacles[0].y != yInitial){

        //}
    }

    private boolean clickOnBitmap(int index, MotionEvent event) {
        if (obstacles[index].type == "tree") {
            current_bm = tree_bm;
        }
        //else if (obstacle.type == "stone"){
        else {
            current_bm = stone_bm;
        }
        float xStart = obstacles[index].x;
        float yStart = obstacles[index].y;
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

        RectF rectF = new RectF();
        backgroundPath.computeBounds(rectF, true);
        region = new Region();
        region.setPath(backgroundPath, new Region((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom));

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
        strokePaint.setStrokeWidth(strokeWidth);
    }

    private void avatarMotion(int speed) {
        // TODO: check if snowflake hits obstacle (how?)

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

                ((PlayGame) context).addPoints(num);

            }
        });
    }
}