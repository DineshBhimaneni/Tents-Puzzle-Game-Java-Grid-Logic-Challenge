import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class DivideConquerCPU {

    /**
     * Attempts to solve the game state using a Divide & Conquer strategy.
     * Divided into independent regions (connected components of dependent trees).
     * Solves each region deterministically without backtracking or guessing.
     */
    public static boolean solve(GameState state) {
        List<Point> trees = state.getTrees();
        if (trees.isEmpty())
            return true;

        // 1. Divide: Partition trees into independent regions
        List<List<Point>> regions = divideIntoRegions(state, trees);

        // 2. Conquer: Solve each region independently
        boolean allSolved = true;
        for (List<Point> region : regions) {
            if (!solveRegion(state, region)) {
                allSolved = false;
                // Continue to other regions even if one fails (deterministic behavior)
            }
        }

        return allSolved && state.isPuzzleComplete();
    }

    /**
     * Partitions trees into connected components (regions) based on dependency.
     * Two trees are dependent if they share a possible tent cell or if their
     * possible tent cells touch.
     */
    private static List<List<Point>> divideIntoRegions(GameState state, List<Point> trees) {
        int tSize = trees.size();
        boolean[][] adj = new boolean[tSize][tSize];

        // Precompute valid spots for each tree to build dependency graph
        List<List<Point>> treeValidSpots = new ArrayList<>();
        for (Point tree : trees) {
            treeValidSpots.add(getValidTentSpots(state, tree));
        }

        for (int i = 0; i < tSize; i++) {
            for (int j = i + 1; j < tSize; j++) {
                if (areTreesDependent(treeValidSpots.get(i), treeValidSpots.get(j))) {
                    adj[i][j] = adj[j][i] = true;
                }
            }
        }

        List<List<Point>> regions = new ArrayList<>();
        boolean[] visited = new boolean[tSize];
        for (int i = 0; i < tSize; i++) {
            if (!visited[i]) {
                List<Point> region = new ArrayList<>();
                List<Integer> queue = new ArrayList<>();
                queue.add(i);
                visited[i] = true;
                int head = 0;
                while (head < queue.size()) {
                    int curr = queue.get(head++);
                    region.add(trees.get(curr));
                    for (int next = 0; next < tSize; next++) {
                        if (adj[curr][next] && !visited[next]) {
                            visited[next] = true;
                            queue.add(next);
                        }
                    }
                }
                regions.add(region);
            }
        }
        return regions;
    }

    private static boolean areTreesDependent(List<Point> spots1, List<Point> spots2) {
        for (Point p1 : spots1) {
            for (Point p2 : spots2) {
                // Share same cell
                if (p1.equals(p2))
                    return true;
                // Touch each other (adjacent including diagonals)
                if (Math.abs(p1.x - p2.x) <= 1 && Math.abs(p1.y - p2.y) <= 1)
                    return true;
            }
        }
        return false;
    }

    /**
     * Solves a single region using deterministic constraint shrinking.
     */
    private static boolean solveRegion(GameState state, List<Point> region) {
        List<Point> remainingTrees = new ArrayList<>(region);

        while (!remainingTrees.isEmpty()) {
            // Filter out trees that are already satisfied
            remainingTrees.removeIf(tree -> isTreeSatisfied(state, tree));
            if (remainingTrees.isEmpty())
                break;

            // 4.1 Compute Valid Cells for each tree in region
            List<List<Point>> allValidSpots = new ArrayList<>();
            for (Point tree : remainingTrees) {
                allValidSpots.add(getValidTentSpots(state, tree));
            }

            // 4.2 Manual Sort trees by number of valid positions
            manualSort(remainingTrees, allValidSpots);

            // 4.3 Pick Most Constrained Tree
            Point mostConstrainedTree = remainingTrees.get(0);
            List<Point> validSpots = allValidSpots.get(0);

            // 4.4 Deterministic Check
            if (validSpots.size() == 1) {
                // 4.5 Update Board
                Point spot = validSpots.get(0);
                state.placeTent(spot.x, spot.y);
                // 4.6 Remove Tree (handled by next iteration's filter or explicit remove)
                remainingTrees.remove(0);
            } else {
                // Stop entire algorithm for this region if not deterministic
                return false;
            }
        }
        return true;
    }

    /**
     * Manual selection sort to avoid built-in sort.
     */
    private static void manualSort(List<Point> trees, List<List<Point>> spots) {
        int n = trees.size();
        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < n; j++) {
                if (spots.get(j).size() < spots.get(minIdx).size()) {
                    minIdx = j;
                }
            }
            // Swap
            Point tempTree = trees.get(minIdx);
            trees.set(minIdx, trees.get(i));
            trees.set(i, tempTree);

            List<Point> tempSpots = spots.get(minIdx);
            spots.set(minIdx, spots.get(i));
            spots.set(i, tempSpots);
        }
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
        // Since the requirement says "Solve the Tents board",
        // and "Each tent placement reduces the problem size",
        // we can just run the solver and if it placed something, we are good.
        // However, the user might want a single move UI.
        // Let's implement it to find the first deterministic move.

        List<Point> trees = state.getTrees();
        List<List<Point>> regions = divideIntoRegions(state, trees);

        for (List<Point> region : regions) {
            List<Point> remainingTrees = new ArrayList<>(region);
            remainingTrees.removeIf(tree -> isTreeSatisfied(state, tree));
            if (remainingTrees.isEmpty())
                continue;

            List<List<Point>> allValidSpots = new ArrayList<>();
            for (Point tree : remainingTrees) {
                allValidSpots.add(getValidTentSpots(state, tree));
            }

            manualSort(remainingTrees, allValidSpots);

            if (!allValidSpots.isEmpty() && allValidSpots.get(0).size() == 1) {
                Point spot = allValidSpots.get(0).get(0);
                state.placeTent(spot.x, spot.y);
                return true;
            }
        }
        return false;
    }

    private static boolean isLegalPlacement(GameState state, int r, int c) {
        if (!state.inBounds(r, c))
            return false;
        if (state.getCell(r, c) != GameState.EMPTY)
            return false;

        // Adjacency check (tents cannot touch)
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

        // Must be next to some tree
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
