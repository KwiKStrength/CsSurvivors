package Class;

public class Customers {
    private int userID;
    private String username;
    private String password;
    private String email;
    private String role;
    private String HWID;

    public Customers(int userID, String username, String password, String email, String role, String HWID) {
        this.userID = userID;
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.HWID = HWID;
    }

    public Customers(int userid, String username, String email) {
        this.userID = userid;
        this.username = username;
        this.email = email;
    }

    public Customers(int userId, String username, String email, String role) {
        this.userID=  userId;
        this.username = username;
        this.email = email;
        this.role = role;

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
