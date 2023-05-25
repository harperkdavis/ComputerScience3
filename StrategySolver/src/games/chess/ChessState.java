package games.chess;

// Author: Andrew Merrill

// Implements most of the game of Chess, with these exceptions:
//
//  Pawns can only be promoted to Queens, not to any other piece.
//  En passant pawn captures are not supported.
//  There is no mechanism for offering a Draw.
//  A Draw is not declared for a dead position or insufficient material.
//  A Draw is not declared when the same position has been reached three times.

// This implementation does include these features:
//
//  Castling, on both sides, and only when strictly legal to do so.
//  A Draw is declared upon stalemate.
//  A Draw is declared upon 50 consecutive full moves (100 half moves) without capture or pawn movement.
//  Pawns are automatically promoted to Queens upon reaching the farthest rank.

/////////////////////////////////////////////////////////////////////////////////////////////////////////

import main.SearchNode;
import main.Side;
import main.State;

import java.util.ArrayList;
import java.util.List;

public class ChessState extends State {
    static final int SIZE = 8;
    static final Side WHITE = Side.ONE;      // Side.ONE is Max Side
    static final Side BLACK = Side.TWO;      // Side.TWO is Min Side

    static final ChessPiece EMPTY = null;


    //private final ChessPiece[][] board;
    private long board01, board23, board45, board67;  // each long holds two rows, with 4 bits per piece

    private int infoBits;  // see below for bitfield layout

    // infoBits bitfield Layout (starting from LSB):
    private static final int WHITE_KING_ROW_BITS = 0;  // 4 bits
    private static final int WHITE_KING_COL_BITS = 4;  // 4 bits
    private static final int BLACK_KING_ROW_BITS = 8;  // 4 bits
    private static final int BLACK_KING_COL_BITS = 12;  // 4 bits
    private static final int DRAW_BIT = 16;  // 1 bit; 1 = game was a draw, 0 = game was not a draw
    private static final int WHITE_LEFT_CASTLE_BIT = 17;  // 1 bit; 0 = this castle is OK, 1 = this castle is not allowed
    private static final int WHITE_RIGHT_CASTLE_BIT = 18;  // 1 bit; 0 = this castle is OK, 1 = this castle is not allowed
    private static final int BLACK_LEFT_CASTLE_BIT = 19;  // 1 bit; 0 = this castle is OK, 1 = this castle is not allowed
    private static final int BLACK_RIGHT_CASTLE_BIT = 20;  // 1 bit; 0 = this castle is OK, 1 = this castle is not allowed

    private static final int MOVE_COUNT_BITS = 21; // 7 bits; number of moves since last capture or pawn movement

    private List<SearchNode> children = null;

    public ChessState() {
        super(WHITE);
//        board = new ChessPiece[SIZE][SIZE];
//        setPiece(ChessPiece.WHITE_ROOK,0,0);
//        setPiece(ChessPiece.WHITE_KNIGHT,0,1);
//        setPiece(ChessPiece.WHITE_BISHOP,0,2);
//        setPiece(ChessPiece.WHITE_QUEEN,0,3);
//        setPiece(ChessPiece.WHITE_KING,0,4);
//        setPiece(ChessPiece.WHITE_BISHOP,0,5);
//        setPiece(ChessPiece.WHITE_KNIGHT,0,6);
//        setPiece(ChessPiece.WHITE_ROOK,0,7);
//
//        setPiece(ChessPiece.BLACK_ROOK,7,0);
//        setPiece(ChessPiece.BLACK_KNIGHT,7,1);
//        setPiece(ChessPiece.BLACK_BISHOP,7,2);
//        setPiece(ChessPiece.BLACK_QUEEN,7,3);
//        setPiece(ChessPiece.BLACK_KING,7,4);
//        setPiece(ChessPiece.BLACK_BISHOP,7,5);
//        setPiece(ChessPiece.BLACK_KNIGHT,7,6);
//        setPiece(ChessPiece.BLACK_ROOK,7,7);
//
//        for (int c=0; c<SIZE; c++) {
//            setPiece(ChessPiece.WHITE_PAWN, 1, c);
//            setPiece(ChessPiece.BLACK_PAWN,6, c);
//        }
//        System.out.println(board01 + " " + board23 + " " + board45 +" "+ board67);

        board01 = 7378697628120527441L;
//        board23 = 0L;
//        board45 = 0L;
        board67 = -8314865606828630563L;

        setWhiteKing(0, 4);
        setBlackKing(7, 4);

    }

