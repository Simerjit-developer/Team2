// Generated code from Butter Knife. Do not modify!
package com.akwares.park_it.activities;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.akwares.park_it.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class Login_ViewBinding implements Unbinder {
  private Login target;

  @UiThread
  public Login_ViewBinding(Login target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public Login_ViewBinding(Login target, View source) {
    this.target = target;

    target._usernameText = Utils.findRequiredViewAsType(source, R.id.txtUsername, "field '_usernameText'", EditText.class);
    target._passwordText = Utils.findRequiredViewAsType(source, R.id.etxtPass, "field '_passwordText'", EditText.class);
    target._loginButton = Utils.findRequiredViewAsType(source, R.id.btnLogin, "field '_loginButton'", Button.class);
    target._signupLink = Utils.findRequiredViewAsType(source, R.id.buttonReg, "field '_signupLink'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    Login target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target._usernameText = null;
    target._passwordText = null;
    target._loginButton = null;
    target._signupLink = null;
  }
}
