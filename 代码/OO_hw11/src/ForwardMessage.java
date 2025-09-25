import com.oocourse.spec3.main.ForwardMessageInterface;
import com.oocourse.spec3.main.PersonInterface;
import com.oocourse.spec3.main.TagInterface;

import static java.lang.Math.abs;

public class ForwardMessage extends Message implements ForwardMessageInterface {
    private final int articldId;

    public ForwardMessage(int messageId, int article,
        PersonInterface messagePerson1, PersonInterface messagePerson2) {
        super(messageId, abs(article) % 200,messagePerson1,messagePerson2);
        articldId = article;
    }

    public ForwardMessage(int messageId, int article,
        PersonInterface messagePerson1, TagInterface messageTag) {
        super(messageId, abs(article) % 200,messagePerson1,messageTag);
        articldId = article;
    }

    @Override
    public int getArticleId() {
        return articldId;
    }

    @Override
    public void send() {
        super.send();
        if (getType() == 0) {
            ((Person)getPerson2()).receiveArticle(articldId);
        } else if (getType() == 1) {
            ((Tag)getTag()).receiveArticle(articldId);
        }
    }
}
