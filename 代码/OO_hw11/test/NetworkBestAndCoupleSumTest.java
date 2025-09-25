import com.oocourse.spec3.exceptions.AcquaintanceNotFoundException;
import com.oocourse.spec3.exceptions.EqualPersonIdException;
import com.oocourse.spec3.exceptions.PersonIdNotFoundException;
import com.oocourse.spec3.exceptions.RelationNotFoundException;
import com.oocourse.spec3.main.NetworkInterface;
import com.oocourse.spec3.main.PersonInterface;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static org.junit.Assert.*;

public class NetworkBestAndCoupleSumTest {
    private NetworkInterface network;
    private PersonInterface p1, p2, p3, p4;

    @Before
    public void setUp() throws EqualPersonIdException {
        network = new Network();
        p1 = new Person(1, "Alice", 20);
        p2 = new Person(2, "Bob", 25);
        p3 = new Person(3, "Carol", 30);
        p4 = new Person(4, "Dave", 35);
        network.addPerson(p1);
        network.addPerson(p2);
        network.addPerson(p3);
        network.addPerson(p4);
    }

    // Tests for queryBestAcquaintance
    @Test(expected = PersonIdNotFoundException.class)
    public void testQueryBestAcqPersonNotFound() throws Exception {
        network.queryBestAcquaintance(99);
    }

    @Test(expected = AcquaintanceNotFoundException.class)
    public void testQueryBestAcqNoAcquaintances() throws Exception {
        network.queryBestAcquaintance(1);
    }

    @Test
    public void testQueryBestAcqSingleRelation() throws Exception {
        network.addRelation(1, 2, 5);
        assertEquals(2, network.queryBestAcquaintance(1));
    }

    @Test
    public void testQueryBestAcqHighestValue() throws Exception {
        network.addRelation(1, 2, 5);
        network.addRelation(1, 3, 10);
        network.addRelation(1, 4, 10);
        // Highest value 10, tie between 3 and 4 => choose smaller ID 3
        assertEquals(3, network.queryBestAcquaintance(1));
    }

    @Test
    public void testQueryBestAcqAfterRelationRemoval() throws Exception {
        network.addRelation(1, 2, 8);
        network.addRelation(1, 3, 6);
        network.addRelation(1, 4, 4);
        assertEquals(2, network.queryBestAcquaintance(1));
        network.modifyRelation(1, 2, -8);
        assertEquals(3, network.queryBestAcquaintance(1));
    }

    @Test(expected = RelationNotFoundException.class)
    public void testQueryBestAcqAfterRelationDeletedThrowsRelationNotFoundException() throws Exception {
        network.addRelation(1, 2, 5);
        network.modifyRelation(1, 2, -5);
        network.queryValue(1, 2);
    }

    @Test(expected = AcquaintanceNotFoundException.class)
    public void testQueryBestAcqAfterRelationDeletedThrowsAcquaintanceNotFoundException() throws Exception {
        network.addRelation(1, 2, 5);
        network.modifyRelation(1, 2, -5);
        network.queryBestAcquaintance(1);
    }

    // Tests for queryCoupleSum
    @Test
    public void testQueryCoupleSumEmptyNetwork() {
        NetworkInterface network = new Network();
        assertEquals(0, network.queryCoupleSum());
    }

    @Test
    public void testQueryCoupleSumNoCouples() throws Exception {
        // No relations => no couples
        assertEquals(0, network.queryCoupleSum());

        network.addRelation(1, 2, 5);
        assertEquals(1, network.queryCoupleSum());
        network.modifyRelation(1, 2, -5);
        assertEquals(0, network.queryCoupleSum());
    }

    @Test
    public void testQueryCoupleSumSingleCouple() throws Exception {
        network.addRelation(1, 2, 15);
        network.addRelation(1, 3, 15);
        network.addRelation(2, 3, 15);
        // 1's best is 2, 2's best is 1
        assertEquals(1, network.queryCoupleSum());
    }

