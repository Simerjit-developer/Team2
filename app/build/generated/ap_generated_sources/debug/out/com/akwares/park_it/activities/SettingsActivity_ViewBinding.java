// Generated code from Butter Knife. Do not modify!
package com.akwares.park_it.activities;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.akwares.park_it.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class SettingsActivity_ViewBinding implements Unbinder {
  private SettingsActivity target;

  @UiThread
  public SettingsActivity_ViewBinding(SettingsActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public SettingsActivity_ViewBinding(SettingsActivity target, View source) {
    this.target = target;

    target.placeRecyclerView = Utils.findRequiredViewAsType(source, R.id.recyclerTypes, "field 'placeRecyclerView'", RecyclerView.class);
    target.progressBar = Utils.findRequiredViewAsType(source, R.id.pbar, "field 'progressBar'", ProgressBar.class);
    target.Warningtxt = Utils.findRequiredViewAsType(source, R.id.warningtxt, "field 'Warningtxt'", TextView.class);
    target.Warningimg = Utils.findRequiredViewAsType(source, R.id.internalErrorIMG, "field 'Warningimg'", ImageView.class);
    target.view = Utils.findRequiredView(source, android.R.id.content, "field 'view'");
  }

  @Override
  @CallSuper
  public void unbind() {
    SettingsActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.placeRecyclerView = null;
    target.progressBar = null;
    target.Warningtxt = null;
    target.Warningimg = null;
    target.view = null;
  }
}
