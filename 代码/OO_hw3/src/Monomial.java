import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

public class Monomial {
    private BigInteger coef;
    private int exp;
    private final HashMap<Polynomial, Integer> sinArray;
    private final HashMap<Polynomial, Integer> cosArray;

    public Monomial(BigInteger coef,
        int exp,
        HashMap<Polynomial, Integer> sinArray,
        HashMap<Polynomial, Integer> cosArray) {
        this.coef = coef;
        this.exp = exp;
        this.sinArray = sinArray != null ? sinArray : new HashMap<>();
        this.cosArray = cosArray != null ? cosArray : new HashMap<>();
    }

    public boolean comparable(Monomial mono) {
        return this.exp == mono.exp &&
            this.cosArray.equals(mono.cosArray) &&
            this.sinArray.equals(mono.sinArray);
    }

    public Monomial merge(Monomial mono) {
        this.coef = this.coef.add(mono.coef);
        return this;
    }

    public Monomial multiply(Monomial mono) {
        BigInteger newCoef = this.coef.multiply(mono.coef);
        int newExp = this.exp + mono.exp;
        HashMap<Polynomial, Integer> newSinArray = mergeMaps(this.sinArray, mono.sinArray);
        HashMap<Polynomial, Integer> newCosArray = mergeMaps(this.cosArray, mono.cosArray);
        return new Monomial(newCoef, newExp, newSinArray, newCosArray);
    }

    private HashMap<Polynomial, Integer> mergeMaps(
        HashMap<Polynomial, Integer> map1,
        HashMap<Polynomial, Integer> map2
    ) {
        HashMap<Polynomial, Integer> merged = new HashMap<>(map1);
        map2.forEach((k, v) -> merged.merge(k, v, Integer::sum));
        return merged;
    }

    public boolean isZero() {
        return coef.equals(BigInteger.ZERO);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Monomial monomial = (Monomial) o;
        return coef.equals(monomial.coef) &&
            exp == monomial.exp &&
            Objects.equals(sinArray, monomial.sinArray) &&
            Objects.equals(cosArray, monomial.cosArray);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exp, sinArray, cosArray);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        boolean isCoefOne = false;
        if (coef.compareTo(BigInteger.ZERO) > 0) {
            sb.append("+");
        }
        if (coef.equals(BigInteger.ONE.negate())) {
            sb.append("-");
            isCoefOne = true;
        } else if (coef.equals(BigInteger.ONE)) {
            isCoefOne = true;
        } else {
            sb.append(coef);
        }
        if (exp == 1) {
            if (! isCoefOne) {
                sb.append("*");
            }
            sb.append("x");
        } else if (exp == 0) {
            if (isCoefOne) {
                sb.append("1");
            }
        } else {
            if (! isCoefOne) {
                sb.append("*");
            }
            sb.append("x^").append(exp);
        }
        if (! sinArray.isEmpty()) {
            for (Map.Entry<Polynomial, Integer> entry : sinArray.entrySet()) {
                String key = entry.getKey().toString();
                if (key.equals("0")) {
                    sb.append("*0");
                } else {
                    sb.append("*sin((").append(key).append("))");
                    if (entry.getValue() != 1) {
                        sb.append("^").append(entry.getValue());
                    }
                }
            }
        }
        if (! cosArray.isEmpty()) {
            for (Map.Entry<Polynomial, Integer> entry : cosArray.entrySet()) {
                String key = entry.getKey().toString();
                if (key.equals("0")) {
                    sb.append("*1");
                } else {
                    sb.append("*cos((").append(key).append("))");
                    if (entry.getValue() != 1) {
                        sb.append("^").append(entry.getValue());
                    }
                }
            }
        }
        return sb.toString();
    }

    public Polynomial derive() {
        // add first mono
        HashSet<Monomial> diffFirstMono = new HashSet<>();
        Monomial firstMono = this.clone();
        firstMono.coef = this.coef.multiply(BigInteger.valueOf(this.exp));
        if (exp != 0) {
            firstMono.exp--;
        }
        diffFirstMono.add(firstMono);
        Polynomial result = new Polynomial(diffFirstMono);
        // add diffSinArray , if possible
        if (! sinArray.isEmpty()) {
            for (Map.Entry<Polynomial, Integer> entry : sinArray.entrySet()) {
                Monomial clone = this.clone();
                Polynomial key = entry.getKey();
                Integer value = entry.getValue();
                clone.sinArray.remove(key);

                HashMap<Polynomial, Integer> oneMapSin = new HashMap<>();
                HashMap<Polynomial, Integer> oneMapCos = new HashMap<>();
                if (value != 1) {
                    oneMapSin.put(key, value - 1);
                }
                oneMapCos.put(key, 1);
                Monomial diffSinItem = new Monomial(BigInteger.valueOf(value), 0,
                    oneMapSin, oneMapCos);
                clone = clone.multiply(diffSinItem);

                HashSet<Monomial> oneSet = new HashSet<>();
                oneSet.add(clone);
                Polynomial diffOneSin = key.derive().multiply(new Polynomial(oneSet));
                result = result.add(diffOneSin);
            }
        }
        // add diffCosArray , if possible
        if (! cosArray.isEmpty()) {
            for (Map.Entry<Polynomial, Integer> entry : cosArray.entrySet()) {
                Monomial clone = this.clone();
                Polynomial key = entry.getKey();
                Integer value = entry.getValue();
                clone.cosArray.remove(key);

                HashMap<Polynomial, Integer> oneMapSin = new HashMap<>();
                HashMap<Polynomial, Integer> oneMapCos = new HashMap<>();
                if (value != 1) {
                    oneMapCos.put(key, value - 1);
                }
                oneMapSin.put(key, 1);
                Monomial diffSinItem = new Monomial(BigInteger.valueOf(value).negate(), 0,
                    oneMapSin, oneMapCos);
                clone = clone.multiply(diffSinItem);

                HashSet<Monomial> oneSet = new HashSet<>();
                oneSet.add(clone);
                Polynomial diffOneCos = key.derive().multiply(new Polynomial(oneSet));
                result = result.add(diffOneCos);
            }
        }
        return result;
    }

    public Monomial clone() {
        HashMap<Polynomial, Integer> clonedSinArray = new HashMap<>();
        for (Polynomial poly : this.sinArray.keySet()) {
            clonedSinArray.put(poly.clone(), this.sinArray.get(poly));
        }

        HashMap<Polynomial, Integer> clonedCosArray = new HashMap<>();
        for (Polynomial poly : this.cosArray.keySet()) {
            clonedCosArray.put(poly.clone(), this.cosArray.get(poly));
        }

        return new Monomial(this.coef, this.exp, clonedSinArray, clonedCosArray);
    }
}
