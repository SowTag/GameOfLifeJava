import java.net.UnknownHostException;

public class ArgumentsParser {

  record Arguments() {
    static char[] keys;
    static String[] values;

    static boolean[] potentiallyValidArguments;
    static boolean[] validArguments;

    static boolean[] isArgumentToggle;

    static final String[] longKeys = {
        "width", "height", "generations", "speed", "population",
        "neighborhood", "creative", "graphical", "silent", "neighbor-map",
        "skip-generation"
    };

    static final char[] shortKeys = { 'w', 'h', 'g', 's', 'p', 'n', 'C', 'G', 'S', 'M', 'x' };

    static boolean creativeMode = false;
    static boolean bailOutOfCreative = false;
    static boolean neighborhoodWarning = false;
  }

  static void parseAll(String[] args) {
    if(args.length == 0) showHelp();

    Arguments.keys = new char[args.length];
    Arguments.values = new String[args.length];

    Arguments.potentiallyValidArguments = new boolean[args.length];
    Arguments.validArguments = new boolean[args.length];

    Arguments.isArgumentToggle = new boolean[args.length];

    for(int i = args.length - 1; i >= 0; i--) {
      parseGNULongSyntax(args[i], i, args, i == args.length - 1);

      if(!Arguments.potentiallyValidArguments[i])
        parseGNUShortSyntax(args[i], i, args, i == args.length - 1);

      if(!Arguments.potentiallyValidArguments[i])
        parseEqualSignSyntax(args[i], i);
    }

    System.out.println("[ArgumentsParser] Doing argument check (1st pass)");
    for(char key: Arguments.keys) {
      isArgumentValid(key, true);
    }

    System.out.println("[ArgumentsParser] Doing argument check (2nd pass)");

    for(char key: Arguments.keys) {
      if(key == '\0') continue;

      boolean isKeyValid = isArgumentValid(key, false);
      Arguments.bailOutOfCreative = !isKeyValid || Arguments.bailOutOfCreative;

      if (!isKeyValid) {
          System.out.println("[ArgumentsParser] Ignoring invalid key " + key);
      }
    }

  }

  static void showHelp() {
    System.out.println("* GOL: A Game Of Life written in Java with a GUI and some weird restrictions.\n");
    System.out.println("Available options (pedantic):");
    System.out.println("\tw=, -w, --width           set grid width (10, 20, 40, 80)");
    System.out.println("\th=, -h, --height          set grid height (10, 20, 40)");
    System.out.println("\tg=, -g, --generations     set generation count (0 = unlimited) (>=0)");
    System.out.println("\ts=, -s, --speed           set generation speed in ms (250-1000)");
    System.out.println("\tn=, -n, --neighborhood    set neighborhood check mode (1-5)");
    System.out.println("\tp=, -p, --population      set population string (see below)");
    System.out.println("\tx=, -x, --skip-generation skip a generation (>= 0)");
    System.out.println("\nAvailable options (non-pedantic, all toggles):");
    System.out.println("\t-C, --creative            enable creative mode (disable restrictions - debugging only)");
    System.out.println("\t-G, --graphical           enable graphical mode");
    System.out.println("\t-S, --silent              stop GameEngine from printing to console, useful in conjunction with -G");
    System.out.println("\t-M  --neighbor-map        enable the neighbor map, will only take effect when used with -G");
    System.out.println("\n");
    System.out.println("Population string syntax: A long string made of 1s, 0s and #s.");
    System.out.println("\t0: A dead cell");
    System.out.println("\t1: An alive cell");
    System.out.println("\t#: Go to next row");
    System.out.println("\nNote that rows don't have to be fully defined in your population, for example");
    System.out.println("--population \"111###101010\" is a valid population string even on a 40x40 grid.");

    System.exit(0);
  }

  static void parseGNULongSyntax(String argument, int index, String[] rawArgs, boolean isLastArgument) {
    Arguments.potentiallyValidArguments[index] = false;
    Arguments.validArguments[index] = false;

    if(argument.startsWith("--") && argument.length() >= 3) {
      String[] split = argument.substring(2).split("=");

      char key = tryToFindShortSyntaxIndexFromLongSyntaxString(split[0]);

      if(key != '\0') {
        Arguments.keys[index] = key;

        if(split.length > 1) {
          Arguments.values[index] = joinStringArrayIgnoring1stElement(split);
        } else {
          if(!isLastArgument && Arguments.potentiallyValidArguments[index + 1]) {
            Arguments.isArgumentToggle[index] = true;
            Arguments.validArguments[index] = true;
          } else if(isLastArgument) {
            Arguments.isArgumentToggle[index] = true;
          } else {
            Arguments.values[index] = rawArgs[index + 1];
          }
        }

        Arguments.potentiallyValidArguments[index] = true;
      } else {
        System.out.println("[ArgumentsParser] Ignoring invalid key '" + argument + "' (not found on translation table)");
      }
    }
  }

