import java.util.Stack;

public class BacktrackingMazeSolver {

    public boolean solve(Maze maze, Maze.MazeListener listener) {
        var start = maze.getInitialRatPosition();
        var end = maze.getInitialCheesePosition();
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }

        var path = new Stack<Maze.Location>();
        var current = start;
        
        while (true) {
            current.place(Maze.Cell.RAT);
            listener.mazeChanged(maze);

            if (current.isAt(end)) {
                return true;
            }

            if (current.above().canBeMovedTo()) {
                path.push(current);
                current.place(Maze.Cell.PATH);
                current = current.above();

            } else if (current.toTheRight().canBeMovedTo()) {
                path.push(current);
                current.place(Maze.Cell.PATH);
                current = current.toTheRight();

            } else if (current.below().canBeMovedTo()) {
                path.push(current);
                current.place(Maze.Cell.PATH);
                current = current.below();

            } else if (current.toTheLeft().canBeMovedTo()) {
                path.push(current);
                current.place(Maze.Cell.PATH);
                current = current.toTheLeft();

            } else {
                if (path.empty()) {
                    return false;
                }
                current = path.pop();
            }
        }
    }
}