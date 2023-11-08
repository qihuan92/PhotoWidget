/*
 * Designed and developed by 2017 skydoves (Jaewoong Eum)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qihuan.photowidget.core.common.view;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.qihuan.photowidget.core.common.R;
import com.qihuan.photowidget.core.common.ktx.UIExtKt;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.databinding.ColorpickerviewDialogColorpickerBinding;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;
import com.skydoves.colorpickerview.listeners.ColorListener;
import com.skydoves.colorpickerview.listeners.ColorPickerViewListener;
import com.skydoves.colorpickerview.preference.ColorPickerPreferenceManager;
import com.skydoves.colorpickerview.sliders.AlphaSlideBar;
import com.skydoves.colorpickerview.sliders.BrightnessSlideBar;

/**
 * ColorPickerDialog is a dialog what having {@link ColorPickerView}, {@link AlphaSlideBar} and
 * {@link BrightnessSlideBar}.
 */
@SuppressWarnings("unused")
public class MaterialColorPickerDialog extends AlertDialog {

    private ColorPickerView colorPickerView;

    public MaterialColorPickerDialog(Context context) {
        super(context);
    }

    /**
     * Builder class for create {@link MaterialColorPickerDialog}.
     */
    public static class Builder extends MaterialAlertDialogBuilder {
        private ColorpickerviewDialogColorpickerBinding dialogBinding;
        private ColorPickerView colorPickerView;
        private boolean shouldAttachAlphaSlideBar = true;
        private boolean shouldAttachBrightnessSlideBar = true;
        private int bottomSpace = UIExtKt.getDp(10f);

        public Builder(Context context) {
            super(context);
            onCreate();
        }

        public Builder(Context context, int themeResId) {
            super(context, themeResId);
            onCreate();
        }

        private void onCreate() {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            this.dialogBinding =
                    ColorpickerviewDialogColorpickerBinding.inflate(layoutInflater, null, false);
            this.colorPickerView = dialogBinding.colorPickerView;
            this.colorPickerView.attachAlphaSlider(dialogBinding.alphaSlideBar);
            this.colorPickerView.attachBrightnessSlider(dialogBinding.brightnessSlideBar);
            this.colorPickerView.setColorListener((ColorEnvelopeListener) (envelope, fromUser) -> {
                // no stubs
            });

            int colorPickerViewSize = UIExtKt.getDp(250f);
            int colorPickerMarginVertical = UIExtKt.getDp(10f);
            FrameLayout.LayoutParams colorPickerViewLp = new FrameLayout.LayoutParams(colorPickerViewSize, colorPickerViewSize);
            colorPickerViewLp.gravity = Gravity.CENTER;
            colorPickerViewLp.setMargins(0, colorPickerMarginVertical, 0, colorPickerMarginVertical);
            dialogBinding.colorPickerView.setLayoutParams(colorPickerViewLp);

            Drawable colorPickerDrawable = AppCompatResources.getDrawable(getContext(), R.drawable.ic_color_picker_selector);
            dialogBinding.colorPickerView.setSelectorDrawable(colorPickerDrawable);
            dialogBinding.alphaSlideBar.setSelectorDrawable(colorPickerDrawable);
            dialogBinding.brightnessSlideBar.setSelectorDrawable(colorPickerDrawable);

            super.setView(dialogBinding.getRoot());
        }

        /**
         * gets {@link ColorPickerView} on {@link Builder}.
         *
         * @return {@link ColorPickerView}.
         */
        public ColorPickerView getColorPickerView() {
            return colorPickerView;
        }

        /**
         * sets {@link ColorPickerView} manually.
         *
         * @param colorPickerView {@link ColorPickerView}.
         * @return {@link Builder}.
         */
        public Builder setColorPickerView(ColorPickerView colorPickerView) {
            this.dialogBinding.colorPickerViewFrame.removeAllViews();
            this.dialogBinding.colorPickerViewFrame.addView(colorPickerView);
            return this;
        }

        /**
         * if true, attaches a {@link AlphaSlideBar} on the {@link MaterialColorPickerDialog}.
         *
         * @param value true or false.
         * @return {@link Builder}.
         */
        public Builder attachAlphaSlideBar(boolean value) {
            this.shouldAttachAlphaSlideBar = value;
            return this;
        }

        /**
         * if true, attaches a {@link BrightnessSlideBar} on the {@link MaterialColorPickerDialog}.
         *
         * @param value true or false.
         * @return {@link Builder}.
         */
        public Builder attachBrightnessSlideBar(boolean value) {
            this.shouldAttachBrightnessSlideBar = value;
            return this;
        }

        /**
         * sets the preference name.
         *
         * @param preferenceName preference name.
         * @return {@link Builder}.
         */
        public Builder setPreferenceName(String preferenceName) {
            if (getColorPickerView() != null) {
                getColorPickerView().setPreferenceName(preferenceName);
            }
            return this;
        }

