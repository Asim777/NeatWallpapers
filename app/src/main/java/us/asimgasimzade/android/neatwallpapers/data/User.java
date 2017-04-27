package us.asimgasimzade.android.neatwallpapers.data;

/**
 * This class holds User info
 */

public class User {

    private String username;
    private String fullname;
    private String email;
    private String profilePicture;

    public User(String fullname, String email, String profilePicture) {
        this.fullname = fullname;
        this.email = email;
        this.profilePicture = profilePicture;
    }

    // Default constructor required for calls to
    // DataSnapshot.getValue(User.class)
    public User() {

    }

    public String getUsername() {
        return username;
    }

    public String getFullname() {
        return fullname;
    }

    public String getEmail() {
        return email;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

}
