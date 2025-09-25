import com.oocourse.spec3.main.OfficialAccountInterface;
import com.oocourse.spec3.main.PersonInterface;

import java.util.HashMap;
import java.util.HashSet;

public class OfficialAccount implements OfficialAccountInterface {
    static final int initContribution = 0;
    private final int ownerId;
    private final int id;
    private final String name;
    /*
    followers: personId, follower
    articles: articleId
    contributions: personId, contribution
     */
    private final HashMap<Integer, Person> followers = new HashMap<>();
    private final HashSet<Integer> articles = new HashSet<>();
    private final PriorityList contributions = new PriorityList();

    public OfficialAccount(int ownerId, int id, String name) {
        this.ownerId = ownerId;
        this.id = id;
        this.name = name;
    }

    @Override
    public int getOwnerId() {
        return ownerId;
    }

    @Override
    public void addFollower(PersonInterface person) {
        followers.put(person.getId(), (Person) person);
        contributions.addPair(person.getId(), initContribution);
    }

    @Override
    public boolean containsFollower(PersonInterface person) {
        return followers.containsKey(person.getId());
    }

    @Override
    public void addArticle(PersonInterface person, int id) {
        articles.add(id);
        contributions.modifySecondByFirst(person.getId(), 1);
    }

    @Override
    public boolean containsArticle(int id) {
        return articles.contains(id);
    }

    @Override
    public void removeArticle(int id) {
        articles.remove(id);
    }

    @Override
    public int getBestContributor() {
        return contributions.getFirstPair().getFirst();
    }

    public void sendArticle(int articleId) {
        for (Person follower : followers.values()) {
            follower.receiveArticle(articleId);
        }
    }

    public void withdrawArticle(int articleId, Integer contributorId) {
        for (Person follower : followers.values()) {
            follower.deleteArticle(articleId);
        }
        contributions.modifySecondByFirst(contributorId, -1);
    }
}