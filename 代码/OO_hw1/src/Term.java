import java.util.ArrayList;
import java.util.TreeMap;

public class Term {
    private final ArrayList<Factor> factors = new ArrayList<>();

    public void addFactor(Factor factor) {
        factors.add(factor);
    }

    public Polynomial calTerm() {
        Polynomial ans = new Polynomial(new TreeMap<>());
        if (! factors.isEmpty()) {
            ans = factors.get(0).calFactor();
            for (int i = 1; i < factors.size(); i++) {
                ans.multiply(factors.get(i).calFactor());
            }
        }
        return ans;
    }

}
