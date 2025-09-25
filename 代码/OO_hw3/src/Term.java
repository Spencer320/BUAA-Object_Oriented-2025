import java.util.ArrayList;
import java.util.HashSet;

public class Term {
    private final ArrayList<Factor> factors = new ArrayList<>();

    public void addFactor(Factor factor) {
        factors.add(factor);
    }

    public Polynomial calTerm() {
        Polynomial ans = new Polynomial(new HashSet<>());
        if (! factors.isEmpty()) {
            ans = factors.get(0).calFactor();
            for (int i = 1; i < factors.size(); i++) {
                ans =
                    ans.multiply(factors.get(i).calFactor());
            }
        }
        return ans;
    }

}
