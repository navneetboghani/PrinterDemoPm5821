package com.adsum.printerdemopm5821;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.hoin.btsdk.BluetoothService;
import com.hoin.btsdk.PrintPic;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

public class MainActivity extends AppCompatActivity {
    Button btnSearch;
    Button btnSendDraw;
    Button btnSend;
    Button btnClose;
    EditText edtContext;
    EditText edtPrint;
    private static final int REQUEST_ENABLE_BT = 2;
    BluetoothService mService = null;
    BluetoothDevice con_dev = null;
    private View qrCodeBtnSend;
    private static final int REQUEST_CONNECT_DEVICE = 1;  //»ñÈ¡Éè±¸ÏûÏ¢
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        try {
            btnSendDraw = (Button)findViewById(R.id.btn_test);
            btnSendDraw.setOnClickListener(new ClickEvent());
            btnSearch = (Button)findViewById(R.id.btnSearch);
            btnSearch.setOnClickListener(new ClickEvent());
            btnSend = (Button)findViewById(R.id.btnSend);
            btnSend.setOnClickListener(new ClickEvent());
            qrCodeBtnSend = (Button)findViewById(R.id.qr_code_Send);
            qrCodeBtnSend.setOnClickListener(new ClickEvent());
            btnClose = (Button)findViewById(R.id.btnClose);
            btnClose.setOnClickListener(new ClickEvent());
            edtContext = (EditText)findViewById(R.id.txt_content);
            btnClose.setEnabled(false);
            btnSend.setEnabled(false);
            qrCodeBtnSend.setEnabled(false);
            btnSendDraw.setEnabled(false);

            mService = new BluetoothService(this, mHandler);
            //À¶ÑÀ²»¿ÉÓÃÍË³ö³ÌÐò
            if( mService.isAvailable() == false ){
                Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if( mService.isBTopen() == false){
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null)
            mService.stop();
        mService = null;
    }
    class ClickEvent implements View.OnClickListener {
        public void onClick(View v) {
            String msg = "";
            switch (v.getId()) {
                case R.id.btn_test:
                    String lang = getString(R.string.bluetooth_strLang);
                    checkpermission();
                   // printImage();

                    byte[] cmd = new byte[3];
                    cmd[0] = 0x1b;
                    cmd[1] = 0x21;
                    if((lang.compareTo("en")) == 0){
                        cmd[2] |= 0x10;
                        mService.write(cmd);           //±¶¿í¡¢±¶¸ßÄ£Ê½
                        mService.sendMessage("Congratulations!\n", "GBK");
                        cmd[2] &= 0xEF;
                        mService.write(cmd);           //È¡Ïû±¶¸ß¡¢±¶¿íÄ£Ê½
                        msg = "  You have sucessfully created communications between your device and our bluetooth printer.\n\n"
                                +"  Our company is a high-tech enterprise which specializes" +
                                " in R&D,manufacturing,marketing of thermal printers and barcode scanners.\n\n";
                        mService.sendMessage(msg,"GBK");
                    }else if((lang.compareTo("ch")) == 0){
                        cmd[2] |= 0x10;
                        mService.write(cmd);           //±¶¿í¡¢±¶¸ßÄ£Ê½
                        mService.sendMessage("¹§Ï²Äú£¡\n", "GBK");
                        cmd[2] &= 0xEF;
                        mService.write(cmd);           //È¡Ïû±¶¸ß¡¢±¶¿íÄ£Ê½
                        msg = "  ÄúÒÑ¾­³É¹¦µÄÁ¬½ÓÉÏÁËÎÒÃÇµÄÀ¶ÑÀ´òÓ¡»ú£¡\n\n"
                                + "  ÎÒÃÇ¹«Ë¾ÊÇÒ»¼Ò×¨Òµ´ÓÊÂÑÐ·¢£¬Éú²ú£¬ÏúÊÛÉÌÓÃÆ±¾Ý´òÓ¡»úºÍÌõÂëÉ¨ÃèÉè±¸ÓÚÒ»ÌåµÄ¸ß¿Æ¼¼ÆóÒµ.\n\n";
                        mService.sendMessage(msg,"GBK");
                    }
                    break;
                case R.id.btnSearch:
                    Intent serverIntent = new Intent(MainActivity.this,DeviceListActivity.class);      //ÔËÐÐÁíÍâÒ»¸öÀàµÄ»î¶¯
                    startActivityForResult(serverIntent,REQUEST_CONNECT_DEVICE);
                    break;
                case R.id.btnSend:
                    msg = edtContext.getText().toString();
                    if( msg.length() > 0 ){
                        mService.sendMessage(msg, "GBK");
                    }
                    break;
                case R.id.qr_code_Send:
                    cmd = new byte[7];
                    cmd[0] = 0x1B;
                    cmd[1] = 0x5A;
                    cmd[2] = 0x00;
                    cmd[3] = 0x02;
                    cmd[4] = 0x07;
                    cmd[5] = 0x17;
                    cmd[6] = 0x00;
                    msg = getResources().getString(R.string.bluetooth_qr_code_Send_string);
                    if( msg.length() > 0){
                        mService.write(cmd);
                        mService.sendMessage(msg, "GBK");
                    }
                    break;
                case R.id.btnClose:
                    mService.stop();
                    break;
            }
        }
    }

    private void checkpermission() {
        try {
            Dexter.withContext(this)
                    .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)

                    .withListener(new PermissionListener() {
                        @Override public void onPermissionGranted(PermissionGrantedResponse response) {
                            printImage();
                        }
                        @Override public void onPermissionDenied(PermissionDeniedResponse response) {/* ... */}
                        @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {/* ... */}
                    }).check();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothService.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:   //ÒÑÁ¬½Ó
                            Toast.makeText(MainActivity.this, "Connect successful",
                                    Toast.LENGTH_SHORT).show();
                            btnClose.setEnabled(true);
                            btnSend.setEnabled(true);
                            qrCodeBtnSend.setEnabled(true);
                            btnSendDraw.setEnabled(true);
                            break;
                        case BluetoothService.STATE_CONNECTING:  //ÕýÔÚÁ¬½Ó
                            Log.d("À¶ÑÀµ÷ÊÔ","ÕýÔÚÁ¬½Ó.....");
                            break;
                        case BluetoothService.STATE_LISTEN:     //¼àÌýÁ¬½ÓµÄµ½À´
                        case BluetoothService.STATE_NONE:
                            Log.d("À¶ÑÀµ÷ÊÔ","µÈ´ýÁ¬½Ó.....");
                            break;
                    }
                    break;
                case BluetoothService.MESSAGE_CONNECTION_LOST:    //À¶ÑÀÒÑ¶Ï¿ªÁ¬½Ó
                    Toast.makeText(MainActivity.this, "Device connection was lost",
                            Toast.LENGTH_SHORT).show();
                    btnClose.setEnabled(false);
                    btnSend.setEnabled(false);
                    qrCodeBtnSend.setEnabled(false);
                    btnSendDraw.setEnabled(false);
                    break;
                case BluetoothService.MESSAGE_UNABLE_CONNECT:     //ÎÞ·¨Á¬½ÓÉè±¸
                    Toast.makeText(MainActivity.this, "Unable to connect device",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }

    };
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_BT:      //ÇëÇó´ò¿ªÀ¶ÑÀ
                if (resultCode == Activity.RESULT_OK) {   //À¶ÑÀÒÑ¾­´ò¿ª
                    Toast.makeText(MainActivity.this, "Bluetooth open successful", Toast.LENGTH_LONG).show();
                }
                break;
            case REQUEST_CONNECT_DEVICE:     //ÇëÇóÁ¬½ÓÄ³Ò»À¶ÑÀÉè±¸
                if (resultCode == Activity.RESULT_OK) {   //ÒÑµã»÷ËÑË÷ÁÐ±íÖÐµÄÄ³¸öÉè±¸Ïî
                    String address = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);  //»ñÈ¡ÁÐ±íÏîÖÐÉè±¸µÄmacµØÖ·
                    con_dev = mService.getDevByMac(address);

                    mService.connect(con_dev);
                }
                break;
        }
    }

    //´òÓ¡Í¼ÐÎ
    @SuppressLint("SdCardPath")
    private void printImage() {
        byte[] sendData = null;
        PrintPic pg = new PrintPic();
        pg.initCanvas(576);
        pg.initPaint();
        pg.drawImage(0, 0, "/mnt/sdcard/icon.jpg");
        //
        sendData = pg.printDraw();
        mService.write(sendData);   //´òÓ¡byteÁ÷Êý¾Ý
        Log.d("À¶ÑÀµ÷ÊÔ",""+sendData.length);
    }
}