package games.checkers;

// Author: Andrew Merrill

import graphics.GamePanel;
import graphics.HumanActionSynchronizer;
import main.*;
import java.awt.*;
import java.util.ArrayList;

class CheckersPanel extends GamePanel {

    private boolean mouse = false, hovering = false, dragging = false, selectedMove = false, multijump = false;
    private int dragrow, dragcol, dragindex;
    private int hoverrow, hovercol, hoverindex;
    private int currentx, currenty;
    private int cellSize, cellMargin, pieceSize;
    private java.util.List<CheckersAction> availableActions = new ArrayList<>();
    private java.util.List<Integer> chosenMoveIndexes = new ArrayList<>();

    private static final Color background = Color.GRAY;
    private static final Color lightColor = new Color(255, 221, 153);
    private static final Color darkColor = new Color(204, 136, 0);
    private static final Color blackColor = new Color(0, 0, 0);
    private static final Color redColor = new Color(255, 0, 0);
    private static final Color blackTransparentColor = new Color(0, 0, 0, 100);
    private static final Color redTransparentColor = new Color(255, 0, 0, 100);
    private static final Color arrowColor = new Color(0, 204, 68);

    private static final Stroke thickStroke = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
    private static final Stroke veryThickStroke = new BasicStroke(6, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);

    CheckersPanel() {
        super(1.0);
    }

    @Override
    protected void paintGamePanel(Graphics pen) {
        CheckersState board = (CheckersState) gameState;
        pen.setColor(background);
        pen.fillRect(xoffset, yoffset, boardWidth, boardHeight);
        cellSize = boardWidth / CheckersState.SIZE;
        cellMargin = cellSize / 8;
        pieceSize = cellSize - cellMargin * 2;

        // draw grid
        for (int r = 0; r < CheckersState.SIZE; r++) {
            for (int c = 0; c < CheckersState.SIZE; c++) {
                Color squareColor = ((r + c) % 2 == 0) ? darkColor : lightColor;
                pen.setColor(squareColor);
                pen.fillRect(cellX(c), cellY(r), cellSize, cellSize);
            }
        }

        if (board != null) {
            // draw the checkers
            for (int r = 0; r < CheckersState.SIZE; r++) {
                for (int c = 0; c < CheckersState.SIZE; c++) {
                    if (((r + c) % 2) == 0) {
                        int cell = board.getCell(r, c);
                        if (cell != 0) {
                            if ((cell & CheckersState.BLACK_MASK) != 0) {
                                pen.setColor(blackColor);
                            } else if ((cell & CheckersState.RED_MASK) != 0) {
                                pen.setColor(redColor);
                            }
                            pen.fillOval(cellX(c) + cellMargin, cellY(r) + cellMargin, pieceSize, pieceSize);

                            if ((cell & CheckersState.KING) != 0) {
                                pen.setColor(darkColor);
                                int radius = pieceSize / 10;
                                int centerX = cellX(c) + cellSize/2;
                                int centerY = cellY(r) + cellSize/2;
                                for (double a=0; a < Math.PI*2; a += (Math.PI*2)/12) {
                                    int x = (int) (centerX + Math.cos(a)*pieceSize/2 + 0.5);
                                    int y = (int) (centerY + Math.sin(a)*pieceSize/2 + 0.5);
                                    pen.fillOval(x-radius, y-radius, radius*2, radius*2);
                                }
                            }

                        }
                    }
                }
            }

            // draw green arrow showing the most recent move
            if (recentAction != null) {
                CheckersAction action = (CheckersAction) recentAction;
                while (action != null) {
                    drawArrow(pen, action);
                    action = action.nextAction;
                }
            }

            if (mouse && ! hovering && ! dragging) {
                // mark all pieces that could move
                if (children != null) {
                    for (SearchNode node : children) {
                        CheckersAction action = (CheckersAction) node.action;
                        markPiece(pen, action.oldrow, action.oldcol);
                    }
                }
            }

            if (mouse && hovering) {
                markSelectedPiece(pen, hoverrow, hovercol);
                for (CheckersAction action : availableActions) {
                    markDestination(pen, action.newrow, action.newcol, (side == CheckersState.BLACK) ? blackColor : redColor);
                }
            }

            if (mouse && dragging) {

                for (CheckersAction action : availableActions) {
                    markDestination(pen, action.newrow, action.newcol, (side == CheckersState.BLACK) ? blackColor : redColor);
                }

                for (int moveIndex : chosenMoveIndexes) {
                    int row = moveIndex / 4;
                    int col = (moveIndex % 4) * 2 + (row % 2);
                    markIntermediateDestination(pen, row, col, (side == CheckersState.BLACK) ? blackColor : redColor);
                }

                if (selectedMove)
                    markSelectedDestination(pen, dragrow, dragcol, Color.WHITE);


                if (side == CheckersState.RED)
                    pen.setColor(redTransparentColor);
                else if (side == CheckersState.BLACK)
                    pen.setColor(blackTransparentColor);
                pen.fillOval(xoffset + currentx - pieceSize / 2, yoffset + currenty - pieceSize / 2, pieceSize, pieceSize);
            }
        }
    }

