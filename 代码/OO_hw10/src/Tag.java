import com.oocourse.spec2.main.PersonInterface;
import com.oocourse.spec2.main.TagInterface;

import java.util.HashMap;

public class Tag implements TagInterface {
    private final int id;
    private final HashMap<Integer, Person> persons = new HashMap<>();
    private int halfValueSum = 0;

    public Tag(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null &&
            obj instanceof TagInterface &&
            (((TagInterface) obj).getId() == id);
    }

    @Override
    public void addPerson(PersonInterface person) {
        persons.put(person.getId(), (Person) person);
        for (Person other : persons.values()) {
            halfValueSum += person.queryValue(other);
        }
    }

    @Override
    public boolean hasPerson(PersonInterface person) {
        return persons.containsKey(person.getId());
    }

    @Override
    public int getValueSum() {
        return halfValueSum * 2;
    }

    @Override
    public int getAgeMean() {
        if (persons.isEmpty()) {
            return 0;
        }
        int sum = 0;
        for (Person person : persons.values()) {
            sum += person.getAge();
        }
        return sum / persons.size();
    }

    @Override
    public int getAgeVar() {
        if (persons.isEmpty()) {
            return 0;
        }
        int mean = getAgeMean();
        int sum = 0;
        for (Person person : persons.values()) {
            sum += (person.getAge() - mean) * (person.getAge() - mean);
        }
        return sum / persons.size();
    }

    @Override
    public void delPerson(PersonInterface person) {
        for (Person other : persons.values()) {
            halfValueSum -= person.queryValue(other);
        }
        persons.remove(person.getId());
    }

    @Override
    public int getSize() {
        return persons.size();
    }

    public void modifyValue(int value) {
        halfValueSum += value;
    }
}
