package datastructures;

/** One undoable operation: a description plus the code that reverses it. */
public class UndoAction {

    private final String description;
    private final Runnable undoCode;

    public UndoAction(String description, Runnable undoCode) {
        this.description = description;
        this.undoCode = undoCode;
    }

    public String getDescription() {
        return description;
    }

    public void execute() {
        undoCode.run();
    }
}
