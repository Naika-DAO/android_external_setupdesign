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

package com.google.android.setupdesign.transition;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Parcelable;
import android.util.Log;
import android.view.Window;
import androidx.annotation.IntDef;
import com.google.android.material.transition.platform.MaterialSharedAxis;
import com.google.android.setupcompat.partnerconfig.PartnerConfig;
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper;
import com.google.android.setupdesign.R;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/** Helper class for apply the transition to the pages which uses platform version. */
public class TransitionHelper {

  private static final String TAG = "TransitionHelper";

  /*
   * In Setup Wizard, all Just-a-sec style screens (i.e. screens that has an indeterminate
   * progress bar and automatically finishes itself), should do a cross-fade when entering or
   * exiting the screen. For all other screens, the transition should be a slide-in-from-right.
   *
   * We use two different ways to override the transitions. The first is calling
   * overridePendingTransition in code, and the second is using windowAnimationStyle in the theme.
   * They have the following priority when framework is figuring out what transition to use:
   * 1. overridePendingTransition, entering activity (highest priority)
   * 2. overridePendingTransition, exiting activity
   * 3. windowAnimationStyle, entering activity
   * 4. windowAnimationStyle, exiting activity
   *
   * This is why, in general, overridePendingTransition is used to specify the fade animation,
   * while windowAnimationStyle is used to specify the slide transition. This way fade animation
   * will take priority over the slide animation.
   *
   * Below are types of animation when switching activities. These are return values for
   * {@link #getTransition()}. Each of these values represents 4 animations: (backward exit,
   * backward enter, forward exit, forward enter).
   *
   * We override the transition in the following flow
   * +--------------+-------------------------+--------------------------+
   * |              | going forward           | going backward           |
   * +--------------+-------------------------+--------------------------+
   * | old activity | startActivity(OnResult) | onActivityResult         |
   * +--------------+-------------------------+--------------------------+
   * | new activity | onStart                 | finish (RESULT_CANCELED) |
   * +--------------+-------------------------+--------------------------+
   */

  /** The constant of transition type. */
  @Retention(RetentionPolicy.SOURCE)
  @IntDef({
    TRANSITION_NONE,
    TRANSITION_NO_OVERRIDE,
    TRANSITION_FRAMEWORK_DEFAULT,
    TRANSITION_SLIDE,
    TRANSITION_FADE,
    TRANSITION_FRAMEWORK_DEFAULT_PRE_P,
    TRANSITION_CAPTIVE,
  })
  public @interface TransitionType {}

  /** No transition, as in overridePendingTransition(0, 0). */
  public static final int TRANSITION_NONE = -1;

  /**
   * No override. If this is specified as the transition, overridePendingTransition will not be
   * called.
   */
  public static final int TRANSITION_NO_OVERRIDE = 0;

  /**
   * Override the transition to the framework default. This values are read from {@link
   * android.R.style#Animation_Activity}.
   */
  public static final int TRANSITION_FRAMEWORK_DEFAULT = 1;

  /** Override the transition to a slide-in-from-right (or from-left for RTL locales). */
  public static final int TRANSITION_SLIDE = 2;

  /**
   * Override the transition to fade in the new activity, while keeping the old activity. Setup
   * wizard does not use cross fade to avoid the bright-dim-bright effect when transitioning between
   * two screens that look similar.
   */
  public static final int TRANSITION_FADE = 3;

  /** Override the transition to the old framework default pre P. */
  public static final int TRANSITION_FRAMEWORK_DEFAULT_PRE_P = 4;

  /**
   * Override the transition to the specific transition and the transition type will depends on the
   * partner resource.
   */
  // TODO: Add new partner resource to determine which transition type would be apply.
  public static final int TRANSITION_CAPTIVE = 5;

  /** Override the transition to the specific type that will depend on the partner resource. */
  private static final int CONFIG_TRANSITION_SHARED_X_AXIS = 1;

  /**
   * Passed in an intent as EXTRA_ACTIVITY_OPTIONS. This is the {@link ActivityOptions} of the
   * transition used in {@link Activity#startActivity} or {@link Activity#startActivityForResult}.
   */
  public static final String EXTRA_ACTIVITY_OPTIONS = "activity_options";

  private TransitionHelper() {}

