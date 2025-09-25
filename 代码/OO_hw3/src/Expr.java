import java.util.ArrayList;
import java.util.HashSet;

public class Expr {
    private final ArrayList<Term> terms = new ArrayList<>();

    public void addTerm(Term term) {
        terms.add(term);
    }

    public Polynomial calExpr() {
        Polynomial ans = new Polynomial(new HashSet<>());
        if (! terms.isEmpty()) {
            ans = terms.get(0).calTerm();
            for (int i = 1; i < terms.size(); i++) {
                ans = ans.add(terms.get(i).calTerm());
            }
        }
        return ans;
    }

}