package edu.stanford.me202.lab1_smartbike;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;

public class ControlActivity extends AppCompatActivity {
    private MaterialAnimatedSwitch lightMode;
    private MaterialAnimatedSwitch lightState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        // initialize objects
        lightMode = (MaterialAnimatedSwitch) findViewById(R.id.lightmode_switch);
        lightState = (MaterialAnimatedSwitch) findViewById(R.id.lightstate_switch);
    }

    public void unlock(View view) {
        AlertDialog.Builder unlockDialogBuilder = new AlertDialog.Builder(this);
        unlockDialogBuilder.setTitle(R.string.unlockDialogTitle);
        unlockDialogBuilder.setCancelable(true);
        unlockDialogBuilder.setIcon(R.drawable.bluetooth);

        final EditText bikeIdentifier = new EditText(this);
        bikeIdentifier.setHint(R.string.unlockDialogHint);
        bikeIdentifier.setInputType(InputType.TYPE_CLASS_TEXT);
        bikeIdentifier.setMaxLines(1);
        unlockDialogBuilder.setView(bikeIdentifier);

        unlockDialogBuilder.setPositiveButton(
                R.string.unlockDialogYes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(ControlActivity.this, bikeIdentifier.getText(), Toast.LENGTH_SHORT).show();
                    }
                });

        unlockDialogBuilder.setNegativeButton(
                R.string.unlockDialogNo,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });


        AlertDialog unlockDialog = unlockDialogBuilder.create();
        unlockDialog.show();
    }
}
