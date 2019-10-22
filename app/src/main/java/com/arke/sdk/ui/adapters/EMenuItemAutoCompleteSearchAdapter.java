package com.arke.sdk.ui.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;

import com.arke.sdk.R;
import com.arke.sdk.models.EMenuItem;
import com.arke.sdk.preferences.AppPrefs;
import com.arke.sdk.utilities.EMenuGenUtils;
import com.arke.sdk.utilities.EMenuLogger;
import com.arke.sdk.utilities.UiUtils;
//import com.elitepath.android.emenu.R;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.List;

public class EMenuItemAutoCompleteSearchAdapter extends ArrayAdapter<EMenuItem> {

    public interface OnSearchItemClickListener {
        void onSearchItemClicked(EMenuItem eMenuItem);
    }

    private String searchString;
    private OnSearchItemClickListener onSearchItemClickListener;

    private List<EMenuItem> items, tempItems, suggestions;
    private Context context;

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public String getSearchString() {
        return searchString;
    }

    public EMenuItemAutoCompleteSearchAdapter(Context context, int resource,
                                              List<EMenuItem> items) {
        super(context, resource, 0, items);
        this.context = context;
        this.items = items;
        tempItems = new ArrayList<>(items);
        suggestions = new ArrayList<>();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (inflater != null) {
                view = inflater.inflate(R.layout.autocomplete_emenu_search_item, parent, false);
            }
        }
        EMenuItem eMenuItem = items.get(position);
        if (eMenuItem != null) {
            TextView itemNameView = view.findViewById(R.id.item_name);
            ImageView itemImagePreview = view.findViewById(R.id.item_image_preview);
            TextView itemPriceView = view.findViewById(R.id.emenu_item_price_view);
            AppCompatImageView currencyIndicatorView = view.findViewById(R.id.currency_indicator);
            if (itemNameView != null) {
                String emenuItemName = eMenuItem.getMenuItemName();
                if (StringUtils.isNotEmpty(searchString)) {
                    EMenuLogger.d("SearchedTag", "Searched String=" + getSearchString());
                    itemNameView.setText(UiUtils.highlightTextIfNecessary(getSearchString(), WordUtils.capitalize(emenuItemName),
                            ContextCompat.getColor(context, R.color.colorAccent)));
                } else {
                    if (StringUtils.isNotEmpty(emenuItemName)) {
                        itemNameView.setText(WordUtils.capitalize(emenuItemName));
                    } else {
                        itemNameView.setText(" ");
                    }
                }
            }
            if (itemImagePreview != null) {
                String imagePreviewUrl = eMenuItem.getMenuItemDisplayPhotoUrl();
                UiUtils.loadImageIntoView(itemImagePreview, imagePreviewUrl);
            }
            if (itemPriceView != null) {
                itemPriceView.setText(EMenuGenUtils.computeAccumulatedPrice(eMenuItem));
            }
            if (currencyIndicatorView != null && itemPriceView != null) {
                tintCurrencyViews(itemPriceView, currencyIndicatorView);
            }
            view.setOnClickListener(view1 -> {
                UiUtils.blinkView(view1);
                onSearchItemClickListener.onSearchItemClicked(eMenuItem);
            });
        }
        return view;
    }

    /**
     * Custom Filter implementation for custom suggestions we provide.
     */
    private Filter itemFilter = new Filter() {

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return ((EMenuItem) resultValue).getMenuItemName();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (constraint != null) {
                suggestions.clear();
                for (EMenuItem item : tempItems) {
                    if (item.getMenuItemName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        suggestions.add(item);
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                return filterResults;
            } else {
                return new FilterResults();
            }
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            List<EMenuItem> filterList = (ArrayList<EMenuItem>) results.values;
            if (results.count > 0) {
                clear();
                for (EMenuItem item : filterList) {
                    add(item);
                    notifyDataSetChanged();
                }
            }
        }
    };

    @Override
    public Filter getFilter() {
        return itemFilter;
    }

    private void tintCurrencyViews(TextView itemPriceView, AppCompatImageView currencyIndicatorView) {
        itemPriceView.setTextColor(AppPrefs.getTertiaryColor());
        currencyIndicatorView.setSupportImageTintList(ColorStateList.valueOf(AppPrefs.getTertiaryColor()));
    }

    public void setOnSearchItemClickedListener(OnSearchItemClickListener onSearchItemClickedListener) {
        this.onSearchItemClickListener = onSearchItemClickedListener;
    }

}
