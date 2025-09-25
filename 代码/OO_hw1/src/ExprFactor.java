import java.math.BigInteger;
import java.util.TreeMap;

public class ExprFactor implements Factor {
    private final Integer exp;
    private final Expr expr;
    private final boolean reverse;

    public ExprFactor(Integer exp, Expr expr, boolean reverse) {
        this.exp = exp;
        this.expr = expr;
        this.reverse = reverse;
    }

    @Override
    public Polynomial calFactor() {
        Polynomial ans = expr.calExpr();
        ans.power(exp);
        if (reverse) {
            TreeMap<Integer, BigInteger> monomial = new TreeMap<>();
            monomial.put(0, BigInteger.ONE.negate());
            ans.multiply(new Polynomial(monomial));
        }
        return ans;
    }
}
