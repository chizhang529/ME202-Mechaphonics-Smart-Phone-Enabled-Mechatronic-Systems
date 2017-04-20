package edu.stanford.me202.smartbike;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ControlActivity extends AppCompatActivity {

    @BindView(R.id.lightmode_switch)
    MaterialAnimatedSwitch lightMode;
    @BindView(R.id.lightstate_switch)
    MaterialAnimatedSwitch lightState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        ButterKnife.bind(this);
    }

    public void unlock(View view) {
        // configure attributes of dialog
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
                        if (bikeIdentifier.getText().toString().isEmpty())
                            Toast.makeText(ControlActivity.this, "Error: please enter an valid bike identifier", Toast.LENGTH_SHORT).show();
                        else
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

        // show dialog
        final AlertDialog unlockDialog = unlockDialogBuilder.create();
        unlockDialog.show();
    }

    public void showRideHistory(View view) {
        Intent intent = new Intent(this, RideHistoryActivity.class);
        startActivity(intent);
    }
}
