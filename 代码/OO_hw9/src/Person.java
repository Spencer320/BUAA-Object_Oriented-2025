import com.oocourse.spec1.main.PersonInterface;
import com.oocourse.spec1.main.TagInterface;

import java.util.HashMap;

public class Person implements PersonInterface {
    private final int id;
    private final String name;
    private final int age;
    /*
    acquaintances : id, acquaintance
    values : id, value
    tags : id, tag
     */
    private final HashMap<Integer, Person> acquaintances = new HashMap<>();
    private final HashMap<Integer, Integer> value = new HashMap<>();
    private final HashMap<Integer, Tag> tags = new HashMap<>();

    public Person(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getAge() {
        return age;
    }

    @Override
    public boolean containsTag(int id) {
        return tags.containsKey(id);
    }

    @Override
    public TagInterface getTag(int id) {
        if (! containsTag(id)) {
            return null;
        }
        return tags.get(id);
    }

    @Override
    public void addTag(TagInterface tag) {
        tags.put(tag.getId(), (Tag) tag);
    }

    @Override
    public void delTag(int id) {
        tags.remove(id);
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null &&
            obj instanceof PersonInterface &&
            (((PersonInterface) obj).getId() == id);
    }

    @Override
    public boolean isLinked(PersonInterface person) {
        if (person.getId() == id) {
            return true;
        }
        return acquaintances.containsKey(person.getId());
    }

    @Override
    public int queryValue(PersonInterface person) {
        if (! acquaintances.containsKey(person.getId())) {
            return 0;
        }
        return value.get(person.getId());
    }

    public void addLink(Integer id, PersonInterface person, Integer value) {
        acquaintances.put(id, (Person) person);
        this.value.put(id, value);
    }

    public void setValue(Integer id, Integer value) {
        this.value.put(id, value);
    }

    public void delLink(Integer id) {
        acquaintances.remove(id);
        this.value.remove(id);
    }

    public HashMap<Integer, Tag> getTags() {
        return tags;
    }

    public HashMap<Integer, Person> getAcquaintances() {
        return acquaintances;
    }
}
