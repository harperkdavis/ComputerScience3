package games.checkers;

// Author: Andrew Merrill

import main.SearchNode;
import main.Side;
import main.State;

import java.util.*;

public class CheckersState extends State {
    static final int SIZE = 8;
    static final Side BLACK = Side.ONE;    // Side.ONE is Max Side
    static final Side RED = Side.TWO;      // Side.TWO is Min Side

    static final Side TIE = null;

    static final int PIECE = 1;
    static final int KING = 2;
    static final int BLACK_MASK = 4;
    static final int RED_MASK = 8;

    private long red, black; // 32 blocks of 2 bits: 00 blank, 01 regular piece, 11 king
    private boolean draw = false;
    private int timeSinceProgress;

    public CheckersState() {
        super(BLACK);
        this.red = 0x5555550000000000L;
        this.black = 0x0000000000555555L;
//        this.red =   0x0014005400550000L;
//        this.black = 0x0000000000005555L;
        timeSinceProgress = 0;
    }

    private CheckersState(Side newSideToPlay, CheckersState oldBoard, int timeSinceProgress) {
        super(newSideToPlay);
        this.red = oldBoard.red;
        this.black = oldBoard.black;
        this.draw = oldBoard.draw;
        this.timeSinceProgress = timeSinceProgress;
    }

    @Override
    public boolean equals(Object other) {
        CheckersState otherState = (CheckersState) other;
        return (black == otherState.black && red == otherState.red);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(black) ^ Long.hashCode(red);
    }

    @Override
    public String toString() {
        return "Black: " + Long.toHexString(black) + ", Red: " + Long.toHexString(red);
    }

    @Override
    public boolean isGameOver() {
        if (timeSinceProgress >= 40) {
            draw = true;
            //System.out.println("Draw!");
            return true;
        }
        List<SearchNode> children = listChildren();
        return children.isEmpty();
    }

    @Override
    public Side getWinner() {
        if (draw)
            return null;
        else if (getSideToPlay() == BLACK)
            return RED;
        else
            return BLACK;
    }

    @Override
    public List<SearchNode> listChildren() {
        List<SearchNode> regularMoves = new ArrayList<SearchNode>();
        List<SearchNode> jumpMoves = new ArrayList<SearchNode>();
        if (getSideToPlay() == BLACK) {
            for (int index = 0; index < 32; index++) {
                int piece = getBlackCell(index);
                if (piece != 0) {
                    List<SearchNode> jumps = findBlackJumpsForPiece(index, piece);
                    if (! jumps.isEmpty())
                        jumpMoves.addAll(jumps);
                    if (jumpMoves.isEmpty())
                        addBlackMovesForPiece(regularMoves, index, piece);
                }
            }

        } else { // RED to play
            for (int index = 0; index < 32; index++) {
                int piece = getRedCell(index);
                if (piece != 0) {
                    List<SearchNode> jumps = findRedJumpsForPiece(index, piece);
                    if (! jumps.isEmpty())
                        jumpMoves.addAll(jumps);
                    if (jumpMoves.isEmpty())
                        addRedMovesForPiece(regularMoves, index, piece);
                }
            }
        }

        if (jumpMoves.isEmpty())
            return regularMoves;
        else
            return jumpMoves;
    }

    private void addBlackMovesForPiece(List<SearchNode> moves, int index, int piece) {
        int row_parity = (index >> 2) & 1;
        if (index < 28) { // not top row
            if (index % 8 != 0) // not far left column
                maybeAddBlackMove(moves, index, index + 3 + row_parity, piece); // NW
            if (index % 8 != 7)  // not far right column
                maybeAddBlackMove(moves, index, index + 4 + row_parity, piece); // NE
        }
        if ((piece & KING) != 0) {
            if (index > 3) {  // not bottom row
                if (index % 8 != 0) // not far left column
                    maybeAddBlackMove(moves, index, index - 5 + row_parity, piece); // SW
                if (index % 8 != 7) // not far right column
                    maybeAddBlackMove(moves, index, index - 4 + row_parity, piece); // SE
            }
        }
    }

