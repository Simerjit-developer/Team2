// Generated code from Butter Knife. Do not modify!
package com.akwares.park_it.activities;

import android.view.View;
import android.widget.Button;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.akwares.park_it.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class ActivateAccountActivity_ViewBinding implements Unbinder {
  private ActivateAccountActivity target;

  @UiThread
  public ActivateAccountActivity_ViewBinding(ActivateAccountActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public ActivateAccountActivity_ViewBinding(ActivateAccountActivity target, View source) {
    this.target = target;

    target.btn_login = Utils.findRequiredViewAsType(source, R.id.buttonLog, "field 'btn_login'", Button.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    ActivateAccountActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.btn_login = null;
  }
}
