import java.math.BigInteger;
import java.util.TreeMap;

public class Num implements Factor {
    private final BigInteger num;
    private final boolean reverse;

    public Num(BigInteger num, boolean reverse) {
        this.num = num;
        this.reverse = reverse;
    }

    @Override
    public Polynomial calFactor() {
        TreeMap<Integer, BigInteger> monomial = new TreeMap<>();
        if (num.equals(BigInteger.ZERO)) {
            return new Polynomial(monomial);
        }
        if (! reverse) {
            monomial.put(0, num);
        } else {
            monomial.put(0, num.negate());
        }
        return new Polynomial(monomial);
    }

}
