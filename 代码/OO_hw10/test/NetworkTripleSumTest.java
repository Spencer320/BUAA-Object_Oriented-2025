import com.oocourse.spec2.exceptions.EqualPersonIdException;
import com.oocourse.spec2.main.NetworkInterface;
import com.oocourse.spec2.main.PersonInterface;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NetworkTripleSumTest {
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

    @Test
    public void testInitialTripleSumZero() {
        assertEquals(0, network.queryTripleSum());
    }

    @Test
    public void testSingleTriangleFormation() throws Exception {
        network.addRelation(1, 2, 1);
        network.addRelation(2, 3, 1);
        // No triangle yet
        assertEquals(0, network.queryTripleSum());
        network.addRelation(3, 1, 1);
        // One triangle
        assertEquals(1, network.queryTripleSum());
    }

    @Test
    public void testMultipleTrianglesCompleteGraph() throws Exception {
        // Build complete graph K4
        network.addRelation(1, 2, 1);
        network.addRelation(1, 3, 1);
        network.addRelation(1, 4, 1);
        network.addRelation(2, 3, 1);
        network.addRelation(2, 4, 1);
        network.addRelation(3, 4, 1);
        // K4 has 4 triangles
        assertEquals(4, network.queryTripleSum());
    }

    @Test
    public void testAddEdgeCreatesTwoTriangles() throws Exception {
        // Create a square with diagonal missing one diagonal
        network.addRelation(1, 2, 1);
        network.addRelation(1, 3, 1);
        network.addRelation(2, 4, 1);
        network.addRelation(3, 4, 1);
        assertEquals(0, network.queryTripleSum());
        // Add diagonal between 1 and 4: should create two triangles (1-2-4 and 1-3-4)
        network.addRelation(1, 4, 1);
        assertEquals(2, network.queryTripleSum());
    }

    @Test
    public void testEdgeRemovalDecreasesTriangles() throws Exception {
        // Form a triangle 1-2-3
        network.addRelation(1, 2, 1);
        network.addRelation(2, 3, 1);
        network.addRelation(3, 1, 1);
        assertEquals(1, network.queryTripleSum());
        // Remove edge by decreasing weight to zero
        network.modifyRelation(1, 2, - 1);
        assertEquals(0, network.queryTripleSum());
    }
}
