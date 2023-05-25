package games.chess;

// Author: Andrew Merrill

import main.Side;

import java.awt.image.BufferedImage;

// images from http://clipart-library.com/clipart/kTKopobEc.htm

public enum ChessPiece {

    WHITE_ROOK(ChessState.WHITE, 0, 360, 212, 276, 1),
    WHITE_BISHOP(ChessState.WHITE, 259, 360, 290, 276, 2),
    WHITE_QUEEN(ChessState.WHITE, 564, 360, 290, 276, 3),
    WHITE_KING(ChessState.WHITE, 866, 360, 290, 276, 4),
    WHITE_KNIGHT(ChessState.WHITE, 1176, 360, 290, 276, 5),
    WHITE_PAWN(ChessState.WHITE, 1512, 360, 185, 276, 6),

    BLACK_ROOK(ChessState.BLACK, 0, 0, 212, 276, 8),
    BLACK_BISHOP(ChessState.BLACK, 259, 0, 290, 276, 9),
    BLACK_QUEEN(ChessState.BLACK, 564, 0, 290, 276, 10),
    BLACK_KING(ChessState.BLACK, 866, 0, 290, 276, 11),
    BLACK_KNIGHT(ChessState.BLACK, 1176, 0, 290, 276, 12),
    BLACK_PAWN(ChessState.BLACK, 1512, 0, 185, 276, 13),

    ;

    /////////////////////////////////

    public final Side side;
    public final BufferedImage image;

    public final byte index;

    private static final ChessPiece[] pieces = new ChessPiece[] {
            ChessState.EMPTY, WHITE_ROOK, WHITE_BISHOP, WHITE_QUEEN, WHITE_KING, WHITE_KNIGHT, WHITE_PAWN,
            ChessState.EMPTY, BLACK_ROOK, BLACK_BISHOP, BLACK_QUEEN, BLACK_KING, BLACK_KNIGHT, BLACK_PAWN
    };

    public static ChessPiece getPiece(int index) {
        return pieces[index];
    }


    private ChessPiece(Side side, int x, int y, int w, int h, int index) {
        this.side = side;
        this.image = ChessPanel.ALL_CHESS_PIECE_IMAGES.getSubimage(x, y, w, h);
        this.index = (byte) index;
    }

    boolean isPawn() {
        return this == WHITE_PAWN || this == BLACK_PAWN;
    }

    boolean isKnight() {
        return this == WHITE_KNIGHT || this == BLACK_KNIGHT;
    }

    boolean isBishop() {
        return this == WHITE_BISHOP || this == BLACK_BISHOP;
    }

    boolean isRook() {
        return this == WHITE_ROOK || this == BLACK_ROOK;
    }

    boolean isQueen() {
        return this == WHITE_QUEEN || this == BLACK_QUEEN;
    }

    boolean isKing() {
        return this == WHITE_KING || this == BLACK_KING;
    }


}
