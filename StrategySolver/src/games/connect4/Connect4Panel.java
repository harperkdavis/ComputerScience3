package games.connect4;

// Author: Andrew Merrill

import graphics.GamePanel;
import main.*;
import java.awt.*;

class Connect4Panel extends GamePanel {
    private static final Color background = new Color(255, 255, 50);

    Connect4Panel() {
        super((double) Connect4State.COLS / (double) Connect4State.ROWS);
    }

    protected void mousePressedHandler(int x, int y) {
        int colWidth = boardWidth / Connect4State.COLS;
        int col = x / colWidth;
        if (col < 0 || col >= Connect4State.COLS) return;

        selectAction(new Connect4Action(col, side));
    }


    protected void paintGamePanel(Graphics pen) {
        Connect4State board = (Connect4State) gameState;
        pen.setColor(background);
        pen.fillRect(xoffset, yoffset, boardWidth, boardHeight);
        int cellSize = boardWidth / Connect4State.COLS;
        int cellMargin = cellSize / 6;
        int circleSize = cellSize - cellMargin * 2;

        if (board != null) {
            for (int r = 0; r < Connect4State.ROWS; r++) {
                for (int c = 0; c < Connect4State.COLS; c++) {
                    Side cell = board.getCell(r, c);
                    int x = xoffset + c * cellSize + cellMargin;
                    int y = yoffset + r * cellSize + cellMargin;
                    if (cell == Connect4State.BLACK) {
                        pen.setColor(Color.BLACK);
                    } else if (cell == Connect4State.RED) {
                        pen.setColor(Color.RED);
                    } else {
                        pen.setColor(Color.WHITE);
                    }
                    pen.fillOval(x, y, circleSize, circleSize);
                }
            }

            if (recentAction != null) {
                Connect4Action connect4Action = (Connect4Action) recentAction;
                int x = xoffset + connect4Action.col * cellSize;
                int margin = cellSize / 10;
                pen.setColor(Color.GREEN);
                pen.drawRect(x + margin, yoffset + margin, cellSize - 2 * margin, boardHeight - 2 * margin);
            }
        }
    }
}
