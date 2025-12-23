package application.models;

public class Account {
    private final String id;
    private final String username;
    private final String password;
    private final String role;
    private final String created;

    public Account(String id, String username, String password, String role, String created) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.created = created;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public String getCreated() {
        return created;
    }
}
