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

package com.google.android.setupdesign.template;

import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.setupcompat.PartnerCustomizationLayout;
import com.google.android.setupcompat.internal.TemplateLayout;
import com.google.android.setupcompat.template.Mixin;
import com.google.android.setupdesign.R;
import com.google.android.setupdesign.util.HeaderAreaStyler;
import com.google.android.setupdesign.util.PartnerStyleHelper;

/**
 * A {@link com.google.android.setupcompat.template.Mixin} for setting and getting the description
 * text.
 */
public class DescriptionMixin implements Mixin {

  private final TemplateLayout templateLayout;

  /**
   * A {@link com.google.android.setupcompat.template.Mixin} for setting and getting the
   * description.
   *
   * @param layout The layout this Mixin belongs to
   * @param attrs XML attributes given to the layout
   * @param defStyleAttr The default style attribute as given to the constructor of the layout
   */
  public DescriptionMixin(
      @NonNull TemplateLayout layout, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
    templateLayout = layout;

    final TypedArray a =
        layout
            .getContext()
            .obtainStyledAttributes(attrs, R.styleable.SudDescriptionMixin, defStyleAttr, 0);

    // Set the description text
    final CharSequence descriptionText =
        a.getText(R.styleable.SudDescriptionMixin_sudDescriptionText);
    if (descriptionText != null) {
      setText(descriptionText);
    }
    // Set the description text color
    final ColorStateList descriptionTextColor =
        a.getColorStateList(R.styleable.SudDescriptionMixin_sudDescriptionTextColor);
    if (descriptionTextColor != null) {
      setTextColor(descriptionTextColor);
    }

    a.recycle();
  }

  /**
   * Applies the partner customizations to the description text (contains text alignment) and
   * background, if apply heavy theme resource, it will apply all partner customizations, otherwise,
   * only apply alignment style.
   */
  public void tryApplyPartnerCustomizationStyle() {
    TextView description = templateLayout.findManagedViewById(R.id.sud_layout_subtitle);
    boolean partnerHeavyThemeLayout = PartnerStyleHelper.isPartnerHeavyThemeLayout(templateLayout);
    if (partnerHeavyThemeLayout) {
      if (description != null) {
        HeaderAreaStyler.applyPartnerCustomizationDescriptionHeavyStyle(description);
      }
    } else if (templateLayout instanceof PartnerCustomizationLayout
        && ((PartnerCustomizationLayout) templateLayout).shouldApplyPartnerResource()) {
      if (description != null) {
        HeaderAreaStyler.applyPartnerCustomizationDescriptionLightStyle(description);
      }
    }
  }

  /** Returns the TextView displaying the description. */
  public TextView getTextView() {
    return (TextView) templateLayout.findManagedViewById(R.id.sud_layout_subtitle);
  }

  /**
   * Sets the description text and also sets the text visibility to visble. This can also be set via
   * the XML attribute {@code app:sudDescriptionText}.
   *
   * @param title The resource ID of the text to be set as description
   */
  public void setText(int title) {
    final TextView titleView = getTextView();
    if (titleView != null) {
      titleView.setText(title);
      setVisibility(View.VISIBLE);
    }
  }

  /**
   * Sets the description text and also sets the text visibility to visble. This can also be set via
   * the XML attribute {@code app:sudDescriptionText}.
   *
   * @param title The text to be set as description
   */
  public void setText(CharSequence title) {
    final TextView titleView = getTextView();
    if (titleView != null) {
      titleView.setText(title);
      setVisibility(View.VISIBLE);
    }
  }

  /** Returns the current description text. */
  public CharSequence getText() {
    final TextView titleView = getTextView();
    return titleView != null ? titleView.getText() : null;
  }

  /** Sets the visibility of description text */
  public void setVisibility(int visibility) {
    final TextView titleView = getTextView();
    if (titleView != null) {
      titleView.setVisibility(visibility);
    }
  }

  /**
   * Sets the color of the description text. This can also be set via XML using {@code
   * app:sudDescriptionTextColor}.
   *
   * @param color The text color of the description
   */
  public void setTextColor(ColorStateList color) {
    final TextView titleView = getTextView();
    if (titleView != null) {
      titleView.setTextColor(color);
    }
  }

  /** Returns the current text color of the description. */
  public ColorStateList getTextColor() {
    final TextView titleView = getTextView();
    return titleView != null ? titleView.getTextColors() : null;
  }
}
