import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Users implements Location {
    private final Map<String, User> users = new HashMap<>();

    User getUser(String id) {
        if (!users.containsKey(id)) {
            users.put(id, new User(id));
        }
        return users.get(id);
    }

    Set<String> getUserIds() {
        return users.keySet();
    }
}
