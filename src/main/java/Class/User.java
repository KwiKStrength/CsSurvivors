package Class;


public class User {
    private final int userID;
    private final String username;
    private final String password;
    private final String email;
    private final String role;
    private final String HWID;

    public User(int userID, String username, String password, String email, String role, String HWID) {
        this.userID = userID;
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.HWID = HWID;
    }

    public int getUserID() {
        return userID;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getHWID() {
        return HWID;
    }
}
