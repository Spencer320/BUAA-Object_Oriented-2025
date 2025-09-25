import java.math.BigInteger;
import java.util.HashSet;

public class DiffFactor implements Factor {

    private final Expr innerExpr;
    private final boolean reverse;

    public DiffFactor(Expr innerExpr, boolean reverse) {
        this.innerExpr = innerExpr;
        this.reverse = reverse;
    }

    @Override
    public Polynomial calFactor() {
        Polynomial diffMono = innerExpr.calExpr().derive();
        if (reverse) {
            HashSet<Monomial> oneSet = new HashSet<>();
            oneSet.add(new Monomial(BigInteger.ONE.negate(), 0, null, null));
            return diffMono.multiply(new Polynomial(oneSet));
        }
        return diffMono;
    }
}

