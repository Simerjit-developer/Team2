// Generated code from Butter Knife. Do not modify!
package com.akwares.park_it.activities;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.akwares.park_it.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class RegisterPage1_ViewBinding implements Unbinder {
  private RegisterPage1 target;

  @UiThread
  public RegisterPage1_ViewBinding(RegisterPage1 target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public RegisterPage1_ViewBinding(RegisterPage1 target, View source) {
    this.target = target;

    target._email = Utils.findRequiredViewAsType(source, R.id.txtEmail, "field '_email'", EditText.class);
    target._next = Utils.findRequiredViewAsType(source, R.id.btnNext, "field '_next'", Button.class);
    target._login = Utils.findRequiredViewAsType(source, R.id.buttonLog, "field '_login'", Button.class);
    target.termsAccept = Utils.findRequiredViewAsType(source, R.id.acceptTermsCheckBox, "field 'termsAccept'", CheckBox.class);
    target.tvTerms = Utils.findRequiredViewAsType(source, R.id.termsTxtView, "field 'tvTerms'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    RegisterPage1 target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target._email = null;
    target._next = null;
    target._login = null;
    target.termsAccept = null;
    target.tvTerms = null;
  }
}
