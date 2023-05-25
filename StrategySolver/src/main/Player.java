package main;

// Author: Andrew Merrill

public abstract class Player implements Cloneable {

    private Side mySide = null;

    public abstract SearchNode pickMove(State currentState);

    public Side getSide() {
        return mySide;
    }

    void setSide(Side mySide) {
        this.mySide = mySide;
    }

    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    Player getClone() {
        try {
            return (Player) this.clone();
        } catch (CloneNotSupportedException e) {
            System.out.println("no clones");
            return null;
        }
    }
}
