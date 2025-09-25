import com.oocourse.spec3.main.MessageInterface;
import com.oocourse.spec3.main.PersonInterface;
import com.oocourse.spec3.main.TagInterface;

public class Message implements MessageInterface {
    private final int id;
    private final int socialValue;
    private final int type;
    private final PersonInterface person1;
    private final PersonInterface person2;
    private final TagInterface tag;

    public Message(int messageId, int messageSocialValue,
        PersonInterface messagePerson1, PersonInterface messagePerson2) {
        type = 0;
        tag = null;
        id = messageId;
        socialValue = messageSocialValue;
        person1 = messagePerson1;
        person2 = messagePerson2;
    }

    public Message(int messageId, int messageSocialValue,
        PersonInterface messagePerson1, TagInterface messageTag) {
        type = 1;
        person2 = null;
        id = messageId;
        socialValue = messageSocialValue;
        person1 = messagePerson1;
        tag = messageTag;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getSocialValue() {
        return socialValue;
    }

    @Override
    public PersonInterface getPerson1() {
        return person1;
    }

    @Override
    public PersonInterface getPerson2() {
        return person2;
    }

    @Override
    public TagInterface getTag() {
        return tag;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof MessageInterface)) {
            return false;
        }
        MessageInterface otherMessage = (MessageInterface) obj;
        return otherMessage.getId() == this.id;
    }

    public void send() {
        if (type == 0) {
            person1.addSocialValue(socialValue);
            person2.addSocialValue(socialValue);
            ((Person)person2).receiveMessage(this);
        } else if (type == 1) {
            person1.addSocialValue(socialValue);
            ((Tag) tag).addSocialValue(socialValue);
            ((Tag) tag).receiveMessage(this);
        }
    }
}
