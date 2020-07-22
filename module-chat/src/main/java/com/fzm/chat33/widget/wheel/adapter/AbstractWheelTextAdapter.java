/*
 *  Copyright 2011 Yuri Kanivets
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.fzm.chat33.widget.wheel.adapter;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fzm.chat33.R;
import com.fzm.chat33.widget.wheel.OnWheelChangedListener;
import com.fzm.chat33.widget.wheel.WheelView;


/**
 * Abstract wheel adapter provides common functionality for adapters.
 */
public abstract class AbstractWheelTextAdapter extends AbstractWheelAdapter {

    /**
     * Text view resource. Used as a default view for adapter.
     */
    private static final int TEXT_VIEW_ITEM_RESOURCE = -1;
    /**
     * Default text color
     */
    // 0xFF101010
    private static final int DEFAULT_TEXT_COLOR = 0XFF333333;
    /**
     * Default text color
     */
    public static final int LABEL_COLOR = 0xFF700070;
    /**
     * Default text size
     */
    // 默认是24
    private static final int DEFAULT_TEXT_SIZE = 18;
    /**
     * No resource constant.
     */
    private static final int NO_RESOURCE = 0;
    // Current context
    private final Context context;
    // Layout inflater
    private final LayoutInflater inflater;
    // Items resources
    private int itemResourceId;
    private int itemTextResourceId;
    // Empty items resources
    private int emptyItemResourceId;
    WheelView wheelView;
    // Text settings
    private int textColor = DEFAULT_TEXT_COLOR;
    private int textSize = DEFAULT_TEXT_SIZE;

    /**
     * Constructor
     *
     * @param context the current context
     */
    AbstractWheelTextAdapter(Context context) {
        this(context, TEXT_VIEW_ITEM_RESOURCE);
    }

    /**
     * Constructor
     *
     * @param context      the current context
     * @param itemResource the resource ID for a layout file containing a TextView to use
     *                     when instantiating items views
     */
    private AbstractWheelTextAdapter(Context context, int itemResource) {
        this(context, itemResource, NO_RESOURCE);
    }

    /**
     * Constructor
     *
     * @param context          the current context
     * @param itemResource     the resource ID for a layout file containing a TextView to use
     *                         when instantiating items views
     * @param itemTextResource the resource ID for a text view in the item layout
     */
    public AbstractWheelTextAdapter(Context context, int itemResource, int itemTextResource) {
        this.context = context;
        itemResourceId = itemResource;
        itemTextResourceId = itemTextResource;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * Gets text color
     *
     * @return the text color
     */
    public int getTextColor() {
        return textColor;
    }

    /**
     * Sets text color
     *
     * @param textColor the text color to set
     */
    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    /**
     * Gets text size
     *
     * @return the text size
     */
    public int getTextSize() {
        return textSize;
    }

    /**
     * Sets text size
     *
     * @param textSize the text size to set
     */
    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    /**
     * Gets resource Id for items views
     *
     * @return the item resource Id
     */
    public int getItemResource() {
        return itemResourceId;
    }

    /**
     * Sets resource Id for items views
     *
     * @param itemResourceId the resource Id to set
     */
    public void setItemResource(int itemResourceId) {
        this.itemResourceId = itemResourceId;
    }

    /**
     * Gets resource Id for text view in item layout
     *
     * @return the item text resource Id
     */
    public int getItemTextResource() {
        return itemTextResourceId;
    }

    /**
     * Sets resource Id for text view in item layout
     *
     * @param itemTextResourceId the item text resource Id to set
     */
    public void setItemTextResource(int itemTextResourceId) {
        this.itemTextResourceId = itemTextResourceId;
    }

    /**
     * Gets resource Id for empty items views
     *
     * @return the empty item resource Id
     */
    public int getEmptyItemResource() {
        return emptyItemResourceId;
    }

    /**
     * Sets resource Id for empty items views
     *
     * @param emptyItemResourceId the empty item resource Id to set
     */
    public void setEmptyItemResource(int emptyItemResourceId) {
        this.emptyItemResourceId = emptyItemResourceId;
    }

    /**
     * Returns text for specified item
     *
     * @param index the item index
     * @return the text of specified items
     */
    protected abstract CharSequence getItemText(int index);

    @Override
    public View getItem(final int index, View convertView, ViewGroup parent) {
        if (index >= 0 && index < getItemsCount()) {
            if (convertView == null) {
                convertView = getView(itemResourceId, parent);
            }
            final TextView textView = getTextView(convertView, itemTextResourceId);
            if (textView != null) {
                CharSequence text = getItemText(index);
                if (text == null) {
                    text = "";
                }
                textView.setText(text);
                final int orangeColor = R.color.chat_widget;
                if (wheelView != null)
                    wheelView.addChangingListener(new OnWheelChangedListener() {
                        @Override
                        public void onChanged(WheelView wheel, int oldValue, int newValue) {
                            // TODO Auto-generated method stub
                            if (itemResourceId == TEXT_VIEW_ITEM_RESOURCE) {
                                if (wheelView != null && index == newValue) {
                                    configureTextView(textView, orangeColor, textSize + 2);
                                } else {
                                    configureTextView(textView, textColor, textSize);
                                }

                            }
                        }
                    });

                if (itemResourceId == TEXT_VIEW_ITEM_RESOURCE) {
                    if (wheelView != null && index == wheelView.getCurrentItem()) {
                        configureTextView(textView, orangeColor, textSize);
                    } else {
                        configureTextView(textView, textColor, textSize);
                    }

                }
            }
            return convertView;
        }
        return null;
    }

    @Override
    public View getEmptyItem(View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = getView(emptyItemResourceId, parent);
        }
        if (emptyItemResourceId == TEXT_VIEW_ITEM_RESOURCE && convertView instanceof TextView) {
            configureTextView((TextView) convertView, textColor, textSize);
        }

        return convertView;
    }

    /**
     * Configures text view. Is called for the TEXT_VIEW_ITEM_RESOURCE views.
     *
     * @param view the text view to be configured
     */
    private void configureTextView(TextView view, int color, int size) {
        view.setTextColor(color);
        view.setGravity(Gravity.CENTER);
        view.setTextSize(size);
        view.setLines(1);
        view.setLineSpacing(1.1f, 1.1f);
        // view.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
    }

    /**
     * Loads a text view from view
     *
     * @param view         the text view or layout containing it
     * @param textResource the text resource Id in layout
     * @return the loaded text view
     */
    private TextView getTextView(View view, int textResource) {
        TextView text = null;
        try {
            if (textResource == NO_RESOURCE && view instanceof TextView) {
                text = (TextView) view;
            } else if (textResource != NO_RESOURCE) {
                text = (TextView) view.findViewById(textResource);
            }
        } catch (ClassCastException e) {
            Log.e("AbstractWheelAdapter", "You must supply a resource ID for a TextView");
            throw new IllegalStateException("AbstractWheelAdapter requires the resource ID to be a TextView", e);
        }

        return text;
    }

    /**
     * Loads view from resources
     *
     * @param resource the resource Id
     * @return the loaded view or null if resource is not set
     */
    private View getView(int resource, ViewGroup parent) {
        switch (resource) {
            case NO_RESOURCE:
                return null;
            case TEXT_VIEW_ITEM_RESOURCE:
                return new TextView(context);
            default:
                return inflater.inflate(resource, parent, false);
        }
    }
}
