import java.util.ArrayList;

public class Lexer {
    private final ArrayList<Token> tokens = new ArrayList<>();
    private int index = 0;

    public Lexer(String input) {
        //pre-process has done
        String expr = input;

        //spilt to tokens
        int pos = 0;
        int len = expr.length();
        while (pos < len) {
            if (expr.charAt(pos) == '(') {
                tokens.add(new Token(Token.Type.LPAREN, "("));
                pos++;
            } else if (expr.charAt(pos) == ')') {
                tokens.add(new Token(Token.Type.RPAREN, ")"));
                pos++;
            } else if (expr.charAt(pos) == '+') {
                tokens.add(new Token(Token.Type.ADD, "+"));
                pos++;
            } else if (expr.charAt(pos) == '-') {
                tokens.add(new Token(Token.Type.SUB, "-"));
                pos++;
            } else if (expr.charAt(pos) == '*') {
                tokens.add(new Token(Token.Type.MUL, "*"));
                pos++;
            } else if (expr.charAt(pos) == '^') {
                tokens.add(new Token(Token.Type.POWER, "^"));
                pos++;
            } else if (expr.charAt(pos) == 'x') {
                tokens.add(new Token(Token.Type.VAR, "x"));
                pos++;
            } else if (expr.charAt(pos) == 'c') {
                tokens.add(new Token(Token.Type.COS, "cos"));
                pos += "cos".length();
            } else if (expr.charAt(pos) == 's') {
                tokens.add(new Token(Token.Type.SIN, "sin"));
                pos += "sin".length();
            } else if (expr.charAt(pos) == 'd') {
                tokens.add(new Token(Token.Type.DIFF, "dx"));
                pos += "dx".length();
            } else {
                char now = expr.charAt(pos);
                StringBuilder sb = new StringBuilder();
                while (now >= '0' && now <= '9') {
                    sb.append(now);
                    pos++;
                    if (pos >= len) {
                        break;
                    }
                    now = expr.charAt(pos);
                }
                tokens.add(new Token(Token.Type.NUM, sb.toString()));
            }
        }
    }

    public Token getCurToken() {
        return tokens.get(index);
    }

    public void nextToken() {
        index++;
    }

    public boolean isTokenEnd() {
        return index >= tokens.size();
    }
}

