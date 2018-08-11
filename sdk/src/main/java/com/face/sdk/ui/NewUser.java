package com.face.sdk.ui;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.face.sdk.R;
import com.face.sdk.api.FaceCameraActivity;
import com.face.sdk.meta.Face;
import com.face.sdk.meta.Person;

/**
 * 新用户弹出注册窗口
 */
public class NewUser extends PopupWindow {

    private FaceCameraActivity ctx;
    private boolean isPop;
    private ImageView profileHead;
    private Face face;
    private Button confirmButton;
    private EditText usernameEditText;
    private View newUser;

    public NewUser (Context ctx, int width, int height) {
        super(width, height);
        this.ctx = (FaceCameraActivity) ctx;
        isPop = false;

        initWindow();
    }

    private void initWindow() {
        newUser = LayoutInflater.from(this.ctx).inflate(R.layout.new_user_activity, null);

        // 设置弹出activity的属性
        this.setContentView(newUser);
        this.setFocusable(true);
        this.setOutsideTouchable(true);

        // 设置淡入淡出动画
        this.setAnimationStyle(R.style.AnimHorizontal);
        this.setBackgroundDrawable(new ColorDrawable(newUser.getResources().getColor(R.color.colorPopupWindowBackground)));
        this.setElevation(20.0f);
        this.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        profileHead = (ImageView) newUser.findViewById(R.id.profile_head_image);
        profileHead.setAdjustViewBounds(true);
        // 确认后的操作
        confirmButton = (Button) newUser.findViewById(R.id.profile_head_button);
        usernameEditText = (EditText) newUser.findViewById(R.id.username_editview);

        // 确认按钮的监听器
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onConfirmButton();
            }
        });
    }

    @Override
    public void dismiss() {
        isPop = false;
        super.dismiss();
    }

    public void onConfirmButton() {
        String userName = usernameEditText.getText().toString();
        if (userName.length() > 0) {
            this.dismiss();
            Person person = new Person(userName, face.getFaceBitmap(), face);
            ctx.registerPersonRepository(person);
            isPop = false;
        }
    }

    public void showAsDropDown(int xoff, int yoff) {
        initWindow();
        // 设置imageview为头像图
        profileHead.setImageBitmap(face.getFaceBitmap());
        super.showAsDropDown(newUser, xoff, yoff);
    }

    public void setFace(Face face) {
        this.face = face;
    }

    public boolean isPop() {
        return isPop;
    }

    public void setPop(boolean pop) {
        isPop = pop;
    }
}
