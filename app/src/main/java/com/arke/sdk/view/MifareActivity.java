package com.arke.sdk.view;

import android.os.Bundle;
import android.os.RemoteException;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.arke.sdk.R;
import com.arke.sdk.api.RFReader;
import com.arke.sdk.util.data.BytesUtil;
import com.google.common.base.Strings;
import com.usdk.apiservice.aidl.data.BytesValue;
import com.usdk.apiservice.aidl.rfreader.CardType;

import static com.usdk.apiservice.aidl.rfreader.KeyType.KEY_A;

public class MifareActivity extends BaseActivity {
    private static final String TAG = MifareActivity.class.getSimpleName();

    /**
     * Mifare card read block.
     */
    private static final int MIFARE_READ_BLOCK = 0;

    /**
     * Mifare card write block.
     */
    private static final int MIFARE_WRITE_BLOCK = 1;

    /**
     * Mifare card increase value.
     */
    private static final int MIFARE_INCREASE_VALUE = 2;

    /**
     * Mifare card decrease value.
     */
    private static final int MIFARE_DECREASE_VALUE = 3;

    /**
     * UID Text View.
     */
    private TextView tvUid;

    /**
     * Card type.
     */
    private int cardType;

    /**
     * Block number Edit Text.
     */
    private EditText etBlockNum;

    /**
     * Increase/Decrease value Edit Text.
     */
    private EditText etValue;

    /**
     * Block data Edit Text.
     */
    private EditText etBlockData;

    /**
     * Read button.
     */
    private Button btnRead;

    /**
     * Write button.
     */
    private Button btnWrite;

    /**
     * Increase button.
     */
    private Button btnIncrease;

