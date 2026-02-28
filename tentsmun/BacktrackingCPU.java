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
