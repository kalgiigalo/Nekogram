package org.telegram.ui.Components.chat.buttons;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.CounterView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.blur3.BlurredBackgroundDrawableViewFactory;
import org.telegram.ui.Components.blur3.drawable.BlurredBackgroundDrawable;
import org.telegram.ui.Components.blur3.drawable.color.BlurredBackgroundColorProvider;

/**
 * A vertical capsule that combines the chat search "up" and "down" navigation buttons into a
 * single rounded-pill container. The capsule is as wide as one circle button's diameter and as
 * tall as both circles' diameters combined. The top half is the "up" button, the bottom half the
 * "down" button, each keeping its own click callback.
 */
@SuppressLint("ViewConstructor")
public class ChatActivityBlurredRoundSearchButtonsCapsule extends FrameLayout {

    /** Width of the capsule: equal to one circle button's diameter. */
    public static final int BUTTON_WIDTH = 56;
    /** Height of the capsule: two circle-button diameters stacked (up on top, down below). */
    public static final int CAPSULE_HEIGHT = BUTTON_WIDTH * 2;

    private final Theme.ResourcesProvider resourcesProvider;

    private BlurredBackgroundDrawable backgroundDrawable;
    private ImageView upImageView;
    private ImageView downImageView;
    private CounterView counterView;

    private OnClickListener upClickListener;
    private OnClickListener downClickListener;

    private boolean upEnabled = true;
    private boolean downEnabled = true;

    private boolean reversed;

    public ChatActivityBlurredRoundSearchButtonsCapsule(Context context, Theme.ResourcesProvider resourcesProvider) {
        super(context);
        this.resourcesProvider = resourcesProvider;
    }

    public static ChatActivityBlurredRoundSearchButtonsCapsule create(
        Context context,
        Theme.ResourcesProvider resourcesProvider,
        BlurredBackgroundDrawableViewFactory factory,
        BlurredBackgroundColorProvider colorProvider,
        @DrawableRes int upRes,
        @DrawableRes int downRes
    ) {
        ChatActivityBlurredRoundSearchButtonsCapsule capsule = new ChatActivityBlurredRoundSearchButtonsCapsule(context, resourcesProvider);

        final int color = Theme.getColor(Theme.key_glass_defaultIcon, resourcesProvider);
        capsule.setBlurredBackgroundDrawable(factory.create(capsule, colorProvider));
        capsule.setIconColor(color);
        capsule.setIcons(upRes, downRes);

        return capsule;
    }

    public void setBlurredBackgroundDrawable(BlurredBackgroundDrawable drawable) {
        backgroundDrawable = drawable;
        backgroundDrawable.setPadding(dp(CLICK_ZONE_MARGIN_INNER));
        backgroundDrawable.setRadius(dp(BUTTON_WIDTH / 2f));
    }

    private static final int CLICK_ZONE_MARGIN_INNER = 6;

    public void setIcons(@DrawableRes int upRes, @DrawableRes int downRes) {
        final int iconSize = 48;

        upImageView = new ImageView(getContext());
        upImageView.setScaleType(ImageView.ScaleType.CENTER);
        upImageView.setImageResource(upRes);
        upImageView.setScaleY(-1f);
        addView(upImageView, LayoutHelper.createFrame(iconSize, iconSize, Gravity.CENTER));

        downImageView = new ImageView(getContext());
        downImageView.setScaleType(ImageView.ScaleType.CENTER);
        downImageView.setImageResource(downRes);
        downImageView.setScaleY(1f);
        addView(downImageView, LayoutHelper.createFrame(iconSize, iconSize, Gravity.CENTER));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        final int iconSize = dp(48);
        if (upImageView != null) {
            upImageView.layout(
                (getWidth() - iconSize) / 2,
                dp(8),
                (getWidth() + iconSize) / 2,
                dp(8) + iconSize);
        }
        if (downImageView != null) {
            downImageView.layout(
                (getWidth() - iconSize) / 2,
                getHeight() - dp(8) - iconSize,
                (getWidth() + iconSize) / 2,
                getHeight() - dp(8));
        }
    }

    public void setIconColor(int color) {
        if (upImageView != null) {
            upImageView.setColorFilter(color);
        }
        if (downImageView != null) {
            downImageView.setColorFilter(color);
        }
    }

    public void reverseIconsByY() {
        reversed = !reversed;
        if (upImageView != null) {
            upImageView.setScaleY(reversed ? 1f : -1f);
        }
        if (downImageView != null) {
            downImageView.setScaleY(reversed ? -1f : 1f);
        }
    }

    public void setCount(int count, boolean animated) {
        if (counterView == null) {
            counterView = new CounterView(getContext(), resourcesProvider);
            counterView.setReverse(reversed);
            addView(counterView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 28, Gravity.TOP));
        }
        counterView.setCount(count, animated);
    }

    public void updateColors() {
        if (backgroundDrawable != null) {
            backgroundDrawable.updateColors();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (backgroundDrawable != null) {
            backgroundDrawable.setBounds(0, 0, w, h);
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (backgroundDrawable != null) {
            backgroundDrawable.draw(canvas);
        }
        super.draw(canvas);
    }

    public void setButtonEnabled(boolean upEnabled, boolean downEnabled, boolean animated) {
        this.upEnabled = upEnabled;
        this.downEnabled = downEnabled;
        setEnabled(upEnabled || downEnabled);
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                return true;
            }
            case MotionEvent.ACTION_UP: {
                final boolean top = event.getY() < getHeight() / 2f;
                if (top) {
                    if (upEnabled && upClickListener != null) {
                        upClickListener.onClick(this);
                    }
                } else {
                    if (downEnabled && downClickListener != null) {
                        downClickListener.onClick(this);
                    }
                }
                return true;
            }
        }
        return true;
    }

    public void setUpClickListener(@Nullable OnClickListener l) {
        upClickListener = l;
    }

    public void setDownClickListener(@Nullable OnClickListener l) {
        downClickListener = l;
    }
}