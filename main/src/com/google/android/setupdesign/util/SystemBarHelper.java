/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.setupdesign.util;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import androidx.annotation.RequiresPermission;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import com.google.android.setupcompat.util.SystemBarBaseHelper;

/**
 * A helper class to manage the system navigation bar and status bar. This will add various
 * systemUiVisibility flags to the given Window or View to make them follow the Setup Wizard style.
 *
 * <p>When the useImmersiveMode intent extra is true, a screen in Setup Wizard should hide the
 * system bars using methods from this class. For Lollipop, {@link
 * #hideSystemBars(android.view.Window)} will completely hide the system navigation bar and change
 * the status bar to transparent, and layout the screen contents (usually the illustration) behind
 * it.
 */
public class SystemBarHelper {

  private static final String TAG = "SystemBarHelper";

  /** Needs to be equal to View.STATUS_BAR_DISABLE_BACK */
  private static final int STATUS_BAR_DISABLE_BACK = 0x00400000;

  /**
   * Hide the navigation bar for a dialog.
   *
   * <p>This will only take effect in versions Lollipop or above. Otherwise this is a no-op.
   *
   * @deprecated If the layout is instance of TemplateLayout, please use
   *     SystemNavBarMixin.hideSystemBars.
   */
  @Deprecated
  public static void hideSystemBars(final Dialog dialog) {
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      final Window window = dialog.getWindow();
      temporarilyDisableDialogFocus(window);
      SystemBarBaseHelper.addVisibilityFlag(window, SystemBarBaseHelper.DIALOG_IMMERSIVE_FLAGS);
      SystemBarBaseHelper.addImmersiveFlagsToDecorView(
          window, SystemBarBaseHelper.DIALOG_IMMERSIVE_FLAGS);

      // Also set the navigation bar and status bar to transparent color. Note that this
      // doesn't work if android.R.boolean.config_enableTranslucentDecor is false.
      window.setNavigationBarColor(0);
      window.setStatusBarColor(0);
    }
  }

  /**
   * Hide the navigation bar, make the color of the status and navigation bars transparent, and
   * specify {@link View#SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN} flag so that the content is laid-out
   * behind the transparent status bar. This is commonly used with {@link
   * android.app.Activity#getWindow()} to make the navigation and status bars follow the Setup
   * Wizard style.
   *
   * <p>This will only take effect in versions Lollipop or above. Otherwise this is a no-op.
   *
   * @deprecated If the layout instance of TemplateLayout, please use
   *     SystemNavBarMixin.hideSystemBars.
   */
  @Deprecated
  public static void hideSystemBars(final Window window) {
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      SystemBarBaseHelper.addVisibilityFlag(window, SystemBarBaseHelper.DEFAULT_IMMERSIVE_FLAGS);
      SystemBarBaseHelper.addImmersiveFlagsToDecorView(
          window, SystemBarBaseHelper.DEFAULT_IMMERSIVE_FLAGS);

      // Also set the navigation bar and status bar to transparent color. Note that this
      // doesn't work if android.R.boolean.config_enableTranslucentDecor is false.
      window.setNavigationBarColor(0);
      window.setStatusBarColor(0);
    }
  }

  /**
   * Revert the actions of hideSystemBars. Note that this will remove the system UI visibility flags
   * regardless of whether it is originally present. You should also manually reset the navigation
   * bar and status bar colors, as this method doesn't know what value to revert it to.
   *
   * @deprecated If the layout is instance of TemplateLayout, please use
   *     SystemNavBarMixin.showSystemBars.
   */
  @Deprecated
  public static void showSystemBars(final Window window, final Context context) {
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      SystemBarBaseHelper.removeVisibilityFlag(window, SystemBarBaseHelper.DEFAULT_IMMERSIVE_FLAGS);
      SystemBarBaseHelper.removeImmersiveFlagsFromDecorView(
          window, SystemBarBaseHelper.DEFAULT_IMMERSIVE_FLAGS);

      if (context != null) {
        //noinspection AndroidLintInlinedApi
        final TypedArray typedArray =
            context.obtainStyledAttributes(
                new int[] {android.R.attr.statusBarColor, android.R.attr.navigationBarColor});
        final int statusBarColor = typedArray.getColor(0, 0);
        final int navigationBarColor = typedArray.getColor(1, 0);
        window.setStatusBarColor(statusBarColor);
        window.setNavigationBarColor(navigationBarColor);
        typedArray.recycle();
      }
    }
  }

  /**
   * Convenience method to add a visibility flag in addition to the existing ones.
   *
   * @deprecated Use SystemBarBaseHelper.addVisibilityFlag(final View view, final int flag).
   */
  @Deprecated
  public static void addVisibilityFlag(final View view, final int flag) {
    SystemBarBaseHelper.addVisibilityFlag(view, flag);
  }

  /**
   * Convenience method to add a visibility flag in addition to the existing ones.
   *
   * @deprecated Use SystemBarBaseHelper.addVisibilityFlag(final Window window, final int flag).
   */
  @Deprecated
  public static void addVisibilityFlag(final Window window, final int flag) {
    SystemBarBaseHelper.addVisibilityFlag(window, flag);
  }

  /**
   * Convenience method to remove a visibility flag from the view, leaving other flags that are not
   * specified intact.
   *
   * @deprecated Use SystemBarBaseHelper.removeVisibilityFlag(final View view, final int flag).
   */
  @Deprecated
  public static void removeVisibilityFlag(final View view, final int flag) {
    SystemBarBaseHelper.removeVisibilityFlag(view, flag);
  }

  /**
   * Convenience method to remove a visibility flag from the window, leaving other flags that are
   * not specified intact.
   *
   * @deprecated Use SystemBarBaseHelper.removeVisibilityFlag(final Window window, final int flag).
   */
  @Deprecated
  public static void removeVisibilityFlag(final Window window, final int flag) {
    SystemBarBaseHelper.removeVisibilityFlag(window, flag);
  }

  /**
   * Sets whether the back button on the software navigation bar is visible. This only works if you
   * have the STATUS_BAR permission. Otherwise framework will filter out this flag and this method
   * call will not have any effect.
   *
   * <p>IMPORTANT: Do not assume that users have no way to go back when the back button is hidden.
   * Many devices have physical back buttons, and accessibility services like TalkBack may have
   * gestures mapped to back. Please use onBackPressed, onKeyDown, or other similar ways to make
   * sure back button events are still handled (or ignored) properly.
   */
  @RequiresPermission("android.permission.STATUS_BAR")
  public static void setBackButtonVisible(final Window window, final boolean visible) {
    if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {
      if (visible) {
        SystemBarBaseHelper.removeVisibilityFlag(window, STATUS_BAR_DISABLE_BACK);
        SystemBarBaseHelper.removeImmersiveFlagsFromDecorView(window, STATUS_BAR_DISABLE_BACK);
      } else {
        SystemBarBaseHelper.addVisibilityFlag(window, STATUS_BAR_DISABLE_BACK);
        SystemBarBaseHelper.addImmersiveFlagsToDecorView(window, STATUS_BAR_DISABLE_BACK);
      }
    }
  }

  /**
   * Set a view to be resized when the keyboard is shown. This will set the bottom margin of the
   * view to be immediately above the keyboard, and assumes that the view sits immediately above the
   * navigation bar.
   *
   * <p>Note that you must set {@link android.R.attr#windowSoftInputMode} to {@code adjustResize}
   * for this class to work. Otherwise window insets are not dispatched and this method will have no
   * effect.
   *
   * <p>This will only take effect in versions Lollipop or above. Otherwise this is a no-op.
   *
   * @param view The view to be resized when the keyboard is shown.
   */
  public static void setImeInsetView(final View view) {
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      view.setOnApplyWindowInsetsListener(new WindowInsetsListener());
    }
  }

  /**
   * Apply a hack to temporarily set the window to not focusable, so that the navigation bar will
   * not show up during the transition.
   */
  private static void temporarilyDisableDialogFocus(final Window window) {
    window.setFlags(
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    // Add the SOFT_INPUT_IS_FORWARD_NAVIGATION_FLAG. This is normally done by the system when
    // FLAG_NOT_FOCUSABLE is not set. Setting this flag allows IME to be shown automatically
    // if the dialog has editable text fields.
    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_IS_FORWARD_NAVIGATION);
    new Handler()
        .post(
            new Runnable() {
              @Override
              public void run() {
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
              }
            });
  }

  @TargetApi(VERSION_CODES.LOLLIPOP)
  private static class WindowInsetsListener implements View.OnApplyWindowInsetsListener {
    private int bottomOffset;
    private boolean hasCalculatedBottomOffset = false;

    @Override
    public WindowInsets onApplyWindowInsets(View view, WindowInsets insets) {
      if (!hasCalculatedBottomOffset) {
        bottomOffset = getBottomDistance(view);
        hasCalculatedBottomOffset = true;
      }

      int bottomInset = insets.getSystemWindowInsetBottom();

      final int bottomMargin = Math.max(insets.getSystemWindowInsetBottom() - bottomOffset, 0);

      final ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
      // Check that we have enough space to apply the bottom margins before applying it.
      // Otherwise the framework may think that the view is empty and exclude it from layout.
      if (bottomMargin < lp.bottomMargin + view.getHeight()) {
        lp.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, bottomMargin);
        view.setLayoutParams(lp);
        bottomInset = 0;
      }

      return insets.replaceSystemWindowInsets(
          insets.getSystemWindowInsetLeft(),
          insets.getSystemWindowInsetTop(),
          insets.getSystemWindowInsetRight(),
          bottomInset);
    }
  }

  private static int getBottomDistance(View view) {
    int[] coords = new int[2];
    view.getLocationInWindow(coords);
    return view.getRootView().getHeight() - coords[1] - view.getHeight();
  }
}
