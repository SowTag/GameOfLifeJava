import java.util.Random;

public class GameEngine {
  static boolean[][] initializeGrid() {
    int gridWidth = Integer.parseInt(ArgumentsParser.getArgument('w'));
    int gridHeight = Integer.parseInt(ArgumentsParser.getArgument('h'));

    return parseInitialGrid(ArgumentsParser.getArgument('p'), gridWidth, gridHeight);
  }

  static void printGridToConsole(boolean[][] grid) {
    for(boolean[] row: grid) {
      for(boolean col: row) {
        System.out.print(col ? "x " : "· ");
      }
      System.out.print("\n");
    }
  }

  static void startGameLoop() throws InterruptedException {
    GameRenderer graphicalRenderer = new GameRenderer();

    boolean[][] grid = initializeGrid();

    int generationCount = Integer.parseInt(ArgumentsParser.getArgument('g'));
    int sleepInterval = Integer.parseInt(ArgumentsParser.getArgument('s'));

    boolean graphicalMode = ArgumentsParser.isToggled('G');

    graphicalRenderer.gridPanel.grid = grid;

    if(graphicalMode)
      graphicalRenderer.setVisible(true);

    System.out.println("[GameEngine] Initial population:");
    printGridToConsole(grid);

    System.out.printf("[GameEngine] Beginning loop (%s generation%s)\n",
        generationCount == 0 ? "unlimited" : generationCount, generationCount == 1 ? "" : "s");

    long totalRenderingTime = 0;
    long totalProcessingTime = 0;
    long start = System.currentTimeMillis();

    int skipGeneration = Integer.parseInt(ArgumentsParser.getArgument('x'));

    if (skipGeneration != -1) {
      System.out.println("[GameEngine] Skipping generation #" + skipGeneration + " (-x argument)");
    }

    for(int g = 0; g < generationCount || generationCount == 0; g++) {
      if(sleepInterval != 0 && g != 0)
        Thread.sleep(sleepInterval);

      long renderingStart = System.currentTimeMillis();

      if(graphicalMode) graphicalRenderer.render(grid, g, totalProcessingTime);


      long renderingEndProcessingStart = System.currentTimeMillis();

      if(g + 1 == skipGeneration) {
        System.out.println("[*] Skipped generation.");
      } else {
        grid = processRules(grid);
      }

      if(!ArgumentsParser.isToggled('S')) {
        System.out.println("[GameEngine] Processing generation #" + (g + 1));
        printGridToConsole(grid);
      }

      totalRenderingTime += (renderingEndProcessingStart - renderingStart);
      totalProcessingTime += System.currentTimeMillis() - renderingEndProcessingStart;
    }

    System.out.printf("[GameRenderer] Finished after %dms (%dms processing / %dms rendering / %dms waiting)\n",
        (System.currentTimeMillis() - start), totalProcessingTime, totalRenderingTime, (System.currentTimeMillis() - start - totalRenderingTime - totalProcessingTime));


  }

  static boolean[][] processRules(boolean[][] grid) {
    boolean[][] out = new boolean[grid.length][grid[0].length];

    if(countAliveCells(grid) != 0) {
      for (int row = 0; row < grid.length; row++) {
        for (int col = 0; col < grid[row].length; col++) {

          out[row][col] = switch (findNeighborCount(grid, row, col)) {
            case 1 -> false;          // underpopulation
            case 2 -> grid[row][col]; // survival
            case 3 -> true;           // reproduction
            default -> false;         // overpopulation
          };
        }
      }
    }

    return out;
  }

  static int countAliveCells(boolean[][] grid) {
    int count = 0;

    for(boolean[] row: grid) {
      for(boolean cell: row) {
        if(cell) count++;
      }
    }

    return count;
  }

  static boolean[][] parseInitialGrid(String initialGrid, int width, int height) {
     final int RANDOM_POPULATION_ALIVE_CELL_PROPORTION = 5;

     boolean[][] out = new boolean[height][width];

     boolean randomMode = initialGrid.equals("rnd");

     Random randomNumberGenerator = new Random();

     for(int r = 0; r < height; r++) {
       for(int c = 0; c < width; c++) {
         out[r][c] = randomMode && randomNumberGenerator.nextInt(RANDOM_POPULATION_ALIVE_CELL_PROPORTION) == 0;
       }
     }

     if(!randomMode) {
       String[] rows = initialGrid.split("#");

       for(int r = 0; r < rows.length; r++) {
         for(int c = 0; c < rows[r].length(); c++) {
           out[r][c] = rows[r].charAt(c) == '1';
         }
       }
     }

     return out;
  }

  static int findNeighborCount(boolean[][] grid, int x, int y) {
    int neighborCount = 0;
    int neighborCheckingType = Integer.parseInt(ArgumentsParser.getArgument('n'));

    for(int r = -1; r <= 1; r++) {
      for(int c = -1; c <= 1; c++) {
        if(shouldCheckCell(neighborCheckingType, r + 1, c + 1)) {
          try {
            if (grid[x + r][y + c]) {
              neighborCount++;
            }
          } catch (Exception ignored) {}
        }
      }
    }

    return neighborCount;
  }

  static boolean shouldCheckCell(int neighborCheckingType, int r, int c) {

    return switch(neighborCheckingType) {
      case 1 -> r == 1 || c == 1;
      case 2 -> !(r == 2 && c == 0) && !(r == 0 && c == 2);
      case 3 -> true;
      case 4 -> r != 1 && c != 1;
      case 5 -> r != 1;
      default -> false; // will never happen anyway
    } && !(r == 1 && c == 1);
  }
}