    private ChessState(Side newSideToPlay, ChessState oldState) {
        super(newSideToPlay);
//        board = new ChessPiece[SIZE][SIZE];
//        for (int r=0; r<SIZE; r++) {
//            for (int c=0; c<SIZE; c++) {
//                board[r][c] = oldState.board[r][c];
//            }
//        }
        this.board01 = oldState.board01;
        this.board23 = oldState.board23;
        this.board45 = oldState.board45;
        this.board67 = oldState.board67;
        if (oldState.getDraw()) this.setDraw();
        //this.draw = oldState.draw;
        this.infoBits = oldState.infoBits;
    }

    @Override
    public boolean equals(Object other) {
        ChessState otherState = (ChessState) other;
//        for (int r = 0; r < SIZE; r++) {
//            for (int c = 0; c < SIZE; c++) {
//                if (getPiece(r,c) != otherState.getPiece(r,c)) return false;
//            }
//        }
        if (this.board01 != otherState.board01) return false;
        if (this.board23 != otherState.board23) return false;
        if (this.board45 != otherState.board45) return false;
        if (this.board67 != otherState.board67) return false;
        if (this.infoBits != otherState.infoBits) return false;
        return true;
    }

    @Override
    public boolean isGameOver() {
        List<SearchNode> children = listChildren();
        if (children.isEmpty()) {
            if (! inCheck(getSideToPlay()))
                setDraw();
            return true;
        } else if (getMoveCount() >= 100) { // 50 move rule (100 half moves)
            setDraw();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Side getWinner() {
        if (getDraw())
            return null;
        else if (getSideToPlay() == BLACK)
            return WHITE;
        else
            return BLACK;
    }

    boolean illegalCell(int row, int col) {
        return row < 0 || row >= SIZE || col < 0 || col >= SIZE;
    }

    @Override
    public List<SearchNode> listChildren() {
        if (children != null)
            return children;  // returns cached list of children computed earlier

        children = new ArrayList<SearchNode>();

        for (int row=0; row<SIZE; row++) {
            for (int col=0; col<SIZE; col++) {
                ChessPiece piece = getPiece(row, col);
                if (piece != null && piece.side == getSideToPlay()) {
                    if (piece.isPawn())        findPawnMoves(children, row, col);
                    else if (piece.isKnight()) findKnightMoves(children, row, col);
                    else if (piece.isBishop()) findBishopMoves(children, row, col);
                    else if (piece.isRook())   findRookMoves(children, row, col);
                    else if (piece.isQueen())  findQueenMoves(children, row, col);
                    else if (piece.isKing())   findKingMoves(children, row, col);
                }
            }
        }

        return children;
    }

    // All moves are added to the moves list by this function,
    //    except for Castling with is handled by the addCastleMove function
    private void addMove(List<SearchNode> moves, int oldrow, int oldcol, int newrow, int newcol) {
        ChessPiece piece = getPiece(oldrow,oldcol);
        ChessState nextState = new ChessState(getOtherSide(), this);
        int moveCount = nextState.getMoveCount() + 1;
        if (getPiece(newrow, newcol) != EMPTY)
            moveCount = 0; // capture resets the move count
        nextState.setPiece(null, oldrow, oldcol);
        nextState.setPiece(piece, newrow, newcol);
        if (piece.isPawn()) {
            moveCount = 0; // pawn movement resets the move count
            // check for pawn promotion!
            if (piece.side == WHITE && newrow == 7) {
                nextState.setPiece(ChessPiece.WHITE_QUEEN, newrow, newcol);
            }
            else if (piece.side == BLACK && newrow == 0) {
                nextState.setPiece(ChessPiece.BLACK_QUEEN, newrow, newcol);
            }
        }
        else if (piece.isKing()) {
            // moving the King makes future Castling illegal
            if (piece.side == WHITE) {
                nextState.setWhiteLeftCastleImpossible();
                nextState.setWhiteRightCastleImpossible();
            } else {
                nextState.setBlackLeftCastleImpossible();
                nextState.setBlackRightCastleImpossible();
            }
        }
        else if (piece.isRook()) {
            // moving a Rook makes future Castling with that Rook illegal
            if (piece.side == WHITE && oldrow == 0)
                if (oldcol == 0)
                    nextState.setWhiteLeftCastleImpossible();
                else if (oldcol == 7)
                    nextState.setWhiteRightCastleImpossible();
            if (piece.side == BLACK && oldrow == 7)
                if (oldcol == 0)
                    nextState.setBlackLeftCastleImpossible();
                else if (oldcol == 7)
                    nextState.setBlackRightCastleImpossible();
        }

        if (nextState.inCheck(getSideToPlay())) return;  // not a legal move if the side to play ends up in check

        nextState.setMoveCount(moveCount);
        moves.add(new SearchNode(new ChessAction(oldrow, oldcol, newrow, newcol, getSideToPlay()), nextState));
    }

    private void addCastleMove(List<SearchNode> moves, Side kingSide, int rookCol) {
        if (inCheck(kingSide)) // can't castle while in check
            return;
        int row = getKingRow(kingSide);
        int kingCol = getKingCol(kingSide);
        ChessPiece kingPiece = getPiece(row, kingCol);
        ChessPiece rookPiece = getPiece(row, rookCol);
        if (rookPiece == EMPTY || ! rookPiece.isRook()) // can only castle with a rook
            return;

        int dir = Integer.signum(rookCol - kingCol);
        for (int col = kingCol+dir; col != rookCol; col += dir) {
            if (getPiece(row, col) != EMPTY) // squares between King and Rook must be empty
                return;
        }

        // King cannot move through a square that is under attack
        if (attackChecker(kingSide, row, kingCol + dir))
            return;
        if (attackChecker(kingSide, row, kingCol + 2*dir))
            return;

        // Castle is OK!

        ChessState nextState = new ChessState(getOtherSide(), this);
        nextState.setPiece(EMPTY, row, kingCol);
        nextState.setPiece(EMPTY, row, rookCol);
        nextState.setPiece(kingPiece, row, kingCol + 2*dir);
        nextState.setPiece(rookPiece, row, kingCol + dir);
        if (kingSide == WHITE) {
            nextState.setWhiteLeftCastleImpossible();
            nextState.setWhiteRightCastleImpossible();
        } else {
            nextState.setBlackLeftCastleImpossible();
            nextState.setBlackRightCastleImpossible();
        }
        nextState.incrementMoveCount();
        moves.add(new SearchNode(new ChessAction(row, kingCol, row, kingCol + 2*dir, kingSide), nextState));
    }

    private void findStraightMoves(List<SearchNode> moves, int startRow, int startCol, int rowDelta, int colDelta, int limit) {
        int row = startRow + rowDelta;
        int col = startCol + colDelta;
        if (illegalCell(row,col)) return;
        ChessPiece destination = getPiece(row, col);
        int count = 1;
        while (destination == EMPTY) {
            addMove(moves, startRow, startCol, row, col);
            count ++;
            if (count > limit) return;
            row += rowDelta;
            col += colDelta;
            if (illegalCell(row, col)) return;
            destination = getPiece(row, col);
        }
        if (destination.side == getOtherSide()) {
            addMove(moves, startRow, startCol, row, col);
        }
    }

    private void findMiscMove(List<SearchNode> moves, int oldrow, int oldcol, int newrow, int newcol, boolean captureAllowed, boolean captureRequired) {
        if (illegalCell(newrow, newcol)) return;
        ChessPiece destination = getPiece(newrow, newcol);
        if (destination == EMPTY && ! captureRequired) {
            addMove(moves, oldrow, oldcol, newrow, newcol);
        } else if (destination != EMPTY && captureAllowed) {
            if (destination.side == getOtherSide()) {
                addMove(moves, oldrow, oldcol, newrow, newcol);
            }
        }
    }

    private void findPawnMoves(List<SearchNode> moves, int row, int col) {
        int rowStep;
        int startRow;
        ChessPiece piece = getPiece(row, col);
        if (piece.side == WHITE) {
            rowStep = 1;
            startRow = 1;
        } else {
            rowStep = -1;
            startRow = 6;
        }
        findMiscMove(moves, row, col, row + rowStep, col, false, false);
        if (row == startRow && getPiece(row + rowStep, col) == EMPTY && getPiece(row + rowStep*2, col) == EMPTY)
            addMove(moves, row, col, row + rowStep*2, col);
        findMiscMove(moves, row, col, row + rowStep, col-1, true, true);
        findMiscMove(moves, row, col, row + rowStep, col+1, true, true);
    }

    private void findKnightMoves(List<SearchNode> moves, int row, int col) {
        findMiscMove(moves, row, col, row+2, col+1, true, false);
        findMiscMove(moves, row, col, row+2, col-1, true, false);
        findMiscMove(moves, row, col, row-2, col+1, true, false);
        findMiscMove(moves, row, col, row-2, col-1, true, false);
        findMiscMove(moves, row, col, row+1, col+2, true, false);
        findMiscMove(moves, row, col, row-1, col+2, true, false);
        findMiscMove(moves, row, col, row+1, col-2, true, false);
        findMiscMove(moves, row, col, row-1, col-2, true, false);
    }

    private void findBishopMoves(List<SearchNode> moves, int row, int col) {
        findStraightMoves(moves, row, col, 1, 1, 8);
        findStraightMoves(moves, row, col, 1, -1,8);
        findStraightMoves(moves, row, col, -1, 1,8);
        findStraightMoves(moves, row, col, -1, -1,8);
    }

    private void findRookMoves(List<SearchNode> moves, int row, int col) {
        findStraightMoves(moves, row, col, 1, 0,8);
        findStraightMoves(moves, row, col, -1, 0,8);
        findStraightMoves(moves, row, col, 0, 1,8);
        findStraightMoves(moves, row, col, 0, -1,8);
    }

    private void findQueenMoves(List<SearchNode> moves, int row, int col) {
        findRookMoves(moves, row, col);
        findBishopMoves(moves, row, col);
    }

    private void findKingMoves(List<SearchNode> moves, int row, int col) {
        findStraightMoves(moves, row, col, 1, 0,1);
        findStraightMoves(moves, row, col, -1, 0,1);
        findStraightMoves(moves, row, col, 0, 1,1);
        findStraightMoves(moves, row, col, 0, -1,1);
        findStraightMoves(moves, row, col, 1, 1, 1);
        findStraightMoves(moves, row, col, 1, -1,1);
        findStraightMoves(moves, row, col, -1, 1,1);
        findStraightMoves(moves, row, col, -1, -1,1);

        ChessPiece king = getPiece(row, col);
        if (king.side == WHITE) {
            if (isWhiteLeftCastlePossible())
                addCastleMove(moves, WHITE, 0);
            if (isWhiteRightCastlePossible())
                addCastleMove(moves, WHITE, 7);
        } else {
            if (isBlackLeftCastlePossible())
                addCastleMove(moves, BLACK, 0);
            if (isBlackRightCastlePossible())
                addCastleMove(moves, BLACK, 7);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////

    // returns true if the given side is in check
    boolean inCheck(Side side) {
        if (side == WHITE)
            return attackChecker(WHITE, getWhiteKingRow(), getWhiteKingCol());
        else if (side == BLACK)
            return attackChecker(BLACK, getBlackKingRow(), getBlackKingCol());
        else
            throw new IllegalStateException("side is not WHITE or BLACK");
    }

    // returns true if the square at (targetRow, targetCol) is under attack
    boolean attackChecker(Side targetSide, int targetRow, int targetCol) {
        if (attackCheckerStraight(targetSide, targetRow, targetCol, -1, -1)) return true;
        if (attackCheckerStraight(targetSide, targetRow, targetCol, -1, 0)) return true;
        if (attackCheckerStraight(targetSide, targetRow, targetCol, -1, 1)) return true;
        if (attackCheckerStraight(targetSide, targetRow, targetCol, 0, -1)) return true;
        if (attackCheckerStraight(targetSide, targetRow, targetCol, 0, 1)) return true;
        if (attackCheckerStraight(targetSide, targetRow, targetCol, 1, -1)) return true;
        if (attackCheckerStraight(targetSide, targetRow, targetCol, 1, 0)) return true;
        if (attackCheckerStraight(targetSide, targetRow, targetCol, 1, 1)) return true;
        if (attackCheckerKnight(targetSide, targetRow+2, targetCol+1)) return true;
        if (attackCheckerKnight(targetSide, targetRow+2, targetCol-1)) return true;
        if (attackCheckerKnight(targetSide, targetRow-2, targetCol+1)) return true;
        if (attackCheckerKnight(targetSide, targetRow-2, targetCol-1)) return true;
        if (attackCheckerKnight(targetSide, targetRow+1, targetCol+2)) return true;
        if (attackCheckerKnight(targetSide, targetRow-1, targetCol+2)) return true;
        if (attackCheckerKnight(targetSide, targetRow+1, targetCol-2)) return true;
        if (attackCheckerKnight(targetSide, targetRow-1, targetCol-2)) return true;
        return false;
    }


    private boolean attackCheckerStraight(Side targetSide, int targetRow, int targetCol, int rowDelta, int colDelta) {
        int row = targetRow;
        int col = targetCol;
        int count = 0;
        ChessPiece attacker;
        do {
            count ++;
            row += rowDelta;
            col += colDelta;
            if (illegalCell(row, col)) return false;
            attacker = getPiece(row, col);
        } while (attacker == EMPTY);
        if (attacker.side != targetSide) {
            if (attacker.isQueen())
                return true;
            if (attacker.isRook() && (rowDelta == 0 || colDelta == 0))
                return true;
            if (attacker.isBishop() && (rowDelta != 0 && colDelta != 0))
                return true;
            if (attacker.isKing() && count == 1)
                return true;
            if (attacker.isPawn() && count == 1 && colDelta != 0) {
                if (attacker.side == WHITE && rowDelta == -1)
                    return true;
                else if (attacker.side == BLACK && rowDelta == 1)
                    return true;
            }
        }
        return false;
    }

    // returns true if there is a Knight at (attackerRow, attackerCol) with the opposite Side from targetSide
    private boolean attackCheckerKnight(Side targetSide, int attackerRow, int attackerCol) {
        if (illegalCell(attackerRow, attackerCol)) return false;
        ChessPiece attacker = getPiece(attackerRow, attackerCol);
        return (attacker != EMPTY && attacker.side != targetSide && attacker.isKnight());
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    ChessPiece getPiece(int row, int col) {
        //return board[row][col];
        switch (row) {
            case 0: return ChessPiece.getPiece((int) ((board01 >> (col*4)) & 0xF));
            case 1: return ChessPiece.getPiece((int) ((board01 >> (32 + col*4)) & 0xF));
            case 2: return ChessPiece.getPiece((int) ((board23 >> (col*4)) & 0xF));
            case 3: return ChessPiece.getPiece((int) ((board23 >> (32 + col*4)) & 0xF));
            case 4: return ChessPiece.getPiece((int) ((board45 >> (col*4)) & 0xF));
            case 5: return ChessPiece.getPiece((int) ((board45 >> (32 + col*4)) & 0xF));
            case 6: return ChessPiece.getPiece((int) ((board67 >> (col*4)) & 0xF));
            case 7: return ChessPiece.getPiece((int) ((board67 >> (32 + col*4)) & 0xF));
        }
        throw new IllegalStateException("illegal row");
    }

    private void setPiece(ChessPiece piece, int row, int col) {
        //board[row][col] = piece;
        int shift;
        switch (row) {
            case 0 -> {
                shift = col * 4;
                board01 &= ~(0xFL << shift);
                if (piece != null) board01 |= ((long) piece.index) << shift;
            }
            case 1 -> {
                shift = 32 + col * 4;
                board01 &= ~(0xFL << shift);
                if (piece != null) board01 |= ((long) piece.index) << shift;
            }
            case 2 -> {
                shift = col * 4;
                board23 &= ~(0xFL << shift);
                if (piece != null) board23 |= ((long) piece.index) << shift;
            }
            case 3 -> {
                shift = 32 + col * 4;
                board23 &= ~(0xFL << shift);
                if (piece != null) board23 |= ((long) piece.index) << shift;
            }
            case 4 -> {
                shift = col * 4;
                board45 &= ~(0xFL << shift);
                if (piece != null) board45 |= ((long) piece.index) << shift;
            }
            case 5 -> {
                shift = 32 + col * 4;
                board45 &= ~(0xFL << shift);
                if (piece != null) board45 |= ((long) piece.index) << shift;
            }
            case 6 -> {
                shift = col * 4;
                board67 &= ~(0xFL << shift);
                if (piece != null) board67 |= ((long) piece.index) << shift;
            }
            case 7 -> {
                shift = 32 + col * 4;
                board67 &= ~(0xFL << shift);
                if (piece != null) board67 |= ((long) piece.index) << shift;
            }
        }

        if (piece != EMPTY && piece.isKing())
            setKing(piece.side, row, col);
    }

    /////////////////////////////

    boolean isSidePiece(Side side, int row, int col) {
        ChessPiece piece = getPiece(row,col);
        if (piece == null)
            return false;
        else
            return piece.side == side;
    }

/////////////////////////////


    boolean getDraw() {
        return getInfoBit(DRAW_BIT);
    }

    private void setDraw() {
        setInfoBit(DRAW_BIT);
    }

    boolean isWhiteLeftCastlePossible() {
        return ! getInfoBit(WHITE_LEFT_CASTLE_BIT);
    }

    boolean isWhiteRightCastlePossible() {
        return ! getInfoBit(WHITE_RIGHT_CASTLE_BIT);
    }

    boolean isBlackLeftCastlePossible() {
        return ! getInfoBit(BLACK_LEFT_CASTLE_BIT);
    }

    boolean isBlackRightCastlePossible() {
        return ! getInfoBit(BLACK_RIGHT_CASTLE_BIT);
    }

    private void setWhiteLeftCastleImpossible() {
        setInfoBit(WHITE_LEFT_CASTLE_BIT);
    }

    private void setWhiteRightCastleImpossible() {
        setInfoBit(WHITE_RIGHT_CASTLE_BIT);
    }

    private void setBlackLeftCastleImpossible() {
        setInfoBit(BLACK_LEFT_CASTLE_BIT);
    }

    private void setBlackRightCastleImpossible() {
        setInfoBit(BLACK_RIGHT_CASTLE_BIT);
    }


    int getWhiteKingRow() {
        return infoBits >> WHITE_KING_ROW_BITS & 0xF;
    }

    int getWhiteKingCol() {
        return (infoBits >> WHITE_KING_COL_BITS) & 0xF;
    }

    int getBlackKingRow() {
        return (infoBits >> BLACK_KING_ROW_BITS) & 0xF;
    }

    int getBlackKingCol() {
        return (infoBits >> BLACK_KING_COL_BITS) & 0xF;
    }

    int getKingRow(Side side) {
        if (side == WHITE)
            return getWhiteKingRow();
        else if (side == BLACK)
            return getBlackKingRow();
        else
            throw new IllegalStateException("side must be WHITE or BLACK");
    }

    int getKingCol(Side side) {
        if (side == WHITE)
            return getWhiteKingCol();
        else if (side == BLACK)
            return getBlackKingCol();
        else
            throw new IllegalStateException("side must be WHITE or BLACK");
    }

    private void setKing(Side side, int row, int col) {
        if (side == WHITE)
            setWhiteKing(row, col);
        else if (side == BLACK)
            setBlackKing(row, col);
    }

    private void setWhiteKing(int row, int col) {
        infoBits &= ~ 0xFF;
        infoBits |= col << WHITE_KING_COL_BITS;
        infoBits |= row << WHITE_KING_ROW_BITS;
    }

    private void setBlackKing(int row, int col) {
        infoBits &= ~ (0xFF << 8);
        infoBits |= col << BLACK_KING_COL_BITS;
        infoBits |= row << BLACK_KING_ROW_BITS;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    private boolean getInfoBit(int bitIndex) {
        return ((infoBits >> bitIndex) & 1) == 1;
    }

    private void setInfoBit(int bitIndex) {
        infoBits |= (1 << bitIndex);
    }

    private void clearInfoBit(int bitIndex) {
        infoBits &= ~(1 << bitIndex);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    int getMoveCount() {
        return ((infoBits >> MOVE_COUNT_BITS) & 0b1111111);
    }

    private void clearMoveCount() {
        infoBits &= ~((0b1111111) << MOVE_COUNT_BITS);
    }

    private void setMoveCount(int newCount) {
        clearMoveCount();
        infoBits |= newCount << MOVE_COUNT_BITS;
    }

    private void incrementMoveCount() {
        setMoveCount(getMoveCount() + 1);
    }


    //////////////////////////////////////////////////////////////////////////////////////////////

}
