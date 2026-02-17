
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Divide and Conquer CPU opponent for Tents & Trees.
 * Uses recursive quadrant-based strategy to solve the puzzle.
 */
public class DivideAndConquerCPU {

    /**
     * Makes a single move using divide-and-conquer strategy.
     * Divides board into quadrants, solves most constrained areas first.
     */
    public static boolean makeMove(GameState state) {
        // First try forced moves (similar to greedy for efficiency)
        if (applyForcedMoves(state))
            return true;

        // Then apply divide-and-conquer strategy
        return applyDivideAndConquer(state);
    }

    /**
     * Divide and Conquer Strategy:
     * 1. Identify most constrained quadrant
     * 2. Solve trees in that quadrant first
     * 3. Propagate constraints
     */
    private static boolean applyDivideAndConquer(GameState state) {
        // Divide board into quadrants
        List<Quadrant> quadrants = divideIntoQuadrants(state);

        // Sort quadrants by constraint level (most constrained first)
        Collections.sort(quadrants, new Comparator<Quadrant>() {
            @Override
            public int compare(Quadrant q1, Quadrant q2) {
                return Double.compare(q2.constraintScore, q1.constraintScore);
            }
        });

        // Try to make a move in the most constrained quadrant
        for (Quadrant quad : quadrants) {
            if (solveMostConstrainedTreeInQuadrant(state, quad))
                return true;
        }

        // Fallback to solution if no strategic move found
        return applySolutionFallback(state);
    }

    /**
     * Divides the board into quadrants and calculates constraint scores.
     */
    private static List<Quadrant> divideIntoQuadrants(GameState state) {
        List<Quadrant> quadrants = new ArrayList<>();
        int n = state.getSize();
        int mid = n / 2;

        // Create 4 quadrants (or 2x2 for small boards)
        quadrants.add(new Quadrant(0, 0, mid, mid, state)); // Top-left
        quadrants.add(new Quadrant(0, mid, mid, n, state)); // Top-right
        quadrants.add(new Quadrant(mid, 0, n, mid, state)); // Bottom-left
        quadrants.add(new Quadrant(mid, mid, n, n, state)); // Bottom-right

        return quadrants;
    }

    /**
     * Represents a quadrant of the board with its constraint score.
     */
    private static class Quadrant {
        int rowStart, colStart, rowEnd, colEnd;
        double constraintScore;

        Quadrant(int rowStart, int colStart, int rowEnd, int colEnd, GameState state) {
            this.rowStart = rowStart;
            this.colStart = colStart;
            this.rowEnd = rowEnd;
            this.colEnd = colEnd;
            this.constraintScore = calculateConstraintScore(state);
        }

        /**
         * Calculates how constrained this quadrant is.
         * Higher score = more constrained = should solve first.
         */
        double calculateConstraintScore(GameState state) {
            double score = 0;

            for (int r = rowStart; r < rowEnd; r++) {
                for (int c = colStart; c < colEnd; c++) {
                    if (state.getCell(r, c) == GameState.TREE) {
                        if (!isTreeSatisfied(state, r, c)) {
                            int validOptions = countValidNeighbors(state, r, c);
                            if (validOptions == 1)
                                score += 10; // Very constrained
                            else if (validOptions == 2)
                                score += 5;
                            else if (validOptions > 0)
                                score += 2;
                        }
                    }
                }
            }

            // Also consider row/col pressure in this quadrant
            for (int r = rowStart; r < rowEnd; r++) {
                int target = state.getRowTarget(r);
                int used = state.getRowUsed(r);
                if (target > 0) {
                    double pressure = (double) used / target;
                    score += pressure * 3;
                }
            }

            return score;
        }
    }

