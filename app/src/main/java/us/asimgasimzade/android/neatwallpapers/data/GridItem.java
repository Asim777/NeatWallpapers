package us.asimgasimzade.android.neatwallpapers.data;

/**
 * Data model class for GridItem
 */

public class GridItem {


    private String image;
    private String name;
    private String author;
    private String link;
    private String thumbnail;
    private int number;

    public GridItem(String image, String name, String author, String link, String thumbnail) {
        this.image = image;
        this.name = name;
        this.author = author;
        this.link = link;
        this.thumbnail = thumbnail;
    }

    // Default constructor required for calls to
    // DataSnapshot.getValue(User.class)
    public GridItem() {

    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getLink() {
        return link;
    }

    public String getAuthor() {
        return author;
    }

    public String getName() {
        return name;
    }

    public int getNumber() {
        return number;
    }

    public String getImage() {
        return image;
    }

    public void setNumber(int number) {
        this.number = number;
    }

}