  /**
   * Apply the transition for going forward which is decided by partner resource {@link
   * PartnerConfig#CONFIG_TRANSITION_TYPE} and system property {@code setupwizard.transition_type}.
   * The default transition that will be applied is {@link #TRANSITION_SLIDE}. The timing to apply
   * the transition is going forward from the previous activity to this, or going forward from this
   * activity to the next.
   *
   * <p>For example, in the flow below, the forward transitions will be applied to all arrows
   * pointing to the right. Previous screen --> This screen --> Next screen
   */
  public static void applyForwardTransition(Activity activity) {
    applyForwardTransition(activity, TRANSITION_CAPTIVE);
  }

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
      Log.w(TAG, "Not apply the forward transition for platform fragment.");
    }
  }

  /**
   * Apply the transition for going forward. This is applied when going forward from the previous
   * activity to this, or going forward from this activity to the next.
   *
   * <p>For example, in the flow below, the forward transitions will be applied to all arrows
   * pointing to the right. Previous screen --> This screen --> Next screen
   */
  public static void applyForwardTransition(Activity activity, @TransitionType int transitionId) {
    if (transitionId == TRANSITION_SLIDE) {
      activity.overridePendingTransition(R.anim.sud_slide_next_in, R.anim.sud_slide_next_out);
    } else if (transitionId == TRANSITION_FADE) {
      activity.overridePendingTransition(android.R.anim.fade_in, R.anim.sud_stay);
    } else if (transitionId == TRANSITION_FRAMEWORK_DEFAULT) {
      TypedArray typedArray =
          activity.obtainStyledAttributes(
              android.R.style.Animation_Activity,
              new int[] {
                android.R.attr.activityOpenEnterAnimation, android.R.attr.activityOpenExitAnimation
              });
      activity.overridePendingTransition(
          typedArray.getResourceId(/* index= */ 0, /* defValue= */ 0),
          typedArray.getResourceId(/* index= */ 1, /* defValue= */ 0));
      typedArray.recycle();
    } else if (transitionId == TRANSITION_FRAMEWORK_DEFAULT_PRE_P) {
      activity.overridePendingTransition(
          R.anim.sud_pre_p_activity_open_enter, R.anim.sud_pre_p_activity_open_exit);
    } else if (transitionId == TRANSITION_NONE) {
      // For TRANSITION_NONE, turn off the transition
      activity.overridePendingTransition(/* enterAnim= */ 0, /* exitAnim= */ 0);
    } else if (transitionId == TRANSITION_CAPTIVE) {
      // 1. If the flag not available, apply TRANSITION_SLIDE
      // 2. If the flag present, apply the transition from transition type
      int configTransitionType =
          PartnerConfigHelper.get(activity)
              .getInteger(activity, PartnerConfig.CONFIG_TRANSITION_TYPE, TRANSITION_SLIDE);
      if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R
          && configTransitionType == CONFIG_TRANSITION_SHARED_X_AXIS) {
        Window window = activity.getWindow();
        if (window != null) {
          MaterialSharedAxis exitTransition =
              new MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true);
          window.setExitTransition(exitTransition);

          window.setAllowEnterTransitionOverlap(true);

          MaterialSharedAxis enterTransition =
              new MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true);
          window.setEnterTransition(enterTransition);
        } else {
          Log.w(TAG, "applyForwardTransition: Invalid window=" + window);
        }
      } else {
        activity.overridePendingTransition(R.anim.sud_slide_next_in, R.anim.sud_slide_next_out);
      }
    }
    // For TRANSITION_NO_OVERRIDE or other values, do not override the transition
  }

  /**
   * Apply the transition for going backward which is decided by partner resource {@link
   * PartnerConfig#CONFIG_TRANSITION_TYPE} and system property {@code setupwizard.transition_type}.
   * The default transition that will be applied is {@link #TRANSITION_SLIDE}. The timing to apply
   * the transition is going backward from the next activity to this, or going backward from this
   * activity to the previous.
   *
   * <p>For example, in the flow below, the backward transitions will be applied to all arrows
   * pointing to the left. Previous screen <-- This screen <-- Next screen
   */
  public static void applyBackwardTransition(Activity activity) {
    applyBackwardTransition(activity, TRANSITION_CAPTIVE);
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
      Log.w(TAG, "Not apply the backward transition for platform fragment.");
    }
  }

  /**
   * Apply the transition for going backward. This is applied when going backward from the next
   * activity to this, or going backward from this activity to the previous.
   *
   * <p>For example, in the flow below, the backward transitions will be applied to all arrows
   * pointing to the left. Previous screen <-- This screen <-- Next screen
   */
  public static void applyBackwardTransition(Activity activity, @TransitionType int transitionId) {
    if (transitionId == TRANSITION_SLIDE) {
      activity.overridePendingTransition(R.anim.sud_slide_back_in, R.anim.sud_slide_back_out);
    } else if (transitionId == TRANSITION_FADE) {
      activity.overridePendingTransition(R.anim.sud_stay, android.R.anim.fade_out);
    } else if (transitionId == TRANSITION_FRAMEWORK_DEFAULT) {
      TypedArray typedArray =
          activity.obtainStyledAttributes(
              android.R.style.Animation_Activity,
              new int[] {
                android.R.attr.activityCloseEnterAnimation,
                android.R.attr.activityCloseExitAnimation
              });
      activity.overridePendingTransition(
          typedArray.getResourceId(/* index= */ 0, /* defValue= */ 0),
          typedArray.getResourceId(/* index= */ 1, /* defValue= */ 0));
      typedArray.recycle();
    } else if (transitionId == TRANSITION_FRAMEWORK_DEFAULT_PRE_P) {
      activity.overridePendingTransition(
          R.anim.sud_pre_p_activity_close_enter, R.anim.sud_pre_p_activity_close_exit);
    } else if (transitionId == TRANSITION_NONE) {
      // For TRANSITION_NONE, turn off the transition
      activity.overridePendingTransition(/* enterAnim= */ 0, /* exitAnim= */ 0);
    } else if (transitionId == TRANSITION_CAPTIVE) {
      // 1. If the flag not available, apply TRANSITION_SLIDE
      // 2. If the flag present, apply the transition from transition type
      int configTransitionType =
          PartnerConfigHelper.get(activity)
              .getInteger(activity, PartnerConfig.CONFIG_TRANSITION_TYPE, TRANSITION_SLIDE);
      if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R
          && configTransitionType == CONFIG_TRANSITION_SHARED_X_AXIS) {
        Window window = activity.getWindow();
        if (window != null) {
          MaterialSharedAxis reenterTransition =
              new MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false);
          window.setReenterTransition(reenterTransition);

          MaterialSharedAxis returnTransition =
              new MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false);
          window.setReturnTransition(returnTransition);
        } else {
          Log.w(TAG, "applyBackwardTransition: Invalid window=" + window);
        }
      } else {
        activity.overridePendingTransition(R.anim.sud_slide_back_in, R.anim.sud_slide_back_out);
      }
    }
    // For TRANSITION_NO_OVERRIDE or other values, do not override the transition
  }

  /**
   * A wrapper method, create an {@link android.app.ActivityOptions} to transition between
   * activities as the {@link ActivityOptions} parameter of {@link Activity#startActivity}.
   *
   * @throws IllegalArgumentException is thrown when {@code activity} or {@code intent} is null.
   * @throws android.content.ActivityNotFoundException if there was no {@link Activity} found to run
   *     the given Intent.
   */
  public static void startActivityWithTransition(Activity activity, Intent intent) {
    if (activity == null) {
      throw new IllegalArgumentException("Invalid activity=" + activity);
    }

    if (intent == null) {
      throw new IllegalArgumentException("Invalid intent=" + intent);
    }

    int configTransitionType =
        PartnerConfigHelper.get(activity)
            .getInteger(activity, PartnerConfig.CONFIG_TRANSITION_TYPE, TRANSITION_SLIDE);
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R
        && configTransitionType == CONFIG_TRANSITION_SHARED_X_AXIS) {
      if (activity.getWindow() != null
          && !activity.getWindow().hasFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)) {
        Log.w(
            TAG, "The transition won't take effect due to NO FEATURE_ACTIVITY_TRANSITIONS feature");
      }

      ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(activity);
      intent.putExtra(EXTRA_ACTIVITY_OPTIONS, (Parcelable) activityOptions.toBundle());
      activity.startActivity(intent, activityOptions.toBundle());
    } else {
      activity.startActivity(intent);
    }
  }

  /**
   * A wrapper method, create an {@link android.app.ActivityOptions} to transition between
   * activities as the {@code activityOptions} parameter of {@link Activity#startActivityForResult}.
   *
   * @throws IllegalArgumentException is thrown when {@code activity} or {@code intent} is null.
   * @throws android.content.ActivityNotFoundException if there was no {@link Activity} found to run
   *     the given Intent.
   */
  public static void startActivityForResultWithTransition(
      Activity activity, Intent intent, int requestCode) {
    if (activity == null) {
      throw new IllegalArgumentException("Invalid activity=" + activity);
    }

    if (intent == null) {
      throw new IllegalArgumentException("Invalid intent=" + intent);
    }

    int configTransitionType =
        PartnerConfigHelper.get(activity)
            .getInteger(activity, PartnerConfig.CONFIG_TRANSITION_TYPE, TRANSITION_SLIDE);
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R
        && configTransitionType == CONFIG_TRANSITION_SHARED_X_AXIS) {
      if (activity.getWindow() != null
          && !activity.getWindow().hasFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)) {
        Log.w(
            TAG, "The transition won't take effect due to NO FEATURE_ACTIVITY_TRANSITIONS feature");
      }

      ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(activity);
      intent.putExtra(EXTRA_ACTIVITY_OPTIONS, (Parcelable) activityOptions.toBundle());
      activity.startActivityForResult(intent, requestCode, activityOptions.toBundle());
    } else {
      activity.startActivityForResult(intent, requestCode);
    }
  }
}
