import java.math.BigInteger;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class Polynomial {
    // items here means terms of polynomial , doing so to avoid ambiguity
    private TreeMap<Integer, BigInteger> items;

    public Polynomial(TreeMap<Integer, BigInteger> items) {
        this.items = items;
    }

    public void add(Polynomial polynomial) {
        for (Map.Entry<Integer, BigInteger> item : polynomial.items.entrySet()) {
            Integer key = item.getKey();
            BigInteger value = item.getValue();
            this.items.merge(key, value, BigInteger::add);
            if (this.items.get(key) != null && this.items.get(key).equals(new BigInteger("0"))) {
                items.remove(key);
            }
        }
    }

    public void subtract(Polynomial polynomial) {
        for (Map.Entry<Integer, BigInteger> item : polynomial.items.entrySet()) {
            Integer key = item.getKey();
            BigInteger value = item.getValue();
            this.items.merge(key, value, BigInteger::subtract);
            if (this.items.get(key) != null && this.items.get(key).equals(new BigInteger("0"))) {
                items.remove(key);
            }
        }
    }

    public void multiply(Polynomial polynomial) {
        //from high-exp to low-exp
        TreeMap<Integer, BigInteger> temp = new TreeMap<>(Collections.reverseOrder());
        for (Map.Entry<Integer, BigInteger> item1 : this.items.entrySet()) {
            for (Map.Entry<Integer, BigInteger> item2 : polynomial.items.entrySet()) {
                int newExp = item1.getKey() + item2.getKey();
                BigInteger newCoeff = item1.getValue().multiply(item2.getValue());
                temp.merge(newExp, newCoeff, BigInteger::add);
                if (temp.get(newExp) != null && temp.get(newExp).equals(BigInteger.ZERO)) {
                    temp.remove(newExp);
                }
            }
        }
        this.items = temp;
    }

    public void power(int n) {
        if (n != 0) {
            Polynomial temp = new Polynomial(this.items);
            for (int i = 1; i < n; i++) {
                temp.multiply(this);
            }
            this.items = temp.items;
        } else {
            items.clear();
            items.put(0,BigInteger.ONE);
        }

    }

    @Override
    public String toString() {
        if (items.isEmpty()) {
            return "0";
        }
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (Map.Entry<Integer, BigInteger> item : items.entrySet()) {
            int exp = item.getKey();
            BigInteger coeff = item.getValue();
            if (coeff.equals(BigInteger.ZERO)) {
                continue;
            }
            String itemStr = formatItem(coeff, exp);
            if (first) {
                sb.append(itemStr);
                first = false;
            } else {
                sb.append((coeff.compareTo(BigInteger.ZERO) >= 0) ? "+" : "-");
                sb.append(itemStr.replace("-", ""));
            }
        }
        return sb.toString();
    }

    private String formatItem(BigInteger coeff, int exp) {
        String coeffStr = coeff.toString();
        if (exp == 0) {
            return coeffStr;
        }
        if (coeff.equals(BigInteger.ONE)) {
            coeffStr = "";
        } else if (coeff.equals(BigInteger.ONE.negate())) {
            coeffStr = "-";
        } else {
            coeffStr = coeffStr + "*";
        }
        String expStr = (exp == 1) ? "x" : "x^" + exp;
        return coeffStr + expStr;
    }
}
