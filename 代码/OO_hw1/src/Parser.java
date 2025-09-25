import java.math.BigInteger;

public class Parser {
    private final Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public Expr parseExpr() {
        Expr expr = new Expr();
        expr.addTerm(parseTerm(false));
        //all terms follow reverse of expr
        while (! lexer.isTokenEnd()) {
            if (lexer.getCurToken().getType() == Token.Type.SUB) {
                lexer.nextToken();
                expr.addTerm(parseTerm(true));
            } else if (lexer.getCurToken().getType() == Token.Type.ADD) {
                lexer.nextToken();
                expr.addTerm(parseTerm(false));
            } else {
                break;
            }
        }
        return expr;
    }

    public Term parseTerm(boolean reverse) {
        Term term = new Term();
        term.addFactor(parseFactor(reverse)); //only the first factor might be reversed
        while (! lexer.isTokenEnd() && lexer.getCurToken().getType() == Token.Type.MUL) {
            lexer.nextToken();
            term.addFactor(parseFactor(false));
        }
        return term;
    }

    public Factor parseFactor(boolean reverse) {
        Token token = lexer.getCurToken();
        boolean reverseNew = reverse;
        //if op here , the next token is NUM or LPAREN
        if (token.getType() == Token.Type.ADD) {
            lexer.nextToken();
        } else if (token.getType() == Token.Type.SUB) {
            lexer.nextToken();
            reverseNew = ! reverse;
        }
        //then parse factor
        token = lexer.getCurToken();
        if (token.getType() == Token.Type.NUM) {
            return parseNum(reverseNew);
        } else if (token.getType() == Token.Type.VAR) {
            return parseVar(reverseNew);
        } else {
            lexer.nextToken();//skip LPAREN
            Expr expr = parseExpr();
            int exp = 1;
            lexer.nextToken();//skip RPAREN
            if (! lexer.isTokenEnd()) {
                token = lexer.getCurToken();
                if (token.getType() == Token.Type.POWER) {
                    lexer.nextToken();
                    token = lexer.getCurToken();//token must be an ADD or NUM
                    if (token.getType() == Token.Type.ADD) {
                        lexer.nextToken();
                        token = lexer.getCurToken();
                    }
                    exp = Integer.parseInt(token.getContent());
                    lexer.nextToken();
                }
            }
            return new ExprFactor(exp, expr, reverseNew);
        }
    }

    public Num parseNum(boolean reverse) {
        Token token = lexer.getCurToken();
        lexer.nextToken();
        return new Num(new BigInteger(token.getContent()), reverse);
    }

    public Var parseVar(boolean reverse) {
        Token token = lexer.getCurToken();
        String content = token.getContent();
        lexer.nextToken();
        int exp = 1;
        if (!lexer.isTokenEnd()) {
            token = lexer.getCurToken();
            if (token.getType() == Token.Type.POWER) {
                lexer.nextToken();
                token = lexer.getCurToken();//token must be an ADD or NUM
                if (token.getType() == Token.Type.ADD) {
                    lexer.nextToken();
                    token = lexer.getCurToken();
                }
                exp = Integer.parseInt(token.getContent());
                lexer.nextToken();
            }
        }
        return new Var(exp, content, reverse);
    }
}
