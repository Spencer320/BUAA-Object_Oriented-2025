import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;

public class TrigFactor implements Factor {
    public enum Trig {
        SIN, COS
    }

    private final Trig trig;
    private final Factor innerFactor;
    private final int exp;
    private final boolean reverse;

    public TrigFactor(int exp, Factor innerFactor, Trig trig, boolean reverse) {
        this.exp = exp;
        this.innerFactor = innerFactor;
        this.trig = trig;
        this.reverse = reverse;
    }

    @Override
    public Polynomial calFactor() {
        Polynomial inside = innerFactor.calFactor();
        BigInteger integer = (reverse) ? BigInteger.ONE.negate() : BigInteger.ONE;
        HashMap<Polynomial, Integer> trigArray = new HashMap<>();
        if (exp != 0) {
            trigArray.put(inside, exp);
        }
        HashSet<Monomial> oneSet = new HashSet<>();
        if (trig == Trig.SIN) {
            oneSet.add(new Monomial(integer, 0, trigArray, null));
        } else {
            oneSet.add(new Monomial(integer, 0, null, trigArray));
        }
        return new Polynomial(oneSet);
    }
}
