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

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
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

  @VisibleForTesting LottieAnimationConfig animationConfig = LottieAnimationConfig.CONFIG_DEFAULT;

  @VisibleForTesting @RawRes int customLottieResource = 0;

  @VisibleForTesting Map<KeyPath, SimpleColorFilter> customizationMap = new HashMap<>();

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

  public void setIllustrationType(String type) {
    if (customLottieResource != 0) {
      throw new IllegalStateException(
          "custom illustration already applied, should not set illustration.");
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
    activity.finish();
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

    if (options == null || Build.VERSION.SDK_INT < VERSION_CODES.JELLY_BEAN) {
      activity.startActivity(intent);
    } else {
      activity.startActivity(intent, options);
    }

    if (finish) {
      activity.finish();
    }
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

    if (options == null || Build.VERSION.SDK_INT < VERSION_CODES.JELLY_BEAN) {
      activity.startActivityForResult(intent, requestCode);
    } else {
      activity.startActivityForResult(intent, requestCode, options);
    }

    if (finish) {
      activity.finish();
    }
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
        applyDarkThemeCustomization();
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

  @VisibleForTesting
  boolean isNightMode(Configuration configuration) {
    int uiMode = configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK;
    return uiMode == Configuration.UI_MODE_NIGHT_YES;
  }

  @VisibleForTesting
  protected void loadDarkThemeCustomization() {
    if (customizationMap.isEmpty()) {
      PartnerConfigHelper helper = PartnerConfigHelper.get(getContext());
      List<String> lists =
          helper.getStringArray(getContext(), animationConfig.getDarkThemeCustomization());
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
  protected void applyDarkThemeCustomization() {
    LottieAnimationView animationView = findViewById(R.id.sud_lottie_view);
    android.util.Log.i("Cating", "applyDarkThemeCustomization animationView=" + animationView + ", isNightMode=" + isNightMode(getResources().getConfiguration()));
    if (animationView != null && isNightMode(getResources().getConfiguration())) {
      loadDarkThemeCustomization();
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
      containerId = R.id.sud_loading_layout_content;
    }
    return super.findContainer(containerId);
  }

  /** The progress config used to maps to different animation */
  public enum LottieAnimationConfig {
    CONFIG_DEFAULT(
        PartnerConfig.CONFIG_PROGRESS_ILLUSTRATION_DEFAULT,
        PartnerConfig.CONFIG_LOADING_LOTTIE_DEFAULT,
        PartnerConfig.CONFIG_LOTTIE_DARK_THEME_CUSTOMIZATION_DEFAULT),
    CONFIG_ACCOUNT(
        PartnerConfig.CONFIG_PROGRESS_ILLUSTRATION_ACCOUNT,
        PartnerConfig.CONFIG_LOADING_LOTTIE_ACCOUNT,
        PartnerConfig.CONFIG_LOTTIE_DARK_THEME_CUSTOMIZATION_ACCOUNT),
    CONFIG_CONNECTION(
        PartnerConfig.CONFIG_PROGRESS_ILLUSTRATION_CONNECTION,
        PartnerConfig.CONFIG_LOADING_LOTTIE_CONNECTION,
        PartnerConfig.CONFIG_LOTTIE_DARK_THEME_CUSTOMIZATION_CONNECTION),
    CONFIG_UPDATE(
        PartnerConfig.CONFIG_PROGRESS_ILLUSTRATION_UPDATE,
        PartnerConfig.CONFIG_LOADING_LOTTIE_UPDATE,
        PartnerConfig.CONFIG_LOTTIE_DARK_THEME_CUSTOMIZATION_UPDATE);

    private final PartnerConfig illustrationConfig;
    private final PartnerConfig lottieConfig;
    private final PartnerConfig darkThemeCustomization;

    LottieAnimationConfig(
        PartnerConfig illustrationConfig,
        PartnerConfig lottieConfig,
        PartnerConfig darkThemeCustomization) {
      if (illustrationConfig.getResourceType() != ResourceType.ILLUSTRATION
          || lottieConfig.getResourceType() != ResourceType.ILLUSTRATION) {
        throw new IllegalArgumentException(
            "Illustration progress only allow illustration resource");
      }
      this.illustrationConfig = illustrationConfig;
      this.lottieConfig = lottieConfig;
      this.darkThemeCustomization = darkThemeCustomization;
    }

    PartnerConfig getIllustrationConfig() {
      return illustrationConfig;
    }

    PartnerConfig getLottieConfig() {
      return lottieConfig;
    }

    PartnerConfig getDarkThemeCustomization() {
      return darkThemeCustomization;
    }
  }

  @Retention(RetentionPolicy.SOURCE)
  @StringDef({
    IllustrationType.ACCOUNT,
    IllustrationType.CONNECTION,
    IllustrationType.DEFAULT,
    IllustrationType.UPDATE
  })
  @interface IllustrationType {
    String DEFAULT = "default";
    String ACCOUNT = "account";
    String CONNECTION = "connection";
    String UPDATE = "update";
  }
}
