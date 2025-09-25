import java.math.BigInteger;
import java.util.HashSet;

public class Polynomial {
    private final HashSet<Monomial> monos;

    public Polynomial(HashSet<Monomial> monos) {
        this.monos = monos;
    }

    public Polynomial add(Polynomial poly) {
        HashSet<Monomial> newMonos = new HashSet<>();
        for (Monomial mono : this.monos) {
            if (! mono.isZero()) {
                newMonos.add(mono);
            }
        }
        for (Monomial mono : poly.monos) {
            if (getComparableMono(newMonos, mono) != null) {
                Monomial oldMono = getComparableMono(newMonos, mono);
                Monomial newMono = mono.merge(oldMono);
                newMonos.remove(oldMono);
                if (! newMono.isZero()) {
                    newMonos.add(newMono);
                }
            } else {
                if (! mono.isZero()) {
                    newMonos.add(mono);
                }
            }
        }
        return new Polynomial(newMonos);
    }

    public Polynomial multiply(Polynomial poly) {
        HashSet<Monomial> newMonos = new HashSet<>();
        for (Monomial mono : poly.monos) {
            for (Monomial monoThis : this.monos) {
                Monomial mulMono = monoThis.multiply(mono);
                if (getComparableMono(newMonos, mulMono) != null) {
                    Monomial oldMono = getComparableMono(newMonos, mulMono);
                    Monomial newMono = mulMono.merge(oldMono);
                    newMonos.remove(oldMono);
                    if (! newMono.isZero()) {
                        newMonos.add(newMono);
                    }
                } else {
                    if (! mono.isZero()) {
                        newMonos.add(mulMono);
                    }
                }
            }
        }
        return new Polynomial(newMonos);
    }

    public Polynomial power(int exp) {
        if (exp == 0) {
            HashSet<Monomial> oneSet = new HashSet<>();
            oneSet.add(new Monomial(BigInteger.ONE, 0, null, null));
            return new Polynomial(oneSet);
        }
        Polynomial result = new Polynomial(this.monos);
        for (int i = 1; i < exp; i++) {
            result = result.multiply(this);
        }
        return result;
    }

    public Monomial getComparableMono(HashSet<Monomial> monoSet, Monomial mono) {
        Monomial result = null;
        for (Monomial monoThis : monoSet) {
            if (mono.comparable(monoThis)) {
                result = monoThis;
                break;
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Polynomial that = (Polynomial) o;
        return monos.equals(that.monos);
    }

    @Override
    public int hashCode() {
        return monos.hashCode();
    }

    @Override
    public String toString() {
        if (monos.isEmpty()) {
            return "0";
        }
        StringBuilder sb = new StringBuilder();
        for (Monomial m : monos) {
            sb.append(m.toString());
        }
        return sb.substring(0, sb.length());
    }

    public Polynomial derive() {
        Polynomial diffPoly = new Polynomial(new HashSet<>());
        Polynomial diffMono;
        for (Monomial mono : monos) {
            diffMono = mono.derive();
            diffPoly = diffPoly.add(diffMono);
        }
        return diffPoly;
    }

    public Polynomial clone() {
        HashSet<Monomial> clonedMonos = new HashSet<>();
        for (Monomial mono : this.monos) {
            clonedMonos.add(mono.clone());
        }
        return new Polynomial(clonedMonos);
    }
}
