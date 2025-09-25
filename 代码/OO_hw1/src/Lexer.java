import java.util.ArrayList;

public class Lexer {
    private final ArrayList<Token> tokens = new ArrayList<>();
    private int index = 0;

    public Lexer(String input) {
        //remove redundant char
        String expr = simplify(input);

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

    public static String simplify(String expr) {
        String expression = expr;
        expression = expression.replaceAll("[ \t]", "");//remove block char
        expression = expression.replaceAll("\\++", "+");//++...+ -> +
        expression = expression.replaceAll("\\+-", "-");//+- -> -
        expression = expression.replaceAll("-\\+", "-");//-+ -> -
        expression = expression.replaceAll("--", "+");//-- -> +
        expression = expression.replaceAll("\\+-", "-");//--- -> -
        expression = expression.replaceAll("\\+\\+", "+");//--+ , +-- -> +
        expression = expression.replaceAll("(?<=\\D|^)(0+)(?=\\d)", "");//remove leading-zero
        return expression;
    }
}

