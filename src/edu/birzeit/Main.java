package edu.birzeit;

/**
 * @author: Abed Al Rahman Murrar
 * @id: 1140155
 * @JDK: 14
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main {
    static public final String WITH_DELIMITER = "((?<=%1$s)|(?=%1$s))";

    static Scanner scanner;

    enum Tokens {
        DOLLAR_SIGN("$"),
        MAIN("main"),
        NUMBER_SIGN("#"),
        INCLUDE("include"),
        CONST("const"),
        VAR("var"),
        INT("int"),
        FLOAT("float"),
        INPUT("input"),
        OUTPUT("output"),
        IF("if"),
        ELSE("else"),
        END("end"),
        WHILE("while"),
        OPEN_PARENTHESIS("("),
        CLOSE_PARENTHESIS(")"),
        OPEN_CURLY_BRACKET("{"),
        CLOSE_CURLY_BRACKET("}"),
        ADD_OPERATION("+"),
        SUBTRACT_OPERATION("-"),
        MULTIPLY_OPERATION("*"),
        DIVIDE_OPERATION("/"),
        MODULO_OPERATION("%"),
        INPUT_OPERATION(">>"),
        OUTPUT_OPERATION("<<"),
        SEMICOLON(";"),
        COMMA(","),
        LARGER_THAN(">"),
        SMALLER_THAN("<"),
        EQUALS("="),
        NOT("!");

        private final String s;

        Tokens(String s) {
            this.s = s;
        }

        @Override
        public String toString() {
            return s;
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        scanner = new Scanner(new File("program.txt"));
        scanner.useDelimiter("(\\s+)|(" + splitWithDelimiter() + ")");
//        while (scanner.hasNext()) {
//            System.out.println(scanner.next());
//        }

        try {
            program();
            System.out.println("Parse successful");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void expect(Tokens token) throws Exception {
        String next = nextSymbol();
        if (!token.toString().equals(next.trim())) {
            throw new Exception("Error parsing: expected "+token.toString()+" but got "+next+" instead");
        }
    }

    public static String nextSymbol() {
        String symbol = scanner.next();

        while (symbol.trim().length() < 1) {
            symbol = scanner.next();
        }
        return symbol;
    }

    public static void program() throws Exception {
        body();
        expect(Tokens.DOLLAR_SIGN);
    }

    public static void body() throws Exception {
        libDeclaration();
        expect(Tokens.MAIN);
        expect(Tokens.OPEN_PARENTHESIS);
        expect(Tokens.CLOSE_PARENTHESIS);
        declarations();
        block();
    }

    public static void libDeclaration() throws Exception {
        // #include<name>
        while (scanner.hasNext(Tokens.NUMBER_SIGN.toString())) {
            expect(Tokens.NUMBER_SIGN);
            expect(Tokens.INCLUDE);
            expect(Tokens.SMALLER_THAN);
            scanner.next(); // library name
            expect(Tokens.LARGER_THAN);
            expect(Tokens.SEMICOLON);
        }
    }

    public static void declarations() throws Exception {
        while (scanner.hasNext(Tokens.CONST.toString())) {
            constantDeclaration();
        }
        while (scanner.hasNext(Tokens.VAR.toString())) {
            variableDeclaration();
        }
    }

    public static void constantDeclaration() throws Exception {
        // const int x = 3;
        // const float y = 3.5;
        expect(Tokens.CONST);
        dataType();
        scanner.next(); // variable name
        expect(Tokens.EQUALS);
        scanner.next(); // value
        expect(Tokens.SEMICOLON);
    }

    public static void variableDeclaration() throws Exception {
        // var int x = 3;
        // var float y = 3.5;
        expect(Tokens.VAR);
        dataType();
        nameList();
        expect(Tokens.SEMICOLON);
    }

    public static void nameList() throws Exception {
        name();
        while (!scanner.hasNext(Tokens.SEMICOLON.toString())) {
            expect(Tokens.COMMA);
            name();
        }
    }

    public static void dataType() throws Exception {
        if (scanner.hasNext(Tokens.INT.toString())) {
            expect(Tokens.INT);
        } else {
            expect(Tokens.FLOAT);
        }
    }

    public static void name() {
        scanner.next();
    }

    public static void block() throws Exception {
        expect(Tokens.OPEN_CURLY_BRACKET);
        statementList();
        expect(Tokens.CLOSE_CURLY_BRACKET);
    }

    public static void statementList() throws Exception {
        while (!scanner.hasNext(Tokens.CLOSE_CURLY_BRACKET.toString())) {
            statement();
        }
    }

    public static void statement() throws Exception {
        if (scanner.hasNext(Tokens.WHILE.toString())) {
            whileStatement();
        } else if (scanner.hasNext(Tokens.IF.toString())) {
            ifStatement();
        } else if (scanner.hasNext(Tokens.INPUT.toString())) {
            inputStatement();
        } else if (scanner.hasNext(Tokens.OUTPUT.toString())) {
            outputStatement();
        } else if (scanner.hasNext(escapeMetaCharacters(Tokens.OPEN_CURLY_BRACKET.toString()))) {
            block();
        } else {
            assignmentStatement();
        }
    }

    public static void assignmentStatement() throws Exception {
        name();
        expect(Tokens.EQUALS);
        expression();
    }

    public static void expression() throws Exception {
        term();
        while (scanner.hasNext(escapeMetaCharacters(Tokens.ADD_OPERATION.toString())) || scanner.hasNext(escapeMetaCharacters(Tokens.SUBTRACT_OPERATION.toString()))) {
            addOperation();
            term();
        }
    }

    public static void term() throws Exception {
        factor();
        while (scanner.hasNext(escapeMetaCharacters(Tokens.MULTIPLY_OPERATION.toString())) ||
                scanner.hasNext(escapeMetaCharacters(Tokens.DIVIDE_OPERATION.toString())) ||
                scanner.hasNext(escapeMetaCharacters(Tokens.MODULO_OPERATION.toString()))) {
            multiplyOperation();
            term();
        }
    }

    public static void factor() throws Exception {
        if (scanner.hasNext(escapeMetaCharacters(Tokens.OPEN_PARENTHESIS.toString()))) {
            expect(Tokens.OPEN_PARENTHESIS);
            expression();
            expect(Tokens.CLOSE_PARENTHESIS);
        } else if (scanner.hasNextFloat() || scanner.hasNextInt()) {
            value();
        } else {
            name();
        }
    }

    public static void value() {
        if (scanner.hasNextInt()) {
            scanner.nextInt();
        } else {
            scanner.nextFloat();
        }
    }

    public static void addOperation() throws Exception {
        if (scanner.hasNext(Tokens.ADD_OPERATION.toString())) {
            expect(Tokens.ADD_OPERATION);
        } else {
            expect(Tokens.SUBTRACT_OPERATION);
        }
    }

    public static void multiplyOperation() throws Exception {
        if (scanner.hasNext(escapeMetaCharacters(Tokens.MULTIPLY_OPERATION.toString()))) {
            expect(Tokens.MULTIPLY_OPERATION);
        } else if (scanner.hasNext(escapeMetaCharacters(Tokens.DIVIDE_OPERATION.toString()))) {
            expect(Tokens.DIVIDE_OPERATION);
        } else {
            expect(Tokens.MODULO_OPERATION);
        }
    }

    public static void inputStatement() throws Exception {
        expect(Tokens.INPUT);
        expect(Tokens.LARGER_THAN);
        expect(Tokens.LARGER_THAN);
        name();
        expect(Tokens.SEMICOLON);
    }

    public static void outputStatement() throws Exception {
        expect(Tokens.OUTPUT);
        expect(Tokens.SMALLER_THAN);
        expect(Tokens.SMALLER_THAN);
        name();
        expect(Tokens.SEMICOLON);

    }

    public static void ifStatement() throws Exception {
        expect(Tokens.IF);
        expect(Tokens.OPEN_PARENTHESIS);
        booleanExpression();
        expect(Tokens.CLOSE_PARENTHESIS);
        statement();
        elsePart();
        expect(Tokens.END);
        expect(Tokens.IF);
        expect(Tokens.SEMICOLON);
    }

    public static void elsePart() throws Exception {
        expect(Tokens.ELSE);
        statement();
    }

    public static void whileStatement() throws Exception {
        expect(Tokens.WHILE);
        expect(Tokens.OPEN_PARENTHESIS);
        booleanExpression();
        expect(Tokens.CLOSE_PARENTHESIS);
        block();
    }

    public static void booleanExpression() throws Exception {
        nameValue();
        relationalOperator();
        nameValue();
    }

    public static void nameValue() {
        if (scanner.hasNextInt() || scanner.hasNextFloat()) {
            value();
        } else {
            name();
        }
    }

    public static void relationalOperator() throws Exception {
        if (scanner.hasNext(Tokens.EQUALS.toString())) {
            expect(Tokens.EQUALS);
            expect(Tokens.EQUALS);
        } else if (scanner.hasNext(Tokens.NOT.toString())) {
            expect(Tokens.NOT);
            expect(Tokens.EQUALS);
        } else if (scanner.hasNext(Tokens.LARGER_THAN.toString())) {
            expect(Tokens.LARGER_THAN);
            if (scanner.hasNext(Tokens.EQUALS.toString())) {
                expect(Tokens.EQUALS);
            }
        } else {
            expect(Tokens.SMALLER_THAN);
            if (scanner.hasNext(Tokens.EQUALS.toString())) {
                expect(Tokens.EQUALS);
            }
        }
    }




    /* HELPER FUNCTIONS */

    public static String escapeMetaCharacters(String inputString) {
        final String[] metaCharacters = {"\\", "^", "$", "{", "}", "[", "]", "(", ")", ".", "*", "+", "?", "<", ">", "-", "&", "%"};

        for (String metaCharacter : metaCharacters) {
            if (inputString.contains(metaCharacter)) {
                inputString = inputString.replace(metaCharacter, "\\" + metaCharacter);
            }
        }
        return inputString;
    }

    public static String splitWithDelimiter() {
        return String.format(WITH_DELIMITER, escapeMetaCharacters(
                Arrays.stream(Tokens.values())
                        .map(Tokens::toString)
                        .collect(Collectors.joining("|"))
                )
        );
    }
}
