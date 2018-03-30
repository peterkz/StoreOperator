package com.wetoop.storeoperator.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.wetoop.storeoperator.R;

/**
 * @author Parck.
 * @date 2017/11/8.
 * @desc
 */

public class SplashDialog {

    private Activity activity;
    private Dialog dialog;

    public SplashDialog(Activity activity) {
        this.dialog = new Dialog(activity, R.style.SplashDialogTheme);
        this.dialog.setContentView(R.layout.dialog_splash);
        this.dialog.getWindow().setDimAmount(0.9f);
        this.dialog.setCanceledOnTouchOutside(true);
        Window window = this.dialog.getWindow();
        window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);
        this.activity = activity;
        ImageView logoImage = (ImageView) dialog.findViewById(R.id.logo_image);
        AlphaAnimation alphaAnimation = (AlphaAnimation) AnimationUtils.loadAnimation(activity, R.anim.anim_alpha_splash);
        logoImage.startAnimation(alphaAnimation);
    }

    public void show() {
        dialog.show();
    }

    public void hide() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
