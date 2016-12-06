package com.asimqasimzade.android.neatwallpapers;


public class Category {
    private String categoryName;
    private int categoryThumbnail;

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
}

