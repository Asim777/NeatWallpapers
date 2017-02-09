package us.asimgasimzade.android.neatwallpapers.Data;

/**
 * Data model class for Category item
 */

public class Category {
    private String categoryName;
    private int categoryThumbnail;
    private String categoryApiName;

    public int getCategoryThumbnail() {
        return categoryThumbnail;
    }

    public void setCategoryThumbnail(int categoryImage) {
        this.categoryThumbnail = categoryImage;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryApiName() {
        return categoryApiName;
    }

    public void setCategoryApiName(String getCategoryApiName) {
        this.categoryApiName = getCategoryApiName;
    }

}

