package us.asimgasimzade.android.neatwallpapers.Data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Data model class for GridItem
 */

public class GridItem {


    private String image;
    private String name;
    private String author;
    private String link;
    private int number;


    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

     /*   // This is where you write the values you want to save to the `Parcel`.
    // The `Parcel` class has methods defined to help you save all of your values.
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(image);
        dest.writeString(name);
        dest.writeString(author);
        dest.writeString(link);
        dest.writeInt(number);
    }

    // Using the `in` variable, we can retrieve the values that
    // we originally wrote into the `Parcel`.  This constructor is usually
    // private so that only the `CREATOR` field can access.
    private GridItem(Parcel in){
        image = in.readString();
        name = in.readString();
        author = in.readString();
        link = in.readString();
        number = in.readInt();
    }

    // In the vast majority of cases you can simply return 0 for this.
    @Override
    public int describeContents() {
        return 0;
    }

    // After implementing the `Parcelable` interface, we need to create the
    // `Parcelable.Creator<GridItem> CREATOR` constant for our class;
    public static final Parcelable.Creator<GridItem> CREATOR = new Parcelable.Creator<GridItem>() {
        // This simply calls our new constructor (typically private) and
        // passes along the unmarshalled `Parcel`, and then returns the new object!
        @Override
        public GridItem createFromParcel(Parcel source) {
            return new GridItem(source);
        }

        //This just has to be here
        @Override
        public GridItem[] newArray(int size) {
            return new GridItem[0];
        }
    };*/
}
