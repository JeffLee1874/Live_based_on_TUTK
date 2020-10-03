package com.example.tutkdemo.view;

import android.content.Context;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.example.tutkdemo.R;

import razerdp.basepopup.BasePopupWindow;
import razerdp.util.animation.AnimationHelper;
import razerdp.util.animation.Direction;
import razerdp.util.animation.RotationConfig;
import razerdp.util.animation.ScaleConfig;
import razerdp.util.animation.TranslationConfig;

public class PopUpWindow extends BasePopupWindow {
    private View connect;

    public PopUpWindow(Context context) {
        super(context);
        connect = onCreateContentView();
    }


    @Override
    public View onCreateContentView() {
        return createPopupById(R.layout.pop_up_window);
    }

    @Override
    protected Animation onCreateShowAnimation() {
        return AnimationHelper.asAnimation()
                .withTranslation(TranslationConfig.FROM_BOTTOM)
                .withRotation(new RotationConfig()
                        .from(-360)
                        .to(0))
                .toShow();
    }

    @Override
    protected Animation onCreateDismissAnimation() {
        return AnimationHelper.asAnimation()
                .withTranslation(TranslationConfig.TO_TOP)
                .withRotation(new RotationConfig()
                        .from(0)
                        .to(-360))
                .toShow();
    }
}
