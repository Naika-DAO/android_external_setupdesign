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

package com.google.android.setupdesign.transition.support;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.fragment.app.Fragment;
import android.util.Log;
import com.google.android.material.transition.platform.MaterialSharedAxis;
import com.google.android.setupcompat.partnerconfig.PartnerConfig;
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper;

/** Helper class for apply the transition to the pages which uses support library. */
public class TransitionHelper {

  private static final String TAG = "TransitionHelper";

  /**
   * No override. If this is specified as the transition, overridePendingTransition will not be
   * called.
   */
  public static final int TRANSITION_NO_OVERRIDE = 0;

  /** Override the transition to the specific type that will depend on the partner resource. */
  private static final int CONFIG_TRANSITION_SHARED_X_AXIS = 1;

  private TransitionHelper() {}

  /**
   * Apply the transition for going forward which is decided by partner resource {@link
   * PartnerConfig#CONFIG_TRANSITION_TYPE} and system property {@code setupwizard.transition_type}.
   * The default transition that will be applied is {@link #TRANSITION_NO_OVERRIDE}. The timing to
   * apply the transition is going forward from the previous {@link Fragment} to this, or going
   * forward from this {@link Fragment} to the next.
   */
  public static void applyForwardTransition(Fragment fragment) {
    int transitionType;
    if (VERSION.SDK_INT > VERSION_CODES.R) {
      transitionType =
          PartnerConfigHelper.get(fragment.getContext())
              .getInteger(
                  fragment.getContext(),
                  PartnerConfig.CONFIG_TRANSITION_TYPE,
                  TRANSITION_NO_OVERRIDE);

      if (CONFIG_TRANSITION_SHARED_X_AXIS == transitionType) {
        MaterialSharedAxis exitTransition =
            new MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true);
        fragment.setExitTransition(exitTransition);

        MaterialSharedAxis enterTransition =
            new MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true);
        fragment.setEnterTransition(enterTransition);
      }
    } else {
      Log.w(TAG, "Not apply the forward transition for support lib's fragment.");
    }
  }

  /**
   * Apply the transition for going backward which is decided by partner resource {@link
   * PartnerConfig#CONFIG_TRANSITION_TYPE} and system property {@code setupwizard.transition_type}.
   * The default transition that will be applied is {@link #TRANSITION_NO_OVERRIDE}. The timing to
   * apply the transition is going backward from the next {@link Fragment} to this, or going
   * backward from this {@link Fragment} to the previous.
   */
  public static void applyBackwardTransition(Fragment fragment) {
    int transitionType;
    if (VERSION.SDK_INT > VERSION_CODES.R) {
      transitionType =
          PartnerConfigHelper.get(fragment.getContext())
              .getInteger(
                  fragment.getContext(),
                  PartnerConfig.CONFIG_TRANSITION_TYPE,
                  TRANSITION_NO_OVERRIDE);

      if (CONFIG_TRANSITION_SHARED_X_AXIS == transitionType) {
        MaterialSharedAxis returnTransition =
            new MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false);
        fragment.setReturnTransition(returnTransition);

        MaterialSharedAxis reenterTransition =
            new MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false);
        fragment.setReenterTransition(reenterTransition);
      }
    } else {
      Log.w(TAG, "Not apply the backward transition for support lib's fragment.");
    }
  }
}
