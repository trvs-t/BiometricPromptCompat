package com.innopage.biometric;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

class BiometricDialogCompat extends BottomSheetDialog implements View.OnClickListener {

    private Context context;

    private Button btnCancel;
    private ImageView imgLogo;
    private TextView itemTitle, itemDescription, itemSubtitle, itemStatus;

    public BiometricDialogCompat(@NonNull Context context) {
        super(context, R.style.BottomSheetDialogTheme);
        this.context = context.getApplicationContext();
        setDialogView();
    }

    public BiometricDialogCompat(@NonNull Context context, int theme) {
        super(context, theme);
        this.context = context.getApplicationContext();
        setDialogView();
    }

    public BiometricDialogCompat(@NonNull Context context, boolean cancelable, DialogInterface.OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.context = context.getApplicationContext();
        setDialogView();
    }

    private void setDialogView() {
        View bottomSheetView = getLayoutInflater().inflate(R.layout.biometric_bottom_sheet, null);
        setContentView(bottomSheetView);

        btnCancel = findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(this);

        imgLogo = findViewById(R.id.img_logo);
        itemTitle = findViewById(R.id.item_title);
        itemStatus = findViewById(R.id.item_status);
        itemSubtitle = findViewById(R.id.item_subtitle);
        itemDescription = findViewById(R.id.item_description);

        updateLogo();
    }

    public void setTitle(String title) {
        itemTitle.setText(title);
    }

    public void updateStatus(String status) {
        itemStatus.setText(status);
    }

    public void setSubtitle(String subtitle) {
        itemSubtitle.setText(subtitle);
    }

    public void setDescription(String description) {
        itemDescription.setText(description);
    }

    public void setButtonText(String negativeButtonText) {
        btnCancel.setText(negativeButtonText);
    }

    private void updateLogo() {
        try {
            Drawable drawable = getContext().getPackageManager().getApplicationIcon(context.getPackageName());
            imgLogo.setImageDrawable(drawable);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        cancel();
    }
}