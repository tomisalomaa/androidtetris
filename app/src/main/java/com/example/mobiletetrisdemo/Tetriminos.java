/**
 * @author Tomi Salomaa 06/2019
 *
 */

package com.example.mobiletetrisdemo;

import android.graphics.Bitmap;

public class Tetriminos {

    private Bitmap block;
    private int[][] location;
    private GameScreen.PlayfieldView playfield;
    private int deltaX = 0;
    private int col, row;
    private int colorTag;
    private int nSpeed = 600;
    private int dropSpeed = 10;
    private int currentSpeed;
    private long time, lastTime;
    private boolean collision;

    public Tetriminos(Bitmap block, int[][] location, GameScreen.PlayfieldView playfield, int colorTag) {

        this.block = block;
        this.location = location;
        this.playfield = playfield;
        this.colorTag = colorTag;
        col = 3;
        row = 0;
        time = 0;
        lastTime = System.currentTimeMillis();
        currentSpeed = nSpeed;
    }

    public void update() {

        time += System.currentTimeMillis() - lastTime;
        lastTime = System.currentTimeMillis();

        // Check to see if collision and create a new tetrimino if true
        if (collision) {
            for (int i = 0; i < location.length; i++) {
                for (int j = 0; j < location[0].length; j++) {
                    if (location[i][j] != 0) {
                        playfield.setPlayfieldMatrix(row + i, col + j, colorTag);
                    }
                }
            }
            playfield.createTetrimino();
        }

        // Prevent moving the tetrimino left or right if it means moving out of the playfield or into another tetrimino
        if (!(col + deltaX + location[0].length > playfield.getPlayfieldWidth()) && !(col + deltaX < 0)) {
            for (int i = 0; i < location.length; i++) {
                for (int j = 0; j < location[0].length; j++) {
                    if (location[i][j] != 0) {
                        if (playfield.getPlayfieldMatrix()[row + i][j + col + deltaX] != 0) {
                            deltaX = 0;
                        }
                    }
                }
            }
            // Move tetrimino down after successfully preventing the run ins
            col += deltaX;
        }

        // Allow downward movement of tetrimino if not going out of the playfield
        if (!(row + 1 + location.length > playfield.getPlayfieldHeight())) {
            for (int i = 0; i < location.length; i++) {
                for (int j = 0; j < location[0].length; j++) {
                    // Detect collision if falling into something other than playfield value of 0 (empty tile).
                    if (location[i][j] != 0) {
                        if (playfield.getPlayfieldMatrix()[row + i + 1][j + col] != 0) {
                            collision = true;
                        }
                    }
                }
            }
            // Drop tetrimino down a tile if no collision with other tetriminos detected and still within the playfield
            if(time > currentSpeed && !collision) {
                row++;
                time = 0;
            }
            // If downward movement means going out of bounds, do not move and detect collision
        } else {
            collision = true;
        }
        // Reset downward movement value
        deltaX = 0;
    }

    public void rotateTetrimino() {
        // Preliminary check to prevent rotation after collision has been detected
        if (!collision) {
            // Create a new matrix that swaps the lengths of [y][x] --> [x][y]
            int[][] newMatrix = new int[location[0].length][location.length];

            /*
             * Creates the new matrix values from the old one as follows:
             *
             * 		OLD MATRIX						NEW MATRIX
             *
             * 		1 2 3							6 5 4 1
             * 		4 0 0							0 0 0 2
             * 		5 0 0							0 0 0 3
             * 		6 0 0
             *
             */
            for (int i = 0; i < location[0].length; i++) {
                for (int j = location.length - 1; j >= 0; j--) {
                    newMatrix[i][location.length - 1 - j] = location[j][i];
                }
            }

            // Checks if the rotation won't cause the tetrimino to go outside the playfield and replaces the old matrix with the new.
            if (!(col + newMatrix[0].length > playfield.getPlayfieldWidth() || row + newMatrix.length > playfield.getPlayfieldHeight())) {
                location = newMatrix;
            } else {
                return;
            }
        } else {
            return;
        }
    }

    public void setDelta(int deltaX) {
        this.deltaX = deltaX;
    }

    public void dropSpeed() {
        currentSpeed = dropSpeed;
    }

    public void normalSpeed() {
        currentSpeed = nSpeed;
    }

    public int[][] getLocation() {
        return location;
    }
    public Bitmap getBitmapImage() {
        return block;
    }

    public int getColor() {
        return colorTag;
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }
}
