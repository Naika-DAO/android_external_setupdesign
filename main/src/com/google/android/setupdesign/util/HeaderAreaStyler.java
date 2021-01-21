/*
 * Copyright (C) 2019 The Android Open Source Project
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

import static com.google.android.setupdesign.util.BuildCompatUtils.isAtLeastS;

import android.content.Context;
import android.graphics.drawable.VectorDrawable;
import android.os.Build;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import com.google.android.setupcompat.internal.TemplateLayout;
import com.google.android.setupcompat.partnerconfig.PartnerConfig;
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper;
import com.google.android.setupdesign.util.TextViewPartnerStyler.TextPartnerConfigs;

/**
 * Applies the partner customization for the header area widgets. The user needs to check if the
 * header area widgets should apply partner heavy theme or light theme before calling these methods.
 */
public final class HeaderAreaStyler {

  private static final String TAG = "HeaderAreaStyler";

  @VisibleForTesting
  static final String WARN_TO_USE_DRAWABLE =
      "To achieve scaling icon in SetupDesign lib, should use vector drawable icon!!";

  /**
   * Applies the partner heavy style of header text to the given textView {@code header}.
   *
   * @param header A header text would apply partner heavy style
   */
  public static void applyPartnerCustomizationHeaderHeavyStyle(@Nullable TextView header) {

    if (header == null) {
      return;
    }
    TextViewPartnerStyler.applyPartnerCustomizationStyle(
        header,
        new TextPartnerConfigs(
            PartnerConfig.CONFIG_HEADER_TEXT_COLOR,
            null,
            PartnerConfig.CONFIG_HEADER_TEXT_SIZE,
            PartnerConfig.CONFIG_HEADER_FONT_FAMILY,
            PartnerConfig.CONFIG_HEADER_TEXT_MARGIN_TOP,
            PartnerConfig.CONFIG_HEADER_TEXT_MARGIN_BOTTOM,
            PartnerStyleHelper.getLayoutGravity(header.getContext())));
  }

  /**
   * Applies the partner heavy style of description text to the given textView {@code description}.
   *
   * @param description A description text would apply partner heavy style
   */
  public static void applyPartnerCustomizationDescriptionHeavyStyle(
      @Nullable TextView description) {

    if (description == null) {
      return;
    }
    TextViewPartnerStyler.applyPartnerCustomizationStyle(
        description,
        new TextPartnerConfigs(
            PartnerConfig.CONFIG_DESCRIPTION_TEXT_COLOR,
            PartnerConfig.CONFIG_DESCRIPTION_LINK_TEXT_COLOR,
            PartnerConfig.CONFIG_DESCRIPTION_TEXT_SIZE,
            PartnerConfig.CONFIG_DESCRIPTION_FONT_FAMILY,
            PartnerConfig.CONFIG_DESCRIPTION_TEXT_MARGIN_TOP,
            PartnerConfig.CONFIG_DESCRIPTION_TEXT_MARGIN_BOTTOM,
            PartnerStyleHelper.getLayoutGravity(description.getContext())));
  }

  /**
   * Applies the partner light style of header text to the given textView {@code header}.
   *
   * @param header A header text would apply partner light style
   */
  public static void applyPartnerCustomizationHeaderLightStyle(@Nullable TextView header) {

    if (header == null) {
      return;
    }

    TextViewPartnerStyler.applyPartnerCustomizationLightStyle(
        header,
        new TextPartnerConfigs(
            null,
            null,
            null,
            null,
            null,
            null,
            PartnerStyleHelper.getLayoutGravity(header.getContext())));
  }

  /**
   * Applies the partner light style of description text to the given textView {@code description}.
   *
   * @param description A description text would apply partner light style
   */
  public static void applyPartnerCustomizationDescriptionLightStyle(
      @Nullable TextView description) {

    if (description == null) {
      return;
    }

    TextViewPartnerStyler.applyPartnerCustomizationLightStyle(
        description,
        new TextPartnerConfigs(
            null,
            null,
            null,
            null,
            null,
            null,
            PartnerStyleHelper.getLayoutGravity(description.getContext())));
  }