    private void addRedMovesForPiece(List<SearchNode> moves, int index, int piece) {
        int row_parity = (index >> 2) & 1;
        if (index > 3) {  // not bottom row
            if (index % 8 != 0) // not far left column
                maybeAddRedMove(moves, index, index - 5 + row_parity, piece); // SW
            if (index % 8 != 7) // not far right column
                maybeAddRedMove(moves, index, index - 4 + row_parity, piece); // SE
        }
        if ((piece & KING) != 0) {
            if (index < 28) { // not top row
                if (index % 8 != 0) // not far left column
                    maybeAddRedMove(moves, index, index + 3 + row_parity, piece); // NW
                if (index % 8 != 7)  // not far right column
                    maybeAddRedMove(moves, index, index + 4 + row_parity, piece); // NE
            }
        }
    }

    private void maybeAddBlackMove(List<SearchNode> moves, int fromIndex, int toIndex, int piece) {
        if (isEmptyCell(toIndex)) {
            CheckersState newBoard = new CheckersState(getOtherSide(), this, (piece & KING) == 0 ? 0 : timeSinceProgress+1);
            newBoard.clearBlackCell(fromIndex);
            if (toIndex >= 28)
                piece = piece | KING;
            newBoard.setBlackCell(toIndex, piece);
            moves.add(new SearchNode(new CheckersAction(fromIndex, toIndex, BLACK), newBoard));
        }
    }

    private void maybeAddRedMove(List<SearchNode> moves, int fromIndex, int toIndex, int piece) {
        if (isEmptyCell(toIndex)) {
            CheckersState newBoard = new CheckersState(getOtherSide(), this, (piece & KING) == 0 ? 0 : timeSinceProgress+1);
            newBoard.clearRedCell(fromIndex);
            if (toIndex <= 3)
                piece = piece | KING;
            newBoard.setRedCell(toIndex, piece);
            moves.add(new SearchNode(new CheckersAction(fromIndex, toIndex, RED), newBoard));
        }
    }

    private List<SearchNode> findBlackJumpsForPiece(int index, int piece) {
        ArrayList<SearchNode> moves = new ArrayList<SearchNode>();
        int row_parity = (index >> 2) & 1;
        if (index < 24) { // not top two rows
            if (index % 4 != 0) // not two far left columns
                maybeAddBlackJump(moves, index, index + 7, index + 3 + row_parity, piece); // NW
            if (index % 4 != 3)  // not two far right columns
                maybeAddBlackJump(moves, index, index + 9, index + 4 + row_parity, piece); // NE
        }
        if ((piece & KING) != 0) {
            if (index > 7) { // not bottom two rows
                if (index % 4 != 0) // not two far left columns
                    maybeAddBlackJump(moves, index, index - 9, index - 5 + row_parity, piece); // SW
                if (index % 4 != 3)  // not two far right columns
                    maybeAddBlackJump(moves, index, index - 7, index - 4 + row_parity, piece); // SE
            }
        }

        return moves;
    }

    private List<SearchNode> findRedJumpsForPiece(int index, int piece) {
        ArrayList<SearchNode> moves = new ArrayList<SearchNode>();
        int row_parity = (index >> 2) & 1;
        if (index > 7) { // not bottom two rows
            if (index % 4 != 0) // not two far left columns
                maybeAddRedJump(moves, index, index - 9, index - 5 + row_parity, piece); // SW
            if (index % 4 != 3)  // not two far right columns
                maybeAddRedJump(moves, index, index - 7, index - 4 + row_parity, piece); // SE
        }
        if ((piece & KING) != 0) {
            if (index < 24) { // not top two rows
                if (index % 4 != 0) // not two far left columns
                    maybeAddRedJump(moves, index, index + 7, index + 3 + row_parity, piece); // NW
                if (index % 4 != 3)  // not two far right columns
                    maybeAddRedJump(moves, index, index + 9, index + 4 + row_parity, piece); // NE
            }
        }
        return moves;
    }

