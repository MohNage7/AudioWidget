package mohnage7.audiowidget.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import mohnage7.audiowidget.R;
import mohnage7.audiowidget.databinding.ActivityMainBinding;

public class AudioActivity extends AppCompatActivity implements PermissionHandler{

    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setHandler(this);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted
                updatePermissionViews(true);

            } else {
                // permission denied
                updatePermissionViews(false);
                showApplicationSettingsDialog();
            }
        }
    }

    private void updatePermissionViews(boolean permissionGranted) {
        if (permissionGranted) {
            binding.permissionLayout.setVisibility(View.GONE);
            binding.permissionGrantedTv.setVisibility(View.VISIBLE);
        } else {
            binding.permissionLayout.setVisibility(View.VISIBLE);
            binding.permissionGrantedTv.setVisibility(View.GONE);
        }
    }

    public void showApplicationSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.permission_dialog_title))
                .setMessage(getString(R.string.msg_permission_required))
                .setPositiveButton(getString(R.string.open_settings), (dialog, which) -> {
                    // continue with delete
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss());
        AlertDialog permissionDialog = builder.create();
        permissionDialog.setCanceledOnTouchOutside(false);
        if (!permissionDialog.isShowing()) {
            permissionDialog.show();
            permissionDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.icon_grey));
            permissionDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
        }
    }

    @Override
    public void onCheckPermissionClicked() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            updatePermissionViews(false);
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // show explanation
                showApplicationSettingsDialog();
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }
        } else {
            updatePermissionViews(true);
        }
    }
}
