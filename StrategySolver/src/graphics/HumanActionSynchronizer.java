package graphics;

// Author: Andrew Merrill

import main.Action;

/**
 * Objects of this class allow one thread to "send" an object to another.
 * Thread 1 calls putAction to store the object,
 * and Thread 2 calls getAction to retrieve it.
 *
 * Adapted from CubbyHole at http://java.sun.com/docs/books/tutorial/essential/threads/synchronization.html
 */

public class HumanActionSynchronizer {
    private Action action;
    private boolean available; // true if there is an action available to get

    public HumanActionSynchronizer() {
        action = null;
        available = false;
    }


    // returns the saved Action, if there is one
    // otherwise it waits...
    public synchronized Action getAction() {
        while (available == false) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        available = false;
        notifyAll();
        return action;
    }

    // stores the given Action, if we don't have one already
    // otherwise it waits...
    public synchronized void putAction(Action action) {
        while (available == true) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        this.action = action;
        available = true;
        notifyAll();
    }
}