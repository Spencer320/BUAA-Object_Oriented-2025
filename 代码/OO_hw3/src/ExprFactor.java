import java.math.BigInteger;
import java.util.HashSet;

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
        ans = ans.power(exp);
        if (reverse) {
            HashSet<Monomial> oneSet = new HashSet<>();
            oneSet.add(new Monomial(BigInteger.ONE.negate(),0,null,null));
            return ans.multiply(new Polynomial(oneSet));
        }
        return ans;
    }
}
