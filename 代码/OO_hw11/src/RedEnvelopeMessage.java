import com.oocourse.spec3.main.PersonInterface;
import com.oocourse.spec3.main.RedEnvelopeMessageInterface;
import com.oocourse.spec3.main.TagInterface;

public class RedEnvelopeMessage extends Message implements RedEnvelopeMessageInterface {
    private final int money;

    public RedEnvelopeMessage(int messageId, int luckyMoney,
        PersonInterface messagePerson1, PersonInterface messagePerson2) {
        super(messageId, luckyMoney * 5, messagePerson1, messagePerson2);
        money = luckyMoney;
    }

    public RedEnvelopeMessage(int messageId, int luckyMoney,
        PersonInterface messagePerson1, TagInterface messageTag) {
        super(messageId, luckyMoney * 5, messagePerson1, messageTag);
        money = luckyMoney;
    }

    @Override
    public int getMoney() {
        return money;
    }

    @Override
    public void send() {
        super.send();
        if (getType() == 0) {
            getPerson1().addMoney(-money);
            getPerson2().addMoney(money);
        } else if (getType() == 1) {
            int size = getTag().getSize();
            if (size != 0) {
                int moneyPerPerson = money / size;
                getPerson1().addMoney(-moneyPerPerson * size);
                ((Tag) getTag()).addMoney(moneyPerPerson);
            }
        }
    }
}
