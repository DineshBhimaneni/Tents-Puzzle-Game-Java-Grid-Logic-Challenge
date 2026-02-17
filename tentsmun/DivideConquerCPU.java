import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class DivideConquerCPU {

    /**
     * Attempts to solve the game state using a Divide and Conquer (Recursive
     * Backtracking) strategy.
     * 1. Propagates constraints using the Greedy solver.
     * 2. If solved, returns true.
     * 3. If invalid, returns false (backtrack).
     * 4. If stuck but valid, picks a branching move (placing a tent for a
     * constrained tree) and recurses.
     */
    public static boolean solve(GameState state) {
        // 1. Propagate constraints using Greedy logic (Safe moves only)
        boolean changed = true;
        while (changed) {
            changed = GreedyCPU.makeSafeMove(state);

            // Check for immediate invalid states during propagation
            if (!isValidState(state)) {
                return false;
            }
        }

        // 2. Check if the puzzle is completely solved
        if (state.isPuzzleComplete()) {
            return true;
        }

        // 3. Divide: Find a branching candidate
        // Heuristic: Pick the tree with the fewest valid remaining tent spots (Minimum
        // Remaining Values)
        Point bestTree = null;
        List<Point> bestMoves = null;
        int minMoves = Integer.MAX_VALUE;

        for (Point tree : state.getTrees()) {
            if (isTreeSatisfied(state, tree))
                continue;

            List<Point> validSpots = getValidTentSpots(state, tree);

            // If a tree has 0 valid spots but is not satisfied, this path is dead
            if (validSpots.isEmpty()) {
                return false;
            }

            if (validSpots.size() < minMoves) {
                minMoves = validSpots.size();
                bestTree = tree;
                bestMoves = validSpots;
                // Optimization: If we find a tree with only 1 move, we could technically treat
                // it as forced,
                // but GreedyCPU should have caught that.
            }
        }

        if (bestTree == null) {
            // No unsatisfied trees found, but isPuzzleComplete returned false?
            // This might happen if row/col counts are wrong even if trees are happy.
            return false;
        }

        // 4. Conquer: Recursively try each valid move
        for (Point move : bestMoves) {
            // Clone the state to creating an independent branch
            GameState nextState = new GameState(state);

            // Apply the move (Divide)
            nextState.placeTent(move.x, move.y);

            // Recurse (Conquer)
            if (solve(nextState)) {
                // If the recursive call succeeded, we found the solution!
                // Copy the solution back to the original state object so the UI sees it.
                state.copyDataFrom(nextState);
                return true;
            }
            // If solve returns false, we discard nextState and loop to the next option
            // (Backtrack)
        }

        return false;
    }

    // Helper: Check if the current partial state is valid (no broken constraints)
    // We already know GreedyCPU checks individual move validity, but we double
    // check global constraints if needed.
    // For now, relies on GreedyCPU's placement checks and local checks.
    private static boolean isValidState(GameState state) {
        int n = state.getSize();

        // Check if any row/col exceeded target
        for (int i = 0; i < n; i++) {
            if (state.getRowUsed(i) > state.getRowTarget(i))
                return false;
            if (state.getColUsed(i) > state.getColTarget(i))
                return false;
        }

        // Note: We don't check for "less than" target here because the board is
        // partially filled.
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
                // We must check if placing a tent here would be valid
                // We can reuse the logic from GreedyCPU essentially, but we need access to it.
                // Since GreedyCPU logic is private/static, we might duplicate or expose it.
                // For now, let's duplicate the basic check or make GreedyCPU public.
                // Actually, GameState has placeTent which just sets it.
                // We need to simulate checking if it's a legal spot.
                if (isLegalPlacement(state, nr, nc)) {
                    spots.add(new Point(nr, nc));
                }
            }
        }
        return spots;
    }

    // Finds a solution from the current state and applies only one tent placement
    // (the next logical step)
    public static boolean makeMove(GameState state) {
        if (state.isPuzzleComplete())
            return false;

        // 1. Working on a clone to find the full solution
        GameState clone = new GameState(state);
        boolean solved = solve(clone);

        if (!solved)
            return false;

        // 2. Find a difference between current state and solved state
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

    // Re-implementing basic legality check similar to GreedyCPU's isValidTentSpot
    // ideally we should refactor GreedyCPU to expose this, but to keep files
    // separate and safe:
    private static boolean isLegalPlacement(GameState state, int r, int c) {
        if (!state.inBounds(r, c))
            return false;
        if (state.getCell(r, c) != GameState.EMPTY)
            return false;

        // Adjacency check
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

        // Row/Col limits check
        if (state.getRowUsed(r) >= state.getRowTarget(r))
            return false;
        if (state.getColUsed(c) >= state.getColTarget(c))
            return false;

        // Must be adjacent to at least one tree (that doesn't already have a tent?)
        // The rule is "Tent must be attached to a tree".
        // In our generator/logic, we usually ensure 1-1 mapping.
        // For a valid move, it must be next to *some* tree.
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
