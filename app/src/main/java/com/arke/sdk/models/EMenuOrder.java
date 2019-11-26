package com.arke.sdk.models;

import androidx.annotation.Nullable;

import com.arke.sdk.converters.EMenuCustomerOrderedItemsTypeConverter;
import com.arke.sdk.converters.OrderPaymentStatusTypeConverter;
import com.arke.sdk.converters.OrderProgressStatusTypeConverter;
import com.arke.sdk.eventbuses.OrderUpdatedEvent;
import com.arke.sdk.companions.Globals;
import com.arke.sdk.database.EMenuDb;
import com.google.gson.annotations.Expose;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings({"WeakerAccess", "unused", "NullableProblems"})
@Table(database = EMenuDb.class,
        primaryKeyConflict = ConflictAction.REPLACE,
        insertConflict = ConflictAction.REPLACE,
        updateConflict = ConflictAction.REPLACE)
public class EMenuOrder extends BaseModel implements Serializable {

    public boolean kitchen_rejected;

    public boolean bar_rejected;

    /**
     * A unique id for this order
     **/
    @PrimaryKey
    @Column
    @Expose
    public String orderId;


    /**
     * A loosely provided tag(name) to identify the table where this order was taken
     **/
    @Column
    @Expose
    public String tableTag;

    /**
     * A loosely provided tag(name) to identify the waiter that is taking this order
     **/
    @Column
    @Expose
    public String waiterTag;

    /**
     * A loosely provided tag(name) to identify the customer that owns this order
     **/
    @Column
    @Expose
    public String customerTag;

    /**
     * This is the unique id of the restaurant or bar where the order is made.
     * This is better than using the email address as a unique identifier
     * because the email address is bound to change or prone
     * to updates
     **/
    @Column
    @Expose
    public String restaurantOrBarId;

    /**
     * If this order contains food items, this holds the tag(name) of the kitchen attendant
     * who is taking this order
     */
    @Column
    @Expose
    public String kitchenAttendantTag;

    /**
     * If this order contains drink items, this holds the tag(name) of the bar attendant
     * who is taking this order
     **/
    @Column
    @Expose
    public String barAttendantTag;

    /**
     * Holds the time stamp when this order was created
     **/
    @Column
    @Expose
    public long createdAt;

    /**
     * Holds the last time stamp this order was updated
     */
    @Column
    @Expose
    public long updatedAt;



    /**
     * If this order contains food items, this holds the unique device id of the kitchen attendant
     * who is processing this order
     */
    @Column
    @Expose
    public String kitchenAttendantDeviceId;

    /**
     * If this order contains drinks, this holds the unique device id of the bar
     * attendant who is processing this order
     */
    @Column
    @Expose
    public String barAttendantDeviceId;

    /**
     * Tracks the payment status of this order
     * Must be one of:
     * PAID_BY_CASH,
     * PAID_BY_TRANSFER,
     * PAID_BY_CARD,
     **/
    @Nullable
    @Column(typeConverter = OrderPaymentStatusTypeConverter.class)
    @Expose
    public Globals.OrderPaymentStatus orderPaymentStatus;

    /**
     * This tracks the progress of the order
     * The Kitchen or Bar updates this progress for the waiter to see and inform the customer accordingly
     **/
    @Nullable
    @Column(typeConverter = OrderProgressStatusTypeConverter.class)
    @Expose
    public Globals.OrderProgressStatus orderProgressStatus;

    /**
     * These are the items that the customer has requested for
     * They could be both food and drinks
     **/
    @Column(typeConverter = EMenuCustomerOrderedItemsTypeConverter.class)
    @Expose
    public List<EMenuItem> items;

    /**
     * This is the unique device id of the waiter that is taking the order
     * This device id is programmatically retrieved from the device and not explicitly supplied
     **/
    @Column
    @Expose
    public String waiterDeviceId;

    /*
     * This determines if the customer has requested for new items or the existing items have not
     * yet being sent to the kitchen or bar
     * **/
    @Column
    @Expose
    boolean dirty = false;

    public void setBarAttendantTag(String barAttendantTag) {
        this.barAttendantTag = barAttendantTag;
    }

    public String getBarAttendantTag() {
        return barAttendantTag;
    }


    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public String getEMenuOrderId() {
        return orderId;
    }

    public void setEMenuOrderId(String eMenuOrderId) {
        this.orderId = eMenuOrderId;
    }

    public String getTableTag() {
        return tableTag;
    }

    public void setTableTag(String tableTag) {
        this.tableTag = tableTag;
    }

    public String getWaiterTag() {
        return waiterTag;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setWaiterTag(String waiterTag) {
        this.waiterTag = waiterTag;
    }

    public void setCustomerTag(String customerTag) {
        this.customerTag = customerTag;
    }

    public String getCustomerTag() {
        return customerTag;
    }

    public void setItems(List<EMenuItem> items) {
        this.items = items;
    }

    public List<EMenuItem> getItems() {
        return items;
    }

    public String getKitchenAttendantTag() {
        return kitchenAttendantTag;
    }

    public void setKitchenAttendantTag(String kitchenAttendantTag) {
        this.kitchenAttendantTag = kitchenAttendantTag;
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

    public void setOrderPaymentStatus(Globals.OrderPaymentStatus orderPaymentStatus) {
        this.orderPaymentStatus = orderPaymentStatus;
    }

    @Nullable
    public Globals.OrderPaymentStatus getOrderPaymentStatus() {
        return orderPaymentStatus;
    }

    public void setOrderProgressStatus(Globals.OrderProgressStatus orderProgressStatus) {
        this.orderProgressStatus = orderProgressStatus;
    }

    public Globals.OrderProgressStatus getOrderProgressStatus() {
        return orderProgressStatus;
    }

    public void setRestaurantOrBarId(String restaurantEmailAddress) {
        this.restaurantOrBarId = restaurantEmailAddress;
    }

    public String getRestaurantOrBarId() {
        return restaurantOrBarId;
    }

    public void setWaiterDeviceId(String waiterDeviceId) {
        this.waiterDeviceId = waiterDeviceId;
    }

    public String getWaiterDeviceId() {
        return waiterDeviceId;
    }

    public void setKitchenAttendantDeviceId(String kitchenAttendantDeviceId) {
        this.kitchenAttendantDeviceId = kitchenAttendantDeviceId;
    }

    public String getKitchenAttendantDeviceId() {
        return kitchenAttendantDeviceId;
    }

    public void setBarAttendantDeviceId(String barAttendantDeviceId) {
        this.barAttendantDeviceId = barAttendantDeviceId;
    }

    public String getBarAttendantDeviceId() {
        return barAttendantDeviceId;
    }

    public boolean isKitchen_rejected() {
        return kitchen_rejected;
    }

    public void setKitchen_rejected(boolean kitchen_rejected) {
        this.kitchen_rejected = kitchen_rejected;
    }

    public boolean isBar_rejected() {
        return bar_rejected;
    }

    public void setBar_rejected(boolean bar_rejected) {
        this.bar_rejected = bar_rejected;
    }

    @Override
    public int hashCode() {
        int result;
        result = this.orderId.hashCode();
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
        EMenuOrder another = (EMenuOrder) obj;
        return this.getEMenuOrderId().equals(another.getEMenuOrderId());
    }

    @Override
    public boolean update() {
        boolean result = super.update();
        EventBus.getDefault().post(new OrderUpdatedEvent(this, false));
        return result;
    }

    @Override
    public boolean delete() {
        boolean deleted = super.delete();
        EventBus.getDefault().post(new OrderUpdatedEvent(this, true));
        return deleted;
    }

}
