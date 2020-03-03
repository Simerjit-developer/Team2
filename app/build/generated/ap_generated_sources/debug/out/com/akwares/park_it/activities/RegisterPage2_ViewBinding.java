// Generated code from Butter Knife. Do not modify!
package com.akwares.park_it.activities;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.akwares.park_it.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class RegisterPage2_ViewBinding implements Unbinder {
  private RegisterPage2 target;

  @UiThread
  public RegisterPage2_ViewBinding(RegisterPage2 target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public RegisterPage2_ViewBinding(RegisterPage2 target, View source) {
    this.target = target;

    target._username = Utils.findRequiredViewAsType(source, R.id.txtUsername, "field '_username'", EditText.class);
    target._name = Utils.findRequiredViewAsType(source, R.id.txtName, "field '_name'", EditText.class);
    target._password = Utils.findRequiredViewAsType(source, R.id.txtPass, "field '_password'", EditText.class);
    target._register = Utils.findRequiredViewAsType(source, R.id.btnRegister, "field '_register'", Button.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    RegisterPage2 target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target._username = null;
    target._name = null;
    target._password = null;
    target._register = null;
  }
}
