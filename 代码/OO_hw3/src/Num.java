import java.math.BigInteger;
import java.util.HashSet;

public class Num implements Factor {
    private final BigInteger num;
    private final boolean reverse;

    public Num(BigInteger num, boolean reverse) {
        this.num = num;
        this.reverse = reverse;
    }

    @Override
    public Polynomial calFactor() {
        HashSet<Monomial> oneSet = new HashSet<>();
        if (num.equals(BigInteger.ZERO)) {
            return new Polynomial(oneSet);
        }
        if (! reverse) {
            oneSet.add(new Monomial(num, 0, null, null));
        } else {
            oneSet.add(new Monomial(num.negate(), 0, null, null));
        }
        return new Polynomial(oneSet);
    }

}

