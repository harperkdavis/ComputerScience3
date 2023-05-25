package games.othello;

// Author: Andrew Merrill

import graphics.GamePanel;
import main.*;
import java.awt.*;

class OthelloPanel extends GamePanel {
    private static final Color background = new Color(0, 204, 0);

    OthelloPanel() {
        super(1.0);
    }

    protected void mousePressedHandler(int x, int y) {
        int cellSize = boardWidth / OthelloState.SIZE;
        int row = y / cellSize;
        int col = x / cellSize;
        if (row < 0 || row >= OthelloState.SIZE || col < 0 || col >= OthelloState.SIZE) return;

        selectAction(new OthelloAction(row, col, side));
    }


    protected void paintGamePanel(Graphics pen) {
        OthelloState board = (OthelloState) gameState;
        pen.setColor(background);
        pen.fillRect(xoffset, yoffset, boardWidth, boardHeight);
        int size = boardWidth;
        int cellSize = size / OthelloState.SIZE;
        int cellMargin = cellSize / 6;
        int circleSize = cellSize - cellMargin * 2;
        pen.setColor(Color.BLACK);
        for (int i = 1; i < OthelloState.SIZE; i++) {
            pen.drawLine(xoffset + i * cellSize, yoffset, xoffset + i * cellSize, yoffset + size);
            pen.drawLine(xoffset, yoffset + i * cellSize, xoffset + size, yoffset + i * cellSize);
        }
        if (board != null) {
            for (int r = 0; r < OthelloState.SIZE; r++) {
                for (int c = 0; c < OthelloState.SIZE; c++) {
                    Side cell = board.getCell(r, c);
                    if (cell != OthelloState.EMPTY) {
                        int x = xoffset + c * cellSize + cellMargin;
                        int y = yoffset + r * cellSize + cellMargin;
                        if (cell == OthelloState.BLACK) {
                            pen.setColor(Color.BLACK);
                            pen.fillOval(x, y, circleSize, circleSize);
                        } else {
                            pen.setColor(Color.WHITE);
                            pen.fillOval(x, y, circleSize, circleSize);
                            pen.setColor(Color.BLACK);
                            pen.drawOval(x, y, circleSize, circleSize);
                        }
                    }
                }
            }

            if (recentAction != null) {
                OthelloAction othelloAction = (OthelloAction) recentAction;
                int x = xoffset + othelloAction.col * cellSize;
                int y = yoffset + othelloAction.row * cellSize;
                int margin = cellSize / 10;
                pen.setColor(Color.RED);
                pen.drawRect(x + margin, y + margin, cellSize - 2 * margin, cellSize - 2 * margin);
            }

            if (children != null) {
                for (SearchNode node : children) {
                    OthelloAction action = (OthelloAction) node.action;
                    int r = action.row;
                    int c = action.col;
                    if (action.side == OthelloState.BLACK) {
                        pen.setColor(Color.BLACK);
                    } else {
                        pen.setColor(Color.WHITE);
                    }
                    int x = xoffset + c * cellSize + cellSize / 2;
                    int y = yoffset + r * cellSize + cellSize / 2;
                    int d = cellSize / 4;
                    pen.drawLine(x - d, y - d, x + d, y + d);
                    pen.drawLine(x - d, y + d, x + d, y - d);
                }
            }
        }
    }
}
