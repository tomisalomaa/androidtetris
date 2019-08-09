/**
 * @author Tomi Salomaa 06/2019
 *
 */

package com.example.mobiletetrisdemo;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class Playfield extends Thread {

    private int FPS = 60;
    private double avgFPS;
    private boolean gameRunning;
    private SurfaceHolder holder;
    private GameScreen.PlayfieldView playfieldView;
    public static Canvas canvas;


    public Playfield (SurfaceHolder holder, GameScreen.PlayfieldView playfieldView) {
        super();
        this.holder = holder;
        this.playfieldView = playfieldView;
    }

    @Override
    public void run() {
        long startTime, timeMillis, waitTime;
        long totalTime = 0;
        long targetTime = 1000 / FPS;
        int frameCount = 0;

        while (gameRunning) {
            startTime = System.nanoTime();
            canvas = null;

            try {
                canvas = this.holder.lockCanvas();
                synchronized (holder) {
                    playfieldView.update();
                    playfieldView.draw(canvas);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (canvas != null) {
                    try {
                        holder.unlockCanvasAndPost(canvas);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            timeMillis = (System.nanoTime() - startTime) / 1000000;
            waitTime = targetTime - timeMillis - (playfieldView.getScore()*1000);

            try {
                this.sleep(waitTime);
            } catch (Exception e) {
                e.printStackTrace();
            }

            totalTime += System.nanoTime() - startTime;
            frameCount++;
            if (frameCount == FPS) {
                avgFPS = 1000 / ((totalTime / frameCount) / 1000000);
                frameCount = 0;
                totalTime = 0;
            }
        }
    }

    public void setGameRunning (boolean gameRunning) {
        this.gameRunning = gameRunning;
    }
}
