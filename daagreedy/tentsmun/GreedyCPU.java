
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class GreedyCPU {

    /**
     * Completes the game fully by filling the board with the correct solution.
     */
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

    /**
     * Makes a single "Greedy" move following a strict hierarchy of heuristics.
     * 1. Forced Row/Column: Remaining Empty == Remaining Needed
     * 2. Forced Tree: Tree has exactly 1 Valid Neighbor
     * 3. Degree Heuristic: Tree has fewest Valid Neighbors (pick one)
     * 4. Fallback: Use solution to place a correct tent
     */
    public static boolean makeGreedyMove(GameState state) {
        if (applyForcedMoves(state))
            return true;
        if (applySingleNeighborTree(state))
            return true;
        if (applyDegreeHeuristic(state))
            return true;
        return applySolutionFallback(state);
    }

    // ===============================================
    // 1. FORCED ROW / COLUMN MOVES
    // ===============================================
    private static boolean applyForcedMoves(GameState state) {
        int n = state.getSize();

        // Rows
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

        // Cols
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

    // ===============================================
    // 2. FORCED TREE (SINGLE VALID NEIGHBOR)
    // ===============================================
    private static boolean applySingleNeighborTree(GameState state) {
        // Iterate all trees. If a tree needs a tent and has exactly 1 valid option,
        // place it.
        // NOTE: We need to know if a tree *needs* a tent.
        // Since we don't have explicit "Tree satisfied" state without checking
        // matching,
        // we'll assume any tree *not adjacent to a tent* needs one.

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

    // ===============================================
    // 3. DEGREE HEURISTIC (MIN VALID NEIGHBORS)
    // ===============================================
    private static boolean applyDegreeHeuristic(GameState state) {
        List<Point> unsatisfiedTrees = getUnsatisfiedTrees(state);

        if (unsatisfiedTrees.isEmpty())
            return false;

        // -----------------------------------------------
        // INSERTION SORT by degree (number of valid tent neighbors), ascending.
        //
        // WHY INSERTION SORT?
        // 1. Dataset is very small (at most N+1 items, where N ≤ 10).
        // For small N, Insertion Sort's minimal overhead outperforms
        // O(N log N) algorithms like Merge Sort or Quick Sort.
        // 2. Best case O(N) if nearly sorted; worst case O(N²) is
        // negligible since N ≤ 11.
        // 3. In-place (no extra memory) and stable (preserves order
        // of equal-degree trees).
        // 4. Fewer comparisons on average than Selection Sort for
        // nearly-sorted or small inputs.
        //
        // We pre-compute the degree (valid neighbor count) for each
        // tree into a parallel array to avoid redundant calls to the
        // expensive getValidTentNeighbors() during element shifts.
        // -----------------------------------------------
        insertionSortByDegree(unsatisfiedTrees, state);

        // Try to place for the most constrained tree
        // We just pick the first valid spot. This is a heuristic.
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

    /**
     * Insertion Sort: sorts trees by their "degree" (number of valid tent
     * neighbors) in ascending order.
     *
     * Algorithm:
     * - Pre-compute degree[] for each tree (avoids repeated expensive calls).
     * - For each element i from 1..n-1, shift it leftward into its correct
     * position among elements 0..i-1.
     *
     * Time Complexity : O(n²) worst-case, O(n) best-case (already sorted).
     * Space Complexity: O(n) for the cached degree array.
     * Stable : Yes (equal-degree elements retain their relative order).
     */
    private static void insertionSortByDegree(List<Point> trees, GameState state) {
        int size = trees.size();

        // Pre-compute degree for each tree to avoid redundant neighbor lookups
        int[] degree = new int[size];
        for (int i = 0; i < size; i++) {
            Point t = trees.get(i);
            degree[i] = getValidTentNeighbors(state, t.x, t.y).size();
        }

        // Insertion Sort on the parallel arrays (trees list + degree array)
        for (int i = 1; i < size; i++) {
            Point keyPoint = trees.get(i);
            int keyDegree = degree[i];
            int j = i - 1;

            // Shift elements that have a larger degree to the right
            while (j >= 0 && degree[j] > keyDegree) {
                trees.set(j + 1, trees.get(j));
                degree[j + 1] = degree[j];
                j--;
            }

            // Place the key in its correct sorted position
            trees.set(j + 1, keyPoint);
            degree[j + 1] = keyDegree;
        }
    }

    // ===============================================
    // 4. FALLBACK (SOLUTION HINT)
    // ===============================================
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

    // ===============================================
    // HELPERS
    // ===============================================

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
        // A tree is satisfied if it has at least one tent orthogonally adjacent.
        // Note: In detailed matching, one tent might belong to another tree, but for
        // greedy local logic, "has a tent neighbor" is the first check.
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

    // Checks if a tent can legally be placed at (r,c) according to game rules:
    // 1. Not touching another tent (diagonal or orthogonal)
    // 2. Row/Col limit not exceeded (already checked in forced logic, but good
    // primarily here?
    // Actually forced logic checks target-current. Individual placement should also
    // respect it.)
    private static boolean isValidTentSpot(GameState state, int r, int c) {
        if (!state.inBounds(r, c))
            return false;
        if (state.getCell(r, c) != GameState.EMPTY)
            return false;

        // Adjacency Check (Touch no other tents)
        // Scan 3x3 around
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

        // Ideally check Row/Col max, but sometimes we place to *reach* the max.
        // If current == target, we cannot place more.
        if (state.getRowUsed(r) >= state.getRowTarget(r))
            return false;
        if (state.getColUsed(c) >= state.getColTarget(c))
            return false;

        // Tree Adjacency Check (Must touch at least one Tree orthogonally)
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