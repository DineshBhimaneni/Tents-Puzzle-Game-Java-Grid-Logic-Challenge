import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

public class BacktrackingCPU {

    private GameState state;
    private List<Point> trees;
    private boolean[] treeAssigned;

    private Stack<Frame> callStack;

    private static final int[][] dirs = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };

    public BacktrackingCPU(GameState state) {
        this.state = state;

        if (state.getSize() == 0) {
            this.trees = new ArrayList<>();
            return;
        }

        this.trees = new ArrayList<>(state.getTrees());
        Collections.sort(trees, new Comparator<Point>() {
            @Override
            public int compare(Point p1, Point p2) {
                if (p1.y != p2.y) {
                    return Integer.compare(p1.y, p2.y);
                }
                return Integer.compare(p1.x, p2.x);
            }
        });

        this.treeAssigned = new boolean[trees.size()];
        this.callStack = new Stack<>();

        callStack.push(new Frame(0));
    }
    public boolean step() {
        if (callStack.isEmpty()) {
            return false;
        }

        Frame current = callStack.peek();

        if (current.treeIndex == trees.size()) {
            if (isValidFinalState(state)) {
                return true;
            } else {
                callStack.pop();
                return false;
            }
        }

        Point tree = trees.get(current.treeIndex);

        if (current.isExploring) {
            int prevDir = current.dirIndex - 1;
            int[] d = dirs[prevDir];
            int nr = tree.x + d[0];
            int nc = tree.y + d[1];

            state.setCell(nr, nc, GameState.EMPTY);
            treeAssigned[current.treeIndex] = false;
            current.isExploring = false;

            return false;
        }

        while (current.dirIndex < dirs.length) {
            int dirToTry = current.dirIndex;
            int[] d = dirs[dirToTry];
            current.dirIndex++;

            int nr = tree.x + d[0];
            int nc = tree.y + d[1];

            if (isValidPlacement(state, nr, nc)) {
                state.placeTent(nr, nc);
                treeAssigned[current.treeIndex] = true;

                current.isExploring = true;

                callStack.push(new Frame(current.treeIndex + 1));

                return false;
            }
        }

        callStack.pop();
        return false;
    }

    public boolean isExhausted() {
        return callStack.isEmpty();
    }

    public boolean isSolved() {
        return currentTreeIndex() == trees.size() && isValidFinalState(state);
    }

    private int currentTreeIndex() {
        if (callStack.isEmpty())
            return -1;
        return callStack.peek().treeIndex;
    }
    private static boolean isValidPlacement(GameState state, int r, int c) {
        if (!state.inBounds(r, c))
            return false;
        if (state.getCell(r, c) != GameState.EMPTY)
            return false;

        if (state.getRowUsed(r) >= state.getRowTarget(r))
            return false;
        if (state.getColUsed(c) >= state.getColTarget(c))
            return false;

        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0)
                    continue;
                int nr = r + dr;
                int nc = c + dc;
                if (state.inBounds(nr, nc) && state.getCell(nr, nc) == GameState.TENT) {
                    return false;
                }
            }
        }

        return true;
    }
    