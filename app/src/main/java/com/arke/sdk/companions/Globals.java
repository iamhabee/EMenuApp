package com.arke.sdk.companions;

import com.arke.sdk.models.EMenuItem;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Globals {
    public static final SimpleDateFormat DATE_FORMATTER_IN_YEARS = new SimpleDateFormat("yyyy", Locale.getDefault());
    public static final SimpleDateFormat DATE_FORMATTER_IN_BIRTHDAY_FORMAT = new SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault());
    public static final SimpleDateFormat FORWARD_SLASH_DATE_FORMAT = new SimpleDateFormat("d/MM/yyyy", Locale.getDefault());
    public static final SimpleDateFormat MONTH_AND_DATE_FORMAT = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
    public static final SimpleDateFormat DATE_FORMATTER_IN_12HRS = new SimpleDateFormat("h:mm a", Locale.getDefault());
    public static final String USE_TYPE = "emenu_use_type";
    public static final String ADMIN_TAG = "admin";
    public static final int ADMIN_TAG_ID = 263389;
    public static final int WAITER = 0;
    public static final int BAR = 3;
    public static final int KITCHEN = 2;
    public static final String DEFAULT_PWD = "12345";
    public static final String RESTAURANT_OR_BAR_NAME = "emenu_restaurant_or_bar_name";
    public static final String IS_APP_SETUP = "is_app_setup";
    public static final String RESTAURANT_OR_BAR_EMAIL_ADDRESS = "emenu_restaurant_or_bar_email_address";
    public static final String RESTAURANT_OR_BAR_PASSWORD = "emenu_restaurant_or_bar_password";
    public static final String RESTAURANT_OR_BAR_REVEALED_PASSWORD = "emenu_restaurant_or_bar_password_revealed";
    public static final String RESTAURANTS_AND_BARS = "EMenuRestaurantsAndBars";
    public static final String EMenuItems = "EMenuItems";
    public static final String DESTINATION_ID = "destination_id";
    public static final String PREVIOUS_PASSWORD = "previous_password";
    public static final String EMENU_ITEM_PHOTO_URL = "emenu_item_photo_url";
    public static final String EMENU_ITEM_NAME = "emenu_item_name";
    public static final String EMENU_ITEM_DESCRIPTION = "emenu_item_description";
    public static final String EMENU_ITEM_INGREDIENTS_LIST = "emenu_items_ingredients_list";
    public static final String EMENU_ITEM_PARENT_CATEGORY = "emenu_item_parent_category";
    public static final String EMENU_ITEM_SUB_PARENT_CATEGORY = "emenu_item_sub_parent_category";
    public static final String IN_STOCK = "in_stock";
    public static final String EMENU_ITEM_PRICE = "emenu_item_price";
    public static final String EMENU_ITEM_CREATOR_TAG = "emenu_item_creator_tag";
    public static final String EMENU_ITEMS_CATEGORIES = "EMenuItemsCategories";
    public static final String EMENU_ITEM_CATEGORY = "emenu_item_category";
    public static final String RESTAURANT_EMENU_MAXIMUM_QUANTITY_ORDER = "emenu_maximum_quantity_order";
    public static final String EMENU_ORDERS = "EMenuOrders";
    public static final String HOST_CONTEXT_NAME = "host_context_name";
    public static final String NOTIFICATIONS = "EMenuNotifications";
    public static final int NEW_MESSAGE_NOTIFICATION_ID = 0x8;
    public static final String ORDER_ID = "order_id";
    public static final String NOTIF_COUNT = "notif_count";
    public static final String EMENU_ORDER_JSON = "emenu_order_json";
    public static final String EMENU_ORDER = "emenu_order_view";
    public static final String DRINKS = "drinks";
    public static final String MEALS = "meals";
    public static final String EMENU_ITEM_SUB_CATEGORY = "emenu_item_sub_category";
    public static final String NON_DRINKS = "non_drinks";
    public static final String WAITER_HOME_CONTENTS_CACHE = "waiter_home_contents_cache";
    public static final String MENU_CATEGORY_PHOTO_URL = "menu_category_photo_url";
    public static final String EMENU_ITEM_AND_HOST = "emenu_item_and_host";
    public static final String EDITABLE_EMENU_ITEM = "editable_emenu_item";
    public static final String AVAILABLE_KITCHEN_CONTENTS_CACHE = "available_kitchen_contents_cache";
    public static final String EMENU_CATEGORIES_CACHE = "emenu_categories_cache";
    public static final String DRINKS_CACHE = "drinks_cache";
    public static final String WAITER_TAG = "waiter_tag";
    public static final String KITCHEN_TAG = "kitchen_tag";
    public static final String BAR_TAG = "bar_tag";
    public static final String BAR_CONTENTS_CACHE = "bar_contents_cache";
    public static final String RESTAURANT_OR_BAR_PROFILE_PHOTO_URL = "restaurant_or_bar_profile_photo_url";
    public static final String RESTAURANT_OR_BAR_COVER_PHOTO_URL = "restaurant_or_bar_cover_photo_url";
    public static final String RESTAURANT_OR_BAR_PRIMARY_COLOR = "restaurant_or_bar_primary_color";
    public static final String RESTAURANT_OR_BAR_SECONDARY_COLOR = "restaurant_or_bar_secondary_color";
    public static final String TAKEN_BY_KITCHEN = "taken_by_kitchen";
    public static final String TAKEN_BY_BAR = "taken_by_bar";
    public static final String RESTAURANT_OR_BAR_ID = "restaurant_or_bar_id";
    public static final String RESTAURANT_OR_BAR_TERTIARY_COLOR = "restaurant_or_bar_tertiary_color";
    public static final String INVALIDATE_SETTINGS = "invalidate_settings";
    public static final String INCOMING_NOTIFICATION_RINGTONE_URI = "incoming_notification_ringtone_uri";
    public static final String NOTIFICATION_TYPE = "notification_type";
    public static final String EMENU_ORDER_NOTIFICATION = "emenu_order_notification";
    public static final String EMENU_ITEM_NOTIFICATION = "emenu_item_notification";
    public static final String NOTIFICATION_DATA = "notification_data";
    public static final String UPDATE_TYPE = "update_type";
    public static final String UPDATE_TYPE_NEW_INSERTION = "new_insertion";
    public static final String UPDATE_TYPE_UPDATE = "update";
    public static final String RESTAURANT_OR_BAR_ACCOUNT_DETAILS = "restaurant_or_bar_account_details";
    public static final String DELETED = "deleted";
    public static final String RESTAURANT_OR_BAR_ADMIN_PASSWORD = "restaurant_or_bar_admin_password";
    public static final String DISPLAY_ADMIN_SETTINGS = "display_admin_settings";
    public static final String PASSWORD_UPDATE_TYPE_ADMIN = "password_update_type_admin";
    public static final int NEW_PAYMENT_REQUEST_CODE = 0x20;
    public static final String CURRENT_CARD_DATA = "current_card_data";
    public static final String APP_TOURED = "app_toured";
    public static final String QTY_IN_STOCK = "quantity_in_stock";
    public static final String WAITERS = "waiters";
    public static final String SEARCHABLE_TAG = "searchable_tag";
    public static final String CUSTOMER_TAG = "customer_tag";
    public static final String KITCHEN_ATTENDANT_TAG = "kitchen_attendant_tag";
    public static final String BAR_ATTENDANT_TAG = "bar_attendant_tag";
    public static final String KITCHEN_ATTENDANT_DEVICE_ID = "kitchen_attendant_device_id";
    public static final String KITCHEN_ATTENDANT_ID = "kitchen_attendant_id";
    public static final String BAR_ATTENDANT_DEVICE_ID = "bar_attendant_device_id";
    public static final String BAR_ATTENDANT_ID = "bar_attendant_id";
    public static final String ORDER_PROGRESS_STATUS = "order_progress_status";
    public static final String ORDER_PAYMENT_STATUS = "order_payment_status";
    public static final String ORDERED_ITEMS = "ordered_items";
    public static final String HAS_DRINK = "has_drink";
    public static final String HAS_FOOD = "has_food";
    public static final String FOOD_READY = "food_ready";
    public static final String DRINK_READY = "drink_ready";
    public static final String WAITER_DEVICE_ID = "waiter_device_id";
    public static final String TABLE_TAG = "table_tag";
    public static final String CREATED_AT = "createdAt";
    public static final String UPDATED_AT = "updatedAt";
    public static final String ADMIN_DEVICE_ID = "admin_device_id";
    public static final String TAKE_AWAY_TABLE_TAG = "Take Away";
    public static final String IS_TAKE_AWAY = "is_take_away";
    public static final String RESTAURANT_OR_BAR_ADMIN_PASSWORD_REVEALED = "restaurant_or_bar_admin_password_revealed";
    public static String APP_PREFS_NAME = "elitepath_emenu";
    public static String EMPTY_PLACEHOLDER_ERROR_MESSAGE = "Nothing Available";
    public static boolean newMenuItemCreated = false;
    public static boolean emenuItemUpdated = false;
    public static EMenuItem updatedEMenuItem = null;
    public static boolean unProcessedOrdersPushed = false;


    public enum StatusPage {
        LOADING_VIEW,
        EMPTY_VIEW,
        OTHER_ERROR_VIEW,
        NETWORK_ERROR_VIEW,
        NON_EMPTY_VIEW
    }

    public static class ModifiableColor {
        public enum ColorType {
            PRIMARY,
            SECONDARY,
            TERTIARY
        }

        int color;
        ColorType colorType;

        public ModifiableColor(ColorType colorType, int color) {
            this.colorType = colorType;
            this.color = color;
        }

        public int getColor() {
            return color;
        }

        public ColorType getColorType() {
            return colorType;
        }

    }

    public enum UseType {
        USE_TYPE_NONE,
        USE_TYPE_WAITER,
        USE_TYPE_KITCHEN,
        USE_TYPE_BAR,
        USE_TYPE_ADMIN,
    }

    public enum OrderType {
        NON_DRINKS,
        DRINKS
    }

    public enum AuthFormStepType {
        STEP_TYPE_TEXT,
        STEP_TYPE_EMAIL,
        STEP_TYPE_PASSWORD,
        STEP_TYPE_REPEAT_PASSWORD
    }

    public enum OrderPaymentStatus {
        PAID_BY_CASH,
        PAID_BY_TRANSFER,
        PAID_BY_CARD,
    }

    public enum OrderProgressStatus {
        NOT_YET_SENT,
        PENDING,
        PROCESSING,
        ALMOST_DONE,
        DONE
    }

}
