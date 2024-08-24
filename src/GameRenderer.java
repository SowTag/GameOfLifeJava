import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;


public class GameRenderer extends JFrame {
  GridPanel gridPanel;

  static int cellSize = 25;
  static int windowWidth = 800, windowHeight = 800;


  static Color[] colorsForeground = {
      Color.GREEN,
      Color.RED,
      Color.BLUE,
      Color.MAGENTA,
      Color.CYAN,
      Color.YELLOW,
      Color.ORANGE,

  };

  static Color[] colorsBackground = new Color[]{
      new Color(0, 30, 0),
      new Color(30, 0, 0),
      new Color(0, 0, 30),
      new Color(30, 0, 30),
      new Color(0, 30, 30),
      new Color(30, 30, 0),
      new Color(30, 30, 15),
  };

  static Color[] colorsNeighborMap = new Color[] {
      new Color(127, 0, 0),
      new Color(255, 0, 0),
      new Color(0, 180, 0),
      new Color(130, 255, 130),
      new Color(127, 127, 255),
      new Color(97, 97, 225),
      new Color(67, 67, 195),
      new Color(37, 37, 165),
      new Color(7, 7, 135)
  };

  static int currentColorPalette = 0;


  public GameRenderer() {
    setTitle("Graphically rendered Conway's Game of Life");
    setSize(windowWidth, windowHeight);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);

    gridPanel = new GridPanel();

    add(gridPanel);

    addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        switch(e.getKeyChar()) {
          case 'q' -> System.exit(0);
          case 'h' -> {
            System.out.println("Available keybindings:");
            System.out.println("\tq: quit");
            System.out.println("\th: help");
            System.out.println("\tf: force window to fit the grid");
            System.out.println("\tp: previous color palette");
            System.out.println("\tn: next color palette");
          }

          case 'f' -> {
            windowWidth = gridPanel.grid[0].length * cellSize;
            windowHeight = gridPanel.grid.length * cellSize;
            setSize(windowWidth, windowHeight);
            System.out.printf("[*] Window has been resized to fit grid (new size: %dx%d)\n", windowWidth, windowHeight);
          }

          case 'p' -> currentColorPalette = currentColorPalette == 0 ? colorsForeground.length - 1 : currentColorPalette - 1;
          case 'n' -> currentColorPalette = currentColorPalette >= colorsForeground.length - 1 ? 0 : currentColorPalette + 1;
        }

        repaint();
      }
    });

    addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        try {
          windowWidth = gridPanel.grid[0].length * cellSize;
          windowHeight = gridPanel.grid.length * cellSize;
        } catch(Exception ignored) {}

        setSize(windowWidth, windowHeight);

        repaint();
      }
    });
  }

  public void render(boolean[][] grid, int currentGeneration, long totalTimeCalculating) {
    gridPanel.setGrid(grid);

    float msPerGeneration = (float) totalTimeCalculating / (currentGeneration + 1);

    setTitle(String.format("Generation #%d - %.2fms/g - %d alive - palette #%d - Conway's Game of Life (press h for help)",
        currentGeneration, msPerGeneration, GameEngine.countAliveCells(grid), currentColorPalette + 1));

    repaint();
  }

  static class GridPanel extends JPanel {
    boolean[][] grid;

    boolean neighborMapMode;

    public GridPanel() {
      setBackground(Color.BLACK);
      setLayout(null);

      setSize(windowWidth, windowHeight);

      neighborMapMode = ArgumentsParser.isToggled('M');
    }

    public void setGrid(boolean[][] grid) {
      this.grid = grid;
    }

    public void paintComponent(Graphics g) {
      super.paintComponent(g);

      drawGrid(g);
      repaint();
    }

    private void drawGrid(Graphics g) {
      cellSize = Math.min(getWidth() / grid[0].length, getHeight() / grid.length);

      g.setColor(GameRenderer.colorsForeground[GameRenderer.currentColorPalette]);

      for(int r = 0; r < grid.length; r++) {
        for(int c = 0; c < grid[0].length; c++) {
          if(neighborMapMode) g.setColor(GameRenderer.colorsNeighborMap[GameEngine.findNeighborCount(grid, r, c)]);
          if(grid[r][c]) g.fillRect(c * cellSize, r * cellSize, cellSize, cellSize);
        }
      }

      g.setColor(GameRenderer.colorsBackground[GameRenderer.currentColorPalette]);
      for(int r = 0; r < grid.length; r++) {

        for(int c = 0; c < grid[r].length; c++) {
          g.drawLine(0, (r + 1) * cellSize, grid[r].length * cellSize, (r + 1) * cellSize);
          g.drawLine((c + 1) * cellSize, 0, (c + 1) * cellSize, grid.length * cellSize);
        }
      }
    }
  }
}