    private void markPiece(Graphics pen, int row, int col) {
        pen.setColor(Color.WHITE);
        int radius = pieceSize / 10;
        pen.fillOval(cellX(col) + cellSize / 2 - radius, cellY(row) + cellSize / 2 - radius, radius * 2, radius * 2);
    }

    private void markSelectedPiece(Graphics pen, int row, int col) {
        pen.setColor(Color.WHITE);
        int radius = pieceSize / 6;
        pen.fillOval(cellX(col) + cellSize / 2 - radius, cellY(row) + cellSize / 2 - radius, radius * 2, radius * 2);
    }

    private void markDestination(Graphics pen, int row, int col, Color color) {
        pen.setColor(color);
        pen.drawOval(cellX(col) + cellMargin, cellY(row) + cellMargin, pieceSize, pieceSize);
    }

    private void markIntermediateDestination(Graphics pen, int row, int col, Color color) {
        Graphics2D pen2 = (Graphics2D) pen.create();
        pen2.setStroke(veryThickStroke);
        pen2.setColor(color);
        pen2.drawOval(cellX(col) + cellMargin, cellY(row) + cellMargin, pieceSize, pieceSize);
        pen2.dispose();
    }

    private void markSelectedDestination(Graphics pen, int row, int col, Color color) {
        Graphics2D pen2 = (Graphics2D) pen.create();
        pen2.setStroke(thickStroke);
        pen2.setColor(color);
        pen2.drawOval(cellX(col) + cellMargin, cellY(row) + cellMargin, pieceSize, pieceSize);
        pen2.dispose();
    }

    private int cellY(int row) {
        return panelHeight - yoffset - (row + 1) * cellSize;
    }

    private int cellX(int col) {
        return xoffset + col * cellSize;
    }

    private void drawArrow(Graphics pen, CheckersAction action) {
        int x1 = cellX(action.oldcol) + cellSize/2;
        int y1 = cellY(action.oldrow) + cellSize/2;
        int x2 = cellX(action.newcol) + cellSize/2;
        int y2 = cellY(action.newrow) + cellSize/2;

        double angle = Math.atan2(y1-y2, x1-x2);
        int arrowSize = cellSize/6;
        int x3 = (int) (x2 + arrowSize * Math.cos(angle + Math.PI/4));
        int y3 = (int) (y2 + arrowSize * Math.sin(angle + Math.PI/4));
        int x4 = (int) (x2 + arrowSize * Math.cos(angle - Math.PI/4));
        int y4 = (int) (y2 + arrowSize * Math.sin(angle - Math.PI/4));
        int[] arrowHeadXs = new int[] {x2, x3, x4};
        int[] arrowHeadYs = new int[] {y2, y3, y4};

        Graphics2D pen2 = (Graphics2D) pen.create();
        pen2.setColor(arrowColor);
        pen2.setStroke(thickStroke);
        pen2.drawLine(x1, y1, x2, y2);
        pen2.drawPolygon(arrowHeadXs, arrowHeadYs, 3);
        pen2.fillPolygon(arrowHeadXs, arrowHeadYs, 3);
        pen2.dispose();
    }

