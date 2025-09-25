import com.oocourse.library2.LibraryBookState;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Users implements Location {
    private final Map<String, User> users = new HashMap<>();

    public Users() {
    }

    public User getUser(String id) {
        if (!users.containsKey(id)) {
            users.put(id, new User(id));
        }
        return users.get(id);
    }

    public Set<String> getUserIds() {
        return users.keySet();
    }

    @Override
    public LibraryBookState getAssociatedState() {
        return LibraryBookState.USER;
    }
}
