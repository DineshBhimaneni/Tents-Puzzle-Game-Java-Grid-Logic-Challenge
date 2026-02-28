import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class DivideConquerCPU {

    public static boolean solve(GameState state) {
        boolean changed = true;
        while (changed) {
            changed = GreedyCPU.makeSafeMove(state);

            if (!isValidState(state)) {
                return false;
            }
        }

        if (state.isPuzzleComplete()) {
            return true;
        }

        List<Point> unsatisfiedTrees = new ArrayList<>();
        for (Point tree : state.getTrees()) {
            if (!isTreeSatisfied(state, tree)) {
                unsatisfiedTrees.add(tree);
            }
        }

        if (unsatisfiedTrees.isEmpty()) {
            return false;
        }

        int[] degrees = new int[unsatisfiedTrees.size()];
        for (int i = 0; i < unsatisfiedTrees.size(); i++) {
            List<Point> spots = getValidTentSpots(state, unsatisfiedTrees.get(i));
            degrees[i] = spots.size();

            if (degrees[i] == 0) {
                return false;
            }
        }

        GameState.insertionSort(unsatisfiedTrees, degrees);

        Point bestTree = unsatisfiedTrees.get(0);
        List<Point> bestMoves = getValidTentSpots(state, bestTree);

        for (Point move : bestMoves) {
            GameState nextState = new GameState(state);

            nextState.placeTent(move.x, move.y);

            if (solve(nextState)) {
                state.copyDataFrom(nextState);
                return true;
            }
        }

        return false;
    }

    private static boolean isValidState(GameState state) {
        int n = state.getSize();

        for (int i = 0; i < n; i++) {
            if (state.getRowUsed(i) > state.getRowTarget(i))
                return false;
            if (state.getColUsed(i) > state.getColTarget(i))
                return false;
        }

        return true;
    }

    private static boolean isTreeSatisfied(GameState state, Point tree) {
        int[][] dirs = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
        for (int[] d : dirs) {
            int nr = tree.x + d[0];
            int nc = tree.y + d[1];
            if (state.inBounds(nr, nc) && state.getCell(nr, nc) == GameState.TENT) {
                return true;
            }
        }
        return false;
    }

    private static List<Point> getValidTentSpots(GameState state, Point tree) {
        List<Point> spots = new ArrayList<>();
        int[][] dirs = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
        for (int[] d : dirs) {
            int nr = tree.x + d[0];
            int nc = tree.y + d[1];
            if (state.inBounds(nr, nc) && state.getCell(nr, nc) == GameState.EMPTY) {
                if (isLegalPlacement(state, nr, nc)) {
                    spots.add(new Point(nr, nc));
                }
            }
        }
        return spots;
    }

    public static boolean makeMove(GameState state) {
        if (state.isPuzzleComplete())
            return false;

        GameState clone = new GameState(state);
        boolean solved = solve(clone);

        if (!solved)
            return false;

        int n = state.getSize();
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (state.getCell(r, c) == GameState.EMPTY && clone.getCell(r, c) == GameState.TENT) {
                    state.placeTent(r, c);
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isLegalPlacement(GameState state, int r, int c) {
        if (!state.inBounds(r, c))
            return false;
        if (state.getCell(r, c) != GameState.EMPTY)
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

        if (state.getRowUsed(r) >= state.getRowTarget(r))
            return false;
        if (state.getColUsed(c) >= state.getColTarget(c))
            return false;

        boolean hasTree = false;
        int[][] dirs = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
        for (int[] d : dirs) {
            int nr = r + d[0];
            int nc = c + d[1];
            if (state.inBounds(nr, nc) && state.getCell(nr, nc) == GameState.TREE) {
                hasTree = true;
                break;
            }
        }
        return hasTree;
    }
}
