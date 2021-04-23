/*
 * Copyright (C) 2021 The Android Open Source Project
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

import android.content.Context;
import androidx.annotation.ColorInt;
import androidx.annotation.VisibleForTesting;
import com.google.android.setupdesign.R;

/** The class to get dynamic colors. */
public final class DynamicColorPalette {

  @VisibleForTesting static int colorRes = 0;

  private DynamicColorPalette() {}

  /** Dynamic color category. */
  public enum DynamicColorCategory {
    PRIMARY_TEXT,
    SECONDARY_TEXT,
    DISABLED_OPTION,
    ERROR_WARNING,
    SUCCESS_DONE,
    FALLBACK_ACCENT
  }

  @ColorInt
  public static int getColor(Context context, DynamicColorCategory dynamicColorCategory) {
    switch (dynamicColorCategory) {
      case PRIMARY_TEXT:
        colorRes = R.color.sud_system_primary_text;
        break;
      case SECONDARY_TEXT:
        colorRes = R.color.sud_system_secondary_text;
        break;
      case DISABLED_OPTION:
        colorRes = R.color.sud_system_disable_option;
        break;
      case ERROR_WARNING:
        colorRes = R.color.sud_system_error_warning;
        break;
      case SUCCESS_DONE:
        colorRes = R.color.sud_system_success_done;
        break;
      case FALLBACK_ACCENT:
        colorRes = R.color.sud_system_fallback_accent;
        break;
        // fall out
    }

    return context.getResources().getColor(colorRes);
  }
}
