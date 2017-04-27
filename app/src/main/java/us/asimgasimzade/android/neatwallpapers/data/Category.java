package us.asimgasimzade.android.neatwallpapers.data;

/**
 * Data model class for Category item
 */

public class Category {
    private String categoryName;
    private int categoryThumbnail;
    private String categoryKeyword;

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

    public String getCategoryKeyword() {
        return categoryKeyword;
    }

    public void setCategoryKeyword(String getCategoryApiName) {
        this.categoryKeyword = getCategoryApiName;
    }

}

