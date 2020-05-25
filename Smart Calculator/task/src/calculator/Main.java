package calculator;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static calculator.Calculator.COMMAND_EXIT;
import static calculator.Calculator.COMMAND_HELP;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        Calculator calculator = new Calculator();

        do {
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            } else if (COMMAND_EXIT.equals(input)) {
                break;
            } else if (COMMAND_HELP.equals(input)) {
                System.out.println(Calculator.getHelp());
                continue;
            } else if (input.startsWith("/")) {
                System.out.println("Unknown command");
                continue;
            }

            String result = calculator.processInput(input);
            if (!result.isEmpty()) {
                System.out.println(result);
            }

        } while(true);

        System.out.println("Bye!");
    }

}
