import java.io.Serializable;

public class User implements Serializable {
    private final String username;  // 用户名
    private final String password;  // 用户密码

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }
}
