import com.oocourse.spec2.exceptions.AcquaintanceNotFoundException;
import com.oocourse.spec2.exceptions.ArticleIdNotFoundException;
import com.oocourse.spec2.exceptions.ContributePermissionDeniedException;
import com.oocourse.spec2.exceptions.DeleteArticlePermissionDeniedException;
import com.oocourse.spec2.exceptions.DeleteOfficialAccountPermissionDeniedException;
import com.oocourse.spec2.exceptions.EqualArticleIdException;
import com.oocourse.spec2.exceptions.EqualOfficialAccountIdException;
import com.oocourse.spec2.exceptions.EqualPersonIdException;
import com.oocourse.spec2.exceptions.EqualRelationException;
import com.oocourse.spec2.exceptions.EqualTagIdException;
import com.oocourse.spec2.exceptions.OfficialAccountIdNotFoundException;
import com.oocourse.spec2.exceptions.PathNotFoundException;
import com.oocourse.spec2.exceptions.PersonIdNotFoundException;
import com.oocourse.spec2.exceptions.RelationNotFoundException;
import com.oocourse.spec2.exceptions.TagIdNotFoundException;
import com.oocourse.spec2.main.NetworkInterface;
import com.oocourse.spec2.main.PersonInterface;
import com.oocourse.spec2.main.TagInterface;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class Network implements NetworkInterface {
    /*
    persons: personId, person
    accounts: accountId, account
    articles: articleId
    articleContributors: articleId, personId
     */
    private final HashMap<Integer, Person> persons = new HashMap<>();
    private final HashMap<Integer, OfficialAccount> accounts = new HashMap<>();
    private final HashSet<Integer> articles = new HashSet<>();
    private final HashMap<Integer, Integer> articleContributors = new HashMap<>();
    private int tripleSum = 0;

    @Override
    public boolean containsPerson(int id) {
        return persons.containsKey(id);
    }

    @Override
    public PersonInterface getPerson(int id) {
        return persons.get(id);
    }

    @Override
    public void addPerson(PersonInterface person) throws EqualPersonIdException {
        checkHasPerson(person.getId());
        persons.put(person.getId(), (Person) person);
    }

    @Override
    public void addRelation(int id1, int id2, int value)
        throws PersonIdNotFoundException, EqualRelationException {
        checkPersonIdNotFound(id1);
        checkPersonIdNotFound(id2);
        checkEqualRelation(id1, id2);

        Person person1 = (Person) getPerson(id1);
        Person person2 = (Person) getPerson(id2);
        person1.addLink(id2, person2, value);
        person2.addLink(id1, person1, value);
        tripleSum += queryCommonAcquaintNum(id1, id2);
        updateTagValueSum(person1, person2, value);
    }

    @Override
    public void modifyRelation(int id1, int id2, int value)
        throws PersonIdNotFoundException, EqualPersonIdException, RelationNotFoundException {
        checkPersonIdNotFound(id1);
        checkPersonIdNotFound(id2);
        checkEqualPersonId(id1, id2);
        checkRelationNotFound(id1, id2);

        Person person1 = (Person) getPerson(id1);
        Person person2 = (Person) getPerson(id2);
        int oldValue = person1.queryValue(person2);
        int newValue = oldValue + value;
        if (newValue > 0) {
            person1.setValue(id2, newValue);
            person2.setValue(id1, newValue);
            updateTagValueSum(person1, person2, value);
        } else {
            tripleSum -= queryCommonAcquaintNum(id1, id2);
            person1.deleteLink(id2);
            person2.deleteLink(id1);
            for (Tag tag1 : person1.getTags().values()) {
                if (tag1.hasPerson(person2)) {
                    tag1.delPerson(person2);
                }
            }
            for (Tag tag2 : person2.getTags().values()) {
                if (tag2.hasPerson(person1)) {
                    tag2.delPerson(person1);
                }
            }
            updateTagValueSum(person1, person2, -oldValue);
        }
    }

    @Override
    public int queryValue(int id1, int id2)
        throws PersonIdNotFoundException, RelationNotFoundException {
        checkPersonIdNotFound(id1);
        checkPersonIdNotFound(id2);
        checkRelationNotFound(id1, id2);

        return getPerson(id1).queryValue(getPerson(id2));
    }

    @Override
    public boolean isCircle(int id1, int id2) throws PersonIdNotFoundException {
        checkPersonIdNotFound(id1);
        checkPersonIdNotFound(id2);

        return (bfsShortestPath(id1, id2) != -1);
    }

    @Override
    public int queryTripleSum() {
        return tripleSum;
    }

    @Override
    public void addTag(int personId, TagInterface tag)
        throws PersonIdNotFoundException, EqualTagIdException {
        checkPersonIdNotFound(personId);
        checkEqualTagId(personId, tag.getId());

        getPerson(personId).addTag(tag);
    }

    @Override
    public void addPersonToTag(int personId1, int personId2, int tagId)
        throws PersonIdNotFoundException, RelationNotFoundException,
        TagIdNotFoundException, EqualPersonIdException {
        checkPersonIdNotFound(personId1);
        checkPersonIdNotFound(personId2);
        checkEqualPersonId(personId1, personId2);
        checkRelationNotFound(personId1, personId2);
        checkTagIdNotFound(personId2, tagId);
        checkTagHasPerson(personId1, personId2, tagId);

        Tag tag = (Tag) getPerson(personId2).getTag(tagId);
        if (tag.getSize() <= 999) {
            tag.addPerson(getPerson(personId1));
        }
    }

    @Override
    public int queryTagValueSum(int personId, int tagId)
        throws PersonIdNotFoundException, TagIdNotFoundException {
        checkPersonIdNotFound(personId);
        checkTagIdNotFound(personId, tagId);

        return getPerson(personId).getTag(tagId).getValueSum();
    }

    @Override
    public int queryTagAgeVar(int personId, int tagId)
        throws PersonIdNotFoundException, TagIdNotFoundException {
        checkPersonIdNotFound(personId);
        checkTagIdNotFound(personId, tagId);

        return getPerson(personId).getTag(tagId).getAgeVar();
    }

    @Override
    public void delPersonFromTag(int personId1, int personId2, int tagId)
        throws PersonIdNotFoundException, TagIdNotFoundException {
        checkPersonIdNotFound(personId1);
        checkPersonIdNotFound(personId2);
        checkTagIdNotFound(personId2, tagId);
        checkTagPersonIdNotFound(personId1, personId2, tagId);

        getPerson(personId2).getTag(tagId).delPerson(getPerson(personId1));
    }

    @Override
    public void delTag(int personId, int tagId)
        throws PersonIdNotFoundException, TagIdNotFoundException {
        checkPersonIdNotFound(personId);
        checkTagIdNotFound(personId, tagId);

        getPerson(personId).delTag(tagId);
    }

    @Override
    public int queryBestAcquaintance(int id)
        throws PersonIdNotFoundException, AcquaintanceNotFoundException {
        checkPersonIdNotFound(id);
        checkAcquaintanceNotFound(id);

        return ((Person) getPerson(id)).queryBestAcquaintance();
    }

    @Override
    public int queryCoupleSum() {
        int cnt = 0;
        for (Person person : persons.values()) {
            // query valid
            if (!person.getAcquaintances().isEmpty()) {
                int personId = person.getId();
                int otherId = person.queryBestAcquaintance();
                Person other = (Person) getPerson(otherId);
                if (personId < otherId && !other.getAcquaintances().isEmpty()) {
                    if (other.queryBestAcquaintance() == personId) {
                        cnt++;
                    }
                }
            }
        }
        return cnt;
    }

    @Override
    public int queryShortestPath(int id1, int id2)
        throws PersonIdNotFoundException, PathNotFoundException {
        checkPersonIdNotFound(id1);
        checkPersonIdNotFound(id2);

        int distance = bfsShortestPath(id1, id2);
        checkPathNotFound(id1, id2, distance);
        return distance;
    }

    @Override
    public boolean containsAccount(int id) {
        return accounts.containsKey(id);
    }

    @Override
    public void createOfficialAccount(int personId, int accountId, String name)
        throws PersonIdNotFoundException, EqualOfficialAccountIdException {
        checkPersonIdNotFound(personId);
        checkEqualOfficialAccountId(accountId);

        OfficialAccount account = new OfficialAccount(personId, accountId, name);
        accounts.put(accountId, account);
        account.addFollower(getPerson(personId));
    }

    @Override
    public void deleteOfficialAccount(int personId, int accountId)
        throws PersonIdNotFoundException, OfficialAccountIdNotFoundException,
        DeleteOfficialAccountPermissionDeniedException {
        checkPersonIdNotFound(personId);
        checkOfficialAccountIdNotFound(accountId);
        checkDeleteOfficialAccountPermissionDenied(personId, accountId);
        accounts.remove(accountId);
    }

    @Override
    public boolean containsArticle(int id) {
        return articles.contains(id);
    }

    @Override
    public void contributeArticle(int personId, int accountId, int articleId)
        throws PersonIdNotFoundException, OfficialAccountIdNotFoundException,
        EqualArticleIdException, ContributePermissionDeniedException {
        checkPersonIdNotFound(personId);
        checkOfficialAccountIdNotFound(accountId);
        checkEqualArticleId(articleId);
        checkContributePermissionDenied(personId, accountId, articleId);

        articles.add(articleId);
        accounts.get(accountId).addArticle(getPerson(personId), articleId);
        articleContributors.put(articleId, personId);
        accounts.get(accountId).sendArticle(articleId);
    }

    @Override
    public void deleteArticle(int personId, int accountId, int articleId)
        throws PersonIdNotFoundException, OfficialAccountIdNotFoundException,
        ArticleIdNotFoundException, DeleteArticlePermissionDeniedException {
        checkPersonIdNotFound(personId);
        checkOfficialAccountIdNotFound(accountId);
        checkArticleIdNotFound(accountId, articleId);
        checkDeleteArticlePermissionDenied(personId, accountId, articleId);

        OfficialAccount account = accounts.get(accountId);
        account.removeArticle(articleId);
        Integer contributorId = articleContributors.get(articleId);
        account.withdrawArticle(articleId, contributorId);

    }

    @Override
    public void followOfficialAccount(int personId, int accountId)
        throws PersonIdNotFoundException, OfficialAccountIdNotFoundException,
        EqualPersonIdException {
        checkPersonIdNotFound(personId);
        checkOfficialAccountIdNotFound(accountId);
        checkAccountHasPerson(personId, accountId);

        accounts.get(accountId).addFollower(getPerson(personId));
    }

    @Override
    public int queryBestContributor(int id)
        throws OfficialAccountIdNotFoundException {
        checkOfficialAccountIdNotFound(id);

        return accounts.get(id).getBestContributor();
    }

    @Override
    public List<Integer> queryReceivedArticles(int id)
        throws PersonIdNotFoundException {
        checkPersonIdNotFound(id);
        return getPerson(id).queryReceivedArticles();
    }

    public int queryCommonAcquaintNum(int id1, int id2) {
        Person p1 = (Person) getPerson(id1);
        Person p2 = (Person) getPerson(id2);
        int size1 = p1.getAcquaintances().size();
        int size2 = p2.getAcquaintances().size();
        return (size1 < size2) ? p1.queryCommonAcquaintNum(p2) :
            p2.queryCommonAcquaintNum(p1);
    }

    private int bfsShortestPath(int id1, int id2) {
        if (id1 == id2) {
            return 0;
        }

        // Bidirectional BFS
        Queue<Integer> queueFwd = new ArrayDeque<>();
        Map<Integer, Integer> distFwd = new HashMap<>();
        queueFwd.offer(id1);
        distFwd.put(id1, 0);

        Queue<Integer> queueBwd = new ArrayDeque<>();
        Map<Integer, Integer> distBwd = new HashMap<>();
        queueBwd.offer(id2);
        distBwd.put(id2, 0);

        while (!queueFwd.isEmpty() && !queueBwd.isEmpty()) {
            int levelSizeFwd = queueFwd.size();
            for (int i = 0; i < levelSizeFwd; i++) {
                int currentId = queueFwd.poll();
                Person currentPerson = (Person) getPerson(currentId);
                int currentDist = distFwd.get(currentId);
                if (distBwd.containsKey(currentId)) {
                    return currentDist + distBwd.get(currentId); // return if meet
                }
                for (Person neighbor : currentPerson.getAcquaintances().values()) {
                    int neighborId = neighbor.getId();
                    if (!distFwd.containsKey(neighborId)) {
                        distFwd.put(neighborId, currentDist + 1);
                        queueFwd.offer(neighborId);
                    }
                }
            }

            int levelSizeBwd = queueBwd.size();
            for (int i = 0; i < levelSizeBwd; i++) {
                int currentId = queueBwd.poll();
                Person currentPerson = (Person) getPerson(currentId);
                int currentDist = distBwd.get(currentId);
                if (distFwd.containsKey(currentId)) {
                    return currentDist + distFwd.get(currentId); // return if meet
                }
                for (Person neighbor : currentPerson.getAcquaintances().values()) {
                    int neighborId = neighbor.getId();
                    if (!distBwd.containsKey(neighborId)) {
                        distBwd.put(neighborId, currentDist + 1);
                        queueBwd.offer(neighborId);
                    }
                }
            }
        }
        return -1;
    }

    public PersonInterface[] getPersons() {
        return persons.values().toArray(new PersonInterface[0]);
    }

    private void updateTagValueSum(Person person1, Person person2, int value) {
        for (Person person : persons.values()) {
            for (Tag tag : person.getTags().values()) {
                if (tag.hasPerson(person1) && tag.hasPerson(person2)) {
                    tag.modifyValue(value);
                }
            }
        }
    }

    /* <exception> */
    private void checkHasPerson(int id)
        throws EqualPersonIdException {
        if (containsPerson(id)) {
            throw new EqualPersonIdException(id);
        }
    }

    private void checkPersonIdNotFound(int id)
        throws PersonIdNotFoundException {
        if (!containsPerson(id)) {
            throw new PersonIdNotFoundException(id);
        }
    }

    private void checkEqualRelation(int id1, int id2)
        throws EqualRelationException {
        if (getPerson(id1).isLinked(getPerson(id2))) {
            throw new EqualRelationException(id1, id2);
        }
    }

    private void checkEqualPersonId(int id1, int id2)
        throws EqualPersonIdException {
        if (id1 == id2) {
            throw new EqualPersonIdException(id1);
        }
    }

    private void checkRelationNotFound(int id1, int id2)
        throws RelationNotFoundException {
        if (!getPerson(id1).isLinked(getPerson(id2))) {
            throw new RelationNotFoundException(id1, id2);
        }
    }

    private void checkEqualTagId(int personId, int tagId)
        throws EqualTagIdException {
        if (getPerson(personId).containsTag(tagId)) {
            throw new EqualTagIdException(tagId);
        }
    }

    private void checkTagIdNotFound(int personId, int tagId)
        throws TagIdNotFoundException {
        if (!getPerson(personId).containsTag(tagId)) {
            throw new TagIdNotFoundException(tagId);
        }
    }

    private void checkTagHasPerson(int personId1, int personId2, int tagId)
        throws EqualPersonIdException {
        if (getPerson(personId2).getTag(tagId).hasPerson(getPerson(personId1))) {
            throw new EqualPersonIdException(personId1);
        }
    }

    private void checkTagPersonIdNotFound(int personId1, int personId2, int tagId)
        throws PersonIdNotFoundException {
        if (!getPerson(personId2).getTag(tagId).hasPerson(getPerson(personId1))) {
            throw new PersonIdNotFoundException(personId1);
        }
    }

    private void checkAcquaintanceNotFound(int id)
        throws AcquaintanceNotFoundException {
        if (((Person) getPerson(id)).getAcquaintances().isEmpty()) {
            throw new AcquaintanceNotFoundException(id);
        }
    }

    private void checkPathNotFound(int id1, int id2, int distance)
        throws PathNotFoundException {
        if (distance == -1) {
            throw new PathNotFoundException(id1, id2);
        }
    }

    private void checkEqualOfficialAccountId(int id)
        throws EqualOfficialAccountIdException {
        if (containsAccount(id)) {
            throw new EqualOfficialAccountIdException(id);
        }
    }

    private void checkOfficialAccountIdNotFound(int id)
        throws OfficialAccountIdNotFoundException {
        if (!containsAccount(id)) {
            throw new OfficialAccountIdNotFoundException(id);
        }
    }

    private void checkDeleteOfficialAccountPermissionDenied(int personId, int accountId)
        throws DeleteOfficialAccountPermissionDeniedException {
        if (accounts.get(accountId).getOwnerId() != personId) {
            throw new DeleteOfficialAccountPermissionDeniedException(personId, accountId);
        }
    }

    private void checkEqualArticleId(int id)
        throws EqualArticleIdException {
        if (containsArticle(id)) {
            throw new EqualArticleIdException(id);
        }
    }

    private void checkContributePermissionDenied(int personId, int accountId, int articleId)
        throws ContributePermissionDeniedException {
        if (!accounts.get(accountId).containsFollower(getPerson(personId))) {
            throw new ContributePermissionDeniedException(personId, articleId);
        }
    }

    private void checkArticleIdNotFound(int accountId, int articleId)
        throws ArticleIdNotFoundException {
        if (!accounts.get(accountId).containsArticle(articleId)) {
            throw new ArticleIdNotFoundException(articleId);
        }
    }

    private void checkDeleteArticlePermissionDenied(int personId, int accountId, int articleId)
        throws DeleteArticlePermissionDeniedException {
        if (accounts.get(accountId).getOwnerId() != personId) {
            throw new DeleteArticlePermissionDeniedException(personId, articleId);
        }
    }

    private void checkAccountHasPerson(int personId, int accountId)
        throws EqualPersonIdException {
        if (accounts.get(accountId).containsFollower(getPerson(personId))) {
            throw new EqualPersonIdException(personId);
        }
    }
    /* </exception> */
}
