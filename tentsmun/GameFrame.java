import java.awt.*;
import javax.swing.*;

public class GameFrame extends JFrame {

    private GameState gameState;
    private BoardPanel boardPanel;
    private JLabel statusLabel;

    public GameFrame() {
        setTitle("Tents & Trees");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        initNewGame(8);
    }

    private void initNewGame(int size) {
        gameState = new GameState(size);
        gameState.generateSolvablePuzzle();

        if (boardPanel != null)
            remove(boardPanel);

        boardPanel = new BoardPanel(gameState, this);
        add(boardPanel, BorderLayout.CENTER);

        if (statusLabel == null) {
            add(createSidePanel(), BorderLayout.WEST);
        }

        updateStatus("Place N+1 tents!");
        revalidate();
        repaint();
    }

    private JPanel createSidePanel() {
        JPanel p = new JPanel();
        p.setPreferredSize(new Dimension(200, 0));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(new Color(230, 230, 230));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton btnNew = new JButton("New Game");
        btnNew.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnNew.addActionListener(e -> askSizeAndRestart());

        JButton btnCheck = new JButton("Check");
        btnCheck.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCheck.addActionListener(e -> checkGame());

        JButton btnGreedy = new JButton("Greedy Move");
        btnGreedy.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnGreedy.addActionListener(e -> doGreedyMove());

        JButton btnSolve = new JButton("Complete Game");
        btnSolve.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSolve.addActionListener(e -> doSolveAll());

        JButton btnDivideConquer = new JButton("Divide & Conquer Move");
        btnDivideConquer.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnDivideConquer.addActionListener(e -> doDivideConquerMove());

        JButton btnSafe = new JButton("Instructions");
        btnSafe.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSafe.addActionListener(e -> showInstructions());

        statusLabel = new JLabel("Welcome!");
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(new JLabel("Tents & Trees"));
        p.add(Box.createVerticalStrut(20));
        p.add(btnNew);
        p.add(Box.createVerticalStrut(10));
        p.add(btnCheck);
        p.add(Box.createVerticalStrut(10));
        p.add(btnGreedy);
        p.add(Box.createVerticalStrut(10));
        p.add(btnSolve);
        p.add(Box.createVerticalStrut(10));
        p.add(btnDivideConquer);
        p.add(Box.createVerticalStrut(10));
        p.add(btnSafe);
        p.add(Box.createVerticalStrut(20));
        p.add(statusLabel);

        return p;
    }

    private void doGreedyMove() {
        if (gameState.isPuzzleComplete()) {
            JOptionPane.showMessageDialog(this, "Puzzle already complete!");
            return;
        }
        boolean moved = GreedyCPU.makeGreedyMove(gameState);
        if (moved) {
            boardPanel.repaint();
            checkAutoCompletion();
        } else {
            JOptionPane.showMessageDialog(this, "No greedy moves available (or puzzle solved).");
        }
    }

    private void doSolveAll() {
        GreedyCPU.solveAll(gameState);
        boardPanel.repaint();
        checkAutoCompletion();
    }

    private void doDivideConquerMove() {
        if (gameState.isPuzzleComplete()) {
            JOptionPane.showMessageDialog(this, "Puzzle already complete!");
            return;
        }

        // Apply a single D&C step
        boolean moved = DivideConquerCPU.makeMove(gameState);

        boardPanel.repaint();
        if (moved) {
            checkAutoCompletion();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Divide & Conquer could not find a logical move from this state.\n(The board might be unsolvable in this state.)");
        }
    }

    private void askSizeAndRestart() {
        String[] opts = { "6 x 6", "8 x 8", "10 x 10" };
        int n = JOptionPane.showOptionDialog(this, "Select Size", "New Game",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opts, opts[1]);

        if (n == 0)
            initNewGame(6);
        else if (n == 1)
            initNewGame(8);
        else if (n == 2)
            initNewGame(10);
    }

    private void checkGame() {
        if (gameState.isPuzzleComplete()) {
            JOptionPane.showMessageDialog(this, "Congratulations! You found the solution!");
        } else {
            String status = gameState.checkPuzzleStatus();
            JOptionPane.showMessageDialog(this, status);
        }
    }

    public void checkAutoCompletion() {
        if (gameState.isPuzzleComplete()) {
            statusLabel.setText("SOLVED!");
            JOptionPane.showMessageDialog(this, "PUZZLE SOLVED!");
        } else {
            statusLabel.setText("Playing...");
        }
    }

    private void showInstructions() {
        JOptionPane.showMessageDialog(this,
                "Rules:\n" +
                        "1. Place one tent for each tree.\n" +
                        "2. No two tents can touch (even diagonally).\n" +
                        "3. Numbers indicate tents in that row/col.\n\n" +
                        "Controls:\n" +
                        "Left Click: Place Tent\n" +
                        "Right Click + Drag: Mark Grass (Empty)");
    }

    public void updateStatus(String s) {
        statusLabel.setText(s);
    }
}
