public class DivideConquerTest {
    public static void main(String[] args) {
        testCopy();
        testSolve();
        testMakeMove();
    }

    private static void testCopy() {
        System.out.println("Testing GameState copy...");
        GameState s1 = new GameState(8);
        s1.generateSolvablePuzzle();
        s1.placeTent(0, 0);

        GameState s2 = new GameState(s1);

        if (s2.getSize() != 8)
            throw new RuntimeException("Size mismatch");
        if (s1.getCell(0, 0) != s2.getCell(0, 0))
            throw new RuntimeException("Board Content mismatch");

        s2.setCell(1, 1, GameState.TENT);
        if (s1.getCell(1, 1) == GameState.TENT)
            throw new RuntimeException("Deep copy failed - s1 modified");

        System.out.println("Copy test passed.");
    }

    private static void testSolve() {
        System.out.println("Testing DivideConquerCPU solve...");

        for (int i = 0; i < 100; i++) {
            GameState s = new GameState(8);
            s.generateSolvablePuzzle();

            boolean result = DivideConquerCPU.solve(s);
            if (!result) {
                System.out.println("Failed to solve a solvable puzzle!");

                throw new RuntimeException("Solver failed");
            }
            if (!s.isPuzzleComplete()) {
                throw new RuntimeException("Solver returned true but puzzle not complete");
            }
        }
        System.out.println("Solver test passed (100 iterations).");
    }

    private static void testMakeMove() {
        System.out.println("Testing DivideConquerCPU makeMove...");
        GameState s = new GameState(6);
        s.generateSolvablePuzzle();

        int placed = 0;
        while (!s.isPuzzleComplete()) {
            boolean moved = DivideConquerCPU.makeMove(s);
            if (!moved) {
                if (s.isPuzzleComplete())
                    break;
                throw new RuntimeException("makeMove returned false but puzzle not complete");
            }
            placed++;
            if (placed > 36)
                throw new RuntimeException("Too many moves - infinite loop?");
        }
        System.out.println("makeMove test passed.");
    }
}
