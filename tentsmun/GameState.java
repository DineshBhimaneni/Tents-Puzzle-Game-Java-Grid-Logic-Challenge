import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GameState {

    public static final int EMPTY = 0;
    public static final int TREE = 1;
    public static final int TENT = 2;
    public static final int GRASS = 3;

    private final int n;
    private final int[][] board;
    private final int[][] solution;
    private final List<Point> trees;

    private final int[] rowTarget;
    private final int[] colTarget;

    private final int[] rowUsed;
    private final int[] colUsed;

    public GameState(int n) {
        this.n = n;
        this.board = new int[n][n];
        this.solution = new int[n][n];
        this.trees = new ArrayList<>();
        this.rowTarget = new int[n];
        this.colTarget = new int[n];
        this.rowUsed = new int[n];
        this.colUsed = new int[n];
    }

    public int getSize() {
        return n;
    }

    public int getCell(int r, int c) {
        return board[r][c];
    }

    public int getSolutionCell(int r, int c) {
        return solution[r][c];
    }

    public int getRowTarget(int r) {
        return rowTarget[r];
    }

    public int getColTarget(int c) {
        return colTarget[c];
    }

    public int getRowUsed(int r) {
        return rowUsed[r];
    }

    public int getColUsed(int c) {
        return colUsed[c];
    }

    public List<Point> getTrees() {
        return trees;
    }

    public void resetBoard() {
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (board[r][c] == TENT || board[r][c] == GRASS) {
                    board[r][c] = EMPTY;
                }
            }
        }
        recalcCounts();
    }

    private void recalcCounts() {
        for (int i = 0; i < n; i++) {
            rowUsed[i] = 0;
            colUsed[i] = 0;
        }
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (board[r][c] == TENT) {
                    rowUsed[r]++;
                    colUsed[c]++;
                }
            }
        }
    }

    public void generateSolvablePuzzle() {
        while (true) {
            if (tryGenerate()) {
                break;
            }
        }
    }

    private boolean tryGenerate() {
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                solution[r][c] = EMPTY;
                board[r][c] = EMPTY;
            }
            rowTarget[r] = 0;
            colTarget[r] = 0;
        }
        trees.clear();

        int targetCount = n + 1;
        List<Point> placedTents = new ArrayList<>();
        Random rand = new Random();

        List<Point> allPositions = new ArrayList<>();
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                allPositions.add(new Point(r, c));
            }
        }
        Collections.shuffle(allPositions, rand);

        for (Point p : allPositions) {
            if (placedTents.size() >= targetCount)
                break;

            int r = p.x;
            int c = p.y;

            if (solution[r][c] != EMPTY)
                continue;
            if (hasNeighborTent(solution, r, c))
                continue;

            List<Point> validTreeSpots = new ArrayList<>();
            int[][] dirs = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
            for (int[] d : dirs) {
                int tr = r + d[0];
                int tc = c + d[1];
                if (inBounds(tr, tc) && solution[tr][tc] == EMPTY) {
                    validTreeSpots.add(new Point(tr, tc));
                }
            }

            if (validTreeSpots.isEmpty())
                continue;

            Point treePos = validTreeSpots.get(rand.nextInt(validTreeSpots.size()));

            solution[r][c] = TENT;
            solution[treePos.x][treePos.y] = TREE;
            placedTents.add(new Point(r, c));
            trees.add(treePos);
        }

        if (placedTents.size() < targetCount) {
            return false;
        }

        for (Point t : placedTents) {
            rowTarget[t.x]++;
            colTarget[t.y]++;
        }

        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (solution[r][c] == TREE) {
                    board[r][c] = TREE;
                } else {
                    board[r][c] = EMPTY;
                }
            }
        }

        recalcCounts();
        return true;
    }

    private boolean hasNeighborTent(int[][] grid, int r, int c) {
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0)
                    continue;
                int nr = r + dr;
                int nc = c + dc;
                if (inBounds(nr, nc) && grid[nr][nc] == TENT) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean inBounds(int r, int c) {
        return r >= 0 && r < n && c >= 0 && c < n;
    }

    public void setCell(int r, int c, int value) {
        if (!inBounds(r, c))
            return;
        if (board[r][c] == TREE)
            return;
        board[r][c] = value;
        recalcCounts();
    }

    public boolean placeTent(int r, int c) {
        if (!inBounds(r, c))
            return false;
        if (board[r][c] == TREE || board[r][c] == TENT)
            return false;
        setCell(r, c, TENT);
        return true;
    }

    public boolean isPuzzleComplete() {
        if (!checkConstraints())
            return false;
        return hasValidMatching();
    }

    public String checkPuzzleStatus() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < n; r++) {
            if (rowUsed[r] != rowTarget[r])
                sb.append("Row ").append(r).append(" mismatch. ");
        }
        for (int c = 0; c < n; c++) {
            if (colUsed[c] != colTarget[c])
                sb.append("Col ").append(c).append(" mismatch. ");
        }
        if (hasAdjacencyViolation())
            sb.append("Tents are touching. ");
        if (!hasValidMatching())
            sb.append("Trees/Tents not matched properly.");

        if (sb.length() == 0)
            return "Correct!";
        return sb.toString();
    }

    private boolean checkConstraints() {
        for (int i = 0; i < n; i++) {
            if (rowUsed[i] != rowTarget[i])
                return false;
            if (colUsed[i] != colTarget[i])
                return false;
        }
        if (hasAdjacencyViolation())
            return false;
        return true;
    }

    private boolean hasAdjacencyViolation() {
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (board[r][c] == TENT) {
                    if (hasNeighborTent(board, r, c))
                        return true;
                }
            }
        }
        return false;
    }

    private boolean hasValidMatching() {
        List<Point> currentTents = new ArrayList<>();
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (board[r][c] == TENT)
                    currentTents.add(new Point(r, c));
            }
        }

        if (currentTents.size() != trees.size())
            return false;

        for (Point t : currentTents) {
            boolean adj = false;
            int[][] dirs = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
            for (int[] d : dirs) {
                int nr = t.x + d[0];
                int nc = t.y + d[1];
                if (inBounds(nr, nc) && board[nr][nc] == TREE) {
                    adj = true;
                    break;
                }
            }
            if (!adj)
                return false;
        }

        return match(currentTents, new boolean[trees.size()], 0);
    }

    private boolean match(List<Point> tents, boolean[] treeUsed, int tentIdx) {
        if (tentIdx == tents.size())
            return true;

        Point t = tents.get(tentIdx);
        int[][] dirs = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
        for (int[] d : dirs) {
            int nr = t.x + d[0];
            int nc = t.y + d[1];

            int treeIdx = -1;
            for (int i = 0; i < trees.size(); i++) {
                Point tree = trees.get(i);
                if (tree.x == nr && tree.y == nc) {
                    treeIdx = i;
                    break;
                }
            }

            if (treeIdx != -1 && !treeUsed[treeIdx]) {
                treeUsed[treeIdx] = true;
                if (match(tents, treeUsed, tentIdx + 1))
                    return true;
                treeUsed[treeIdx] = false;
            }
        }
        return false;
    }

    /*
    // Quick Sort module to sort trees by degree (ascending)

    static int partition(Tree[] arr, int low, int high) {
        int pivot = arr[high].degree;
        int i = low - 1;

        for (int j = low; j < high; j++) {
            if (arr[j].degree <= pivot) {
                i++;
                Tree temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }

        Tree temp = arr[i + 1];
        arr[i + 1] = arr[high];
        arr[high] = temp;

        return i + 1;
    }

    static void quickSort(Tree[] arr, int low, int high) {
        if (low < high) {
            int pi = partition(arr, low, high);
            quickSort(arr, low, pi - 1);
            quickSort(arr, pi + 1, high);
    }
}

     */
    public GameState(GameState other) {
        this.n = other.n;
        this.board = new int[n][n];
        this.solution = new int[n][n]; // solution is static/read-only usually, but we copy for safety
        this.trees = new ArrayList<>(other.trees); // Shallow copy of points is fine since Point is immutable-ish here
        this.rowTarget = new int[n];
        this.colTarget = new int[n];
        this.rowUsed = new int[n];
        this.colUsed = new int[n];

        copyDataFrom(other);
    }

    public void copyDataFrom(GameState other) {
        for(int r=0; r<n; r++) {
            System.arraycopy(other.board[r], 0, this.board[r], 0, n);
            System.arraycopy(other.solution[r], 0, this.solution[r], 0, n);
        }
        System.arraycopy(other.rowTarget, 0, this.rowTarget, 0, n);
        System.arraycopy(other.colTarget, 0, this.colTarget, 0, n);
        System.arraycopy(other.rowUsed, 0, this.rowUsed, 0, n);
        System.arraycopy(other.colUsed, 0, this.colUsed, 0, n);
        // trees list is final and structural, assumed constant for a puzzle instance
    }
}
