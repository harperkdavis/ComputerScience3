package games.sillygame;

// Author: Andrew Merrill

import graphics.GamePanel;
import main.*;
import java.awt.*;

class SillyPanel extends GamePanel {
    private static final Color background = new Color(60, 179, 113);

    private boolean dragging = false;
    private int startx, starty;
    private int currentx, currenty;

    private Color redalpha = new Color(255, 0, 0, 100);
    private Color bluealpha = new Color(0, 0, 255, 100);


    SillyPanel() {
        super((double) SillyState.COLS / (double) SillyState.ROWS);
    }

    protected void mousePressedHandler(int x, int y) {
        int colWidth = boardWidth / SillyState.COLS;
        int rowHeight = boardHeight / SillyState.ROWS;
        int startcol = x / colWidth;
        int startrow = y / rowHeight;
        if (startcol < 0 || startcol >= SillyState.COLS) return;
        if (startrow < 0 || startrow >= SillyState.ROWS) return;
        SillyState board = (SillyState) gameState;
        if (board.getCell(startrow, startcol) != side) return;
        dragging = true;
        startx = x;
        starty = y;
    }

    protected void mouseDraggedHandler(int x, int y) {
        if (dragging) {
            currentx = x;
            currenty = y;
            repaint();
        }
    }

    protected void mouseReleasedHandler(int endx, int endy) {
        if (!dragging) return;
        dragging = false;
        int colWidth = boardWidth / SillyState.COLS;
        int rowHeight = boardHeight / SillyState.ROWS;
        int startcol = startx / colWidth;
        int startrow = starty / rowHeight;
        int endcol = endx / colWidth;
        int endrow = endy / rowHeight;
        if (startcol < 0 || startcol >= SillyState.COLS) return;
        if (endcol < 0 || endcol >= SillyState.COLS) return;
        if (startrow < 0 || startrow >= SillyState.ROWS) return;
        if (endrow < 0 || endrow >= SillyState.ROWS) return;
        SillyState board = (SillyState) gameState;
        if (board.getCell(startrow, startcol) != side) {
            selectAction(null);
            return;
        }
        if (board.getCell(endrow, endcol) != SillyState.EMPTY) {
            selectAction(null);
            return;
        }
        selectAction(new SillyAction(startrow, startcol, endrow, endcol, side));
    }


    protected void paintGamePanel(Graphics pen) {
        SillyState board = (SillyState) gameState;
        pen.setColor(background);
        pen.fillRect(xoffset, yoffset, boardWidth, boardHeight);
        int cellSize = boardWidth / SillyState.COLS;
        int cellMargin = cellSize / 6;
        int circleSize = cellSize - cellMargin * 2;

        if (board != null) {
            for (int r = 0; r < SillyState.ROWS; r++) {
                for (int c = 0; c < SillyState.COLS; c++) {
                    Side cell = board.getCell(r, c);
                    int x = xoffset + c * cellSize + cellMargin;
                    int y = yoffset + r * cellSize + cellMargin;
                    if (cell == SillyState.BLUE) {
                        pen.setColor(Color.BLUE);
                    } else if (cell == SillyState.RED) {
                        pen.setColor(Color.RED);
                    } else {
                        pen.setColor(Color.WHITE);
                    }
                    pen.fillOval(x, y, circleSize, circleSize);
                }
            }
            if (dragging) {
                if (side == SillyState.RED)
                    pen.setColor(redalpha);
                if (side == SillyState.BLUE)
                    pen.setColor(bluealpha);
                pen.fillOval(xoffset + currentx - circleSize / 2, yoffset + currenty - circleSize / 2, circleSize, circleSize);
            }

        }
    }
}