    /**
     * Solves the most constrained tree in the given quadrant.
     */
    private static boolean solveMostConstrainedTreeInQuadrant(GameState state, Quadrant quad) {
        List<Point> unsatisfiedTrees = new ArrayList<>();

        // Find all unsatisfied trees in this quadrant
        for (int r = quad.rowStart; r < quad.rowEnd; r++) {
            for (int c = quad.colStart; c < quad.colEnd; c++) {
                if (state.getCell(r, c) == GameState.TREE) {
                    if (!isTreeSatisfied(state, r, c)) {
                        unsatisfiedTrees.add(new Point(r, c));
                    }
                }
            }
        }

        if (unsatisfiedTrees.isEmpty())
            return false;

        // Sort by valid neighbor count (most constrained first)
        Collections.sort(unsatisfiedTrees, new Comparator<Point>() {
            @Override
            public int compare(Point p1, Point p2) {
                int n1 = countValidNeighbors(state, p1.x, p1.y);
                int n2 = countValidNeighbors(state, p2.x, p2.y);
                return Integer.compare(n1, n2);
            }
        });

        // Try to place tent for the most constrained tree
        for (Point tree : unsatisfiedTrees) {
            List<Point> validNeighbors = getValidTentNeighbors(state, tree.x, tree.y);
            if (!validNeighbors.isEmpty()) {
                // Use strategic scoring for position selection
                Point bestPos = selectBestPosition(state, validNeighbors, quad);
                state.placeTent(bestPos.x, bestPos.y);
                return true;
            }
        }

        return false;
    }

    /**
     * Selects the best position from valid neighbors.
     * Prioritizes positions within the quadrant and strategic locations.
     */
    private static Point selectBestPosition(GameState state, List<Point> validNeighbors, Quadrant quad) {
        Point bestPos = validNeighbors.get(0);
        double bestScore = -1;

        for (Point pos : validNeighbors) {
            double score = 0;

            // Prefer positions within current quadrant
            if (pos.x >= quad.rowStart && pos.x < quad.rowEnd &&
                    pos.y >= quad.colStart && pos.y < quad.colEnd) {
                score += 15;
            }

            // Add strategic factors
            score += calculatePositionScore(state, pos.x, pos.y);

            if (score > bestScore) {
                bestScore = score;
                bestPos = pos;
            }
        }

        return bestPos;
    }

    /**
     * Calculates strategic score for a position.
     */
    private static double calculatePositionScore(GameState state, int r, int c) {
        double score = 0;

        // Row/col completion pressure
        int rowTarget = state.getRowTarget(r);
        int rowUsed = state.getRowUsed(r);
        if (rowTarget > 0) {
            score += ((double) rowUsed / rowTarget) * 8;
        }

        int colTarget = state.getColTarget(c);
        int colUsed = state.getColUsed(c);
        if (colTarget > 0) {
            score += ((double) colUsed / colTarget) * 8;
        }

        // Help multiple trees
        int helpfulTreeCount = 0;
        int[][] dirs = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
        for (int[] d : dirs) {
            int nr = r + d[0];
            int nc = c + d[1];
            if (state.inBounds(nr, nc) && state.getCell(nr, nc) == GameState.TREE) {
                if (!isTreeSatisfied(state, nr, nc)) {
                    helpfulTreeCount++;
                }
            }
        }
        score += helpfulTreeCount * 4;

        return score;
    }

    // ===============================================
    // FORCED MOVES (Shared with Greedy for efficiency)
    // ===============================================
    private static boolean applyForcedMoves(GameState state) {
        int n = state.getSize();

        // Forced rows
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

        // Forced columns
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

        // Forced single-neighbor trees
        List<Point> allTrees = state.getTrees();
        for (Point tree : allTrees) {
            if (!isTreeSatisfied(state, tree.x, tree.y)) {
                List<Point> validNeighbors = getValidTentNeighbors(state, tree.x, tree.y);
                if (validNeighbors.size() == 1) {
                    Point p = validNeighbors.get(0);
                    state.placeTent(p.x, p.y);
                    return true;
                }
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

    // ===============================================
    // HELPER METHODS
    // ===============================================

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

    private static int countValidNeighbors(GameState state, int r, int c) {
        return getValidTentNeighbors(state, r, c).size();
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

        // Check tent adjacency
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

        // Check row/col limits
        if (state.getRowUsed(r) >= state.getRowTarget(r))
            return false;
        if (state.getColUsed(c) >= state.getColTarget(c))
            return false;

        // Check tree adjacency
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
