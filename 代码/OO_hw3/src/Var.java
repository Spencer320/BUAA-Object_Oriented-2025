import java.math.BigInteger;
import java.util.HashSet;

public class Var implements Factor {
    private final Integer exp;
    private final String name;
    private final boolean reverse;

    public Var(Integer exp, String name, boolean reverse) {
        this.exp = exp;
        this.name = name;
        this.reverse = reverse;
    }

    @Override
    public Polynomial calFactor() {
        HashSet<Monomial> oneSet = new HashSet<>();
        if (! reverse) {
            oneSet.add(new Monomial(BigInteger.ONE, exp, null, null));
        } else {
            oneSet.add(new Monomial(BigInteger.ONE.negate(), exp, null, null));
        }
        return new Polynomial(oneSet);
    }
}
