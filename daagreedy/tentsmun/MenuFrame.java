
import java.awt.*;
import javax.swing.*;

public class MenuFrame extends JFrame {

    private int selectedSize = 8;
    private String selectedOpponent = "Greedy";

    public MenuFrame() {
        setTitle("Tents & Trees - Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        createUI();
        setVisible(true);
    }

    private void createUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(245, 245, 235));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));

        // Title
        JLabel title = new JLabel("Tents & Trees");
        title.setFont(new Font("SansSerif", Font.BOLD, 42));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(new Color(34, 139, 34));

        JLabel subtitle = new JLabel("Challenge the AI!");
        subtitle.setFont(new Font("SansSerif", Font.ITALIC, 18));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setForeground(new Color(100, 100, 100));

        mainPanel.add(title);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(subtitle);
        mainPanel.add(Box.createVerticalStrut(40));

        // Board Size Selection
        JLabel sizeLabel = new JLabel("Select Board Size:");
        sizeLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        sizeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel sizePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        sizePanel.setBackground(new Color(245, 245, 235));

        ButtonGroup sizeGroup = new ButtonGroup();
        JRadioButton size6 = new JRadioButton("6x6");
        JRadioButton size8 = new JRadioButton("8x8", true);
        JRadioButton size10 = new JRadioButton("10x10");

        size6.addActionListener(e -> selectedSize = 6);
        size8.addActionListener(e -> selectedSize = 8);
        size10.addActionListener(e -> selectedSize = 10);

        sizeGroup.add(size6);
        sizeGroup.add(size8);
        sizeGroup.add(size10);

        sizePanel.add(size6);
        sizePanel.add(size8);
        sizePanel.add(size10);

        mainPanel.add(sizeLabel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(sizePanel);
        mainPanel.add(Box.createVerticalStrut(30));

        // Opponent Selection
        JLabel opponentLabel = new JLabel("Select AI Opponent:");
        opponentLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        opponentLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel opponentPanel = new JPanel();
        opponentPanel.setLayout(new BoxLayout(opponentPanel, BoxLayout.Y_AXIS));
        opponentPanel.setBackground(new Color(245, 245, 235));

        ButtonGroup opponentGroup = new ButtonGroup();
        JRadioButton greedyAI = new JRadioButton("Greedy AI - Smart heuristic-based moves", true);
        JRadioButton divideConquerAI = new JRadioButton("Divide & Conquer AI - Recursive strategy");

        greedyAI.setAlignmentX(Component.CENTER_ALIGNMENT);
        divideConquerAI.setAlignmentX(Component.CENTER_ALIGNMENT);
        greedyAI.setBackground(new Color(245, 245, 235));
        divideConquerAI.setBackground(new Color(245, 245, 235));

        greedyAI.addActionListener(e -> selectedOpponent = "Greedy");
        divideConquerAI.addActionListener(e -> selectedOpponent = "DivideConquer");

        opponentGroup.add(greedyAI);
        opponentGroup.add(divideConquerAI);

        opponentPanel.add(greedyAI);
        opponentPanel.add(Box.createVerticalStrut(10));
        opponentPanel.add(divideConquerAI);

        mainPanel.add(opponentLabel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(opponentPanel);
        mainPanel.add(Box.createVerticalStrut(40));

        // Buttons
        JButton startBtn = new JButton("Start Game");
        startBtn.setFont(new Font("SansSerif", Font.BOLD, 18));
        startBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        startBtn.setPreferredSize(new Dimension(200, 50));
        startBtn.setBackground(new Color(34, 139, 34));
        startBtn.setForeground(Color.WHITE);
        startBtn.setFocusPainted(false);
        startBtn.addActionListener(e -> startGame());

        JButton instructionsBtn = new JButton("Instructions");
        instructionsBtn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        instructionsBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        instructionsBtn.addActionListener(e -> showInstructions());

        mainPanel.add(startBtn);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(instructionsBtn);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void startGame() {
        dispose(); // Close menu
        SwingUtilities.invokeLater(() -> {
            GameFrame game = new GameFrame(selectedSize, selectedOpponent);
            game.setVisible(true);
        });
    }

    private void showInstructions() {
        JOptionPane.showMessageDialog(this,
                "Rules:\n" +
                        "1. Place one tent for each tree.\n" +
                        "2. No two tents can touch (even diagonally).\n" +
                        "3. Numbers indicate tents in that row/col.\n\n" +
                        "Controls:\n" +
                        "Left Click: Place Tent\n" +
                        "Right Click + Drag: Mark Grass (Empty)\n\n" +
                        "Gameplay:\n" +
                        "You play against the AI - you move first,\n" +
                        "then the AI automatically responds after 1.5 seconds.",
                "Game Instructions",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
