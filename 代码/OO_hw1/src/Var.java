import java.math.BigInteger;
import java.util.TreeMap;

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
        TreeMap<Integer, BigInteger> monomial = new TreeMap<>();
        if (! reverse) {
            monomial.put(exp, BigInteger.ONE);
        } else {
            monomial.put(exp, BigInteger.ONE.negate());
        }
        return new Polynomial(monomial);
    }
}