    @Test
    public void testQueryCoupleSumWithAddAndRemoval() throws Exception {
        // Couple1: 1<->2 (7)
        // Couple2: 3<->4 (8)
        network.addRelation(1, 2, 7);
        network.addRelation(1, 3, 3);
        network.addRelation(2, 3, 4);
        network.addRelation(3, 4, 8);
        network.addRelation(1, 4, 1);
        // 1<->3 (5) no effect
        network.modifyRelation(1, 3, 2);
        assertEquals(2, network.queryCoupleSum());
        // Couple1: 1<->3 (9)
        network.modifyRelation(1, 3, 4);
        assertEquals(1, network.queryCoupleSum());
        // backup and break relation
        network.modifyRelation(3, 1, -4);
        network.modifyRelation(3, 4, -10);
        assertEquals(1, network.queryCoupleSum());
    }

    @Test
    public void testQueryCoupleSumWithSameValue() throws Exception {
        // Couple1: 1<->4 (10)
        // 2's best is 1, 3's best is 2
        network.addRelation(1, 2, 7);
        network.addRelation(2, 3, 7);
        network.addRelation(1, 4, 10);
        assertEquals(1, network.queryCoupleSum());
        // Couple2: 2<->3 (9)
        network.modifyRelation(2, 3, 2);
        assertEquals(2, network.queryCoupleSum());
    }

    @Test
    public void testQueryCoupleSumNoCouplesAfterModifications() throws Exception {
        network.addRelation(1, 2, 10);
        network.addRelation(3, 4, 9);
        assertEquals(2, network.queryCoupleSum());
        network.modifyRelation(1, 2, -5);
        network.modifyRelation(3, 4, -5);
        assertEquals(2, network.queryCoupleSum());
    }

    @Test
    public void testQueryCoupleSumIsPureThroughCapture() throws Exception {
        network.addRelation(1, 2, 10);
        network.addRelation(1, 3, 5);
        Map<Integer, Map<Integer, Integer>> stateBefore = captureNetworkState(network);
        network.queryCoupleSum();
        Map<Integer, Map<Integer, Integer>> stateAfter = captureNetworkState(network);
        assertEquals(stateBefore.size(), stateAfter.size());
        for (Entry<Integer, Map<Integer, Integer>> entry : stateBefore.entrySet()) {
            int personId = entry.getKey();
            Map<Integer, Integer> before = entry.getValue();
            Map<Integer, Integer> after = stateAfter.get(personId);
            assertNotNull(after);
            assertEquals(before, after);
        }
    }

    private Map<Integer, Map<Integer, Integer>> captureNetworkState(NetworkInterface network) {
        Map<Integer, Map<Integer, Integer>> state = new HashMap<>();
        PersonInterface[] allPersons = ((Network) network).getPersons();
        for (PersonInterface person : allPersons) {
            int id = person.getId();
            Map<Integer, Integer> relationships = new HashMap<>();
            ArrayList<PersonInterface> acquaintances = new ArrayList<>();
            for (PersonInterface other : allPersons) {
                if (person.isLinked(other) && person.getId() != other.getId()) {
                    acquaintances.add(other);
                }
            }
            for (PersonInterface acquaintance : acquaintances) {
                int acquId = acquaintance.getId();
                int value = person.queryValue(acquaintance);
                relationships.put(acquId, value);
            }
            state.put(id, relationships);
        }
        return state;
    }

    @Test
    public void testQueryCoupleSumIsPureThroughCloneNetwork() throws Exception {
        NetworkInterface networkManualClone = new Network();
        PersonInterface p5, p6, p7, p8;
        p5 = new Person(1, "Alice", 20);
        p6 = new Person(2, "Bob", 25);
        p7 = new Person(3, "Carol", 30);
        p8 = new Person(4, "Dave", 35);
        networkManualClone.addPerson(p5);
        networkManualClone.addPerson(p6);
        networkManualClone.addPerson(p7);
        networkManualClone.addPerson(p8);

        network.addRelation(1, 2, 15);
        network.addRelation(1, 3, 15);
        network.addRelation(2, 3, 15);
        networkManualClone.addRelation(1, 2, 15);
        networkManualClone.addRelation(1, 3, 15);
        networkManualClone.addRelation(2, 3, 15);

        PersonInterface[] personsBefore = ((Network) networkManualClone).getPersons();
        network.queryCoupleSum();
        PersonInterface[] personsAfter = ((Network) network).getPersons();
        assertEquals(personsBefore.length, personsAfter.length);
        for (int i = 0; i < personsBefore.length; i++) {
            assertTrue(((Person) personsBefore[i]).strictEquals(personsAfter[i]));
        }
    }
}
