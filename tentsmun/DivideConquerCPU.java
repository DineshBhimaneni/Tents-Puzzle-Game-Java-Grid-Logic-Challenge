import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class DivideConquerCPU {

    public static boolean solve(GameState state) {
        if (state.getSize() == 0)
            return true;

        boolean changed = true;
        boolean anyChange = false;

        while (changed) {
            changed = solveRecursive(state, 0, state.getSize() - 1);
            if (changed)
                anyChange = true;

            if (!isValidState(state))
                return false;
        }

        return anyChange || state.isPuzzleComplete();
    }

    private static boolean solveRecursive(GameState state, int leftCol, int rightCol) {

        if (leftCol == rightCol) {
            return solveSingleColumn(state, leftCol);
        }

        int mid = (leftCol + rightCol) / 2;

        boolean leftPlaced = solveRecursive(state, leftCol, mid);
        boolean rightPlaced = solveRecursive(state, mid + 1, rightCol);

        if (!Merge(state, mid)) {

            return false;
        }

        return leftPlaced || rightPlaced;
    }

    private static boolean solveSingleColumn(GameState state, int col) {
        boolean placedSomething = false;
        List<Point> treesInCol = new ArrayList<>();
        for (Point tree : state.getTrees()) {
            if (tree.y == col) {
                treesInCol.add(tree);
            }
        }


        for (int i = 0; i < treesInCol.size() - 1; i++) {
            for (int j = 0; j < treesInCol.size() - i - 1; j++) {
                Point t1 = treesInCol.get(j);
                Point t2 = treesInCol.get(j + 1);
                int spots1 = getValidTentSpots(state, t1).size();
                int spots2 = getValidTentSpots(state, t2).size();
                if (spots1 > spots2) {
                    treesInCol.set(j, t2);
                    treesInCol.set(j + 1, t1);
                }
            }
        }

        for (Point tree : treesInCol) {
            if (isTreeSatisfied(state, tree))
                continue;

            List<Point> validSpots = getValidTentSpots(state, tree);

            if (validSpots.size() == 1) {
                Point spot = validSpots.get(0);
                state.placeTent(spot.x, spot.y);
                placedSomething = true;
            }
        }
        return placedSomething;
    }

    private static boolean Merge(GameState state, int mid) {
        int n = state.getSize();

        if (mid + 1 < n) {
            for (int r = 0; r < n; r++) {
                if (state.getCell(r, mid) == GameState.TENT) {
                    for (int dr = -1; dr <= 1; dr++) {
                        int nr = r + dr;
                        if (state.inBounds(nr, mid + 1) && state.getCell(nr, mid + 1) == GameState.TENT) {
                            return false;
                        }
                    }
                }
            }
        }

        return isValidState(state);
    }

    private static boolean isValidState(GameState state) {
        int n = state.getSize();
        for (int i = 0; i < n; i++) {
            if (state.getRowUsed(i) > state.getRowTarget(i))
                return false;
            if (state.getColUsed(i) > state.getColTarget(i))
                return false;
        }

        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (state.getCell(r, c) == GameState.TENT) {
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
                }
            }
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
        return solveRecursive(state, 0, state.getSize() - 1);
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
