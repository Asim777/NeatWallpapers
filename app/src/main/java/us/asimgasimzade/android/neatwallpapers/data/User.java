package us.asimgasimzade.android.neatwallpapers.data;

/**
 * Created by asim on 4/3/17.
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
    public User(){

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }
}
