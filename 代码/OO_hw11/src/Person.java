import com.oocourse.spec3.main.MessageInterface;
import com.oocourse.spec3.main.PersonInterface;
import com.oocourse.spec3.main.TagInterface;

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
     */
    private final HashMap<Integer, Person> acquaintances = new HashMap<>();
    private final PriorityList value = new PriorityList();
    private final HashMap<Integer, Tag> tags = new HashMap<>();

    /*
    articleCount: internal id, ensure unique
    receivedArticles: articleCount
    recentArticles: recentArticleCount
    countToOriginalMap, originalToListOfCountsMap: as their names
     */
    private static int articleCount = 0;
    private static final int RECENT_ARTICLE_NUM = 5;
    private final HashMap<Integer, Integer> countToOriginalMap = new HashMap<>();
    private final HashMap<Integer, List<Integer>> originalToListOfCountsMap = new HashMap<>();
    private final LinkedHashSet<Integer> receivedArticles = new LinkedHashSet<>();
    private final LinkedList<Integer> recentArticles = new LinkedList<>(); // cache

    private int money = 0;
    private int socialValue = 0;
    private static final int RECENT_MESSAGE_NUM = 5;
    private final LinkedHashSet<Message> receivedMessages = new LinkedHashSet<>();
    private final LinkedList<Message> recentMessages = new LinkedList<>(); // cache

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
        LinkedList<Integer> originalIds = new LinkedList<>();
        for (Integer internalId : receivedArticles) {
            originalIds.addFirst(countToOriginalMap.get(internalId));
        }
        return new ArrayList<>(originalIds);
    }

    @Override
    public List<Integer> queryReceivedArticles() {
        if (recentArticles.size() != Math.min(receivedArticles.size(), RECENT_ARTICLE_NUM)) {
            rebuildRecentArticles();
        }
        List<Integer> originalIds = new ArrayList<>(recentArticles.size());
        for (Integer internalId : recentArticles) {
            originalIds.add(countToOriginalMap.get(internalId));
        }
        return new ArrayList<>(originalIds);
    }

    @Override
    public void addSocialValue(int num) {
        socialValue += num;
    }

    @Override
    public int getSocialValue() {
        return socialValue;
    }

    @Override
    public List<MessageInterface> getMessages() {
        LinkedList<Message> reversedMessages = new LinkedList<>();
        for (Message message : receivedMessages) {
            reversedMessages.addFirst(message);
        }
        return new ArrayList<>(reversedMessages);
    }

    @Override
    public List<MessageInterface> getReceivedMessages() {
        return new ArrayList<>(recentMessages);
    }

    @Override
    public void addMoney(int num) {
        money += num;
    }

    @Override
    public int getMoney() {
        return money;
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
        int internalId = articleCount++; // dispatch a new id
        countToOriginalMap.put(internalId, id);
        originalToListOfCountsMap.computeIfAbsent(id, k -> new LinkedList<>()).add(internalId);
        receivedArticles.add(internalId);
        recentArticles.addFirst(internalId);
        if (recentArticles.size() > RECENT_ARTICLE_NUM) {
            recentArticles.removeLast();
        }
    }

    public void deleteArticle(Integer id) {
        List<Integer> internalIdsToRemove = originalToListOfCountsMap.remove(id);
        if (internalIdsToRemove != null && !internalIdsToRemove.isEmpty()) {
            for (Integer internalId : internalIdsToRemove) {
                countToOriginalMap.remove(internalId);
                receivedArticles.remove(internalId);
                recentArticles.remove(internalId);
            }
        }
    }

    private void rebuildRecentArticles() {
        recentArticles.clear();
        List<Integer> articlesList = new ArrayList<>(receivedArticles);
        int actualSize = articlesList.size();
        for (int i = 0; i < min(RECENT_ARTICLE_NUM, actualSize); i++) {
            recentArticles.addLast(articlesList.get(actualSize - 1 - i));
        }
    }

    public void receiveMessage(Message message) {
        receivedMessages.add(message);
        recentMessages.addFirst(message);
        if (recentMessages.size() > RECENT_MESSAGE_NUM) {
            recentMessages.removeLast();
        }
    }

    public boolean strictEquals(PersonInterface other) {
        // placeholder method
        return true;
    }
}
