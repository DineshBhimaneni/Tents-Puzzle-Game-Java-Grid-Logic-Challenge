import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GreedyCPU {

    public static void solveAll(GameState state) {
        int n = state.getSize();
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (state.getSolutionCell(r, c) == GameState.TENT) {
                    state.setCell(r, c, GameState.TENT);
                } else if (state.getSolutionCell(r, c) == GameState.EMPTY && state.getCell(r, c) != GameState.TREE) {
                    state.setCell(r, c, GameState.GRASS);
                }
            }
        }
    }

    public static boolean makeGreedyMove(GameState state) {
        if (applyForcedMoves(state))
            return true;
        if (applySingleNeighborTree(state))
            return true;
        if (applyDegreeHeuristic(state))
            return true;
        return applySolutionFallback(state);
    }

    // Only applies logically forced moves, safe for backtracking propagation
    public static boolean makeSafeMove(GameState state) {
        if (applyForcedMoves(state))
            return true;
        if (applySingleNeighborTree(state))
            return true;
        return false;
    }

    private static boolean applyForcedMoves(GameState state) {
        int n = state.getSize();

        for (int r = 0; r < n; r++) {
            int target = state.getRowTarget(r);
            int current = state.getRowUsed(r);
            if (current >= target)
                continue;

            List<Point> validSpots = new ArrayList<>();
            for (int c = 0; c < n; c++) {
                if (state.getCell(r, c) == GameState.EMPTY && isValidTentSpot(state, r, c)) {
                    validSpots.add(new Point(r, c));
                }
            }

            if (validSpots.size() > 0 && (target - current) == validSpots.size()) {
                Point p = validSpots.get(0);
                state.placeTent(p.x, p.y);
                return true;
            }
        }

        for (int c = 0; c < n; c++) {
            int target = state.getColTarget(c);
            int current = state.getColUsed(c);
            if (current >= target)
                continue;

            List<Point> validSpots = new ArrayList<>();
            for (int r = 0; r < n; r++) {
                if (state.getCell(r, c) == GameState.EMPTY && isValidTentSpot(state, r, c)) {
                    validSpots.add(new Point(r, c));
                }
            }

            if (validSpots.size() > 0 && (target - current) == validSpots.size()) {
                Point p = validSpots.get(0);
                state.placeTent(p.x, p.y);
                return true;
            }
        }

        return false;
    }

    private static boolean applySingleNeighborTree(GameState state) {
        List<Point> unsatisfiedTrees = getUnsatisfiedTrees(state);

        for (Point tree : unsatisfiedTrees) {
            List<Point> validNeighbors = getValidTentNeighbors(state, tree.x, tree.y);
            if (validNeighbors.size() == 1) {
                Point p = validNeighbors.get(0);
                state.placeTent(p.x, p.y);
                return true;
            }
        }
        return false;
    }

    private static boolean applyDegreeHeuristic(GameState state) {
        List<Point> unsatisfiedTrees = getUnsatisfiedTrees(state);

        if (unsatisfiedTrees.isEmpty())
            return false;

        Collections.sort(unsatisfiedTrees, new Comparator<Point>() {
            @Override
            public int compare(Point p1, Point p2) {
                int n1 = getValidTentNeighbors(state, p1.x, p1.y).size();
                int n2 = getValidTentNeighbors(state, p2.x, p2.y).size();
                return Integer.compare(n1, n2);
            }
        });

        for (Point tree : unsatisfiedTrees) {
            List<Point> validNeighbors = getValidTentNeighbors(state, tree.x, tree.y);
            if (!validNeighbors.isEmpty()) {
                Point p = validNeighbors.get(0);
                state.placeTent(p.x, p.y);
                return true;
            }
        }

        return false;
    }

    private static boolean applySolutionFallback(GameState state) {
        int n = state.getSize();
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (state.getSolutionCell(r, c) == GameState.TENT && state.getCell(r, c) != GameState.TENT) {
                    if (isValidTentSpot(state, r, c)) {
                        state.placeTent(r, c);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static List<Point> getUnsatisfiedTrees(GameState state) {
        List<Point> list = new ArrayList<>();
        List<Point> allTrees = state.getTrees();

        for (Point t : allTrees) {
            if (!isTreeSatisfied(state, t.x, t.y)) {
                list.add(t);
            }
        }
        return list;
    }

    private static boolean isTreeSatisfied(GameState state, int r, int c) {
        int[][] dirs = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
        for (int[] d : dirs) {
            int nr = r + d[0];
            int nc = c + d[1];
            if (state.inBounds(nr, nc) && state.getCell(nr, nc) == GameState.TENT) {
                return true;
            }
        }
        return false;
    }

    private static List<Point> getValidTentNeighbors(GameState state, int r, int c) {
        List<Point> neighbors = new ArrayList<>();
        int[][] dirs = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
        for (int[] d : dirs) {
            int nr = r + d[0];
            int nc = c + d[1];
            if (state.inBounds(nr, nc) && state.getCell(nr, nc) == GameState.EMPTY) {
                if (isValidTentSpot(state, nr, nc)) {
                    neighbors.add(new Point(nr, nc));
                }
            }
        }
        return neighbors;
    }

    private static boolean isValidTentSpot(GameState state, int r, int c) {
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
        if (!hasTree)
            return false;

        return true;
    }
}
