package edu.stanford.me202.smartbike;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ControlActivity extends AppCompatActivity {

    private BluetoothLEService bleService;
    private EditText bikeIdentifier;

    // light settings
    @BindView(R.id.lightmode_switch) MaterialAnimatedSwitch lightMode_switch;
    @BindView(R.id.lightstate_switch) MaterialAnimatedSwitch lightState_switch;
    @BindView(R.id.lightmodeText) TextView lightMode_text;
    @BindView(R.id.lightstateText) TextView lightState_text;
    // bluetooth status parameters
    @BindView(R.id.connectionStatus) TextView connectionStatus;
    @BindView(R.id.bikecode) TextView bikeCode;
    @BindView(R.id.lock_unlock) TextView lock_unlock;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothLEService.LocalBinder binder = (BluetoothLEService.LocalBinder)service;
            bleService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bleService = null;
        }
    };

    public BroadcastReceiver BluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(bleService.ACTION_DATA_AVAILABLE)){
                // do nothing
            }

            // Bluetooth Connected
            if (intent.getAction().equals(bleService.ACTION_GATT_CONNECTED)){
                // tell Arduino that BLE is connected
                Log.d("bluetooth", "CONNECTED");
                String string = "c";
                byte[] b = string.getBytes();
                bleService.writeRXCharacteristic(b);

                String bikeMAC = bikeIdentifier.getText().toString();
                Toast connectBluetoothToast = Toast.makeText(ControlActivity.this, "Successfully connected " + bikeMAC, Toast.LENGTH_SHORT);
                connectBluetoothToast.show();

                // update connection status and bike identifier on display
                connectionStatus.setText(R.string.connectionYes);
                lock_unlock.setText(R.string.lockhint);
                String bikeID = getString(R.string.bikeID) + bikeMAC;
                bikeCode.setText(bikeID);

                // allow access to light settings and show default settings
                lightMode_switch.setVisibility(View.VISIBLE);
                lightState_switch.setVisibility(View.VISIBLE);
                lightMode_text.setVisibility(View.VISIBLE);
                lightState_text.setVisibility(View.VISIBLE);
            }

            // Bluetooth Disconnected
            if (intent.getAction().equals(bleService.ACTION_GATT_DISCONNECTED)){
                // Show a toast
                Toast connectBluetoothToast = Toast.makeText(ControlActivity.this, R.string.disconnectwarning, Toast.LENGTH_SHORT);
                connectBluetoothToast.show();

                // hide light settings
                lightMode_switch.setVisibility(View.GONE);
                lightState_switch.setVisibility(View.GONE);
                lightMode_text.setVisibility(View.GONE);
                lightState_text.setVisibility(View.GONE);

                // update status
                connectionStatus.setText(R.string.connectionNo);
                lock_unlock.setText(R.string.unlockhint);
                bikeCode.setText(R.string.bikecodeNULL);
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        ButterKnife.bind(this);

        // Before unlocking, users have no access to light settings
        lightMode_switch.setVisibility(View.GONE);
        lightState_switch.setVisibility(View.GONE);
        lightMode_text.setVisibility(View.GONE);
        lightState_text.setVisibility(View.GONE);

        // check light state
        lightState_switch.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(boolean isChecked) {
                if (isChecked){ // light state: ON
                    // update light state text
                    lightState_text.setText(R.string.lightstateON);
                    // send string to Adafruit bluetooth module
                    Log.d("bluetooth", "ON");
                    String string = "n";
                    byte[] b = string.getBytes();
                    bleService.writeRXCharacteristic(b);
                } else { // light state: AUTO
                    lightState_text.setText(R.string.lightstateAUTO);
                    Log.d("bluetooth", "AUTO");
                    String string = "a";
                    byte[] b = string.getBytes();
                    bleService.writeRXCharacteristic(b);
                }
            }
        });

        lightMode_switch.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(boolean isChecked) {

                if (isChecked){ // light mode: BLINK
                    lightMode_text.setText(R.string.lightmodeBLINK);
                    Log.d("bluetooth", "BLINK");
                    String string = "b";
                    byte[] b = string.getBytes();
                    bleService.writeRXCharacteristic(b);
                } else { // light mode: SOLID
                    lightMode_text.setText(R.string.lightmodeSOLID);
                    Log.d("bluetooth", "SOLID");
                    String string = "s";
                    byte[] b = string.getBytes();
                    bleService.writeRXCharacteristic(b);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        /* Service start: this service will live and die with this activity; unless we
           control the lifecycle of this this by using startService(intent), stopService(intent)
         */
        Intent intent = new Intent(ControlActivity.this,BluetoothLEService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

        IntentFilter filter = new IntentFilter(bleService.ACTION_DATA_AVAILABLE);
        filter.addAction(bleService.ACTION_GATT_CONNECTED );
        filter.addAction(bleService.ACTION_GATT_DISCONNECTED );

        LocalBroadcastManager.getInstance(this).registerReceiver(BluetoothReceiver,filter);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(BluetoothReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        bleService = null;
    }

    public void unlock(View view) {
        if (lock_unlock.getText().toString().equals(getString(R.string.unlockhint))) {
            // configure attributes of dialog
            AlertDialog.Builder unlockDialogBuilder = new AlertDialog.Builder(this);
            unlockDialogBuilder.setTitle(R.string.unlockDialogTitle);
            unlockDialogBuilder.setCancelable(true);
            unlockDialogBuilder.setIcon(R.drawable.bluetooth);

            bikeIdentifier = new EditText(ControlActivity.this);
            bikeIdentifier.setHint(R.string.unlockDialogHint);
            bikeIdentifier.setText(R.string.bikeIdentifier);
            bikeIdentifier.setInputType(InputType.TYPE_CLASS_TEXT);
            bikeIdentifier.setMaxLines(1);
            unlockDialogBuilder.setView(bikeIdentifier);

            unlockDialogBuilder.setPositiveButton(
                    R.string.unlockDialogYes,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (bikeIdentifier.getText().toString().isEmpty()) {
                                Toast.makeText(ControlActivity.this, R.string.emptybikeIDerror, Toast.LENGTH_SHORT).show();
                            } else {
                                // get bike identifier user enters
                                String bike2connect = bikeIdentifier.getText().toString();
                                // show toast
                                Toast.makeText(ControlActivity.this, "Connecting " + bike2connect + "...", Toast.LENGTH_SHORT).show();
                                // Initialize bluetooth service
                                bleService.initialize();
                                // Try to connect to bluetooth of this address
                                bleService.connect(bike2connect);
                            }
                        }
                    });

            unlockDialogBuilder.setNegativeButton(
                    R.string.unlockDialogNo,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            // show dialog
            final AlertDialog unlockDialog = unlockDialogBuilder.create();
            unlockDialog.show();
        }

        if (lock_unlock.getText().toString().equals(getString(R.string.lockhint))) {
            // tell Arduino to reset
            Log.d("bluetooth", "RESET");
            String string = "r";
            byte[] b = string.getBytes();
            bleService.writeRXCharacteristic(b);
            bleService.disconnect();
        }
    }

    public void showRideHistory(View view) {
        Intent intent = new Intent(this, RideHistoryActivity.class);
        startActivity(intent);
        lightState_text.setText(R.string.lightstateAUTO);
    }
}
