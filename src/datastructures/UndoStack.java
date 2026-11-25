package datastructures;

/** Linked-list based Stack (LIFO) that stores undoable operations. */
public class UndoStack {

    private static class Node {
        UndoAction action;
        Node below;

        Node(UndoAction action, Node below) {
            this.action = action;
            this.below = below;
        }
    }

    private Node top;
    private int size;

    public void push(UndoAction action) {
        top = new Node(action, top);
        size++;
    }

    /** Removes and returns the latest action, or null if the stack is empty. */
    public UndoAction pop() {
        if (top == null) {
            return null;
        }
        UndoAction action = top.action;
        top = top.below;
        size--;
        return action;
    }

    public UndoAction peek() {
        return top == null ? null : top.action;
    }

    public boolean isEmpty() {
        return top == null;
    }

    public int size() {
        return size;
    }
}
