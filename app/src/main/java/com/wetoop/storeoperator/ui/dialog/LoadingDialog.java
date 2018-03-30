package com.wetoop.storeoperator.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.widget.TextView;

import com.wetoop.storeoperator.R;

/**
 * Created by User on 2018/1/19.
 */

public class LoadingDialog {

    private static Dialog loadingDialog;

    public static void show(Activity activity, String loadingTextStr) {
        show(activity, loadingTextStr, null);
    }

    public static void show(Activity activity, String loadingTextStr, final DialogInterface.OnCancelListener listener) {
        if (loadingDialog == null) {
            loadingDialog = new Dialog(activity, R.style.LoadingDialogTheme);
            loadingDialog.setContentView(R.layout.dialog_loading);
            loadingDialog.setCanceledOnTouchOutside(false);
            loadingDialog.getWindow().setDimAmount(0.0f);
            TextView loadingText = (TextView) loadingDialog.findViewById(R.id.loading_text);
            loadingText.setText(loadingTextStr);
        }
        if (loadingDialog.isShowing()) {
            return;
        }
        loadingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (listener != null)
                    listener.onCancel(dialog);
                loadingDialog.dismiss();
                loadingDialog = null;
            }
        });
        if (activity.isFinishing()) return;
        loadingDialog.show();
    }

    public static void hide(Activity activity) {
        if (loadingDialog != null) {
            if (activity.isFinishing()) return;
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }

    public static void hide() {
        if (loadingDialog != null) {
            if (loadingDialog.getOwnerActivity() != null && loadingDialog.getOwnerActivity().isFinishing())
                return;
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }
}
