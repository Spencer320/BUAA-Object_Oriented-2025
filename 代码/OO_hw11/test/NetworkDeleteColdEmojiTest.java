import com.oocourse.spec3.exceptions.EmojiIdNotFoundException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class NetworkDeleteColdEmojiTest {
    private Network network;
    private Person person1;
    private Person person2;
    private static int count = 0;

    @Before
    public void setUp() throws Exception {
        network = new Network();
        person1 = new Person(100, "Alice", 20);
        person2 = new Person(200, "Bob", 21);
        network.addPerson(person1);
        network.addPerson(person2);
        network.addRelation(person1.getId(), person2.getId(), 1);
    }

    @Test
    public void testEmptyList() {
        assertEquals(0, network.deleteColdEmoji(0));
        assertEquals(0, network.deleteColdEmoji(100));
        assertEquals(0, network.deleteColdEmoji(-5));
        assertTrue(network.getMessages().length == 0);
    }

    @Test
    public void testLimitZeroKeepsAll() throws Exception {
        addEmojis(
            new int[]{101, 102, 103},
            new int[]{0, 5, 10}
        );
        EmojiMessage msg = new EmojiMessage(
            count,
            101,
            person1,
            person2
        );
        count++;
        network.addMessage(msg);
        int remain = network.deleteColdEmoji(0);
        assertEquals(3, remain);
        for (int id : new int[]{101, 102, 103}) {
            assertTrue(network.containsEmojiId(id));
        }
        assertTrue(network.containsMessage(count - 1));
    }

    @Test
    public void testNegativeLimitKeepsAll() throws Exception {
        addEmojis(
            new int[]{11, 22},
            new int[]{3, 7}
        );
        int remain = network.deleteColdEmoji(-1);
        assertEquals(2, remain);
        assertTrue(network.containsEmojiId(11));
        assertTrue(network.containsEmojiId(22));
    }

    @Test
    public void testLargeLimitRemovesAll() throws Exception {
        addEmojis(
            new int[]{5, 6, 7},
            new int[]{1, 2, 3}
        );
        EmojiMessage msg = new EmojiMessage(
            count,
            6,
            person1,
            person2
        );
        count++;
        network.addMessage(msg);
        int remain = network.deleteColdEmoji(10);
        assertEquals(0, remain);
        for (int id : new int[]{5, 6, 7}) {
            assertFalse(network.containsEmojiId(id));
        }
        assertFalse(network.containsMessage(count - 1));
    }

    @Test
    public void testMixedRemovals() throws Exception {
        addEmojis(
            new int[]{1, 2, 3, 4, 5},
            new int[]{0, 5, 10, 4, 5}
        );
        int remain = network.deleteColdEmoji(5);
        assertEquals(3, remain);

        for (int keep : new int[]{2, 3, 5}) {
            assertTrue(network.containsEmojiId(keep));
            assertEquals((int) (keep == 3 ? 10 : 5),
                network.queryPopularity(keep));
        }
        for (int rem : new int[]{1, 4}) {
            assertFalse(network.containsEmojiId(rem));
            try {
                network.queryPopularity(rem);
            } catch (EmojiIdNotFoundException ignored) {
            }
        }
    }

    private void addEmojis(int[] ids, int[] heats) throws Exception {
        for (int i = 0; i < ids.length; i++) {
            bumpHeat(ids[i],heats[i]);
        }
    }

    private void bumpHeat(int emojiId, int times) throws Exception {
        network.storeEmojiId(emojiId);

        for (int i = 0; i < times; i++) {
            int messageId = count;
            count++;
            EmojiMessage msg = new EmojiMessage(
                messageId,
                emojiId,
                person1,
                person2
            );
            network.addMessage(msg);
            network.sendMessage(messageId);
        }
    }
}