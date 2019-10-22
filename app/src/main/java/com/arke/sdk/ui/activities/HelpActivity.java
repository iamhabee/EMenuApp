package com.arke.sdk.ui.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.arke.sdk.R;
import com.arke.sdk.utilities.UiUtils;
//import com.elitepath.android.emenu.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HelpActivity extends BaseActivity {

    @BindView(R.id.close_activity)
    ImageView closeActivityView;

    @BindView(R.id.help_description)
    WebView helpContentView;

    @BindView(R.id.top_view)
    View topView;

    @BindView(R.id.help_header)
    TextView helpHeaderView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_screen);
        ButterKnife.bind(this);
        tintToolbarAndTabLayout(primaryColorInt);
        tintToolbar();
        closeActivityView.setOnClickListener(view -> finish());
        String helpContent = "After a successful setup and/or authentication on the EMenu Platform, you can use this as a handy reference for commonly\n" +
                "performed tasks on the platform<br /><br />\n" +
                "\n" +
                "<h2>For the Waiter</h2>\n" +
                "<ul>\n" +
                "    <li>To take an <b>order</b> from a customer, search for the item(s) on the <b>menu tab</b> or <b>categories tab</b>\n" +
                "        of the waiter's main page. Tap on them item; <br />- Specify the <b>orderedQuantity</b> of item <b>within stock</b> as\n" +
                "        requested by the customer<br />- Provide a way to identify the customer using the <b>Individual Tag\n" +
                "        </b>field;<br />- Provide the <b>Waiter's Tag</b>, that is your name;<br /> - Provide the <b>Table's Tag</b>,\n" +
                "        that is a way to identify the table you are taking orders from.<br />- If you wish to add <b>drinks</b> to the\n" +
                "        order, tap on the <b>drinks</b> text/icon at the <b>bottom right</b> of the item details page<br />- Tap <b>Add\n" +
                "            to table Cart</b> to add the order to the table, at this point, the <b>order is not yet sent</b> to the\n" +
                "        kitchen or bar.<br />- To send the <b>Order(s)</b> to the <b>Kitchen/Bar</b>, leave the item details screen by\n" +
                "        pressing the <b>back button</b> and then tap on the <b>Cart Icon</b> at the top right of the <b>Waiter's Main\n" +
                "            Page</b><br />- In the <b>UnProcessed Orders Page,</b> Tap on <b>Send Orders to Kitchen/Bar</b> to send the\n" +
                "        orders to the Kitchen/Bar\n" +
                "    </li>\n" +
                "    <br />\n" +
                "    <li>\n" +
                "        At any point in time, if you wish to perform further actions on an <b>item or order</b>, simply\n" +
                "        <b>long-click</b> on it\n" +
                "    </li>\n" +
                "</ul>\n" +
                "\n" +
                "<h2>For the Kitchen</h2>\n" +
                "<ul>\n" +
                "    <li>To create a new food item from the kitchen, tap on the <b>circled button</b> at the bottom right of the kitchen\n" +
                "        screen\n" +
                "    </li>\n" +
                "    <br />\n" +
                "    <li>\n" +
                "        If a food item is no longer <b>in stock</b>, long click on the item to mark it as out of stock\n" +
                "    </li>\n" +
                "    <br />\n" +
                "    <li>To set the amount available in stock for an item, long click on the item to set this information</li>\n" +
                "    <br />\n" +
                "    <li>If you want to <b>edit or update</b> the details of an item, <b>click on the item</b> and in the <b>details\n" +
                "            page</b> of the item you will see an option at the bottom of the screen to update the item</li>\n" +
                "</ul>\n" +
                "\n" +
                "<h2>For the Bar</h2>\n" +
                "<ul>\n" +
                "    <li>To create a new drink item from the bar, tap on the <b>circled button</b> at the bottom right of the bar screen\n" +
                "    </li>\n" +
                "    <br />\n" +
                "    <li>\n" +
                "        If a drink item is no longer <b>in stock</b>, long click on the item to mark it as out of stock\n" +
                "    </li>\n" +
                "    <br />\n" +
                "    <li>To set the amount available in stock for an item, long click on the item to set this information</li>\n" +
                "    <br />\n" +
                "    <li>If you want to <b>edit or update</b> the details of an item, <b>click on the item</b> and in the <b>details\n" +
                "            page</b> of the item you will see an option at the bottom of the screen to update the item</li>\n" +
                "</ul>";
        helpContentView.loadData(helpContent, "text/html", "utf-8");
    }


    private void tintToolbar() {
        if (UiUtils.whitish(primaryColorInt)) {
            helpHeaderView.setTextColor(Color.BLACK);
            topView.setBackgroundColor(Color.WHITE);
            closeActivityView.setImageResource(getBlackBackButton());
        } else {
            closeActivityView.setImageResource(getWhiteBackButton());
            topView.setBackgroundColor(Color.parseColor(primaryColorHex));
            helpHeaderView.setTextColor(Color.WHITE);
        }
    }

}
