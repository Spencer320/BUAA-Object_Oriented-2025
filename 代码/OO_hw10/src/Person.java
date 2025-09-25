import com.oocourse.spec2.main.PersonInterface;
import com.oocourse.spec2.main.TagInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Math.min;

public class Person implements PersonInterface {
    private final int id;
    private final String name;
    private final int age;
    /*
    acquaintances: id, acquaintance
    values: id, value
    tags: id, tag
    receivedArticles: id
     */
    private final HashMap<Integer, Person> acquaintances = new HashMap<>();
    private final PriorityList value = new PriorityList();
    private final HashMap<Integer, Tag> tags = new HashMap<>();
    private static final int RECENT_ARTICLE_NUM = 5;
    private final LinkedHashSet<Integer> receivedArticles = new LinkedHashSet<>();
    private final LinkedList<Integer> recentArticles = new LinkedList<>();
    private boolean isRecentArticlesDirty = false;

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
        if (!containsTag(id)) {
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
        int personId = person.getId();
        return personId == id || acquaintances.containsKey(personId);
    }

    @Override
    public int queryValue(PersonInterface person) {
        if (!acquaintances.containsKey(person.getId())) {
            return 0;
        }
        return value.getSecondByFirst(person.getId());
    }

    @Override
    public List<Integer> getReceivedArticles() {
        return new ArrayList<>(receivedArticles);
    }

    @Override
    public List<Integer> queryReceivedArticles() {
        if (isRecentArticlesDirty) {
            rebuildRecentArticles();
            isRecentArticlesDirty = false;
        }
        return new ArrayList<>(recentArticles);
    }

    public void addLink(Integer id, PersonInterface person, Integer value) {
        acquaintances.put(id, (Person) person);
        this.value.addPair(id, value);
    }

    public void setValue(Integer id, Integer value) {
        this.value.addPair(id, value);
    }

    public void deleteLink(Integer id) {
        acquaintances.remove(id);
        this.value.removePairByFirst(id);
    }

    public HashMap<Integer, Tag> getTags() {
        return tags;
    }

    public int queryBestAcquaintance() {
        return value.getFirstPair().getFirst();
    }

    public HashMap<Integer, Person> getAcquaintances() {
        return acquaintances;
    }

    public int queryCommonAcquaintNum(PersonInterface other) {
        int cnt = 0;
        for (Person acquaint : acquaintances.values()) {
            if (other.getId() != acquaint.getId() &&
                acquaint.isLinked(this) && acquaint.isLinked(other)) {
                cnt++;
            }
        }
        return cnt;
    }

    public void receiveArticle(Integer id) {
        receivedArticles.add(id);
        recentArticles.addFirst(id);
        if (recentArticles.size() > RECENT_ARTICLE_NUM) {
            recentArticles.removeLast();
        }
        if (recentArticles.size() == RECENT_ARTICLE_NUM) {
            isRecentArticlesDirty = false;
        }
    }

    public void deleteArticle(Integer id) {
        receivedArticles.remove(id);
        recentArticles.remove(id);
        if (recentArticles.size() < RECENT_ARTICLE_NUM &&
            receivedArticles.size() >= RECENT_ARTICLE_NUM) {
            isRecentArticlesDirty = true;
        }
    }

    private void rebuildRecentArticles() {
        recentArticles.clear();
        List<Integer> articlesList = new ArrayList<>(receivedArticles);
        int actualSize = articlesList.size();
        for (int i = 0; i < min(RECENT_ARTICLE_NUM,actualSize); i++) {
            recentArticles.addLast(articlesList.get(actualSize - 1 - i));
        }
    }

    public boolean strictEquals(PersonInterface other) {
        // placeholder method
        return true;
    }
}
