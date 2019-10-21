package com.arke.sdk;

import android.app.AlertDialog;
import android.app.ExpandableListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Toast;

import com.arke.sdk.demo.BeeperDemo;
import com.arke.sdk.demo.CardDemo;
import com.arke.sdk.demo.EMVDemo;
import com.arke.sdk.demo.EthernetDemo;
import com.arke.sdk.demo.LEDDemo;
import com.arke.sdk.demo.MultimediaDemo;
import com.arke.sdk.demo.NetworkSwitchDemo;
import com.arke.sdk.demo.OtherDemo;
import com.arke.sdk.demo.PinpadForDUKPTDemo;
import com.arke.sdk.demo.PinpadForFKDemo;
import com.arke.sdk.demo.PinpadForMKSKDemo;
import com.arke.sdk.demo.PrinterDemo;
import com.arke.sdk.demo.ScannerDemo;
import com.arke.sdk.demo.SerialPortDemo;
import com.arke.sdk.demo.SignPanelDemo;
import com.arke.sdk.demo.SimplePayDemo;
import com.arke.sdk.demo.StatisticDemo;
import com.arke.sdk.demo.UpgradeDemo;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * The activity is used to manage all test modules.
 */

public class MainActivity extends ExpandableListActivity {

    private static final String TAG = "MainActivity";
    private static final String MODULE = "MODULE";
    private static final String FUNCTION = "FUNCTION";
    private static final int BEEPER_INDEX = 0;
    private static final int CARD_INDEX = 1;
    private static final int EMV_INDEX = 2;
    private static final int LED_INDEX = 3;
    private static final int PINPAD_DUKPT_INDEX = 4;
    private static final int PINPAD_FK_INDEX = 5;
    private static final int PINPAD_MKSK_INDEX = 6;
    private static final int PRINTER_INDEX = 7;
    private static final int SCANNER_INDEX = 8;
    private static final int SERIAL_PORT_INDEX = 9;
    private static final int OTHER_INDEX = 10;
    private static final int SIMPLE_PAY_INDEX = 11;
    private static final int ETHERNET_INDEX = 12;
    private static final int SIGN_PANEL_INDEX = 13;
    private static final int STATISTIC_INDEX = 14;
    private static final int UPGRADE_INDEX = 15;
    private static final int NETWORK_SWITCH_INDEX = 16;
    private static final int MULTIMEDIA_INDEX = 17;

    /**
     * Function list.
     */
    private List<List<Map<String, String>>> functionList;

    /**
     * Alert dialog.
     */
    private AlertDialog dialog;

    /**
     * Toast.
     */
    private Toast toast;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        setContentView(R.layout.activity_main);

        // Init view
        initView();

