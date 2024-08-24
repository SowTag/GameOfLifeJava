/*
 * I was allowed to use Java records and use public, static methods from
 * other classes by the professor (no instancing). This program does not
 * use Object-Oriented Programming but Java forces one class per file, no
 * getting around that.
 */

public class GameOfLife {
  public static void main(String[] args) throws InterruptedException {
    ArgumentsParser.parseAll(args);

    ArgumentsParser.debugPrintAll();

    if(!ArgumentsParser.shouldWeContinue()) {
      System.out.println("[Main] Either invalid or missing arguments. Quitting!");
      System.exit(1);
    }

    GameEngine.startGameLoop();
  }
}
