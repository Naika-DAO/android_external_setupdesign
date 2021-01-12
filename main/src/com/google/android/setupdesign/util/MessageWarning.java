/*
 * Copyright (C) 2020 The Android Open Source Project
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

import android.os.Build;
import android.util.Log;

/** Utilities to warn developer in Android S later debug build. */
public final class MessageWarning {

  private static final String TAG = "ToastWarning";

  /**
   * Shows the warning message.
   *
   * @param warningMsg The message to show for warning .
   */
  public static void makeWarning(String warningMsg) {
    if (isAtLeastS()) {
      Log.w(TAG, warningMsg);
    }
  }

  public static boolean isAtLeastS() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
      return false;
    }
    return (Build.VERSION.CODENAME.equals("REL") && Build.VERSION.SDK_INT >= 31)
        || (Build.VERSION.CODENAME.length() == 1
            && Build.VERSION.CODENAME.charAt(0) >= 'S'
            && Build.VERSION.CODENAME.charAt(0) <= 'Z');
  }

  private MessageWarning() {}
}
