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

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build.VERSION_CODES;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.google.android.setupcompat.partnerconfig.PartnerConfig;
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper;
import com.google.android.setupdesign.R;

/**
 * Applies the partner style of layout to the given View {@code view}. The user needs to check if
 * the {@code view} should apply partner heavy theme before calling this method.
 */
public final class ItemStyler {

  /**
   * Applies the extended partner configs to the given view {@code view}. The user needs to check
   * before calling this method:
   *
   * <p>1) If the {@code view} should apply extended partner configs by calling {@link
   * ThemeHelper#shouldApplyExtendedPartnerConfig}.
   *
   * <p>2) If the layout of the {@code view} contains fixed resource IDs which attempts to apply
   * extended partner resources (The resource ID of the title is "sud_items_title" and the resource
   * ID of the summary is "sud_items_summary"), refer to {@link R.layout#sud_items_default}.
   *
   * @param view A view would be applied extended partner styles
   */
  @TargetApi(VERSION_CODES.JELLY_BEAN_MR1)
  public static void applyPartnerCustomizationItemStyle(@Nullable View view) {
    if (view == null) {
      return;
    }

    final Context context = view.getContext();

    if (!PartnerStyleHelper.shouldApplyPartnerHeavyThemeResource(view)
        || !PartnerConfigHelper.shouldApplyExtendedPartnerConfig(context)) {
      return;
    }

    // TODO: Move to TextViewPartnerStyler in ItemStyler
    final TextView titleView = view.findViewById(R.id.sud_items_title);

    if (PartnerConfigHelper.get(context)
        .isPartnerConfigAvailable(PartnerConfig.CONFIG_ITEMS_TITLE_TEXT_SIZE)) {
      final float titleSize =
          PartnerConfigHelper.get(context)
              .getDimension(context, PartnerConfig.CONFIG_ITEMS_TITLE_TEXT_SIZE);
      titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleSize);
    }

    if (PartnerConfigHelper.get(context)
        .isPartnerConfigAvailable(PartnerConfig.CONFIG_ITEMS_TITLE_FONT_FAMILY)) {

      final String textFont =
          PartnerConfigHelper.get(context)
              .getString(context, PartnerConfig.CONFIG_ITEMS_TITLE_FONT_FAMILY);

      final Typeface typeface = Typeface.create(textFont, Typeface.NORMAL);

      titleView.setTypeface(typeface);
    }

    TextView summaryView = view.findViewById(R.id.sud_items_summary);
    if (summaryView.getVisibility() == View.GONE && view instanceof LinearLayout) {
      // Set list items to vertical center when no summary.
      ((LinearLayout) view).setGravity(Gravity.CENTER_VERTICAL);
    }

    if (PartnerConfigHelper.get(context)
        .isPartnerConfigAvailable(PartnerConfig.CONFIG_ITEMS_SUMMARY_TEXT_SIZE)) {
      final float summarySize =
          PartnerConfigHelper.get(context)
              .getDimension(context, PartnerConfig.CONFIG_ITEMS_SUMMARY_TEXT_SIZE);
      summaryView.setTextSize(TypedValue.COMPLEX_UNIT_PX, summarySize);
    }

    if (PartnerConfigHelper.get(context)
        .isPartnerConfigAvailable(PartnerConfig.CONFIG_ITEMS_SUMMARY_FONT_FAMILY)) {

      final String textFont =
          PartnerConfigHelper.get(context)
              .getString(context, PartnerConfig.CONFIG_ITEMS_SUMMARY_FONT_FAMILY);

      final Typeface typeface = Typeface.create(textFont, Typeface.NORMAL);

      summaryView.setTypeface(typeface);
    }

    if (PartnerConfigHelper.get(context)
        .isPartnerConfigAvailable(PartnerConfig.CONFIG_ITEMS_SUMMARY_MARGIN_TOP)) {
      float summaryMarginTop =
          PartnerConfigHelper.get(context)
              .getDimension(context, PartnerConfig.CONFIG_ITEMS_SUMMARY_MARGIN_TOP);
      final ViewGroup.LayoutParams lp = summaryView.getLayoutParams();
      if (lp instanceof LinearLayout.LayoutParams) {
        final LinearLayout.LayoutParams mlp = (LinearLayout.LayoutParams) lp;
        mlp.setMargins(mlp.leftMargin, (int) summaryMarginTop, mlp.rightMargin, mlp.bottomMargin);
        summaryView.setLayoutParams(lp);
      }
    }

    float paddingTop;
    if (PartnerConfigHelper.get(context)
        .isPartnerConfigAvailable(PartnerConfig.CONFIG_ITEMS_PADDING_TOP)) {
      paddingTop =
          PartnerConfigHelper.get(context)
              .getDimension(context, PartnerConfig.CONFIG_ITEMS_PADDING_TOP);
    } else {
      paddingTop = view.getPaddingTop();
    }

    float paddingBottom;
    if (PartnerConfigHelper.get(context)
        .isPartnerConfigAvailable(PartnerConfig.CONFIG_ITEMS_PADDING_BOTTOM)) {
      paddingBottom =
          PartnerConfigHelper.get(context)
              .getDimension(context, PartnerConfig.CONFIG_ITEMS_PADDING_BOTTOM);
    } else {
      paddingBottom = view.getPaddingBottom();
    }

    if (paddingTop != view.getPaddingTop() || paddingBottom != view.getPaddingBottom()) {
      view.setPadding(
          view.getPaddingStart(), (int) paddingTop, view.getPaddingEnd(), (int) paddingBottom);
    }

    if (PartnerConfigHelper.get(context)
        .isPartnerConfigAvailable(PartnerConfig.CONFIG_ITEMS_MIN_HEIGHT)) {
      float minHeight =
          PartnerConfigHelper.get(context)
              .getDimension(context, PartnerConfig.CONFIG_ITEMS_MIN_HEIGHT);
      view.setMinimumHeight((int) minHeight);
    }
  }

  private ItemStyler() {}
}
