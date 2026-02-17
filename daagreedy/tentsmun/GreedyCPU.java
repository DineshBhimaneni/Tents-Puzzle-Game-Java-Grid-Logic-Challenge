
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    // 3. DEGREE HEURISTIC (MIN VALID NEIGHBORS) - ENHANCED
    // ===============================================
    private static boolean applyDegreeHeuristic(GameState state) {
        List<Point> unsatisfiedTrees = getUnsatisfiedTrees(state);

        if (unsatisfiedTrees.isEmpty())
            return false;

        // Sort by number of valid neighbors (ascending)
        // This is the "Degree" - fewest options = most constrained = best guess
        Collections.sort(unsatisfiedTrees, new Comparator<Point>() {
            @Override
            public int compare(Point p1, Point p2) {
                int n1 = getValidTentNeighbors(state, p1.x, p1.y).size();
                int n2 = getValidTentNeighbors(state, p2.x, p2.y).size();
                return Integer.compare(n1, n2);
            }
        });

        // Try to place for the most constrained tree
        // Use intelligent position scoring instead of just picking first
        for (Point tree : unsatisfiedTrees) {
            List<Point> validNeighbors = getValidTentNeighbors(state, tree.x, tree.y);
            if (!validNeighbors.isEmpty()) {
                // Score each position and pick the best one
                Point bestPos = null;
                double bestScore = -1;

                for (Point pos : validNeighbors) {
                    double score = scorePosition(state, pos.x, pos.y);
                    if (score > bestScore) {
                        bestScore = score;
                        bestPos = pos;
                    }
                }

                if (bestPos != null) {
                    state.placeTent(bestPos.x, bestPos.y);
                    return true;
                }
            }
        }

        return false;
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

    /**
     * Scores a potential tent position based on strategic factors.
     * Higher score = better position.
     * Factors:
     * - Row/Column pressure (how close to target)
     * - Tree clustering (helps multiple unsatisfied trees)
     * - Board positioning (avoid edges when possible for flexibility)
     * - Chain reaction potential (creates forced moves)
     */
    private static double scorePosition(GameState state, int r, int c) {
        double score = 0;

        // 1. Row/Column Pressure Score (prioritize nearly-complete rows/cols)
        int rowTarget = state.getRowTarget(r);
        int rowUsed = state.getRowUsed(r);
        int colTarget = state.getColTarget(c);
        int colUsed = state.getColUsed(c);

        // Percentage complete for row and column
        double rowPressure = rowTarget > 0 ? (double) rowUsed / rowTarget : 0;
        double colPressure = colTarget > 0 ? (double) colUsed / colTarget : 0;

        // Prefer positions in rows/cols that are closer to completion
        score += rowPressure * 10;
        score += colPressure * 10;

        // 2. Tree Clustering Score (helps multiple nearby unsatisfied trees)
        int[][] dirs = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
        int unsatisfiedNeighborTrees = 0;
        for (int[] d : dirs) {
            int nr = r + d[0];
            int nc = c + d[1];
            if (state.inBounds(nr, nc) && state.getCell(nr, nc) == GameState.TREE) {
                if (!isTreeSatisfied(state, nr, nc)) {
                    unsatisfiedNeighborTrees++;
                }
            }
        }
        // Bonus for helping multiple trees
        score += unsatisfiedNeighborTrees * 5;

        // 3. Flexibility Score (avoid corners/edges slightly)
        int n = state.getSize();
        boolean isCorner = (r == 0 || r == n - 1) && (c == 0 || c == n - 1);
        boolean isEdge = (r == 0 || r == n - 1 || c == 0 || c == n - 1);

        if (!isCorner && !isEdge) {
            score += 2; // Small bonus for central positions
        }

        // 4. Chain Reaction Score (creates forced moves)
        // Check if placing here would create forced situations for other trees
        int forcedMovesCreated = 0;
        List<Point> allTrees = state.getTrees();
        for (Point tree : allTrees) {
            if (isTreeSatisfied(state, tree.x, tree.y))
                continue;

            // Count how many valid spots this tree would have after our placement
            int validCount = 0;
            for (int[] d : dirs) {
                int nr = tree.x + d[0];
                int nc = tree.y + d[1];
                if (nr == r && nc == c)
                    continue; // This is our placement position
                if (state.inBounds(nr, nc) && state.getCell(nr, nc) == GameState.EMPTY) {
                    // Would this still be valid after our tent placement?
                    if (Math.abs(nr - r) > 1 || Math.abs(nc - c) > 1) { // Not adjacent to our tent
                        if (isValidTentSpot(state, nr, nc)) {
                            validCount++;
                        }
                    }
                }
            }

            // If this tree would have exactly 1 valid option left, that's a forced move
            if (validCount == 1) {
                forcedMovesCreated++;
            }
        }
        score += forcedMovesCreated * 8;

        return score;
    }
}