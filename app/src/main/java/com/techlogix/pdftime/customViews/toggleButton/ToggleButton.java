package com.techlogix.pdftime.customViews.toggleButton;

import android.widget.Checkable;

public interface ToggleButton extends Checkable {

    void setOnCheckedChangeListener(OnCheckedChangeListener listener);
}