    private void maybeAddBlackJump(List<SearchNode> moves, int fromIndex, int toIndex, int captureIndex, int piece) {
        if (isEmptyCell(toIndex) && isRedCell(captureIndex)) {
            boolean newKing = false;
            CheckersState newBoard = new CheckersState(RED, this, 0);
            newBoard.clearBlackCell(fromIndex);
            if (toIndex >= 28 && (piece & KING) == 0) {
                piece = piece | KING;
                newKing = true;
            }
            newBoard.setBlackCell(toIndex, piece);
            newBoard.clearRedCell(captureIndex);

            List<SearchNode> nextMoves = newBoard.findBlackJumpsForPiece(toIndex, piece);

            if (newKing || nextMoves.isEmpty()) {
                CheckersAction action = new CheckersAction(fromIndex, toIndex, BLACK);
                moves.add(new SearchNode(action, newBoard));
            } else {
                for (SearchNode nextMove : nextMoves) {
                    CheckersAction action = new CheckersAction(fromIndex, toIndex, BLACK, (CheckersAction) nextMove.action);
                    ((CheckersAction) nextMove.action).prevAction = action;
                    moves.add(new SearchNode(action, nextMove.state));
                }
            }
        }
    }

    private void maybeAddRedJump(List<SearchNode> moves, int fromIndex, int toIndex, int captureIndex, int piece) {
        if (isEmptyCell(toIndex) && isBlackCell(captureIndex)) {
            boolean newKing = false;
            CheckersState newBoard = new CheckersState(BLACK, this, 0);
            newBoard.clearRedCell(fromIndex);
            if (toIndex <= 3 && (piece & KING) == 0) {
                piece = piece | KING;
                newKing = true;
            }
            newBoard.setRedCell(toIndex, piece);
            newBoard.clearBlackCell(captureIndex);

            List<SearchNode> nextMoves = newBoard.findRedJumpsForPiece(toIndex, piece);

            if (newKing || nextMoves.isEmpty()) {
                CheckersAction action = new CheckersAction(fromIndex, toIndex, RED);
                moves.add(new SearchNode(action, newBoard));
            } else {
                for (SearchNode nextMove : nextMoves) {
                    CheckersAction action = new CheckersAction(fromIndex, toIndex, RED, (CheckersAction) nextMove.action);
                    ((CheckersAction) nextMove.action).prevAction = action;
                    moves.add(new SearchNode(action, nextMove.state));
                }
            }

        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    int getRedCell(int index) {
        return ((int) (red >> (index*2))) & 3;
    }

    int getBlackCell(int index) {
        return ((int) (black >> (index*2))) & 3;
    }

    private void setBlackCell(int index, int piece) {
        black |= ((((long) piece) & 3L) << (index*2));
    }

    private void setRedCell(int index, int piece) {
        red |= ((((long) piece) & 3L) << (index*2));
    }

    private void clearBlackCell(int index) {
        black &= ~(3L << (index*2));
    }

    private void clearRedCell(int index) {
        red &= ~(3L << (index*2));
    }


    boolean isBlackCell(int row, int col) {
        return getBlackCell(row * 4 + col / 2) != 0;
    }

    boolean isRedCell(int row, int col) {
        return getRedCell(row * 4 + col / 2) != 0;
    }

    boolean isBlackKing(int row, int col) { return (getBlackCell(row * 4 + col / 2) & KING) != 0; };

    boolean isRedKing(int row, int col) { return (getRedCell(row * 4 + col / 2) & KING) != 0; };

    boolean isBlackCell(int index) {
        return getBlackCell(index) != 0;
    }

    boolean isRedCell(int index) {
        return getRedCell(index) != 0;
    }

    boolean isEmptyCell(int row, int col) {
        int index = row * 4 + col / 2;
        return getBlackCell(index) == 0  &&  getRedCell(index) == 0;
    }

    boolean isEmptyCell(int index) {
        return getBlackCell(index) == 0 && getRedCell(index) == 0;
    }

    boolean isSideCell(Side side, int row, int col) {
        if (side == BLACK)
            return isBlackCell(row, col);
        else if (side == RED)
            return isRedCell(row, col);
        else
            throw new IllegalStateException("Side is neither red nor black: " + side);
    }

    int getCell(int row, int col) {
        int index = row * 4 + col / 2;
        int blackCell = getBlackCell(index);
        int redCell = getRedCell(index);
        if (blackCell != 0)
            return blackCell | BLACK_MASK;
        else if (redCell != 0)
            return redCell | RED_MASK;
        else
            return 0;
    }

}
