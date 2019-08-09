/**
 * @author Tomi Salomaa 06/2019
 *
 */

package com.example.mobiletetrisdemo;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;


/*
 * AppCompatActivity to Activity in order to make our full screen without title work.
 */
public class GameScreen extends Activity {

    PlayfieldView playView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // No title (requires 'android:theme="@android:style/Theme.DeviceDefault"' in manifest file)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        playView = new PlayfieldView(this);
        playView.setBackgroundColor(Color.BLACK);
        setContentView(playView);
    }

    public static int screenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int screenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    public class PlayfieldView extends SurfaceView implements SurfaceHolder.Callback {

        private Playfield playfield;
        private Bitmap block;
        MediaPlayer mediaPlayer;
        private int[][] playfieldMatrix = new int[20][10];
        private Tetriminos[] tetriminos = new Tetriminos[7];
        private Tetriminos currentTetrimino;
        private int points = 0;
        private int blockSize = 30;
        private int playfieldWidth = 10;
        private int playfieldHeight = 20;
        private float screenWidthDivided = screenWidth() / 6;

        private float touchPosX;
        private float touchPosY;
        private float startX;
        private float startY;

        public PlayfieldView(Context context) {
            super(context);
            getHolder().addCallback(this);
            block = BitmapFactory.decodeResource(getResources(), R.drawable.blocks);
            playfield = new Playfield(getHolder(), this);
            setFocusable(true);

            // Defining the different tetriminos as matrixes
            // I-shape
            tetriminos[0] = new Tetriminos(Bitmap.createBitmap(block, 0, 0, blockSize, blockSize), new int[][] {
                    {1, 1, 1, 1}
            }, this, 1);

            // Box-shape
            tetriminos[1] = new Tetriminos(Bitmap.createBitmap(block, blockSize, 0, blockSize, blockSize), new int[][] {
                    {1, 1},
                    {1, 1}
            }, this, 2);

            // S-shape
            tetriminos[2] = new Tetriminos(Bitmap.createBitmap(block, 2 * blockSize, 0, blockSize, blockSize), new int[][] {
                    {0, 1, 1},
                    {1, 1, 0}
            }, this, 3);

            // >-shape
            tetriminos[3] = new Tetriminos(Bitmap.createBitmap(block, 3 * blockSize, 0, blockSize, blockSize), new int[][] {
                    {1, 1, 0},
                    {0, 1, 1}
            }, this, 4);

            // T-shape
            tetriminos[4] = new Tetriminos(Bitmap.createBitmap(block, 4 * blockSize, 0, blockSize, blockSize), new int[][] {
                    {0, 1, 0},
                    {1, 1, 1}
            }, this, 5);

            // J-shape
            tetriminos[5] = new Tetriminos(Bitmap.createBitmap(block, 5 * blockSize, 0, blockSize, blockSize), new int[][] {
                    {1, 0, 0},
                    {1, 1, 1}
            }, this, 6);

            // L-shape
            tetriminos[6] = new Tetriminos(Bitmap.createBitmap(block, 6 * blockSize, 0, blockSize, blockSize), new int[][] {
                    {0, 0, 1},
                    {1, 1, 1}
            }, this, 7);

            createTetrimino();

            mediaPlayer = MediaPlayer.create(getContext(), R.raw.tetris1);
            mediaPlayer.start();
        }

        public void update() {
            currentTetrimino.update();

            // If the first column of any row -- starting from the bottom -- has a value other than 0 (meaning it has a piece of tetrimino),
            // check if the row is full
            for (int i = playfieldHeight-1; i >= 0; i--) {
                if (playfieldMatrix[i][0] != 0) {
                    checkRow(i, 0);
                }
            }
            invalidate();
            playSound(points);
        }

        public void createTetrimino() {
            // Creates a new tetrimino by choosing a random value int
            int rand = (int)(Math.random() * tetriminos.length);
            currentTetrimino = new Tetriminos(Bitmap.createBitmap(block, rand * blockSize, 0, blockSize, blockSize),
                    tetriminos[rand].getLocation(), this, tetriminos[rand].getColor());
        }

        public void checkRow(int checkRow, int checkCol) {
            /*
             * Uses recursion to check if the row is full
             * If the row is full, "removes" the full row by copying the rows above it and moving them down a step
             */
            if (playfieldMatrix[checkRow][checkCol] != 0 && checkCol + 1 < playfieldMatrix[0].length) {
                checkRow(checkRow, checkCol + 1);
            } else if (playfieldMatrix[checkRow][checkCol] != 0 && checkCol + 1 == playfieldMatrix[0].length) {
                for (int i = checkRow; i > 0; i--) {
                    playfieldMatrix[i] = playfieldMatrix[i-1].clone();
                }
                points++;
            }
        }

        // Responsible for setting the music according to "level"
        public void playSound(int points) {

            if (!mediaPlayer.isPlaying()) {
                if (points < 5) {
                    mediaPlayer = MediaPlayer.create(getContext(), R.raw.tetris1);
                } else if (points >= 5 && points < 10) {
                    mediaPlayer = MediaPlayer.create(getContext(), R.raw.tetris1half);
                } else if (points >= 10 && points < 15) {
                    mediaPlayer = MediaPlayer.create(getContext(), R.raw.tetris2);
                } else if (points >= 15 && points < 20) {
                    mediaPlayer = MediaPlayer.create(getContext(), R.raw.tetris3);
                } else if (points >= 20) {
                    mediaPlayer = MediaPlayer.create(getContext(), R.raw.tetris4);
                }
                mediaPlayer.start();
            }
        }

        public void setPlayfieldMatrix(int yLoc, int xLoc, int blockValue) {
            playfieldMatrix[yLoc][xLoc] = blockValue;
        }

        public int[][] getPlayfieldMatrix() {
            return playfieldMatrix;
        }

        public Tetriminos[] getTetriminos() {
            return tetriminos;
        }

        public int getPlayfieldWidth() {
            return playfieldWidth;
        }

        public int getPlayfieldHeight() {
            return playfieldHeight;
        }

        public int getScore() {
            return points;
        }

        @Override
        public void draw(Canvas canvas) {
            super.draw(canvas);
            Paint paint = new Paint();

            if (canvas != null) {
                // Painting the playfield. Gray if no block, red if block.
                for (int i = 0; i < playfieldMatrix.length; i++) {
                    for (int j = 0; j < playfieldMatrix[i].length; j++) {
                        if (playfieldMatrix[i][j] == 0) {
                            paint.setColor(Color.DKGRAY);
                            canvas.drawRect(screenWidthDivided + (j * ((screenWidth() - 2 * screenWidthDivided) / 10)),
                                    ((float) 0.5 * screenWidthDivided + (i * ((screenWidth() - 2 * screenWidthDivided) / 10))),
                                    screenWidthDivided + ((j+1) * ((screenWidth() - 2 * screenWidthDivided) / 10)),
                                    ((float) 0.5 * screenWidthDivided + ((i+1) * ((screenWidth() - 2 * screenWidthDivided) / 10))),
                                    paint);
                        } else {
                            paint.setColor(Color.RED);
                            canvas.drawRect(screenWidthDivided + (j * ((screenWidth() - 2 * screenWidthDivided) / 10)),
                                    ((float) 0.5 * screenWidthDivided + (i * ((screenWidth() - 2 * screenWidthDivided) / 10))),
                                    screenWidthDivided + ((j+1) * ((screenWidth() - 2 * screenWidthDivided) / 10)),
                                    ((float) 0.5 * screenWidthDivided + ((i+1) * ((screenWidth() - 2 * screenWidthDivided) / 10))),
                                    paint);
                        }
                    }
                }

                // Painting the current block.
                paint.setColor(Color.BLUE);
                for(int y = 0; y < currentTetrimino.getLocation().length; y++) {
                    for(int x = 0; x < currentTetrimino.getLocation()[y].length; x++) {
                        if(currentTetrimino.getLocation()[y][x] != 0) {
                            canvas.drawRect(screenWidthDivided + (currentTetrimino.getCol() * ((screenWidth() - 2 * screenWidthDivided) / 10)) + (x * ((screenWidth() - 2 * screenWidthDivided) / 10)),
                                    ((float) 0.5 * screenWidthDivided + (currentTetrimino.getRow() * ((screenWidth() - 2 * screenWidthDivided) / 10)) + (y * ((screenWidth() - 2 * screenWidthDivided) / 10))),
                                    screenWidthDivided + (currentTetrimino.getCol() * ((screenWidth() - 2 * screenWidthDivided) / 10)) + ((x+1) * ((screenWidth() - 2 * screenWidthDivided) / 10)),
                                    ((float) 0.5 * screenWidthDivided + (currentTetrimino.getRow() * ((screenWidth() - 2 * screenWidthDivided) / 10)) + ((y+1) * ((screenWidth() - 2 * screenWidthDivided) / 10))),
                                    paint);
                        }
                    }
                }

                paint.setColor(Color.WHITE);
                // Vertical lines
                for (int i = 0; i < 11; i++) {
                    canvas.drawLine((screenWidthDivided) + (i * ((screenWidth() - 2 * screenWidthDivided) / 10)),
                            ((float) 0.5 * screenWidthDivided),
                            (screenWidthDivided) + (i * ((screenWidth() - 2 * screenWidthDivided) / 10)),
                            ((float) 0.5 * screenWidthDivided) + (20 * ((screenWidth() - 2 * screenWidthDivided) / 10)),
                            paint);
                }

                // Horizontal lines
                for (int i = 0; i < 21; i++) {
                    canvas.drawLine(screenWidthDivided,
                            ((float) 0.5 * screenWidthDivided) + (i * ((screenWidth() - 2 * screenWidthDivided) / 10)),
                            screenWidth() - screenWidthDivided,
                            ((float) 0.5 * screenWidthDivided) + (i * ((screenWidth() - 2 * screenWidthDivided) / 10)),
                            paint);
                }

                // Draws the current score to the bottom of the screen in yellow.
                paint.setColor(Color.YELLOW);
                paint.setTextSize(screenWidthDivided);
                canvas.drawText("SCORE: " + points, screenWidthDivided, screenHeight()-screenWidthDivided, paint);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            int currentAction = event.getAction();

            switch (currentAction) {

                case MotionEvent.ACTION_DOWN:
                    startX = event.getX();
                    startY = event.getY();

                case MotionEvent.ACTION_UP:
                    touchPosX = event.getX();
                    touchPosY = event.getY();

                    System.out.println("TouchPosX: " + touchPosX + ", " + "TouchPosY: " + touchPosY);
                    System.out.println("startX: " + startX + ", " + "startY: " + startY);

                    if (touchPosY > startY - 200 && touchPosY < startY + 200) {
                        if (touchPosX > startX + screenWidthDivided) {
                            currentTetrimino.setDelta(1);
                            startX = touchPosX;
                        } else if (touchPosX < startX - screenWidthDivided) {
                            currentTetrimino.setDelta(-1);
                            startX = touchPosX;
                        }
                    } else if (touchPosX > startX - 200 && touchPosX < startX + 200){
                        if (touchPosY > startY + screenWidthDivided) {
                            currentTetrimino.dropSpeed();
                            startY = touchPosY;
                        } else if (touchPosY < startY - screenWidthDivided) {
                            currentTetrimino.rotateTetrimino();
                            startY = touchPosY;
                        }
                    }
            }
            return true;
        }

        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            playfield.setGameRunning(true);
            playfield.start();
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int form, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            boolean retry = true;
            while (retry) {
                try {
                    playfield.setGameRunning(false);
                    playfield.join();
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
                retry = false;
            }
        }
    }

    public class CanvasButton
    {
        private Matrix buttonMatrix = new Matrix();
        private RectF buttonRect;
        private float width;
        private float height;
        private Bitmap buttonBackground;

        public CanvasButton(float width, float height, Bitmap background)
        {
            this.width = width;
            this.height = height;
            this.buttonBackground = background;

            buttonRect = new RectF(0, 0, width, height);
        }

        public void setButtonPos(float x, float y)
        {
            buttonMatrix.setTranslate(x, y);
            buttonMatrix.mapRect(buttonRect);
        }

        public void draw(Canvas canvas)
        {
            canvas.drawBitmap(buttonBackground, buttonMatrix, null);
        }
    }
}
