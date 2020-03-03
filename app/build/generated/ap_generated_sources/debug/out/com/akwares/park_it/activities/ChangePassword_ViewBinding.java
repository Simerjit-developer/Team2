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

public class ChangePassword_ViewBinding implements Unbinder {
  private ChangePassword target;

  @UiThread
  public ChangePassword_ViewBinding(ChangePassword target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public ChangePassword_ViewBinding(ChangePassword target, View source) {
    this.target = target;

    target.oldpwd = Utils.findRequiredViewAsType(source, R.id.pswOld, "field 'oldpwd'", EditText.class);
    target.newpwd = Utils.findRequiredViewAsType(source, R.id.pswNew, "field 'newpwd'", EditText.class);
    target.newpwdR = Utils.findRequiredViewAsType(source, R.id.pswNewRep, "field 'newpwdR'", EditText.class);
    target.save = Utils.findRequiredViewAsType(source, R.id.btnSavepsw, "field 'save'", Button.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    ChangePassword target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.oldpwd = null;
    target.newpwd = null;
    target.newpwdR = null;
    target.save = null;
  }
}