  static void parseGNUShortSyntax(String argument, int index, String[] rawArgs, boolean isLastArgument) {
    Arguments.potentiallyValidArguments[index] = false;
    Arguments.validArguments[index] = false;

    if(argument.startsWith("-") && argument.length() >= 2) {

      Arguments.keys[index] = argument.charAt(1);

      if(argument.length() > 2 && argument.charAt(2) == '=') {
        Arguments.values[index] = argument.substring(3);
      } else {
        if(isLastArgument) {
          Arguments.isArgumentToggle[index] = true;
          Arguments.validArguments[index] = true;
        } else {
          if(!Arguments.potentiallyValidArguments[index + 1]) {
            Arguments.values[index] = rawArgs[index + 1];
          } else {
            Arguments.isArgumentToggle[index] = true;
            Arguments.validArguments[index] = true;
          }
        }
      }
      Arguments.potentiallyValidArguments[index] = true;
    }
  }

  static void parseEqualSignSyntax(String argument, int index) {
    Arguments.potentiallyValidArguments[index] = false;
    Arguments.validArguments[index] = false;

    if(argument.length() >= 3 && argument.charAt(1) == '=') {
      Arguments.keys[index] = argument.charAt(0);
      Arguments.values[index] = argument.substring(2);
      Arguments.potentiallyValidArguments[index] = true;
    } else {
      Arguments.potentiallyValidArguments[index] = false;
    }
  }

  static boolean doCreativeCheck() {
    try {
      String hostname = java.net.InetAddress.getLocalHost().getHostName();

      if(!hostname.equals("arch")) {
        System.out.println("[Creative] Creative Mode blocked on your computer. This mode is only for debugging - do NOT use it!");
        System.exit(2);
      }
    } catch (UnknownHostException e) {
      System.out.println("[Creative] Warning: Couldn't figure out your hostname. Use Creative Mode with caution!");
    }

    return true;
  }


  static boolean isArgumentValid(char key, boolean specialKeysOnly) {
    String value = Arguments.values[findKeyIndex(key)];
    boolean result = false;

    int index = findKeyIndex(key);

    if(specialKeysOnly) {
      if (key == 'C') {
        result = doCreativeCheck();
        Arguments.creativeMode = true;
        Arguments.validArguments[index] = true;
      }
    } else {
      if(isToggled(key)) result = true;
      else {
        if(Arguments.creativeMode) {
          result = switch(key) {
            case 'w', 'h' -> isValidNumber(value) && isNumberBetween(value, 1, Integer.MAX_VALUE);
            case 's', 'g', 'x' -> isValidNumber(value) && isNumberBetween(value, 0, Integer.MAX_VALUE);
            case 'n' -> isValidNumber(value) && isNumberBetween(value, 1, 5);
            case 'p' -> isPopulationStringValid(value);
            default -> false;
          };
        } else {
          result = switch(key) {
            case 'w' -> isValidNumber(value) && isNumberWithin(value, new int[]{ 10, 20, 40, 80 });
            case 'h' -> isValidNumber(value) && isNumberWithin(value, new int[]{ 10, 20, 40 });
            case 'g', 'x' -> isValidNumber(value) && isNumberBetween(value, 0, Integer.MAX_VALUE);
            case 's' -> isValidNumber(value) && isNumberBetween(value, 250, 1000);
            case 'n' -> isValidNumber(value) && isNumberBetween(value, 1, 5);
            case 'p' -> isPopulationStringValid(value);
            default -> false;
          };
        }
      }

      Arguments.validArguments[index] = result;
    }

    return result;
  }

  // Utility functions
  static boolean isValidNumber(String value) {
    boolean valid = true;

    try {
      Integer.parseInt(value);
    } catch(NumberFormatException ignored) {
      valid = false;
    }
    return valid;
  }
  static boolean isNumberWithin(String value, int[] validValues) {
    boolean isWithin = false;

    if(isValidNumber(value)) {
      int valueAsInt = Integer.parseInt(value);

      for (int number : validValues) {
        if (number == valueAsInt) isWithin = true;
      }
    }

    return isWithin;
  }
  static boolean isNumberBetween(String value, int lowEnd, int highEnd) {
    boolean isBetween = false;

    if(isValidNumber(value)) {
      int valueAsInt = Integer.parseInt(value);

      isBetween = valueAsInt >= lowEnd && valueAsInt <= highEnd;
    }

    return isBetween;
  }
  static boolean isKeyDefined(char key) {
    boolean found = false;

    for(char currentKey : Arguments.keys) {
      if(currentKey == key) found = true;
    }

    return found;
  }
  static char tryToFindShortSyntaxIndexFromLongSyntaxString(String longSyntax) {
    char output = '\0';

    for(int i = 0; i < Arguments.longKeys.length; i++) {
      if(Arguments.longKeys[i].equals(longSyntax)) {
        output = Arguments.shortKeys[i];
      }
    }

    return output;
  }
  static String joinStringArrayIgnoring1stElement(String[] array) {
    String output = "";

    for(int i = 1; i < array.length; i++) {
      output += array[i];
    }

    return output;
  }
  static int findKeyIndex(char key) {
    int keyIndex = -1;

    // This code is garbage.
    for(int i = Arguments.keys.length - 1; i >= 0; i--) {
      if(key == Arguments.keys[i]) {
        keyIndex = i;
      }
    }

    return keyIndex;
  }
  static boolean isToggled(char key) {
    boolean output = false;

    int index = findKeyIndex(key);

    if(index != -1) {
      output = Arguments.isArgumentToggle[index];
    }

    return output;
  }

