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
import android.content.res.TypedArray;
import androidx.annotation.IntDef;
import com.google.android.setupdesign.R;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class TransitionHelper {

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
    TRANSITION_FRAMEWORK_DEFAULT_PRE_P
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

  private TransitionHelper() {}

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
          typedArray.getResourceId(0, 0), typedArray.getResourceId(1, 0));
      typedArray.recycle();
    } else if (transitionId == TRANSITION_FRAMEWORK_DEFAULT_PRE_P) {
      activity.overridePendingTransition(
          R.anim.sud_pre_p_activity_open_enter, R.anim.sud_pre_p_activity_open_exit);
    } else if (transitionId == TRANSITION_NONE) {
      // For TRANSITION_NONE, turn off the transition
      activity.overridePendingTransition(0, 0);
    }
    // For TRANSITION_NO_OVERRIDE or other values, do not override the transition
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
      activity.overridePendingTransition(android.R.anim.fade_in, R.anim.sud_stay);
    } else if (transitionId == TRANSITION_FRAMEWORK_DEFAULT) {
      TypedArray typedArray =
          activity.obtainStyledAttributes(
              android.R.style.Animation_Activity,
              new int[] {
                android.R.attr.activityCloseEnterAnimation,
                android.R.attr.activityCloseExitAnimation
              });
      activity.overridePendingTransition(
          typedArray.getResourceId(0, 0), typedArray.getResourceId(1, 0));
      typedArray.recycle();
    } else if (transitionId == TRANSITION_FRAMEWORK_DEFAULT_PRE_P) {
      activity.overridePendingTransition(
          R.anim.sud_pre_p_activity_close_enter, R.anim.sud_pre_p_activity_close_exit);
    } else if (transitionId == TRANSITION_NONE) {
      // For TRANSITION_NONE, turn off the transition
      activity.overridePendingTransition(0, 0);
    }
    // For TRANSITION_NO_OVERRIDE or other values, do not override the transition
  }
}