        // Init toast
        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        // Init alert dialog
        dialog = new AlertDialog.Builder(this)
                .setNegativeButton(getString(R.string.cancel), null)
                .setCancelable(false)
                .create();
    }

    protected boolean allowDisableSystemButton() {
        return true;
    }

    /**
     * Catch the HOME key event.
     */
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (allowDisableSystemButton()) {
            Window win = getWindow();
            try {
                Class<?> cls = win.getClass();
                final Class<?>[] PARAM_TYPES = new Class[]{int.class};
                Method method = cls.getMethod("addCustomFlags", PARAM_TYPES);
                method.setAccessible(true);
                method.invoke(win, new Object[]{0x00000001});
            } catch (Exception e) {
                // handle the error here.
                Timber.e(e.getCause());
            }
        }
    }

    /**
     * Deal the BACK and HOME key events.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_HOME) {
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            getApplication().onTerminate();
            finish();
        }

        return super.onKeyDown(keyCode, event);
    }

//    navigate to web view activity
    public void navWebView(View view){
        Intent intent = new Intent(this, WebviewActivity.class);
        startActivity(intent);
    }

    /**
     * Called when the item is clicked.
     */
    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        String value = functionList.get(groupPosition).get(childPosition).get(FUNCTION);
        Log.d(TAG, "g:" + groupPosition + ", c:" + childPosition + ", v:" + value);

        try {
            switch (groupPosition) {
                case BEEPER_INDEX:
                    BeeperDemo.getInstance(getApplicationContext(), toast, dialog).execute(value);
                    break;

                case CARD_INDEX:
                    CardDemo.getInstance(getApplicationContext(), toast, dialog).execute(value);
                    break;

                case EMV_INDEX:
                    EMVDemo.getInstance(getApplicationContext(), toast, dialog).execute(value);
                    break;

                case LED_INDEX:
                    LEDDemo.getInstance(getApplicationContext(), toast, dialog).execute(value);
                    break;

                case PINPAD_DUKPT_INDEX:
                    PinpadForDUKPTDemo.getInstance(getApplicationContext(), toast, dialog).execute(value);
                    break;

                case PINPAD_FK_INDEX:
                    PinpadForFKDemo.getInstance(getApplicationContext(), toast, dialog).execute(value);
                    break;

                case PINPAD_MKSK_INDEX:
                    PinpadForMKSKDemo.getInstance(getApplicationContext(), toast, dialog).execute(value);
                    break;

                case PRINTER_INDEX:
                    PrinterDemo.getInstance(getApplicationContext(), toast, dialog).execute(value);
                    break;

                case SCANNER_INDEX:
                    ScannerDemo.getInstance(getApplicationContext(), toast, dialog).execute(value);
                    break;

                case SERIAL_PORT_INDEX:
                    SerialPortDemo.getInstance(getApplicationContext(), toast, dialog).execute(value);
                    break;

                case OTHER_INDEX:
                    OtherDemo.getInstance(getApplicationContext(), toast, dialog).execute(value);
                    break;

                case SIMPLE_PAY_INDEX:
                    SimplePayDemo.getInstance(getApplicationContext(), toast, dialog).execute(value);
                    break;

                case ETHERNET_INDEX:
                    EthernetDemo.getInstance(getApplicationContext(), toast, dialog).execute(value);
                    break;

                case SIGN_PANEL_INDEX:
                    SignPanelDemo.getInstance(getApplicationContext(), toast, dialog).execute(value);
                    break;

                case STATISTIC_INDEX:
                    StatisticDemo.getInstance(getApplicationContext(), toast, dialog).execute(value);
                    break;

                case UPGRADE_INDEX:
                    UpgradeDemo.getInstance(getApplicationContext(), toast, dialog).execute(value);
                    break;

                case NETWORK_SWITCH_INDEX:
                    NetworkSwitchDemo.getInstance(getApplicationContext(), toast, dialog).execute(value);
                    break;

                case MULTIMEDIA_INDEX:
                    MultimediaDemo.getInstance(getApplicationContext(), toast, dialog).execute(value);
                    break;
            }
        } catch (RemoteException e) {
            Log.e(TAG, e.getLocalizedMessage());
            toast.setText(e.getLocalizedMessage() != null ? e.getLocalizedMessage() : getString(R.string.unknown_error));
            toast.show();
        }

        return super.onChildClick(parent, v, groupPosition, childPosition, id);
    }

    /**
     * Init the activity view.
     */
    private void initView() {

        // Beeper function list
        List<Map<String, String>> beeperFunctionList = new ArrayList<>();
        beeperFunctionList = addMap(beeperFunctionList, FUNCTION, getString(R.string.normal));
        beeperFunctionList = addMap(beeperFunctionList, FUNCTION, getString(R.string.error));
        beeperFunctionList = addMap(beeperFunctionList, FUNCTION, getString(R.string.interval));
        beeperFunctionList = addMap(beeperFunctionList, FUNCTION, getString(R.string.beep_two_seconds));

        // Card function list
        List<Map<String, String>> cardFunctionList = new ArrayList<>();
        cardFunctionList = addMap(cardFunctionList, FUNCTION, getString(R.string.magnetic_stripe_card));
        cardFunctionList = addMap(cardFunctionList, FUNCTION, getString(R.string.cpu_ic_card));
        cardFunctionList = addMap(cardFunctionList, FUNCTION, getString(R.string.psam_ic_card));
        cardFunctionList = addMap(cardFunctionList, FUNCTION, getString(R.string.at1604_ic_card));
        cardFunctionList = addMap(cardFunctionList, FUNCTION, getString(R.string.at1608_ic_card));
        cardFunctionList = addMap(cardFunctionList, FUNCTION, getString(R.string.at24cxx_ic_card));
        cardFunctionList = addMap(cardFunctionList, FUNCTION, getString(R.string.sim4428_ic_card));
        cardFunctionList = addMap(cardFunctionList, FUNCTION, getString(R.string.sim4442_ic_card));
        cardFunctionList = addMap(cardFunctionList, FUNCTION, getString(R.string.rf_card));
        cardFunctionList = addMap(cardFunctionList, FUNCTION, getString(R.string.is_card_in));
        cardFunctionList = addMap(cardFunctionList, FUNCTION, getString(R.string.indonesia_rf_card));

        // EMV function list
        List<Map<String, String>> emvFunctionList = new ArrayList<>();
        emvFunctionList = addMap(emvFunctionList, FUNCTION, getString(R.string.start_process));
        emvFunctionList = addMap(emvFunctionList, FUNCTION, getString(R.string.get_dol));
        emvFunctionList = addMap(emvFunctionList, FUNCTION, getString(R.string.get_tlv));
        emvFunctionList = addMap(emvFunctionList, FUNCTION, getString(R.string.get_data_apdu));
        emvFunctionList = addMap(emvFunctionList, FUNCTION, getString(R.string.get_balance));
        emvFunctionList = addMap(emvFunctionList, FUNCTION, getString(R.string.get_ecc_log));
        emvFunctionList = addMap(emvFunctionList, FUNCTION, getString(R.string.get_icc_log));

        // LED function list
        List<Map<String, String>> ledFunctionList = new ArrayList<>();
        ledFunctionList = addMap(ledFunctionList, FUNCTION, getString(R.string.led_on_all));
        ledFunctionList = addMap(ledFunctionList, FUNCTION, getString(R.string.led_off_all));
        ledFunctionList = addMap(ledFunctionList, FUNCTION, getString(R.string.operate_red_light));
        ledFunctionList = addMap(ledFunctionList, FUNCTION, getString(R.string.operate_green_light));
        ledFunctionList = addMap(ledFunctionList, FUNCTION, getString(R.string.operate_yellow_light));
        ledFunctionList = addMap(ledFunctionList, FUNCTION, getString(R.string.operate_blue_light));
        ledFunctionList = addMap(ledFunctionList, FUNCTION, getString(R.string.turn_on_blue_and_red_lights));

        // Pinpad for DUKPT function list
        List<Map<String, String>> pinpadDUKPTFunctionList = new ArrayList<>();
        pinpadDUKPTFunctionList = addMap(pinpadDUKPTFunctionList, FUNCTION, getString(R.string.open));
        pinpadDUKPTFunctionList = addMap(pinpadDUKPTFunctionList, FUNCTION, getString(R.string.format_pinpad));
        pinpadDUKPTFunctionList = addMap(pinpadDUKPTFunctionList, FUNCTION, getString(R.string.load_plain_text_key));
        pinpadDUKPTFunctionList = addMap(pinpadDUKPTFunctionList, FUNCTION, getString(R.string.is_key_exist));
        pinpadDUKPTFunctionList = addMap(pinpadDUKPTFunctionList, FUNCTION, getString(R.string.switch_to_work_mode));
        pinpadDUKPTFunctionList = addMap(pinpadDUKPTFunctionList, FUNCTION, getString(R.string.init_dukpt_ik_ksn));
        pinpadDUKPTFunctionList = addMap(pinpadDUKPTFunctionList, FUNCTION, getString(R.string.get_current_ksn));
        pinpadDUKPTFunctionList = addMap(pinpadDUKPTFunctionList, FUNCTION, getString(R.string.increase_current_ksn));
        pinpadDUKPTFunctionList = addMap(pinpadDUKPTFunctionList, FUNCTION, getString(R.string.start_pin_entry));
        pinpadDUKPTFunctionList = addMap(pinpadDUKPTFunctionList, FUNCTION, getString(R.string.cancel_pin_entry));
        pinpadDUKPTFunctionList = addMap(pinpadDUKPTFunctionList, FUNCTION, getString(R.string.start_offline_pin_entry));
        pinpadDUKPTFunctionList = addMap(pinpadDUKPTFunctionList, FUNCTION, getString(R.string.verify_offline_pin));
        pinpadDUKPTFunctionList = addMap(pinpadDUKPTFunctionList, FUNCTION, getString(R.string.encrypt_mag_track));
        pinpadDUKPTFunctionList = addMap(pinpadDUKPTFunctionList, FUNCTION, getString(R.string.calculate_mac));
        pinpadDUKPTFunctionList = addMap(pinpadDUKPTFunctionList, FUNCTION, getString(R.string.calculate_des));
        pinpadDUKPTFunctionList = addMap(pinpadDUKPTFunctionList, FUNCTION, getString(R.string.calculate_kcv));
        pinpadDUKPTFunctionList = addMap(pinpadDUKPTFunctionList, FUNCTION, getString(R.string.get_random));
        pinpadDUKPTFunctionList = addMap(pinpadDUKPTFunctionList, FUNCTION, getString(R.string.get_existent_kap_ids));
        pinpadDUKPTFunctionList = addMap(pinpadDUKPTFunctionList, FUNCTION, getString(R.string.set_serial_number));
        pinpadDUKPTFunctionList = addMap(pinpadDUKPTFunctionList, FUNCTION, getString(R.string.delete_key));
        pinpadDUKPTFunctionList = addMap(pinpadDUKPTFunctionList, FUNCTION, getString(R.string.close));

        // Pinpad for FK function list
        List<Map<String, String>> pinpadFKFunctionList = new ArrayList<>();
        pinpadFKFunctionList = addMap(pinpadFKFunctionList, FUNCTION, getString(R.string.open));
        pinpadFKFunctionList = addMap(pinpadFKFunctionList, FUNCTION, getString(R.string.format_pinpad));
        pinpadFKFunctionList = addMap(pinpadFKFunctionList, FUNCTION, getString(R.string.load_plain_text_key));
        pinpadFKFunctionList = addMap(pinpadFKFunctionList, FUNCTION, getString(R.string.is_key_exist));
        pinpadFKFunctionList = addMap(pinpadFKFunctionList, FUNCTION, getString(R.string.switch_to_work_mode));
        pinpadFKFunctionList = addMap(pinpadFKFunctionList, FUNCTION, getString(R.string.start_pin_entry));
        pinpadFKFunctionList = addMap(pinpadFKFunctionList, FUNCTION, getString(R.string.cancel_pin_entry));
        pinpadFKFunctionList = addMap(pinpadFKFunctionList, FUNCTION, getString(R.string.start_offline_pin_entry));
        pinpadFKFunctionList = addMap(pinpadFKFunctionList, FUNCTION, getString(R.string.verify_offline_pin));
        pinpadFKFunctionList = addMap(pinpadFKFunctionList, FUNCTION, getString(R.string.encrypt_mag_track));
        pinpadFKFunctionList = addMap(pinpadFKFunctionList, FUNCTION, getString(R.string.calculate_mac));
        pinpadFKFunctionList = addMap(pinpadFKFunctionList, FUNCTION, getString(R.string.calculate_des));
        pinpadFKFunctionList = addMap(pinpadFKFunctionList, FUNCTION, getString(R.string.calculate_kcv));
        pinpadFKFunctionList = addMap(pinpadFKFunctionList, FUNCTION, getString(R.string.get_random));
        pinpadFKFunctionList = addMap(pinpadFKFunctionList, FUNCTION, getString(R.string.get_existent_kap_ids));
        pinpadFKFunctionList = addMap(pinpadFKFunctionList, FUNCTION, getString(R.string.set_serial_number));
        pinpadFKFunctionList = addMap(pinpadFKFunctionList, FUNCTION, getString(R.string.delete_key));
        pinpadFKFunctionList = addMap(pinpadFKFunctionList, FUNCTION, getString(R.string.close));

        // Pinpad for MK/SK function list
        List<Map<String, String>> pinpadMKSKFunctionList = new ArrayList<>();
        pinpadMKSKFunctionList = addMap(pinpadMKSKFunctionList, FUNCTION, getString(R.string.open));
        pinpadMKSKFunctionList = addMap(pinpadMKSKFunctionList, FUNCTION, getString(R.string.format_pinpad));
        pinpadMKSKFunctionList = addMap(pinpadMKSKFunctionList, FUNCTION, getString(R.string.load_plain_text_key));
        pinpadMKSKFunctionList = addMap(pinpadMKSKFunctionList, FUNCTION, getString(R.string.is_key_exist));
        pinpadMKSKFunctionList = addMap(pinpadMKSKFunctionList, FUNCTION, getString(R.string.switch_to_work_mode));
        pinpadMKSKFunctionList = addMap(pinpadMKSKFunctionList, FUNCTION, getString(R.string.load_encrypted_key));
        pinpadMKSKFunctionList = addMap(pinpadMKSKFunctionList, FUNCTION, getString(R.string.start_pin_entry));
        pinpadMKSKFunctionList = addMap(pinpadMKSKFunctionList, FUNCTION, getString(R.string.cancel_pin_entry));
        pinpadMKSKFunctionList = addMap(pinpadMKSKFunctionList, FUNCTION, getString(R.string.start_offline_pin_entry));
        pinpadMKSKFunctionList = addMap(pinpadMKSKFunctionList, FUNCTION, getString(R.string.verify_offline_pin));
        pinpadMKSKFunctionList = addMap(pinpadMKSKFunctionList, FUNCTION, getString(R.string.encrypt_mag_track));
        pinpadMKSKFunctionList = addMap(pinpadMKSKFunctionList, FUNCTION, getString(R.string.calculate_mac));
        pinpadMKSKFunctionList = addMap(pinpadMKSKFunctionList, FUNCTION, getString(R.string.calculate_des));
        pinpadMKSKFunctionList = addMap(pinpadMKSKFunctionList, FUNCTION, getString(R.string.calculate_kcv));
        pinpadMKSKFunctionList = addMap(pinpadMKSKFunctionList, FUNCTION, getString(R.string.get_random));
        pinpadMKSKFunctionList = addMap(pinpadMKSKFunctionList, FUNCTION, getString(R.string.get_existent_kap_ids));
        pinpadMKSKFunctionList = addMap(pinpadMKSKFunctionList, FUNCTION, getString(R.string.set_serial_number));
        pinpadMKSKFunctionList = addMap(pinpadMKSKFunctionList, FUNCTION, getString(R.string.delete_key));
        pinpadMKSKFunctionList = addMap(pinpadMKSKFunctionList, FUNCTION, getString(R.string.close));

        // Printer function list
        List<Map<String, String>> printerFunctionList = new ArrayList<>();
        printerFunctionList = addMap(printerFunctionList, FUNCTION, getString(R.string.get_printer_status));
        printerFunctionList = addMap(printerFunctionList, FUNCTION, getString(R.string.feed_paper));
        printerFunctionList = addMap(printerFunctionList, FUNCTION, getString(R.string.print));
        printerFunctionList = addMap(printerFunctionList, FUNCTION, getString(R.string.print_html5));
        printerFunctionList = addMap(printerFunctionList, FUNCTION, getString(R.string.print_multi_languages));

        // Scanner function list
        List<Map<String, String>> scannerFunctionList = new ArrayList<>();
        scannerFunctionList = addMap(scannerFunctionList, FUNCTION, getString(R.string.start_front_scan));
        scannerFunctionList = addMap(scannerFunctionList, FUNCTION, getString(R.string.start_back_scan));
        scannerFunctionList = addMap(scannerFunctionList, FUNCTION, getString(R.string.stop_front_scan));
        scannerFunctionList = addMap(scannerFunctionList, FUNCTION, getString(R.string.stop_back_scan));

        // Serial port function list
        List<Map<String, String>> serialPortFunctionList = new ArrayList<>();
        serialPortFunctionList = addMap(serialPortFunctionList, FUNCTION, getString(R.string.open));
        serialPortFunctionList = addMap(serialPortFunctionList, FUNCTION, getString(R.string.send_data_timeout_0));
        serialPortFunctionList = addMap(serialPortFunctionList, FUNCTION, getString(R.string.read_data_timeout_0));
        serialPortFunctionList = addMap(serialPortFunctionList, FUNCTION, getString(R.string.send_data_timeout_10000));
        serialPortFunctionList = addMap(serialPortFunctionList, FUNCTION, getString(R.string.read_data_timeout_10000));
        serialPortFunctionList = addMap(serialPortFunctionList, FUNCTION, getString(R.string.flush));
        serialPortFunctionList = addMap(serialPortFunctionList, FUNCTION, getString(R.string.close));

        // Other function list
        List<Map<String, String>> otherFunctionList = new ArrayList<>();
        otherFunctionList = addMap(otherFunctionList, FUNCTION, getString(R.string.is_first_run));
        otherFunctionList = addMap(otherFunctionList, FUNCTION, getString(R.string.is_exist));
        otherFunctionList = addMap(otherFunctionList, FUNCTION, getString(R.string.get_boolean_param));
        otherFunctionList = addMap(otherFunctionList, FUNCTION, getString(R.string.get_string_param));
        otherFunctionList = addMap(otherFunctionList, FUNCTION, getString(R.string.get_terminal_info));
        otherFunctionList = addMap(otherFunctionList, FUNCTION, getString(R.string.update_system_time));

        // Simple pay function list
        List<Map<String, String>> simplePayFunctionList = new ArrayList<>();
        simplePayFunctionList = addMap(simplePayFunctionList, FUNCTION, getString(R.string.sale));

        // Multimedia function list
        List<Map<String, String>> multimediaFunctionList = new ArrayList<>();
        multimediaFunctionList = addMap(multimediaFunctionList,FUNCTION,getString(R.string.video_player));

        // Ethernet function list
        List<Map<String, String>> ethernetFunctionList = new ArrayList<>();
        ethernetFunctionList = addMap(ethernetFunctionList, FUNCTION, getString(R.string.is_ethernet_exist));
        ethernetFunctionList = addMap(ethernetFunctionList, FUNCTION, getString(R.string.is_ethernet_enabled));
        ethernetFunctionList = addMap(ethernetFunctionList, FUNCTION, getString(R.string.open));
        ethernetFunctionList = addMap(ethernetFunctionList, FUNCTION, getString(R.string.get_ethernet_info));
        ethernetFunctionList = addMap(ethernetFunctionList, FUNCTION, getString(R.string.get_ethernet_config_info));
        ethernetFunctionList = addMap(ethernetFunctionList, FUNCTION, getString(R.string.get_ethernet_state));
        ethernetFunctionList = addMap(ethernetFunctionList, FUNCTION, getString(R.string.get_ethernet_enabled_state));
        ethernetFunctionList = addMap(ethernetFunctionList, FUNCTION, getString(R.string.config_ethernet_dhcp_to_false));
        ethernetFunctionList = addMap(ethernetFunctionList, FUNCTION, getString(R.string.config_ethernet_dhcp_to_true));
        ethernetFunctionList = addMap(ethernetFunctionList, FUNCTION, getString(R.string.close));

        // Sign panel function list
        List<Map<String, String>> signPanelFunctionList = new ArrayList<>();
        signPanelFunctionList = addMap(signPanelFunctionList, FUNCTION, getString(R.string.show_sign_panel));
        signPanelFunctionList = addMap(signPanelFunctionList, FUNCTION, getString(R.string.hide_sign_panel));

        // Statistic function list
        List<Map<String, String>> statisticFunctionList = new ArrayList<>();
        statisticFunctionList = addMap(statisticFunctionList, FUNCTION, getString(R.string.system_statistic));

        // File upgrade function list
        List<Map<String, String>> upgradeFunctionList = new ArrayList<>();
        upgradeFunctionList = addMap(upgradeFunctionList, FUNCTION, getString(R.string.get_version));
        upgradeFunctionList = addMap(upgradeFunctionList, FUNCTION, getString(R.string.mock_download));
        upgradeFunctionList = addMap(upgradeFunctionList, FUNCTION, getString(R.string.offline_dll_upgrade));
        upgradeFunctionList = addMap(upgradeFunctionList, FUNCTION, getString(R.string.install_apk));
        upgradeFunctionList = addMap(upgradeFunctionList, FUNCTION, getString(R.string.uninstall_apk));

        // Network switch function list
        List<Map<String, String>> networkSwitchFunctionList = new ArrayList<>();
        networkSwitchFunctionList = addMap(networkSwitchFunctionList, FUNCTION, getString(R.string.switch_to_wifi));
        networkSwitchFunctionList = addMap(networkSwitchFunctionList, FUNCTION, getString(R.string.switch_to_gprs));
        networkSwitchFunctionList = addMap(networkSwitchFunctionList, FUNCTION, getString(R.string.switch_to_eth));

        // Module list
        List<Map<String, String>> moduleList = new ArrayList<>();
        moduleList = addMapByLocation(moduleList, MODULE, BEEPER_INDEX, getString(R.string.beeper));
        moduleList = addMapByLocation(moduleList, MODULE, CARD_INDEX, getString(R.string.card));
        moduleList = addMapByLocation(moduleList, MODULE, EMV_INDEX, getString(R.string.emv));
        moduleList = addMapByLocation(moduleList, MODULE, LED_INDEX, getString(R.string.led));
        moduleList = addMapByLocation(moduleList, MODULE, PINPAD_DUKPT_INDEX, getString(R.string.pinpad_dukpt));
        moduleList = addMapByLocation(moduleList, MODULE, PINPAD_FK_INDEX, getString(R.string.pinpad_fk));
        moduleList = addMapByLocation(moduleList, MODULE, PINPAD_MKSK_INDEX, getString(R.string.pinpad_mksk));
        moduleList = addMapByLocation(moduleList, MODULE, PRINTER_INDEX, getString(R.string.printer));
        moduleList = addMapByLocation(moduleList, MODULE, SCANNER_INDEX, getString(R.string.scanner));
        moduleList = addMapByLocation(moduleList, MODULE, SERIAL_PORT_INDEX, getString(R.string.serial_port));
        moduleList = addMapByLocation(moduleList, MODULE, OTHER_INDEX, getString(R.string.other));
        moduleList = addMapByLocation(moduleList, MODULE, SIMPLE_PAY_INDEX, getString(R.string.simple_pay));
        moduleList = addMapByLocation(moduleList, MODULE, ETHERNET_INDEX, getString(R.string.ethernet));
        moduleList = addMapByLocation(moduleList, MODULE, SIGN_PANEL_INDEX, getString(R.string.sign_panel));
        moduleList = addMapByLocation(moduleList, MODULE, STATISTIC_INDEX, getString(R.string.statistic));
        moduleList = addMapByLocation(moduleList, MODULE, UPGRADE_INDEX, getString(R.string.upgrade));
        moduleList = addMapByLocation(moduleList, MODULE, NETWORK_SWITCH_INDEX, getString(R.string.network_switch));
        moduleList = addMapByLocation(moduleList, MODULE, MULTIMEDIA_INDEX, getString(R.string.multimedia));

        // Function list
        functionList = new ArrayList<>();
        functionList.add(BEEPER_INDEX, beeperFunctionList);
        functionList.add(CARD_INDEX, cardFunctionList);
        functionList.add(EMV_INDEX, emvFunctionList);
        functionList.add(LED_INDEX, ledFunctionList);
        functionList.add(PINPAD_DUKPT_INDEX, pinpadDUKPTFunctionList);
        functionList.add(PINPAD_FK_INDEX, pinpadFKFunctionList);
        functionList.add(PINPAD_MKSK_INDEX, pinpadMKSKFunctionList);
        functionList.add(PRINTER_INDEX, printerFunctionList);
        functionList.add(SCANNER_INDEX, scannerFunctionList);
        functionList.add(SERIAL_PORT_INDEX, serialPortFunctionList);
        functionList.add(OTHER_INDEX, otherFunctionList);
        functionList.add(SIMPLE_PAY_INDEX, simplePayFunctionList);
        functionList.add(ETHERNET_INDEX, ethernetFunctionList);
        functionList.add(SIGN_PANEL_INDEX, signPanelFunctionList);
        functionList.add(STATISTIC_INDEX,statisticFunctionList);
        functionList.add(UPGRADE_INDEX, upgradeFunctionList);
        functionList.add(NETWORK_SWITCH_INDEX, networkSwitchFunctionList);
        functionList.add(MULTIMEDIA_INDEX, multimediaFunctionList);

        // Set list adapter
        setListAdapter(new SimpleExpandableListAdapter(this,
                moduleList, R.layout.partial_module, new String[]{MODULE}, new int[]{R.id.tv_module},
                functionList, R.layout.partial_function, new String[]{FUNCTION}, new int[]{R.id.tv_function}
        ));
    }

    /**
     * Add a new map instance to list.
     */
    private List<Map<String, String>> addMap(List<Map<String, String>> list, String key, String value) {
        Map<String, String> map = new HashMap<>();
        map.put(key, value);
        list.add(map);
        return list;
    }

    /**
     * Add a new map instance to list by location.
     */
    private List<Map<String, String>> addMapByLocation(List<Map<String, String>> list, String key, int location, String value) {
        Map<String, String> map = new HashMap<>();
        map.put(key, value);
        list.add(location, map);
        return list;
    }
}
