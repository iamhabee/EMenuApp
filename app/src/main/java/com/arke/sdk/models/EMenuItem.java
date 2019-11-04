package com.arke.sdk.models;

import com.arke.sdk.converters.StringListTypeConverter;
import com.arke.sdk.database.EMenuDb;
import com.google.gson.annotations.Expose;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings({"WeakerAccess", "unused"})
@Table(database = EMenuDb.class,
        primaryKeyConflict = ConflictAction.REPLACE,
        insertConflict = ConflictAction.REPLACE,
        updateConflict = ConflictAction.REPLACE)
public class EMenuItem extends BaseModel implements Serializable {

    @PrimaryKey
    @Column
    @Expose
    public String menuItemId;

    @Column
    @Expose
    public String restaurantOrBarName;

    @Column
    @Expose
    public String restaurantOrBarId;

    @Column
    @Expose
    public String menuItemName;

    @Column
    @Expose
    public String menuItemDescription;

    @Column
    @Expose
    public String parentCategory;

    @Column
    @Expose
    public String subParentCategory;

    @Column
    @Expose
    public String menuItemDisplayPhotoUrl;

    @Column
    @Expose
    public long createdAt;

    @Column
    @Expose
    public long updatedAt;

    @Column
    @Expose
    public boolean inStock;

    @Column
    @Expose
    public int quantityAvailableInStock;



    @Column
    @Expose
    public String tableTag;




    @Column
    @Expose
    public String  customerTag;



    @Column
    @Expose
    public String waiterTag;

    @Column
    @Expose
    public String menuItemPrice;

    @Column
    @Expose
    public long favouriteCount;

    @Column
    @Expose
    public long reviewsCount;

    @Column
    @Expose
    public String creatorTag;

    @Column
    @Expose
    public String metaData;

    @Column
    @Expose
    public int metaDataIcon;

    @Column(typeConverter = StringListTypeConverter.class)
    @Expose
    public List<String> emenuItemIngredientsList;

    @Column
    @Expose
    public int orderedQuantity;

    @Column
    @Expose
    public String extrasData;

    public String getTableTag() {
        return tableTag;
    }

    public void setTableTag(String tableTag) {
        this.tableTag = tableTag;
    }

    public String getCustomerTag() {
        return customerTag;
    }

    public void setCustomerTag(String customerTag) {
        this.customerTag = customerTag;
    }

    public String getWaiterTag() {
        return waiterTag;
    }

    public void setWaiterTag(String waiterTag) {
        this.waiterTag = waiterTag;
    }

    public void setMetaData(String metaData) {
        this.metaData = metaData;
    }

    public void setQuantityAvailableInStock(int quantityAvailableInStock) {
        this.quantityAvailableInStock = quantityAvailableInStock;
    }

    public int getQuantityAvailableInStock() {
        return quantityAvailableInStock;
    }

    public void setMetaDataIcon(int metaDataIcon) {
        this.metaDataIcon = metaDataIcon;
    }

    public String getMetaData() {
        return metaData;
    }

    public int getMetaDataIcon() {
        return metaDataIcon;
    }

    public void setExtrasData(String extrasData) {
        this.extrasData = extrasData;
    }

    public String getExtrasData() {
        return extrasData;
    }

    public void setRestaurantOrBarName(String restaurantOrBarName) {
        this.restaurantOrBarName = restaurantOrBarName;
    }

    public void setRestaurantOrBarId(String restaurantOrBarId) {
        this.restaurantOrBarId = restaurantOrBarId;
    }

    public String getRestaurantOrBarId() {
        return restaurantOrBarId;
    }

    public String getRestaurantOrBarName() {
        return restaurantOrBarName;
    }

    public void setMenuItemId(String menuItemId) {
        this.menuItemId = menuItemId;
    }

    public String getMenuItemId() {
        return menuItemId;
    }

    public void setCreatorTag(String creatorTag) {
        this.creatorTag = creatorTag;
    }

    public String getCreatorTag() {
        return creatorTag;
    }

    public String getMenuItemName() {
        return menuItemName;
    }

    public void setMenuItemName(String menuItemName) {
        this.menuItemName = menuItemName;
    }

    public String getMenuItemDescription() {
        return menuItemDescription;
    }

    public void setMenuItemDescription(String menuItemDescription) {
        this.menuItemDescription = menuItemDescription;
    }

    public String getParentCategory() {
        return parentCategory;
    }

    public void setParentCategory(String parentCategory) {
        this.parentCategory = parentCategory;
    }

    public String getSubParentCategory() {
        return subParentCategory;
    }

    public void setSubParentCategory(String subParentCategory) {
        this.subParentCategory = subParentCategory;
    }

    public String getMenuItemDisplayPhotoUrl() {
        return menuItemDisplayPhotoUrl;
    }

    public void setMenuItemDisplayPhotoUrl(String menuItemDisplayPhotoUrl) {
        this.menuItemDisplayPhotoUrl = menuItemDisplayPhotoUrl;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isInStock() {
        return inStock;
    }

    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }

    public String getMenuItemPrice() {
        return menuItemPrice;
    }

    public void setMenuItemPrice(String menuItemPrice) {
        this.menuItemPrice = menuItemPrice;
    }

    public long getFavouriteCount() {
        return favouriteCount;
    }

    public void setFavouriteCount(long favouriteCount) {
        this.favouriteCount = favouriteCount;
    }

    public long getReviewsCount() {
        return reviewsCount;
    }

    public void setOrderedQuantity(int orderedQuantity) {
        this.orderedQuantity = orderedQuantity;
    }

    public int getOrderedQuantity() {
        return orderedQuantity;
    }

    public void setReviewsCount(long reviewsCount) {
        this.reviewsCount = reviewsCount;
    }

    public void setMenuItemIngredientsList(List<String> emenuItemIngredientsList) {
        this.emenuItemIngredientsList = emenuItemIngredientsList;
    }

    public List<String> getEmenuItemIngredientsList() {
        return emenuItemIngredientsList;
    }

    @Override
    public int hashCode() {
        int result;
        result = this.menuItemId.hashCode();
        final String name = getClass().getName();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        EMenuItem another = (EMenuItem) obj;
        return this.getMenuItemId().equals(another.getMenuItemId());
    }

}