    /**
     * Decrease button.
     */
    private Button btnDecrease;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mifare);

        tvUid = (TextView) findViewById(R.id.tv_uid_value);
        etBlockNum = (EditText) findViewById(R.id.et_block_num_value);
        etValue = (EditText) findViewById(R.id.et_inc_dec_value);
        etBlockData = (EditText) findViewById(R.id.et_block_data);
        btnRead = (Button) findViewById(R.id.btn_read);
        btnWrite = (Button) findViewById(R.id.btn_write);
        btnIncrease = (Button) findViewById(R.id.btn_increase);
        btnDecrease = (Button) findViewById(R.id.btn_decrease);

        String uid = getIntent().getStringExtra("UID");
        if (!Strings.isNullOrEmpty(uid)) {
            tvUid.setText(uid);
        }

        cardType = getIntent().getIntExtra("CardType", 0);

        setListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            RFReader.getInstance().stopSearch();
            RFReader.getInstance().halt();
        } catch (RemoteException e) {
            showToast(e.getLocalizedMessage());
        }
    }

    /**
     * Called after click read button.
     *
     * @param view
     */
    public void onReadBlockData(View view) {
        if (etBlockNum.getText().toString().isEmpty()) {
            return;
        }

        handleS50S70Card(MIFARE_READ_BLOCK);
    }

    /**
     * Called after click write button.
     *
     * @param view
     */
    public void onWriteBlockData(View view) {
        if (etBlockNum.getText().toString().isEmpty()) {
            return;
        }

        handleS50S70Card(MIFARE_WRITE_BLOCK);
    }

    /**
     * Called after click increase button.
     *
     * @param view
     */
    public void onIncreaseValue(View view) {
        if (etBlockNum.getText().toString().isEmpty() || etValue.getText().toString().isEmpty()) {
            return;
        }

        handleS50S70Card(MIFARE_INCREASE_VALUE);
    }

    /**
     * Called after click decrease button.
     *
     * @param view
     */
    public void onDecreaseValue(View view) {
        if (etBlockNum.getText().toString().isEmpty() || etValue.getText().toString().isEmpty()) {
            return;
        }

        handleS50S70Card(MIFARE_DECREASE_VALUE);
    }

    /**
     * Set listener.
     */
    private void setListener() {
        etBlockNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    btnRead.setEnabled(true);
                    btnWrite.setEnabled(true);
                } else {
                    btnRead.setEnabled(false);
                    btnWrite.setEnabled(false);
                }
            }
        });

        etValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    btnIncrease.setEnabled(true);
                    btnDecrease.setEnabled(true);
                } else {
                    btnIncrease.setEnabled(false);
                    btnDecrease.setEnabled(false);
                }
            }
        });
    }

    /**
     * Handle S50/S70 card.
     */
    private void handleS50S70Card(int doEvent) {
        byte[] aucMifKeyB0 = {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
        byte[] writeData = {0x05, 0x00, 0x00, 0x00, (byte) 0xFA, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x05, 0x00, 0x00, 0x00, 0x01, (byte) 0xFE, 0x01, (byte) 0xFE};;
        BytesValue readData = new BytesValue();

        /**
         * S50: blockNum value is from 0 to 63
         * S70: blockNum value is from 0 to 255
         */
        int blockNum = Integer.parseInt(etBlockNum.getText().toString());
        if (cardType == CardType.S50_CARD && blockNum > 63) {
            showToast(getString(R.string.block_number_exceeds_maximum));
            return;
        }

        if (cardType == CardType.S70_CARD && blockNum > 255) {
            showToast(getString(R.string.block_number_exceeds_maximum));
            return;
        }

        try {
            if (!RFReader.getInstance().isExist()) {
                showToast(getString(R.string.card_not_exist));
                return;
            }

            // Auth sector
            RFReader.getInstance().authSector(blockNum / 4, KEY_A, aucMifKeyB0);

            // Auth block
            RFReader.getInstance().authBlock(blockNum, KEY_A, aucMifKeyB0);
        } catch (RemoteException e) {
            e.printStackTrace();
            showToast(e.getMessage());
            return;
        }

        try {
            switch (doEvent) {
                case MIFARE_READ_BLOCK:
                    // Read block
                    RFReader.getInstance().readBlock(blockNum, readData);

                    etBlockData.setText(BytesUtil.bytes2HexString(readData.getData()));
                    break;
                case MIFARE_WRITE_BLOCK:
                    if (etBlockData.getText().toString().length() != 32) {
                        showToast(getString(R.string.block_data_length_32_please_enter_again));
                        break;
                    }
                    writeData = BytesUtil.hexString2ByteArray(etBlockData.getText().toString());

                    // Write block
                    RFReader.getInstance().writeBlock(blockNum, writeData);
                    break;
                case MIFARE_INCREASE_VALUE:
                    /**
                     * The increase value of block data must be(like writeData):
                     *  |   15  |  14  |   13  |  12  | 11 10 9 8 | 7 6 5 4 | 3 2 1 0 |
                     *  | ~addr | addr | ~addr | addr |   value   | ~value  |  value  |
                     *
                     *  addr is block number
                     */
                    // Increase value
                    RFReader.getInstance().increaseValue(blockNum, Integer.parseInt(etValue.getText().toString()));

                    saveIncDecResult(blockNum);
                    break;
                case MIFARE_DECREASE_VALUE:
                    /**
                     * The decrease value of block data must be(like writeData):
                     *  |   15  |  14  |   13  |  12  | 11 10 9 8 | 7 6 5 4 | 3 2 1 0 |
                     *  | ~addr | addr | ~addr | addr |   value   | ~value  |  value  |
                     *
                     *  addr is block number.
                     */
                    // Decrease value
                    RFReader.getInstance().decreaseValue(blockNum, Integer.parseInt(etValue.getText().toString()));

                    saveIncDecResult(blockNum);
                    break;
                default:
                    break;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            showToast(e.getMessage());
        }
    }

    /**
     * Save increase or decrease result.
     *
     * @param blockNum
     */
    private void saveIncDecResult(int blockNum) {
        try {
            RFReader.getInstance().transferRAM(blockNum);
        } catch (RemoteException e) {
            e.printStackTrace();
            showToast("transferRAM fail: " + e.getMessage());
        }
    }
}