  static String getArgument(char key) {
    int index = findKeyIndex(key);
    String result = "";
    
    if(index != -1) {
      result = Arguments.values[index];
    }
    
    // Defaults
    if(result == null || result.isEmpty()) {
      if(Arguments.creativeMode) {
        result = switch(key) {
          case 'w', 'h' -> "20";
          case 'g', 'p' -> "0";
          case 's' -> "500";
          case 'n' -> "3";
          case 'x' -> "-1";
          default -> throw new IllegalStateException("Unexpected value: " + key);
        };
      } else {
        switch(key) {
          case 'n' -> result = "3";
          case 'x' -> result = "-1";
        }
      }
    } else {
      if(key == 'n' && !isArgumentValid('n', false)) {
        if(!Arguments.neighborhoodWarning) {
          System.out.println(
              "[ArgumentsParser] Warning: Neighborhood value invalid - defaulting to 3.");
          Arguments.neighborhoodWarning = true;
        }
        result = "3";
      }
    }


    
    return result;
  }

  static boolean isPopulationStringValid(String value) {
    boolean valid = true;

    if(!value.equals("rnd")) {
      if (isKeyDefined('w') && isKeyDefined('h')) {
        String widthString = Arguments.values[findKeyIndex('w')];
        String heightString = Arguments.values[findKeyIndex('h')];

        if (isValidNumber(widthString) && isValidNumber(heightString)) {

          int width = Integer.parseInt(widthString);
          int height = Integer.parseInt(heightString);

          String[] rows = value.split("#");

          for (int row = 0; row < rows.length; row++) {
            for (int c = 0; c < rows[row].length(); c++) {
              char currentCell = rows[row].charAt(c);

              if (currentCell != '0' && currentCell != '1' && currentCell != '#')
                valid = false;

              if (c >= width && valid) {
                System.out.printf(
                    "[ArgumentsParser] Population string way too wide on row %d (current board width is %d)\n",
                    row, width);
                valid = false;
              }
            }
          }

          if (rows.length > height) {
            System.out.printf(
                "[ArgumentsParser] Population string way too tall - increase board height (%d > %d).\n",
                rows.length, height);
            valid = false;
          }
        } else {
          System.out.println(
              "[ArgumentsParser] Refusing to parse population string - either width or height are invalid numbers.\n");
          valid = false;
        }
      } else {
        System.out.println(
            "[ArgumentsParser] Refusing to parse population string - either width or height values missing.\n");
        valid = false;
      }
    }

    return valid;
  }

  static boolean shouldWeContinue() {
    boolean output = true;

    for(char key: new char[] { 'w', 'h', 'g', 's', 'p' }) {
      int keyIndex = findKeyIndex(key);

      if(keyIndex != -1) {
        if (!Arguments.potentiallyValidArguments[keyIndex] || !Arguments.validArguments[keyIndex])
          output = false;
      } else {
        output = false;
      }

      if(getArgument(key) == null) output = false;
    }

    if(Arguments.creativeMode && Arguments.bailOutOfCreative) {
      System.out.println("[ArgumentsParser] Creative Mode blocked: Some arguments are invalid. Provide valid values or no value at all to use defaults.");
    }


    return output || (Arguments.creativeMode && !Arguments.bailOutOfCreative);
  }
  static void debugPrintAll() {
    for(int i = 0; i < Arguments.keys.length; i++) {
      if(Arguments.potentiallyValidArguments[i])
        System.out.printf("Argument #%d [%s] [%s] [%s]: %s = %s\n",
            i,
            Arguments.potentiallyValidArguments[i] ? "pv" : "~pv",
            Arguments.validArguments[i] ? "v" : "~v",
            Arguments.isArgumentToggle[i] ? "t" : "~t",
            Arguments.keys[i], Arguments.values[i]);
    }

    if (Arguments.creativeMode) {
      System.out.println("[!] Warning: Creative Mode enabled. Most argument restrictions disabled. Expect exceptions if you input stuff wrong.");
    }
    System.out.println("[*] Graphical mode: " + isToggled('G'));

    for(String option: new String[] { "Width", "Height", "Generations", "Speed", "Population", "Neighborhood" }) {
      char key = tryToFindShortSyntaxIndexFromLongSyntaxString(option.toLowerCase());

      if (key != '\0') {
        int index = findKeyIndex(key);

        String value;

        if(index != -1 && !Arguments.isArgumentToggle[index]) {
          if (!Arguments.validArguments[index])
            value = "Invalid";
          else if (key == 'p')
            value = "\"" + getArgument(key) + "\"";
          else value = getArgument(key);
        } else {
          value = "Not defined";
        }

        //if(value == null) value = "Not defined";

        System.out.printf("%s: [%s]\n", option, value);

      } else {
        System.out.println("Warning: Tried to print unknown long option " + option);
      }
    }
  }
}
