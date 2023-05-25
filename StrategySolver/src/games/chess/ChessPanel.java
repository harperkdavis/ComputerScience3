package games.chess;

// Author: Andrew Merrill

import graphics.GamePanel;
import main.SearchNode;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

class ChessPanel extends GamePanel {

    private boolean mouse = false, hovering = false, dragging = false;
    private int dragrow, dragcol;
    private int hoverrow, hovercol;
    private int currentx, currenty;
    private int cellSize, cellMargin, pieceSize;
    private java.util.List<ChessAction> availableActions = new ArrayList<>();

    private ChessPiece dragPiece = null;

    private static final Color background = Color.GRAY;
    private static final Color lightColor = new Color(255, 221, 153);
    private static final Color darkColor = new Color(204, 136, 0);
    private static final Color arrowColor = new Color(0, 204, 68);
    private static final Color inCheckColor = new Color(255, 0,255,128);

    private static final BasicStroke thickStroke = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
    private static final BasicStroke veryThickStroke = new BasicStroke(7, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);

    static final BufferedImage ALL_CHESS_PIECE_IMAGES;

    static {
        try {
            ALL_CHESS_PIECE_IMAGES = ImageIO.read(new File("src/games/chess/chess piece images.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    ChessPanel() {
        super(1.0);
    }

    @Override
    protected void paintGamePanel(Graphics pen) {
        ChessState board = (ChessState) gameState;

        pen.setColor(background);
        pen.fillRect(xoffset, yoffset, boardWidth, boardHeight);
        cellSize = boardWidth / ChessState.SIZE;
        cellMargin = cellSize / 8;
        pieceSize = cellSize - cellMargin * 2;

        // draw grid
        for (int r = 0; r < ChessState.SIZE; r++) {
            for (int c = 0; c < ChessState.SIZE; c++) {
                Color squareColor = ((r + c) % 2 == 0) ? darkColor : lightColor;
                pen.setColor(squareColor);
                pen.fillRect(cellX(c), cellY(r), cellSize, cellSize);
            }
        }

        if (board != null) {

            pen.setFont(new Font("Sans Serif",  Font.PLAIN, 10));
            pen.setColor(Color.BLACK);
            pen.drawString(""+board.getMoveCount(), xoffset+1,yoffset+13);

            // draw the pieces
            for (int r = 0; r < ChessState.SIZE; r++) {
                for (int c = 0; c < ChessState.SIZE; c++) {
                    ChessPiece piece = board.getPiece(r, c);
                    if (piece != null) {
                        int pieceHeight = pieceSize;
                        int pieceWidth = (int)(pieceSize * ((double) piece.image.getWidth()/piece.image.getHeight()));
                        int pieceMarginX = (pieceSize - pieceWidth) / 2;
                        pen.drawImage(piece.image, cellX(c)+cellMargin+pieceMarginX, cellY(r)+cellMargin, pieceWidth, pieceHeight, this);
                    }
                }
            }

            pen.setColor(inCheckColor);
            if (board.inCheck(board.getSideToPlay()))
                pen.fillOval(cellX(board.getKingCol(board.getSideToPlay()))+2, cellY(board.getKingRow(board.getSideToPlay()))+2, cellSize-4, cellSize-4);
//            pen.setColor(new Color(255, 0,255,100));
//            if (board.inCheck(board.getOtherSide()))
//                pen.fillOval(cellX(board.getKingCol(board.getOtherSide()))+cellSize/4, cellY(board.getKingRow(board.getOtherSide()))+cellSize/4, cellSize/2, cellSize/2);

            // draw green arrow showing the most recent move
            if (recentAction != null) {
                ChessAction action = (ChessAction) recentAction;
                drawArrow(pen, action);
            }

            // mark piece under mouse and its possible actions
            if (mouse && (hovering || dragging)) {
                for (ChessAction action : availableActions) {
                    markCell(pen, action.newrow, action.newcol, Color.BLUE,
                            (dragging && dragrow == action.newrow && dragcol == action.newcol) ? veryThickStroke : thickStroke);
                }
                markCell(pen, hoverrow, hovercol, Color.RED, veryThickStroke);
            }

            // draw faded piece with mouse while dragging
            if (mouse && dragging) {
                Graphics2D pen2 = (Graphics2D) pen.create();
                AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f);
                pen2.setComposite(alphaComposite);
                int pieceHeight = pieceSize;
                int pieceWidth = (int)(pieceSize * ((double) dragPiece.image.getWidth()/dragPiece.image.getHeight()));
                pen2.drawImage(dragPiece.image, xoffset + currentx-pieceWidth/2, yoffset + currenty-pieceHeight/2, pieceWidth, pieceHeight, this);
           }
        }
    }

    private void markCell(Graphics pen, int row, int col, Color color, BasicStroke stroke) {
        Graphics2D pen2 = (Graphics2D) pen.create();
        pen2.setStroke(stroke);
        pen2.setColor(color);
        int w = (int) stroke.getLineWidth() - 1;
        int s = w > 2 ? cellSize - w + 2 : cellSize;
        int x = cellX(col) + w/2 - 1;
        int y = cellY(row) + w/2 - 1;
        pen2.drawRect(x, y, s, s);
    }

    private int cellY(int row) {
        return panelHeight - yoffset - (row + 1) * cellSize;
    }

    private int cellX(int col) {
        return xoffset + col * cellSize;
    }

    private void drawArrow(Graphics pen, ChessAction action) {
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
        cellSize = boardWidth / ChessState.SIZE;
        int row = ChessState.SIZE - (y / cellSize) - 1;
        int col = x / cellSize;
        mouse = true;
        if (row >= 0 && row < ChessState.SIZE && col >= 0 && col < ChessState.SIZE) {

            if (hovering && row == hoverrow && col == hovercol) return;

            hoverrow = row;
            hovercol = col;
            hovering = false;
            availableActions.clear();
            ChessState board = (ChessState) gameState;
            if (board.isSidePiece(side, row, col)) {
                if (children != null) {
                    for (SearchNode node : children) {
                        ChessAction action = (ChessAction) node.action;
                        if (action.oldrow == hoverrow && action.oldcol == hovercol) {
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
        cellSize = boardWidth / ChessState.SIZE;
        int row = ChessState.SIZE - (y / cellSize) - 1;
        int col = x / cellSize;
        if (row >= 0 && row < ChessState.SIZE && col >= 0 && col < ChessState.SIZE) {

            currentx = x;
            currenty = y;
            hoverrow = row;
            hovercol = col;
            hovering = false;
            dragging = false;
            availableActions.clear();

            ChessState board = (ChessState) gameState;
            if (board.isSidePiece(side, row, col)) {
                for (SearchNode node : children) {
                    ChessAction action = (ChessAction) node.action;
                    if (action.oldrow == row && action.oldcol == col) {
                        dragging = true;
                        dragPiece = board.getPiece(row, col);
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
            cellSize = boardWidth / ChessState.SIZE;
            currentx = x;
            currenty = y;
            int row = ChessState.SIZE - (y / cellSize) - 1;
            int col = x / cellSize;

            if (row >= 0 && row < ChessState.SIZE && col >= 0 && col < ChessState.SIZE) {
                dragrow = row;
                dragcol = col;
            }
            repaint();
        }
    }

    @Override
    protected void mouseReleasedHandler(int x, int y) {
        if (!dragging) return;
        dragging = false;
        dragPiece = null;
        hovering = false;
        cellSize = boardWidth / ChessState.SIZE;
        int row = ChessState.SIZE - (y / cellSize) - 1;
        int col = x / cellSize;
        if (row >= 0 && row < ChessState.SIZE && col >= 0 && col < ChessState.SIZE) {
            for (ChessAction action : availableActions) {
                if (action.newrow == row && action.newcol == col) {
                    selectAction(action);
                    break;
                }
            }
        }
        repaint();
    }
}