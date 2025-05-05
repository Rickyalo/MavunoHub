package com.example.mavunohub;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SignOutDialog extends Dialog {

    public interface SignOutListener {
        void onConfirmSignOut();
    }

    public SignOutDialog(Context context, SignOutListener listener) {
        super(context);
        setContentView(R.layout.dialog_signout);

        TextView signOutMessage = findViewById(R.id.signOutMessage);
        Button btnCancel = findViewById(R.id.btnCancel);
        Button btnConfirmSignOut = findViewById(R.id.btnConfirmSignOut);


        btnCancel.setOnClickListener(v -> dismiss());


        btnConfirmSignOut.setOnClickListener(v -> {
            listener.onConfirmSignOut();
            dismiss();
        });
    }
}