        /**
         * sets the margin of the bottom. this space visible when {@link AlphaSlideBar} or {@link
         * BrightnessSlideBar} is attached.
         *
         * @param bottomSpace space of the bottom.
         * @return {@link Builder}.
         */
        public Builder setBottomSpace(int bottomSpace) {
            this.bottomSpace = UIExtKt.getDp(bottomSpace);
            return this;
        }

        /**
         * sets positive button with {@link ColorPickerViewListener} on the {@link MaterialColorPickerDialog}.
         *
         * @param textId        string resource integer id.
         * @param colorListener {@link ColorListener}.
         * @return {@link Builder}.
         */
        public Builder setPositiveButton(int textId, final ColorPickerViewListener colorListener) {
            super.setPositiveButton(textId, getOnClickListener(colorListener));
            return this;
        }

        /**
         * sets positive button with {@link ColorPickerViewListener} on the {@link MaterialColorPickerDialog}.
         *
         * @param text          string text value.
         * @param colorListener {@link ColorListener}.
         * @return {@link Builder}.
         */
        public Builder setPositiveButton(
                CharSequence text, final ColorPickerViewListener colorListener) {
            super.setPositiveButton(text, getOnClickListener(colorListener));
            return this;
        }

        @NonNull
        @Override
        public Builder setNegativeButton(int textId, OnClickListener listener) {
            super.setNegativeButton(textId, listener);
            return this;
        }

        @NonNull
        @Override
        public Builder setNegativeButton(CharSequence text, OnClickListener listener) {
            super.setNegativeButton(text, listener);
            return this;
        }

        private OnClickListener getOnClickListener(final ColorPickerViewListener colorListener) {
            return (dialogInterface, i) -> {
                if (colorListener instanceof ColorListener) {
                    ((ColorListener) colorListener).onColorSelected(getColorPickerView().getColor(), true);
                } else if (colorListener instanceof ColorEnvelopeListener) {
                    ((ColorEnvelopeListener) colorListener)
                            .onColorSelected(getColorPickerView().getColorEnvelope(), true);
                }
                if (getColorPickerView() != null) {
                    ColorPickerPreferenceManager.getInstance(getContext())
                            .saveColorPickerData(getColorPickerView());
                }
            };
        }

        /**
         * shows a created {@link MaterialColorPickerDialog}.
         *
         * @return {@link AlertDialog}.
         */
        @Override
        @NonNull
        public AlertDialog create() {
            if (getColorPickerView() != null) {
                this.dialogBinding.colorPickerViewFrame.removeAllViews();
                this.dialogBinding.colorPickerViewFrame.addView(getColorPickerView());

                AlphaSlideBar alphaSlideBar = getColorPickerView().getAlphaSlideBar();
                if (shouldAttachAlphaSlideBar && alphaSlideBar != null) {
                    this.dialogBinding.alphaSlideBarFrame.removeAllViews();
                    this.dialogBinding.alphaSlideBarFrame.addView(alphaSlideBar);
                    this.getColorPickerView().attachAlphaSlider(alphaSlideBar);
                } else if (!shouldAttachAlphaSlideBar) {
                    this.dialogBinding.alphaSlideBarFrame.removeAllViews();
                }

                BrightnessSlideBar brightnessSlideBar = getColorPickerView().getBrightnessSlider();
                if (shouldAttachBrightnessSlideBar && brightnessSlideBar != null) {
                    this.dialogBinding.brightnessSlideBarFrame.removeAllViews();
                    this.dialogBinding.brightnessSlideBarFrame.addView(brightnessSlideBar);
                    this.getColorPickerView().attachBrightnessSlider(brightnessSlideBar);
                } else if (!shouldAttachBrightnessSlideBar) {
                    this.dialogBinding.brightnessSlideBarFrame.removeAllViews();
                }

                if (!shouldAttachAlphaSlideBar && !shouldAttachBrightnessSlideBar) {
                    this.dialogBinding.spaceBottom.setVisibility(View.GONE);
                } else {
                    this.dialogBinding.spaceBottom.setVisibility(View.VISIBLE);
                    this.dialogBinding.spaceBottom.getLayoutParams().height = bottomSpace;
                }
            }

            super.setView(dialogBinding.getRoot());
            return super.create();
        }

        @NonNull
        @Override
        public Builder setTitle(int titleId) {
            super.setTitle(titleId);
            return this;
        }

        @NonNull
        @Override
        public Builder setTitle(CharSequence title) {
            super.setTitle(title);
            return this;
        }

        @NonNull
        @Override
        public Builder setCustomTitle(View customTitleView) {
            super.setCustomTitle(customTitleView);
            return this;
        }

        @NonNull
        @Override
        public Builder setMessage(int messageId) {
            super.setMessage(getContext().getString(messageId));
            return this;
        }