    @Override
    protected void mouseMovedHandler(int x, int y) {
        cellSize = boardWidth / CheckersState.SIZE;
        int row = CheckersState.SIZE - (y / cellSize) - 1;
        int col = x / cellSize;
        mouse = true;
        if (row >= 0 && row < CheckersState.SIZE && col >= 0 && col < CheckersState.SIZE && ((row + col) % 2) == 0) {

            int oldHoverindex = hoverindex;
            hoverrow = row;
            hovercol = col;
            hoverindex = row * 4 + col / 2;
            if (hovering && oldHoverindex == hoverindex) return;

            hovering = false;
            availableActions.clear();
            CheckersState board = (CheckersState) gameState;
            if (board.isSideCell(side, row, col)) {
                if (children != null) {
                    for (SearchNode node : children) {
                        CheckersAction action = (CheckersAction) node.action;
                        if (action.oldindex == hoverindex) {
                            hovering = true;
                            availableActions.add(action);
                        }
                    }
                }
            }
            repaint();

        } else {
            boolean repaintNeeded = hovering;
            hovering = false;
            if (repaintNeeded)
                repaint();
        }
    }

    @Override
    protected void mousePressedHandler(int x, int y) {
        cellSize = boardWidth / CheckersState.SIZE;
        int row = CheckersState.SIZE - (y / cellSize) - 1;
        int col = x / cellSize;
        if (row >= 0 && row < CheckersState.SIZE && col >= 0 && col < CheckersState.SIZE && ((row + col) % 2) == 0) {

            int startindex = row * 4 + col / 2;
            currentx = x;
            currenty = y;
            hovering = false;
            dragging = false;
            selectedMove = false;
            multijump = false;
            availableActions.clear();
            chosenMoveIndexes.clear();

            CheckersState board = (CheckersState) gameState;
            if (board.isSideCell(side, row, col)) {
                for (SearchNode node : children) {
                    CheckersAction action = (CheckersAction) node.action;
                    if (action.oldindex == startindex) {
                        dragging = true;
                        availableActions.add(action);
                    }
                }
            }
            repaint();
        }
    }

    @Override
    protected void mouseDraggedHandler(int x, int y) {
        if (dragging) {
            cellSize = boardWidth / CheckersState.SIZE;
            currentx = x;
            currenty = y;
            int row = CheckersState.SIZE - (y / cellSize) - 1;
            int col = x / cellSize;

            if (row >= 0 && row < CheckersState.SIZE && col >= 0 && col < CheckersState.SIZE && ((row + col) % 2) == 0) {
                dragrow = row;
                dragcol = col;
                int oldDragindex = dragindex;
                dragindex = dragrow * 4 + dragcol / 2;
                if (!selectedMove || dragindex != oldDragindex) {
                    selectedMove = false;
                    for (CheckersAction action : availableActions) {
                        if (action.newindex == dragindex) {
                            selectedMove = true;
                            break;
                        }
                    }
                    if (selectedMove) {

                        if (!multijump) {
                            for (CheckersAction action : availableActions) {
                                if (action.newindex == dragindex) {
                                    if (action.nextAction != null)
                                        multijump = true;
                                }
                            }
                        }
                        if (multijump) {
                            java.util.List<CheckersAction> nextAvailableActions = new ArrayList<>();
                            for (CheckersAction action : availableActions) {
                                if (action.newindex == dragindex && action.nextAction != null)
                                    nextAvailableActions.add(action.nextAction);
                            }
                            if (! nextAvailableActions.isEmpty()) {
                                chosenMoveIndexes.add(dragindex);
                                availableActions = nextAvailableActions;
                            }
                        }
                    }
                }
            } else {
                selectedMove = false;
            }
            repaint();
        }
    }

    @Override
    protected void mouseReleasedHandler(int x, int y) {
        if (!dragging) return;
        dragging = false;
        hovering = false;
        cellSize = boardWidth / CheckersState.SIZE;
        int row = CheckersState.SIZE - (y / cellSize) - 1;
        int col = x / cellSize;
        if (row >= 0 && row < CheckersState.SIZE && col >= 0 && col < CheckersState.SIZE && ((row + col) % 2) == 0) {

            int endindex = row * 4 + col / 2;

            for (CheckersAction action : availableActions) {
                if (action.newindex == endindex) {

                    if (multijump) {
                        while (action.prevAction != null)
                            action = action.prevAction;
                    }

                    selectAction(action);
                    break;
                }
            }
        }

        repaint();
    }
}