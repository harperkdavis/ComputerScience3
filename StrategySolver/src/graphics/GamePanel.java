package graphics;

// Author: Andrew Merrill

import main.Action;
import main.SearchNode;
import main.Side;
import main.State;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public abstract class GamePanel extends JPanel implements MouseInputListener {
    // the current state of the game
    protected State gameState;

    // the most recently taken action (in case you want to highlight it on the screen)
    protected main.Action recentAction;

    // a list of the possible children on the current state
    //   (in case you want to visually mark them on the screen)
    protected java.util.List<SearchNode> children;

    // the side that is currently playing
    protected Side side;

    // the width and height of the board on the screen
    //  (does not include the dead gray area)
    protected int boardWidth, boardHeight;

    // the offsets from the upper left corner of the panel to the upper left corner of the board
    protected int xoffset, yoffset;

    // the width and height of the entire game panel, including the dead gray area
    protected int panelWidth, panelHeight;

    // ratio of boardwidth to boardheight - set by the constructor
    protected final double boardAspectRatio;

    private HumanActionSynchronizer humanActionSynchronizer;

    public GamePanel(double boardAspectRatio) {
        this.boardAspectRatio = boardAspectRatio;
        xoffset = 0;
        yoffset = 0;
        gameState = null;
        recentAction = null;
        humanActionSynchronizer = null;
        children = null;
        addComponentListener(new GamePanelComponentListener());
    }

    //////////////////////////////////////////////////////////////////////////
    // You need to override this function in your subclass.
    //  It should draw the game board on the screen.
    //  You will want to use the class fields:
    //     boardWidth, boardHeight, xoffset, yoffset, panelWidth, panelHeight

    abstract protected void paintGamePanel(Graphics pen);

    //////////////////////////////////////////////////////////////////////////
    // Override whichever of these you need to handle mouse events
    // If the user has selected an action,
    //   then you must call selectAction to indicate what action they selected

    protected void mousePressedHandler(int x, int y) {
    }

    protected void mouseReleasedHandler(int x, int y) {
    }

    protected void mouseClickedHandler(int x, int y) {
    }

    protected void mouseMovedHandler(int x, int y) {
    }

    protected void mouseDraggedHandler(int x, int y) {
    }

    //////////////////////////////////////////////////////////////////////////
    // Your subclass should call this function
    //   when the user has selected an Action to take.

    protected void selectAction(main.Action selectedAction) {
        humanActionSynchronizer.putAction(selectedAction);
    }

    //////////////////////////////////////////////////////////////////////////
    // You don't need to worry about the functions below this point

    protected void paintComponent(Graphics pen) {
        pen.setColor(Color.GRAY);
        pen.fillRect(0, 0, panelWidth, panelHeight);
        paintGamePanel(pen);
    }

    public void setState(State gameState) {
        this.gameState = gameState;
        repaint();
    }

    public void setRecentAction(Action recentAction) {
        this.recentAction = recentAction;
        repaint();
    }

    public void readyForAction(Side side, java.util.List<SearchNode> children, HumanActionSynchronizer humanActionSynchronizer) {
        this.side = side;
        this.humanActionSynchronizer = humanActionSynchronizer;
        this.children = children;
        repaint();
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public void actionSelected() {
        removeMouseListener(this);
        removeMouseMotionListener(this);
        children = null;
        humanActionSynchronizer = null;
    }


    ////////////////////////////////////////////////////////////////////////////////////

    public void mousePressed(MouseEvent event) {
        int x = event.getX();
        int y = event.getY();
        if (x < xoffset || y < yoffset) return;
        if (x > xoffset + boardWidth || y > yoffset + boardHeight) return;
        mousePressedHandler(x - xoffset, y - yoffset);
    }

    public void mouseClicked(MouseEvent event) {
        int x = event.getX();
        int y = event.getY();
        if (x < xoffset || y < yoffset) return;
        if (x > xoffset + boardWidth || y > yoffset + boardHeight) return;
        mouseClickedHandler(x - xoffset, y - yoffset);
    }

    public void mouseReleased(MouseEvent event) {
        int x = event.getX();
        int y = event.getY();
        if (x < xoffset || y < yoffset) return;
        if (x > xoffset + boardWidth || y > yoffset + boardHeight) return;
        mouseReleasedHandler(x - xoffset, y - yoffset);
    }

    public void mouseMoved(MouseEvent event) {
        int x = event.getX();
        int y = event.getY();
        if (x < xoffset || y < yoffset) return;
        if (x > xoffset + boardWidth || y > yoffset + boardHeight) return;
        mouseMovedHandler(x - xoffset, y - yoffset);
    }

    public void mouseDragged(MouseEvent event) {
        int x = event.getX();
        int y = event.getY();
        if (x < xoffset || y < yoffset) return;
        if (x > xoffset + boardWidth || y > yoffset + boardHeight) return;
        mouseDraggedHandler(x - xoffset, y - yoffset);
    }

    public void mouseEntered(MouseEvent event) {
    }

    public void mouseExited(MouseEvent event) {
    }

////////////////////////////////////////////////////////////////////////////////////

    class GamePanelComponentListener extends ComponentAdapter {
        // resizes the board when the panel is resized
        // maintains the original board aspect ratio
        public void componentResized(ComponentEvent event) {
            Dimension d = getSize();
            panelWidth = d.width;
            panelHeight = d.height;
            int border = 10;
            double panelAspectRatio = panelWidth / (double) panelHeight;
            if (panelAspectRatio == boardAspectRatio) {
                // panel is just right!
                boardWidth = panelWidth - border * 2;
                boardHeight = panelHeight - border * 2;
                xoffset = border;
                yoffset = border;
            } else if (panelAspectRatio > boardAspectRatio) {
                // panel is too wide - add dead space on left and right sides
                boardHeight = panelHeight - border * 2;
                boardWidth = (int) (boardHeight * boardAspectRatio);
                yoffset = border;
                xoffset = (panelWidth - boardWidth) / 2;
            } else {
                // panel is too high - add dead space on top and bottom sides
                boardWidth = panelWidth - border * 2;
                boardHeight = (int) (boardWidth / boardAspectRatio);
                xoffset = border;
                yoffset = (panelHeight - boardHeight) / 2;
            }
            repaint();
        }
    } // class GamePanelComponentListener
} // class GamePanel