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

package com.google.android.setupdesign;

import static com.google.android.setupcompat.partnerconfig.Util.isNightMode;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import androidx.annotation.StringDef;
import androidx.annotation.VisibleForTesting;
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.SimpleColorFilter;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.value.LottieValueCallback;
import com.airbnb.lottie.value.SimpleLottieValueCallback;
import com.google.android.setupcompat.partnerconfig.PartnerConfig;
import com.google.android.setupcompat.partnerconfig.PartnerConfig.ResourceType;
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper;
import com.google.android.setupcompat.partnerconfig.ResourceEntry;
import com.google.android.setupdesign.lottieloadinglayout.R;
import com.google.android.setupdesign.util.BuildCompatUtils;
import com.google.android.setupdesign.view.IllustrationVideoView;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlifLoadingLayout extends GlifLayout {
  private static final String TAG = "GlifLoadingLayout";

  @VisibleForTesting @IllustrationType String illustrationType = IllustrationType.DEFAULT;
  @VisibleForTesting LottieAnimationConfig animationConfig = LottieAnimationConfig.CONFIG_DEFAULT;

  @VisibleForTesting @RawRes int customLottieResource = 0;

  @VisibleForTesting Map<KeyPath, SimpleColorFilter> customizationMap = new HashMap<>();

  @VisibleForTesting LottieAnimationFinishListener animationFinishListener;

  public GlifLoadingLayout(Context context) {
    this(context, 0, 0);
  }

  public GlifLoadingLayout(Context context, int template) {
    this(context, template, 0);
  }

  public GlifLoadingLayout(Context context, int template, int containerId) {
    super(context, template, containerId);
    init(null, R.attr.sudLayoutTheme);
  }

  public GlifLoadingLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(attrs, R.attr.sudLayoutTheme);
  }

  @TargetApi(VERSION_CODES.HONEYCOMB)
  public GlifLoadingLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(attrs, defStyleAttr);
  }

  private void init(AttributeSet attrs, int defStyleAttr) {
    TypedArray a =
        getContext()
            .obtainStyledAttributes(attrs, R.styleable.SudGlifLoadingLayout, defStyleAttr, 0);
    customLottieResource = a.getResourceId(R.styleable.SudGlifLoadingLayout_sudLottieRes, 0);
    String illustrationType = a.getString(R.styleable.SudGlifLoadingLayout_sudIllustrationType);
    a.recycle();
    if (customLottieResource != 0) {
      inflateLottieView();
      ViewGroup container = findContainer(0);
      container.setVisibility(View.VISIBLE);
    } else {
      if (illustrationType != null) {
        setIllustrationType(illustrationType);
      }

      if (BuildCompatUtils.isAtLeastS()) {
        inflateLottieView();
      } else {
        inflateIllustrationStub();
      }
    }
  }

  public void setIllustrationType(@IllustrationType String type) {
    if (customLottieResource != 0) {
      throw new IllegalStateException(
          "custom illustration already applied, should not set illustration.");
    }

    if (!illustrationType.equals(type)) {
      illustrationType = type;
      customizationMap.clear();
    }

    switch (type) {
      case IllustrationType.ACCOUNT:
        animationConfig = LottieAnimationConfig.CONFIG_ACCOUNT;
        break;

      case IllustrationType.CONNECTION:
        animationConfig = LottieAnimationConfig.CONFIG_CONNECTION;
        break;

      case IllustrationType.UPDATE:
        animationConfig = LottieAnimationConfig.CONFIG_UPDATE;
        break;

      default:
        animationConfig = LottieAnimationConfig.CONFIG_DEFAULT;
        break;
    }

    updateAnimationView();
  }

  // TODO: [GlifLoadingLayout] Should add testcase. LottieAnimationView was auto
  // generated not able to mock. So we have no idea how to detected is the api pass to
  // LottiAnimationView correctly.
  public boolean setAnimation(InputStream inputStream, String keyCache) {
    LottieAnimationView lottieAnimationView = findLottieAnimationView();
    if (lottieAnimationView != null) {
      lottieAnimationView.setAnimation(inputStream, keyCache);
      return true;
    } else {
      return false;
    }
  }

  public boolean setAnimation(String assetName) {
    LottieAnimationView lottieAnimationView = findLottieAnimationView();
    if (lottieAnimationView != null) {
      lottieAnimationView.setAnimation(assetName);
      return true;
    } else {
      return false;
    }
  }

  public boolean setAnimation(@RawRes int rawRes) {
    LottieAnimationView lottieAnimationView = findLottieAnimationView();
    if (lottieAnimationView != null) {
      lottieAnimationView.setAnimation(rawRes);
      return true;
    } else {
      return false;
    }
  }

  private void updateAnimationView() {
    if (BuildCompatUtils.isAtLeastS()) {
      setLottieResource();
    } else {
      setIllustrationResource();
    }
  }

  /**
   * Call this when your activity is done and should be closed. The activity will be finished while
   * animation finished.
   */
  public void finish(@NonNull Activity activity) {
    if (activity == null) {
      throw new NullPointerException("activity should not be null");
    }
    animationFinishListener =
        new LottieAnimationFinishListener(findLottieAnimationView()) {

          @Override
          void onAnimationFinished() {
            activity.finish();
            removeListener();
          }
        };
  }

  /**
   * Launch a new activity after the animation finished.
   *
   * @param activity The activity which is GlifLoadingLayout attached to.
   * @param intent The intent to start.
   * @param options Additional options for how the Activity should be started. See {@link
   *     android.content.Context#startActivity(Intent, Bundle)} for more details.
   * @param finish Finish the activity after startActivity
   * @see Activity#startActivity(Intent)
   * @see Activity#startActivityForResult
   */
  public void startActivity(
      @NonNull Activity activity,
      @NonNull Intent intent,
      @Nullable Bundle options,
      boolean finish) {
    if (activity == null) {
      throw new NullPointerException("activity should not be null");
    }

    if (intent == null) {
      throw new NullPointerException("intent should not be null");
    }

    animationFinishListener =
        new LottieAnimationFinishListener(findLottieAnimationView()) {
          @Override
          void onAnimationFinished() {
            if (options == null || Build.VERSION.SDK_INT < VERSION_CODES.JELLY_BEAN) {
              activity.startActivity(intent);
            } else {
              activity.startActivity(intent, options);
            }

            if (finish) {
              activity.finish();
            }

            removeListener();
          }
        };
  }

  /**
   * Waiting for the animation finished and launch an activity for which you would like a result
   * when it finished.
   *
   * @param activity The activity which the GlifLoadingLayout attached to.
   * @param intent The intent to start.
   * @param requestCode If >= 0, this code will be returned in onActivityResult() when the activity
   *     exits.
   * @param options Additional options for how the Activity should be started.
   * @param finish Finish the activity after startActivityForResult. The onActivityResult might not
   *     be called because the activity already finixhed.
   *     <p>See {@link android.content.Context#startActivity(Intent, Bundle)}
   *     Context.startActivity(Intent, Bundle)} for more details.
   */
  public void startActivityForResult(
      @NonNull Activity activity,
      @NonNull Intent intent,
      int requestCode,
      @Nullable Bundle options,
      boolean finish) {
    if (activity == null) {
      throw new NullPointerException("activity should not be null");
    }

    if (intent == null) {
      throw new NullPointerException("intent should not be null");
    }

    animationFinishListener =
        new LottieAnimationFinishListener(findLottieAnimationView()) {

          @Override
          void onAnimationFinished() {
            if (options == null || Build.VERSION.SDK_INT < VERSION_CODES.JELLY_BEAN) {
              activity.startActivityForResult(intent, requestCode);
            } else {
              activity.startActivityForResult(intent, requestCode, options);
            }

            if (finish) {
              activity.finish();
            }
            removeListener();
          }
        };
  }

  private void inflateLottieView() {
    final View lottieLayout = peekLottieLayout();
    if (lottieLayout == null) {
      ViewStub viewStub = findManagedViewById(R.id.sud_loading_layout_lottie_stub);
      if (viewStub != null) {
        viewStub.inflate();
        setLottieResource();
      }
    }
  }

  private void inflateIllustrationStub() {
    final View progressLayout = peekProgressIllustrationLayout();
    if (progressLayout == null) {
      ViewStub viewStub = findManagedViewById(R.id.sud_loading_layout_illustration_stub);
      if (viewStub != null) {
        viewStub.inflate();
        setIllustrationResource();
      }
    }
  }

  private void setLottieResource() {
    LottieAnimationView lottieView = findViewById(R.id.sud_lottie_view);
    if (lottieView == null) {
      Log.w(TAG, "Lottie view not found, skip set resource. Wait for layout inflated.");
      return;
    }
    if (customLottieResource != 0) {
      InputStream inputRaw = getResources().openRawResource(customLottieResource);
      lottieView.setAnimation(inputRaw, null);
      lottieView.playAnimation();
    } else {
      PartnerConfigHelper partnerConfigHelper = PartnerConfigHelper.get(getContext());
      ResourceEntry resourceEntry =
          partnerConfigHelper.getIllustrationResourceEntry(
              getContext(), animationConfig.getLottieConfig());

      if (resourceEntry != null) {
        InputStream inputRaw =
            resourceEntry.getResources().openRawResource(resourceEntry.getResourceId());
        lottieView.setAnimation(inputRaw, null);
        lottieView.playAnimation();
        setLottieLayoutVisibility(View.VISIBLE);
        setIllustrationLayoutVisibility(View.GONE);
        applyThemeCustomization();
      } else {
        setLottieLayoutVisibility(View.GONE);
        setIllustrationLayoutVisibility(View.VISIBLE);
        inflateIllustrationStub();
      }
    }
  }

  private void setIllustrationLayoutVisibility(int visibility) {
    View illustrationLayout = findViewById(R.id.sud_layout_progress_illustration);
    if (illustrationLayout != null) {
      illustrationLayout.setVisibility(visibility);
    }
  }

  private void setLottieLayoutVisibility(int visibility) {
    View lottieLayout = findViewById(R.id.sud_layout_lottie_illustration);
    if (lottieLayout != null) {
      lottieLayout.setVisibility(visibility);
    }
  }

  private void setIllustrationResource() {
    View illustrationLayout = findViewById(R.id.sud_layout_progress_illustration);
    if (illustrationLayout == null) {
      Log.i(TAG, "Illustration stub not inflated, skip set resource");
      return;
    }

    IllustrationVideoView illustrationVideoView =
        findManagedViewById(R.id.sud_progress_illustration);
    ProgressBar progressBar = findManagedViewById(R.id.sud_progress_bar);

    PartnerConfigHelper partnerConfigHelper = PartnerConfigHelper.get(getContext());
    ResourceEntry resourceEntry =
        partnerConfigHelper.getIllustrationResourceEntry(
            getContext(), animationConfig.getIllustrationConfig());

    if (resourceEntry != null) {
      progressBar.setVisibility(GONE);
      illustrationVideoView.setVisibility(VISIBLE);
      illustrationVideoView.setVideoResourceEntry(resourceEntry);
    } else {
      progressBar.setVisibility(VISIBLE);
      illustrationVideoView.setVisibility(GONE);
    }
  }

  private LottieAnimationView findLottieAnimationView() {
    return findViewById(R.id.sud_lottie_view);
  }

  //TODO: Should add testcase with mocked LottieAnimationView.
  /** Add an animator listener to {@link LottieAnimationView}. */
  public void addAnimatorListener(Animator.AnimatorListener listener) {
    LottieAnimationView animationView = findLottieAnimationView();
    if (animationView != null) {
      animationView.addAnimatorListener(listener);
    }
  }

  /** Remove the listener from {@link LottieAnimationView}. */
  public void removeAnimatorListener(AnimatorListener listener) {
    LottieAnimationView animationView = findLottieAnimationView();
    if (animationView != null) {
      animationView.removeAnimatorListener(listener);
    }
  }

  /** Remove all {@link AnimatorListener} from {@link LottieAnimationView}. */
  public void removeAllAnimatorListener() {
    LottieAnimationView animationView = findLottieAnimationView();
    if (animationView != null) {
      animationView.removeAllAnimatorListeners();
    }
  }

  /** Add a value callback with property {@link LottieProperty.COLOR_FILTER}. */
  public void addColorCallback(KeyPath keyPath, LottieValueCallback<ColorFilter> callback) {
    LottieAnimationView animationView = findLottieAnimationView();
    if (animationView != null) {
      animationView.addValueCallback(keyPath, LottieProperty.COLOR_FILTER, callback);
    }
  }

  /** Add a simple value callback with property {@link LottieProperty.COLOR_FILTER}. */
  public void addColorCallback(KeyPath keyPath, SimpleLottieValueCallback<ColorFilter> callback) {
    LottieAnimationView animationView = findLottieAnimationView();
    if (animationView != null) {
      animationView.addValueCallback(keyPath, LottieProperty.COLOR_FILTER, callback);
    }
  }

  @VisibleForTesting
  protected void loadCustomization() {
    if (customizationMap.isEmpty()) {
      PartnerConfigHelper helper = PartnerConfigHelper.get(getContext());
      List<String> lists =
          helper.getStringArray(
              getContext(),
              isNightMode(getResources().getConfiguration())
                  ? animationConfig.getDarkThemeCustomization()
                  : animationConfig.getLightThemeCustomization());
      for (String item : lists) {
        String[] splitItem = item.split(":");
        if (splitItem.length == 2) {
          customizationMap.put(
              new KeyPath(splitItem[0]), new SimpleColorFilter(Color.parseColor(splitItem[1])));
        } else {
          Log.w(TAG, "incorrect format customization, value=" + item);
        }
      }
    }
  }

  @VisibleForTesting
  protected void applyThemeCustomization() {
    LottieAnimationView animationView = findLottieAnimationView();
    if (animationView != null) {
      loadCustomization();
      for (KeyPath keyPath : customizationMap.keySet()) {
        animationView.addValueCallback(
            keyPath,
            LottieProperty.COLOR_FILTER,
            new LottieValueCallback<>(customizationMap.get(keyPath)));
      }
    }
  }

  @Nullable
  private View peekLottieLayout() {
    return findViewById(R.id.sud_layout_lottie_illustration);
  }

  @Nullable
  private View peekProgressIllustrationLayout() {
    return findViewById(R.id.sud_layout_progress_illustration);
  }

  @Override
  protected View onInflateTemplate(LayoutInflater inflater, int template) {
    if (template == 0) {
      template = R.layout.sud_glif_loading_template;
    }
    return inflateTemplate(inflater, R.style.SudThemeGlif_Light, template);
  }

  @Override
  protected ViewGroup findContainer(int containerId) {
    if (containerId == 0) {
      containerId = R.id.sud_layout_content;
    }
    return super.findContainer(containerId);
  }

  /** The progress config used to maps to different animation */
  public enum LottieAnimationConfig {
    CONFIG_DEFAULT(
        PartnerConfig.CONFIG_PROGRESS_ILLUSTRATION_DEFAULT,
        PartnerConfig.CONFIG_LOADING_LOTTIE_DEFAULT,
        PartnerConfig.CONFIG_LOTTIE_LIGHT_THEME_CUSTOMIZATION_DEFAULT,
        PartnerConfig.CONFIG_LOTTIE_DARK_THEME_CUSTOMIZATION_DEFAULT),
    CONFIG_ACCOUNT(
        PartnerConfig.CONFIG_PROGRESS_ILLUSTRATION_ACCOUNT,
        PartnerConfig.CONFIG_LOADING_LOTTIE_ACCOUNT,
        PartnerConfig.CONFIG_LOTTIE_LIGHT_THEME_CUSTOMIZATION_ACCOUNT,
        PartnerConfig.CONFIG_LOTTIE_DARK_THEME_CUSTOMIZATION_ACCOUNT),
    CONFIG_CONNECTION(
        PartnerConfig.CONFIG_PROGRESS_ILLUSTRATION_CONNECTION,
        PartnerConfig.CONFIG_LOADING_LOTTIE_CONNECTION,
        PartnerConfig.CONFIG_LOTTIE_LIGHT_THEME_CUSTOMIZATION_CONNECTION,
        PartnerConfig.CONFIG_LOTTIE_DARK_THEME_CUSTOMIZATION_CONNECTION),
    CONFIG_UPDATE(
        PartnerConfig.CONFIG_PROGRESS_ILLUSTRATION_UPDATE,
        PartnerConfig.CONFIG_LOADING_LOTTIE_UPDATE,
        PartnerConfig.CONFIG_LOTTIE_LIGHT_THEME_CUSTOMIZATION_UPDATE,
        PartnerConfig.CONFIG_LOTTIE_DARK_THEME_CUSTOMIZATION_UPDATE);

    private final PartnerConfig illustrationConfig;
    private final PartnerConfig lottieConfig;
    private final PartnerConfig lightThemeCustomization;
    private final PartnerConfig darkThemeCustomization;

    LottieAnimationConfig(
        PartnerConfig illustrationConfig,
        PartnerConfig lottieConfig,
        PartnerConfig lightThemeCustomization,
        PartnerConfig darkThemeCustomization) {
      if (illustrationConfig.getResourceType() != ResourceType.ILLUSTRATION
          || lottieConfig.getResourceType() != ResourceType.ILLUSTRATION) {
        throw new IllegalArgumentException(
            "Illustration progress only allow illustration resource");
      }
      this.illustrationConfig = illustrationConfig;
      this.lottieConfig = lottieConfig;
      this.lightThemeCustomization = lightThemeCustomization;
      this.darkThemeCustomization = darkThemeCustomization;
    }

    PartnerConfig getIllustrationConfig() {
      return illustrationConfig;
    }

    PartnerConfig getLottieConfig() {
      return lottieConfig;
    }

    PartnerConfig getLightThemeCustomization() {
      return lightThemeCustomization;
    }

    PartnerConfig getDarkThemeCustomization() {
      return darkThemeCustomization;
    }
  }

  @VisibleForTesting
  abstract class LottieAnimationFinishListener {

    private final LottieAnimationView lottieAnimationView;

    @VisibleForTesting
    AnimatorListener animatorListener =
        new AnimatorListener() {
          @Override
          public void onAnimationStart(Animator animation) {
            // Do nothing.
          }

          @Override
          public void onAnimationEnd(Animator animation) {
            onAnimationFinished();
          }

          @Override
          public void onAnimationCancel(Animator animation) {
            // Do nothing.
          }

          @Override
          public void onAnimationRepeat(Animator animation) {
            // Do nothing.
          }
        };

    private LottieAnimationFinishListener(LottieAnimationView lottieAnimationView) {
      this.lottieAnimationView = lottieAnimationView;

      if (lottieAnimationView != null && lottieAnimationView.isAnimating()) {
        lottieAnimationView.addAnimatorListener(animatorListener);
        lottieAnimationView.setRepeatCount(0);
      } else {
        onAnimationFinished();
      }
    }

    public void removeListener() {
      if (lottieAnimationView != null) {
        lottieAnimationView.removeAnimatorListener(animatorListener);
      }
      animationFinishListener = null;
    }

    abstract void onAnimationFinished();
  }

  @Retention(RetentionPolicy.SOURCE)
  @StringDef({
    IllustrationType.ACCOUNT,
    IllustrationType.CONNECTION,
    IllustrationType.DEFAULT,
    IllustrationType.UPDATE
  })
  public @interface IllustrationType {
    String DEFAULT = "default";
    String ACCOUNT = "account";
    String CONNECTION = "connection";
    String UPDATE = "update";
  }
}
