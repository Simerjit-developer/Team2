// Generated code from Butter Knife. Do not modify!
package com.akwares.park_it.activities;

import android.view.View;
import android.webkit.WebView;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.akwares.park_it.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class Terms_ViewBinding implements Unbinder {
  private Terms target;

  @UiThread
  public Terms_ViewBinding(Terms target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public Terms_ViewBinding(Terms target, View source) {
    this.target = target;

    target.webview = Utils.findRequiredViewAsType(source, R.id.termsView, "field 'webview'", WebView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    Terms target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.webview = null;
  }
}
