// Generated code from Butter Knife. Do not modify!
package com.akwares.park_it.activities;

import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.akwares.park_it.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.lang.IllegalStateException;
import java.lang.Override;

public class BotActivity_ViewBinding implements Unbinder {
  private BotActivity target;

  @UiThread
  public BotActivity_ViewBinding(BotActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public BotActivity_ViewBinding(BotActivity target, View source) {
    this.target = target;

    target.sendMessage = Utils.findRequiredViewAsType(source, R.id.btn_send, "field 'sendMessage'", FloatingActionButton.class);
    target.enterMessage = Utils.findRequiredViewAsType(source, R.id.ed_msg, "field 'enterMessage'", EditText.class);
    target.listView = Utils.findRequiredViewAsType(source, R.id.listViewMsg, "field 'listView'", ListView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    BotActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.sendMessage = null;
    target.enterMessage = null;
    target.listView = null;
  }
}
