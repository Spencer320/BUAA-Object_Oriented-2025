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
        //deal op
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
        } else if (token.getType() == Token.Type.COS || token.getType() == Token.Type.SIN) {
            return parseTrigFactor(reverseNew);
        } else if (token.getType() == Token.Type.DIFF) {
            return parseDiffFactor(reverseNew);
        } else {
            return parseExprFactor(reverseNew);
        }
    }

    private DiffFactor parseDiffFactor(boolean reverse) {
        lexer.nextToken();
        lexer.nextToken();//skip LPAREN
        Expr innerExpr = parseExpr();
        lexer.nextToken();//skip RPAREN
        return new DiffFactor(innerExpr, reverse);
    }

    private TrigFactor parseTrigFactor(boolean reverse) {
        Token token = lexer.getCurToken();
        final TrigFactor.Trig trig = (token.getType() == Token.Type.COS) ? TrigFactor.Trig.COS
            : TrigFactor.Trig.SIN;
        lexer.nextToken();
        lexer.nextToken();//skip LPAREN
        Factor innerFactor = parseFactor(false);
        lexer.nextToken();//skip RPAREN
        int exp = parseExponent();
        return new TrigFactor(exp, innerFactor, trig, reverse);
    }

    private ExprFactor parseExprFactor(boolean reverse) {
        lexer.nextToken();//skip LPAREN
        Expr expr = parseExpr();
        lexer.nextToken();//skip RPAREN
        int exp = parseExponent();
        return new ExprFactor(exp, expr, reverse);
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
        int exp = parseExponent();
        return new Var(exp, content, reverse);
    }

    public int parseExponent() {
        int exp = 1;
        if (! lexer.isTokenEnd()) {
            Token token = lexer.getCurToken();
            if (token.getType() == Token.Type.POWER) {
                lexer.nextToken();
                token = lexer.getCurToken();//token must be an ADD or NUM
                if (token.getType() == Token.Type.ADD) {
                    lexer.nextToken();
                    token = lexer.getCurToken();//skip ADD
                }
                exp = Integer.parseInt(token.getContent());
                lexer.nextToken();
            }
        }
        return exp;
    }
}