  /**
   * Applies the partner style of header area to the given layout {@code headerArea}. The theme
   * should set partner heavy theme first, and then the partner style of header would be applied. Ａs
   * for the margin bottom of header, it would aslo be appied when extended parter config is
   * enabled.
   *
   * @param headerArea A ViewGroup would apply the partner style of header area
   */
  public static void applyPartnerCustomizationHeaderAreaStyle(ViewGroup headerArea) {
    if (headerArea == null) {
      return;
    }
    if (PartnerStyleHelper.shouldApplyPartnerHeavyThemeResource(headerArea)) {
      Context context = headerArea.getContext();

      int color =
          PartnerConfigHelper.get(context)
              .getColor(context, PartnerConfig.CONFIG_HEADER_AREA_BACKGROUND_COLOR);
      headerArea.setBackgroundColor(color);

      if (PartnerConfigHelper.shouldApplyExtendedPartnerConfig(context)) {
        final ViewGroup.LayoutParams lp = headerArea.getLayoutParams();
        if (lp instanceof ViewGroup.MarginLayoutParams) {
          final ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;

          int bottomMargin =
              (int)
                  PartnerConfigHelper.get(context)
                      .getDimension(context, PartnerConfig.CONFIG_HEADER_CONTAINER_MARGIN_BOTTOM);
          mlp.setMargins(mlp.leftMargin, mlp.topMargin, mlp.rightMargin, bottomMargin);
          headerArea.setLayoutParams(lp);
        }
      }
    }
  }

  /**
   * Applies the partner style of header icon to the given {@code iconImage}. The theme should set
   * partner heavy theme and enable extended parter config first, and then the partner icon size
   * would be applied.
   *
   * @param iconImage A ImageView would apply the partner style of header icon
   * @param templateLayout The template containing this mixin
   */
  public static void applyPartnerCustomizationIconStyle(
      @Nullable ImageView iconImage, TemplateLayout templateLayout) {
    if (iconImage == null) {
      return;
    }

    if (PartnerStyleHelper.shouldApplyPartnerResource(templateLayout)) {
      Context context = iconImage.getContext();
      int gravity = PartnerStyleHelper.getLayoutGravity(context);
      if (gravity != 0) {
        setGravity(iconImage, gravity);
      }

      if (PartnerStyleHelper.shouldApplyPartnerHeavyThemeResource(iconImage)
          && PartnerConfigHelper.shouldApplyExtendedPartnerConfig(context)) {
        final ViewGroup.LayoutParams lp = iconImage.getLayoutParams();
        boolean partnerConfigAvailable =
            PartnerConfigHelper.get(context)
                .isPartnerConfigAvailable(PartnerConfig.CONFIG_ICON_MARGIN_TOP);
        if (partnerConfigAvailable && lp instanceof ViewGroup.MarginLayoutParams) {
          final ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
          int topMargin =
              (int)
                  PartnerConfigHelper.get(context)
                      .getDimension(context, PartnerConfig.CONFIG_ICON_MARGIN_TOP);
          mlp.setMargins(mlp.leftMargin, topMargin, mlp.rightMargin, mlp.bottomMargin);
        }

        if (PartnerConfigHelper.get(context)
            .isPartnerConfigAvailable(PartnerConfig.CONFIG_ICON_SIZE)) {

          checkImageType(iconImage);

          lp.height =
              (int)
                  PartnerConfigHelper.get(context)
                      .getDimension(context, PartnerConfig.CONFIG_ICON_SIZE);
          lp.width = LayoutParams.WRAP_CONTENT;
          iconImage.setScaleType(ScaleType.FIT_CENTER);
        }
      }
    }
  }

  private static void checkImageType(ImageView imageView) {
    ViewTreeObserver vto = imageView.getViewTreeObserver();
    vto.addOnPreDrawListener(
        new ViewTreeObserver.OnPreDrawListener() {
          @Override
          public boolean onPreDraw() {
            imageView.getViewTreeObserver().removeOnPreDrawListener(this);

            // TODO: Remove when Partners all used Drawable icon image and never use
            if (isAtLeastS()
                && !(imageView.getDrawable() instanceof VectorDrawable
                    || imageView.getDrawable() instanceof VectorDrawableCompat)) {
              if (Build.TYPE.equals("userdebug") || Build.TYPE.equals("eng")) {
                Toast.makeText(imageView.getContext(), WARN_TO_USE_DRAWABLE, Toast.LENGTH_LONG)
                    .show();
              }
              Log.w(TAG, WARN_TO_USE_DRAWABLE);
            }
            return true;
          }
        });
  }

  private static void setGravity(ImageView icon, int gravity) {
    if (icon.getLayoutParams() instanceof LinearLayout.LayoutParams) {
      LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) icon.getLayoutParams();
      layoutParams.gravity = gravity;
      icon.setLayoutParams(layoutParams);
    }
  }

  private HeaderAreaStyler() {}
}
