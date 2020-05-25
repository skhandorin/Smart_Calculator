package calculator;

import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Calculator {

    static final String COMMAND_EXIT = "/exit";
    static final String COMMAND_HELP = "/help";

    private Map<String, BigInteger> mapOfVars;

    public Calculator() {
        mapOfVars = new HashMap<>();
    }

    public static String getHelp() {
        return "The program calculates expressions like these: 3 + 8 * ((4 + 3) * 2 + 1) - 6 / (2 + 1) ^ 3 , "
                + "and so on. It supports both unary and binary minus operators. "
                + "If several operators + or - follow each other, the program still works.";
    }

    private static int precedence(String elem) {
        switch (elem) {
            case "+":
            case "-":
                return 1;
            case "*":
            case "/":
                return 2;
            case "^":
                return 3;
            default:
                throw new CalculatorException("Internal error", " - unknown precedence of (" + elem + ")");
        }
    }

    public String processInput(String input){
        String result;

        try {
            if (input.contains("=")) {
                doAssignment(input);
                result = "";
            } else {
                String[] postfixExpr = infixToPostfix(input);
                BigInteger intResult = calculateWithPostfix(postfixExpr);
                result = intResult.toString();
            }
        } catch (CalculatorException e) {
            //result = e.getMessage() + e.getDescription();
            result = e.getMessage();
        }

        return result;
    }

    private String[] infixToPostfix(String infixExpr) {
        List<String> postfixList = new ArrayList<>();
        Deque<String> stack = new ArrayDeque<>();

        final String regex = "\\s*(\\d+|\\p{Alpha}+|\\(|\\)|\\++|-+|\\*|/|\\^)\\s*";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(infixExpr);
        List<String> infixList = new ArrayList<>();

        while(matcher.find()) {
            infixList.add(matcher.group().trim());
        }
        //System.out.println(infixList);
        String previousElem = "(";        // For the unary minus the beginning of the input = (

        for (String elem : infixList) {
            if (elem.matches("\\++")) {
                elem = "+";
            } else if (elem.matches("-+")) {
                elem = elem.length() % 2 == 0 ? "+" : "-";
            }

            if ("-".equals(elem) && "(".equals(previousElem)) {   // unary minus
                postfixList.add("0");
            }
            previousElem = elem;

            if (elem.matches("\\d+")) {                     // p1 - number
                postfixList.add(elem);
            } else if (elem.matches("\\p{Alpha}+")) {       // p1 - variable
                if (!mapOfVars.containsKey(elem)) {
                    throw new CalculatorException("Unknown variable", elem + " in infixToPostfix");
                }
                postfixList.add(mapOfVars.get(elem).toString());
            } else if ("(".equals(elem)) {                          // p5 - (
                stack.offerLast(elem);
            } else if (")".equals(elem)) {                          // p6 - )
                while(true) {
                    String previousOperator = stack.pollLast();
                    if (previousOperator == null) {
                        throw new CalculatorException("Invalid expression", " - unbalanced brackets, missing (");
                    } else if ("(".equals(previousOperator)) {
                        break;
                    }
                    postfixList.add(previousOperator);
                }
            } else {                                                // some operator (pp 2,3,4)
                String previousOperator = stack.peekLast();
                if (previousOperator == null || "(".equals(previousOperator)) { // p2 - empty stack or (
                    stack.offerLast(elem);
                } else if (precedence(elem) > precedence(previousOperator)) {   // p3
                    stack.offerLast(elem);
                } else {    // p4 - the incoming operator has lower or equal precedence
                    do {
                        postfixList.add(stack.pollLast());
                        previousOperator = stack.peekLast();
                    } while (previousOperator != null &&
                            !"(".equals(previousOperator) &&
                            precedence(elem) <= precedence(previousOperator));
                    stack.offerLast(elem);
                }
            }
        }

        while (stack.peekLast() != null) {                          // p7
            if ("(".equals(stack.peekLast())) {
                throw new CalculatorException("Invalid expression", " - unbalanced brackets, missing )");
            }
            postfixList.add(stack.pollLast());
        }

        //System.out.println(postfixList);
        return postfixList.toArray(new String[0]);
    }


    private BigInteger calculateWithPostfix(String[] postfixExpr) {
        Deque<BigInteger> stack = new ArrayDeque<>();
        for (String elem : postfixExpr) {
            if (elem.matches("\\d+")) {
                stack.offerLast(new BigInteger(elem));
            } else {
                BigInteger b = stack.pollLast();
                BigInteger a = stack.pollLast();

                if (a == null || b == null) {
                    throw new CalculatorException("Invalid expression", " - operand is null");
                }
                BigInteger result;
                switch (elem) {
                    case "+":
                        result = a.add(b);
                        break;
                    case "-":
                        result = a.subtract(b);
                        break;
                    case "*":
                        result = a.multiply(b);
                        break;
                    case "/":
                        result = a.divide(b);
                        break;
                    case "^":
                        result = BigInteger.valueOf((long) Math.pow(a.doubleValue(), b.doubleValue()));
                        break;
                    default:
                        throw new CalculatorException("Internal error", " - unknown operator " + elem);
                }
                stack.offerLast(result);
            }
        }

        if (stack.size() != 1) {
            throw new CalculatorException("Invalid expression", " - stack.size()!=1 in calculateWithPostfix");
        }

        return stack.pollLast();
    }

    private void doAssignment(String input) {
        if (!input.matches("[a-zA-Z]+\\s*=.*")) {
            throw new CalculatorException("Invalid identifier",
                    "Identifier does not match the assignment pattern");
        }

        if (!input.matches("[a-zA-Z]+\\s*=\\s*(-?\\d+|[a-zA-Z]+)\\s*")) {
            throw new CalculatorException("Invalid assignment",
                    "Input does not match the assignment pattern");
        }

        Pattern varNamePattern = Pattern.compile("[a-zA-Z]+\\s*");
        Matcher varNameMatcher = varNamePattern.matcher(input);

        String varName;
        varNameMatcher.find();
        varName = varNameMatcher.group().trim();

        String expression = input.substring(varNameMatcher.end() + 1).trim();
        //System.out.println("varName = " + varName);
        //System.out.println("expression = " + expression);

        BigInteger value = new BigInteger(expression);

        //System.out.println("value = " + value);
        mapOfVars.put(varName, value);
    }

/*
        private int calculate(String input) throws CalculatorException {
        Pattern pattern = Pattern.compile("[+-]?(\\d+|\\s*\\w+)(\\s*[+-]+\\s*(\\d+|\\w+))*");
        Matcher matcher = pattern.matcher(input.trim());

        if (!matcher.matches()) {
            throw new CalculatorException("Invalid expression",
                    "The expression does not match the pattern");
        }

        String expression = input.replaceAll("\\++", " ")
                .replaceAll("-\\s*-", " ")
                .replaceAll("-\\s*", " -")
                .trim();

        String[] tokens = expression.split("\\s+");

        int result = 0;
        for (String token : tokens) {
            if (token.matches("[+-]?\\d+")) {
                result += Integer.parseInt(token);
            } else {
                int sign = 1;
                if (token.startsWith("-")) {
                    sign = -1;
                    token = token.substring(1);
                }
                if (!mapOfVars.containsKey(token)) {
                    throw new CalculatorException("Unknown variable", "");
                }
                result += sign * mapOfVars.get(token);
            }
        }
        return result;
    }
*/


}