        @NonNull
        @Override
        public Builder setMessage(CharSequence message) {
            super.setMessage(message);
            return this;
        }

        @NonNull
        @Override
        public Builder setIcon(int iconId) {
            super.setIcon(iconId);
            return this;
        }

        @NonNull
        @Override
        public Builder setIcon(Drawable icon) {
            super.setIcon(icon);
            return this;
        }

        @NonNull
        @Override
        public Builder setIconAttribute(int attrId) {
            super.setIconAttribute(attrId);
            return this;
        }

        @NonNull
        @Override
        public Builder setCancelable(boolean cancelable) {
            super.setCancelable(cancelable);
            return this;
        }

        @NonNull
        @Override
        public Builder setOnCancelListener(OnCancelListener onCancelListener) {
            super.setOnCancelListener(onCancelListener);
            return this;
        }

        @NonNull
        @Override
        public Builder setOnDismissListener(OnDismissListener onDismissListener) {
            super.setOnDismissListener(onDismissListener);
            return this;
        }

        @NonNull
        @Override
        public Builder setOnKeyListener(OnKeyListener onKeyListener) {
            super.setOnKeyListener(onKeyListener);
            return this;
        }

        @NonNull
        @Override
        public Builder setPositiveButton(int textId, OnClickListener listener) {
            super.setPositiveButton(textId, listener);
            return this;
        }

        @NonNull
        @Override
        public Builder setPositiveButton(CharSequence text, OnClickListener listener) {
            super.setPositiveButton(text, listener);
            return this;
        }

        @NonNull
        @Override
        public Builder setNeutralButton(int textId, OnClickListener listener) {
            super.setNeutralButton(textId, listener);
            return this;
        }

        @NonNull
        @Override
        public Builder setNeutralButton(CharSequence text, OnClickListener listener) {
            super.setNeutralButton(text, listener);
            return this;
        }

        @NonNull
        @Override
        public Builder setItems(int itemsId, OnClickListener listener) {
            super.setItems(itemsId, listener);
            return this;
        }

        @NonNull
        @Override
        public Builder setItems(CharSequence[] items, OnClickListener listener) {
            super.setItems(items, listener);
            return this;
        }

        @NonNull
        @Override
        public Builder setAdapter(ListAdapter adapter, OnClickListener listener) {
            super.setAdapter(adapter, listener);
            return this;
        }

        @NonNull
        @Override
        public Builder setCursor(Cursor cursor, OnClickListener listener, @NonNull String labelColumn) {
            super.setCursor(cursor, listener, labelColumn);
            return this;
        }

        @NonNull
        @Override
        public Builder setMultiChoiceItems(
                int itemsId, boolean[] checkedItems, OnMultiChoiceClickListener listener) {
            super.setMultiChoiceItems(itemsId, checkedItems, listener);
            return this;
        }

        @NonNull
        @Override
        public Builder setMultiChoiceItems(
                CharSequence[] items, boolean[] checkedItems, OnMultiChoiceClickListener listener) {
            super.setMultiChoiceItems(items, checkedItems, listener);
            return this;
        }

        @NonNull
        @Override
        public Builder setMultiChoiceItems(
                Cursor cursor,
                @NonNull String isCheckedColumn,
                @NonNull String labelColumn,
                OnMultiChoiceClickListener listener) {
            super.setMultiChoiceItems(cursor, isCheckedColumn, labelColumn, listener);
            return this;
        }

        @NonNull
        @Override
        public Builder setSingleChoiceItems(int itemsId, int checkedItem, OnClickListener listener) {
            super.setSingleChoiceItems(itemsId, checkedItem, listener);
            return this;
        }

        @NonNull
        @Override
        public Builder setSingleChoiceItems(
                Cursor cursor, int checkedItem, @NonNull String labelColumn, OnClickListener listener) {
            super.setSingleChoiceItems(cursor, checkedItem, labelColumn, listener);
            return this;
        }

        @NonNull
        @Override
        public Builder setSingleChoiceItems(
                CharSequence[] items, int checkedItem, OnClickListener listener) {
            super.setSingleChoiceItems(items, checkedItem, listener);
            return this;
        }

        @NonNull
        @Override
        public Builder setSingleChoiceItems(
                ListAdapter adapter, int checkedItem, OnClickListener listener) {
            super.setSingleChoiceItems(adapter, checkedItem, listener);
            return this;
        }

        @NonNull
        @Override
        public Builder setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener) {
            super.setOnItemSelectedListener(listener);
            return this;
        }

        @NonNull
        @Override
        public Builder setView(int layoutResId) {
            super.setView(layoutResId);
            return this;
        }

        @NonNull
        @Override
        public Builder setView(View view) {
            super.setView(view);
            return this;
        }
    }
}
