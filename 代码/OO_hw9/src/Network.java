import com.oocourse.spec1.exceptions.AcquaintanceNotFoundException;
import com.oocourse.spec1.exceptions.EqualPersonIdException;
import com.oocourse.spec1.exceptions.EqualRelationException;
import com.oocourse.spec1.exceptions.EqualTagIdException;
import com.oocourse.spec1.exceptions.PersonIdNotFoundException;
import com.oocourse.spec1.exceptions.RelationNotFoundException;
import com.oocourse.spec1.exceptions.TagIdNotFoundException;
import com.oocourse.spec1.main.NetworkInterface;
import com.oocourse.spec1.main.PersonInterface;
import com.oocourse.spec1.main.TagInterface;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class Network implements NetworkInterface {
    private final HashMap<Integer, Person> persons = new HashMap<>();
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
        if (containsPerson(person.getId())) {
            throw new EqualPersonIdException(person.getId());
        } else {
            persons.put(person.getId(), (Person) person);
        }
    }

    @Override
    public void addRelation(int id1, int id2, int value)
        throws PersonIdNotFoundException, EqualRelationException {
        if (! containsPerson(id1)) {
            throw new PersonIdNotFoundException(id1);
        } else if (! containsPerson(id2)) {
            throw new PersonIdNotFoundException(id2);
        } else if (getPerson(id1).isLinked(getPerson(id2))) {
            throw new EqualRelationException(id1, id2);
        }

        Person person1 = (Person) getPerson(id1);
        Person person2 = (Person) getPerson(id2);
        person1.addLink(id2, person2, value);
        person2.addLink(id1, person1, value);
        tripleSum += queryCommonAcquaintNum(id1, id2);
    }

    @Override
    public void modifyRelation(int id1, int id2, int value)
        throws PersonIdNotFoundException, EqualPersonIdException, RelationNotFoundException {
        if (! containsPerson(id1)) {
            throw new PersonIdNotFoundException(id1);
        } else if (! containsPerson(id2)) {
            throw new PersonIdNotFoundException(id2);
        } else if (id1 == id2) {
            throw new EqualPersonIdException(id1);
        } else if (! getPerson(id1).isLinked(getPerson(id2))) {
            throw new RelationNotFoundException(id1, id2);
        }

        Person person1 = (Person) getPerson(id1);
        Person person2 = (Person) getPerson(id2);
        int newValue = person1.queryValue(person2) + value;
        if (newValue > 0) {
            person1.setValue(id2, newValue);
            person2.setValue(id1, newValue);
        } else {
            tripleSum -= queryCommonAcquaintNum(id1, id2);
            person1.delLink(id2);
            person2.delLink(id1);
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
        }
    }

    @Override
    public int queryValue(int id1, int id2)
        throws PersonIdNotFoundException, RelationNotFoundException {
        if (! containsPerson(id1)) {
            throw new PersonIdNotFoundException(id1);
        } else if (! containsPerson(id2)) {
            throw new PersonIdNotFoundException(id2);
        } else if (! getPerson(id1).isLinked(getPerson(id2))) {
            throw new RelationNotFoundException(id1, id2);
        }
        return getPerson(id1).queryValue(getPerson(id2));
    }

    @Override
    public boolean isCircle(int id1, int id2) throws PersonIdNotFoundException {
        if (! containsPerson(id1)) {
            throw new PersonIdNotFoundException(id1);
        } else if (! containsPerson(id2)) {
            throw new PersonIdNotFoundException(id2);
        }
        return (bfsShortestPath(id1, id2) != - 1);
    }

    @Override
    public int queryTripleSum() {
        return tripleSum;
    }

    @Override
    public void addTag(int personId, TagInterface tag)
        throws PersonIdNotFoundException, EqualTagIdException {
        if (! containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        } else if (getPerson(personId).containsTag(tag.getId())) {
            throw new EqualTagIdException(tag.getId());
        }
        getPerson(personId).addTag(tag);
    }

    @Override
    public void addPersonToTag(int personId1, int personId2, int tagId)
        throws PersonIdNotFoundException, RelationNotFoundException,
        TagIdNotFoundException, EqualPersonIdException {
        if (! containsPerson(personId1)) {
            throw new PersonIdNotFoundException(personId1);
        } else if (! containsPerson(personId2)) {
            throw new PersonIdNotFoundException(personId2);
        } else if (personId1 == personId2) {
            throw new EqualPersonIdException(personId1);
        } else if (! getPerson(personId2).isLinked(getPerson(personId1))) {
            throw new RelationNotFoundException(personId1, personId2);
        } else if (! getPerson(personId2).containsTag(tagId)) {
            throw new TagIdNotFoundException(tagId);
        } else if (getPerson(personId2).getTag(tagId).hasPerson(getPerson(personId1))) {
            throw new EqualPersonIdException(personId1);
        }

        Tag tag = (Tag) getPerson(personId2).getTag(tagId);
        if (tag.getSize() <= 999) {
            tag.addPerson(getPerson(personId1));
        }
    }

    @Override
    public int queryTagAgeVar(int personId, int tagId)
        throws PersonIdNotFoundException, TagIdNotFoundException {
        if (! containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        } else if (! getPerson(personId).containsTag(tagId)) {
            throw new TagIdNotFoundException(tagId);
        }

        return getPerson(personId).getTag(tagId).getAgeVar();
    }

    @Override
    public void delPersonFromTag(int personId1, int personId2, int tagId)
        throws PersonIdNotFoundException, TagIdNotFoundException {
        if (! containsPerson(personId1)) {
            throw new PersonIdNotFoundException(personId1);
        } else if (! containsPerson(personId2)) {
            throw new PersonIdNotFoundException(personId2);
        } else if (! getPerson(personId2).containsTag(tagId)) {
            throw new TagIdNotFoundException(tagId);
        } else if (! getPerson(personId2).getTag(tagId).hasPerson(getPerson(personId1))) {
            throw new PersonIdNotFoundException(personId1);
        }

        getPerson(personId2).getTag(tagId).delPerson(getPerson(personId1));
    }

    @Override
    public void delTag(int personId, int tagId)
        throws PersonIdNotFoundException, TagIdNotFoundException {
        if (! containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        } else if (! getPerson(personId).containsTag(tagId)) {
            throw new TagIdNotFoundException(tagId);
        }

        getPerson(personId).delTag(tagId);
    }

    @Override
    public int queryBestAcquaintance(int id)
        throws PersonIdNotFoundException, AcquaintanceNotFoundException {
        if (! containsPerson(id)) {
            throw new PersonIdNotFoundException(id);
        } else if (((Person) getPerson(id)).getAcquaintances().isEmpty()) {
            throw new AcquaintanceNotFoundException(id);
        }

        Person person = (Person) getPerson(id);
        boolean first = true;
        int bestId = 0;
        int bestValue = 0;
        for (Person acquaintance : person.getAcquaintances().values()) {
            if (first) {
                first = false;
                bestId = acquaintance.getId();
                bestValue = person.queryValue(acquaintance);
            } else {
                if (person.queryValue(acquaintance) > bestValue) {
                    bestId = acquaintance.getId();
                    bestValue = person.queryValue(acquaintance);
                } else if (person.queryValue(acquaintance) == bestValue &&
                    acquaintance.getId() < bestId) {
                    bestId = acquaintance.getId();
                    bestValue = person.queryValue(acquaintance);
                }
            }
        }
        return bestId;
    }

    public int queryCommonAcquaintNum(int id1, int id2) {
        PersonInterface p1 = getPerson(id1);
        PersonInterface p2 = getPerson(id2);
        int cnt = 0;
        for (Person other : persons.values()) {
            int otherId = other.getId();
            if (otherId != id1 && otherId != id2
                && p1.isLinked(other) && p2.isLinked(other)) {
                cnt++;
            }
        }
        return cnt;
    }

    private int bfsShortestPath(int id1, int id2) {
        if (id1 == id2) {
            return 0;
        }

        Queue<Integer> queue = new LinkedList<>();
        HashMap<Integer, Integer> distance = new HashMap<>();
        HashSet<Integer> visited = new HashSet<>();

        queue.offer(id1);
        distance.put(id1, 0);
        visited.add(id1);

        // BFS
        while (! queue.isEmpty()) {
            int currentId = queue.poll();
            Person currentPerson = (Person) getPerson(currentId);
            int currentDistance = distance.get(currentId);

            for (Person neighbor : currentPerson.getAcquaintances().values()) {
                int neighborId = neighbor.getId();
                if (! visited.contains(neighborId)) {
                    visited.add(neighborId);
                    distance.put(neighborId, currentDistance + 1);
                    queue.offer(neighborId);
                    if (neighborId == id2) {
                        return distance.get(neighborId);
                    }
                }
            }
        }
        return - 1;
    }
}
