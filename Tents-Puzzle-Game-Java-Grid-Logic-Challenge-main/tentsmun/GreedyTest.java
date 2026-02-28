public class GreedyTest {
    public static void main(String[] args) {
        System.out.println("Starting Greedy Logic Test...");

        GameState state = new GameState(5);
        state.generateSolvablePuzzle();

        printBoardStatus(state);

        boolean moved = true;
        int moves = 0;
        while (moved && moves < 25) {
            moved = GreedyCPU.makeGreedyMove(state);
            if (moved) {
                moves++;
                System.out.println("Move " + moves + " executed.");
                printBoardStatus(state);
            } else {
                System.out.println("No greedy moves possible.");
            }

            if (state.isPuzzleComplete()) {
                System.out.println("Puzzle Solved!");
                break;
            }
        }

        System.out.println("Test Complete. Total Moves: " + moves);
        System.out.println("Final Status: " + state.checkPuzzleStatus());
    }

    private static void printBoardStatus(GameState state) {
        int n = state.getSize();
        for (int r = 0; r < n; r++) {
            System.out.print("|");
            for (int c = 0; c < n; c++) {
                int cell = state.getCell(r, c);
                String s = " ";
                if (cell == GameState.TENT)
                    s = "^";
                if (cell == GameState.TREE)
                    s = "T";
                if (cell == GameState.GRASS)
                    s = ".";
                System.out.print(s);
            }
            System.out.println("| " + state.getRowUsed(r) + "/" + state.getRowTarget(r));
        }
        System.out.print(" ");
        for (int c = 0; c < n; c++) {
            System.out.print(state.getColUsed(c));
        }
        System.out.println();
        for (int c = 0; c < n; c++) {
            System.out.print(state.getColTarget(c));
        }
        System.out.println();
    }
}
