import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class BoardPanel extends JPanel {

    private final GameState gameState;
    private final GameFrame frame;

    private final int cellSize = 60;
    private final int offset = 80;

    private final Color cBoard = new Color(245, 245, 235);
    private final Color cGrid = new Color(180, 180, 180);
    private final Color cGrass = new Color(154, 205, 50);

    private boolean isDragging = false;
    private int dragType = -1;

    public BoardPanel(GameState gameState, GameFrame frame) {
        this.gameState = gameState;
        this.frame = frame;

        int size = gameState.getSize();
        int pref = offset + size * cellSize + 20;
        setPreferredSize(new Dimension(pref, pref));
        setBackground(Color.WHITE);

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMousePress(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isDragging = false;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                handleMouseDrag(e);
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
    }

    private Point getGridPos(int x, int y) {
        int r = (y - offset) / cellSize;
        int c = (x - offset) / cellSize;
        return new Point(r, c);
    }

    private void handleMousePress(MouseEvent e) {
        Point p = getGridPos(e.getX(), e.getY());
        int r = p.x;
        int c = p.y;

        if (!gameState.inBounds(r, c))
            return;

        if (SwingUtilities.isRightMouseButton(e)) {
            isDragging = true;
            int current = gameState.getCell(r, c);
            if (current == GameState.EMPTY) {
                gameState.setCell(r, c, GameState.GRASS);
                dragType = GameState.GRASS;
            } else if (current == GameState.GRASS) {
                gameState.setCell(r, c, GameState.EMPTY);
                dragType = GameState.EMPTY;
            } else {
                dragType = -1;
            }
        } else if (SwingUtilities.isLeftMouseButton(e)) {
            int current = gameState.getCell(r, c);
            if (current == GameState.EMPTY || current == GameState.GRASS) {
                gameState.setCell(r, c, GameState.TENT);
            } else if (current == GameState.TENT) {
                gameState.setCell(r, c, GameState.EMPTY);
            }
            frame.checkAutoCompletion();
        }
        repaint();
    }

    private void handleMouseDrag(MouseEvent e) {
        if (!isDragging || !SwingUtilities.isRightMouseButton(e))
            return;
        if (dragType == -1)
            return;

        Point p = getGridPos(e.getX(), e.getY());
        int r = p.x;
        int c = p.y;

        if (!gameState.inBounds(r, c))
            return;

        int current = gameState.getCell(r, c);

        if (current != GameState.TREE && current != GameState.TENT) {
            if (dragType == GameState.GRASS && current == GameState.EMPTY) {
                gameState.setCell(r, c, GameState.GRASS);
            } else if (dragType == GameState.EMPTY && current == GameState.GRASS) {
                gameState.setCell(r, c, GameState.EMPTY);
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int n = gameState.getSize();

        g2.setColor(cBoard);
        g2.fillRect(offset, offset, n * cellSize, n * cellSize);

        g2.setColor(cGrid);
        g2.setStroke(new BasicStroke(1));
        for (int i = 0; i <= n; i++) {
            g2.drawLine(offset, offset + i * cellSize, offset + n * cellSize, offset + i * cellSize);
            g2.drawLine(offset + i * cellSize, offset, offset + i * cellSize, offset + n * cellSize);
        }

        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                int x = offset + c * cellSize;
                int y = offset + r * cellSize;
                int cell = gameState.getCell(r, c);

                if (cell == GameState.TREE) {
                    drawTree(g2, x, y);
                } else if (cell == GameState.TENT) {
                    drawTent(g2, x, y, r, c);
                } else if (cell == GameState.GRASS) {
                    drawGrass(g2, x, y);
                }
            }
        }

        g2.setFont(new Font("SansSerif", Font.BOLD, 22));

        for (int r = 0; r < n; r++) {
            int target = gameState.getRowTarget(r);
            int current = gameState.getRowUsed(r);

            if (current == target)
                g2.setColor(new Color(0, 180, 0));
            else if (current > target)
                g2.setColor(Color.RED);
            else
                g2.setColor(Color.BLACK);

            String s = String.valueOf(target);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(s, offset - 30, offset + r * cellSize + (cellSize + fm.getAscent()) / 2 - 5);
        }

        for (int c = 0; c < n; c++) {
            int target = gameState.getColTarget(c);
            int current = gameState.getColUsed(c);

            if (current == target)
                g2.setColor(new Color(0, 180, 0));
            else if (current > target)
                g2.setColor(Color.RED);
            else
                g2.setColor(Color.BLACK);

            String s = String.valueOf(target);
            FontMetrics fm = g2.getFontMetrics();
            int strW = fm.stringWidth(s);
            g2.drawString(s, offset + c * cellSize + (cellSize - strW) / 2, offset - 20);
        }
    }

    private void drawTree(Graphics2D g2, int x, int y) {
        g2.setColor(new Color(139, 69, 19));
        g2.fillRect(x + 26, y + 35, 8, 15);

        g2.setColor(new Color(34, 139, 34));
        g2.fillOval(x + 10, y + 10, 40, 40);
    }

    private void drawTent(Graphics2D g2, int x, int y, int r, int c) {
        g2.setColor(new Color(255, 140, 0));

        int[] xs = { x + 10, x + 30, x + 50 };
        int[] ys = { y + 45, y + 15, y + 45 };
        g2.fillPolygon(xs, ys, 3);

        boolean error = false;
        int[][] dirs = { { -1, -1 }, { -1, 0 }, { -1, 1 }, { 0, -1 }, { 0, 1 }, { 1, -1 }, { 1, 0 }, { 1, 1 } };
        for (int[] d : dirs) {
            int nr = r + d[0];
            int nc = c + d[1];
            if (gameState.inBounds(nr, nc) && gameState.getCell(nr, nc) == GameState.TENT) {
                error = true;
                break;
            }
        }

        if (error) {
            g2.setColor(Color.RED);
            g2.setFont(new Font("SansSerif", Font.BOLD, 24));
            g2.drawString("!", x + 25, y + 40);
        }
    }

    private void drawGrass(Graphics2D g2, int x, int y) {
        g2.setColor(cGrass);
        int cx = x + cellSize / 2;
        int cy = y + cellSize / 2;
        g2.fillOval(cx - 3, cy - 3, 6, 6);
    }
}
