
    /**
     * Per-child layout information for layouts that support margins.
     * See
     * {@link android.R.styleable#ViewGroup_MarginLayout ViewGroup Margin Layout Attributes}
     * for a list of all child view attributes that this class supports.
     */
    public static class MarginLayoutParams extends ViewGroup.LayoutParams {
        /**
         * The left margin in pixels of the child. Margin values should be positive.
         * Call {@link ViewGroup#setLayoutParams(LayoutParams)} after reassigning a new value
         * to this field.
         */
        @ViewDebug.ExportedProperty(category = "layout")
        public int leftMargin;

        /**
         * The top margin in pixels of the child. Margin values should be positive.
         * Call {@link ViewGroup#setLayoutParams(LayoutParams)} after reassigning a new value
         * to this field.
         */
        @ViewDebug.ExportedProperty(category = "layout")
        public int topMargin;

        /**
         * The right margin in pixels of the child. Margin values should be positive.
         * Call {@link ViewGroup#setLayoutParams(LayoutParams)} after reassigning a new value
         * to this field.
         */
        @ViewDebug.ExportedProperty(category = "layout")
        public int rightMargin;

        /**
         * The bottom margin in pixels of the child. Margin values should be positive.
         * Call {@link ViewGroup#setLayoutParams(LayoutParams)} after reassigning a new value
         * to this field.
         */
        @ViewDebug.ExportedProperty(category = "layout")
        public int bottomMargin;

        /**
         * The start margin in pixels of the child. Margin values should be positive.
         * Call {@link ViewGroup#setLayoutParams(LayoutParams)} after reassigning a new value
         * to this field.
         */
        @ViewDebug.ExportedProperty(category = "layout")
        private int startMargin = DEFAULT_MARGIN_RELATIVE;

        /**
         * The end margin in pixels of the child. Margin values should be positive.
         * Call {@link ViewGroup#setLayoutParams(LayoutParams)} after reassigning a new value
         * to this field.
         */
        @ViewDebug.ExportedProperty(category = "layout")
        private int endMargin = DEFAULT_MARGIN_RELATIVE;

        /**
         * The default start and end margin.
         * @hide
         */
        public static final int DEFAULT_MARGIN_RELATIVE = Integer.MIN_VALUE;

        /**
         * Bit  0: layout direction
         * Bit  1: layout direction
         * Bit  2: left margin undefined
         * Bit  3: right margin undefined
         * Bit  4: is RTL compatibility mode
         * Bit  5: need resolution
         *
         * Bit 6 to 7 not used
         *
         * @hide
         */
        @ViewDebug.ExportedProperty(category = "layout", flagMapping = {
                @ViewDebug.FlagToString(mask = LAYOUT_DIRECTION_MASK,
                        equals = LAYOUT_DIRECTION_MASK, name = "LAYOUT_DIRECTION"),
                @ViewDebug.FlagToString(mask = LEFT_MARGIN_UNDEFINED_MASK,
                        equals = LEFT_MARGIN_UNDEFINED_MASK, name = "LEFT_MARGIN_UNDEFINED_MASK"),
                @ViewDebug.FlagToString(mask = RIGHT_MARGIN_UNDEFINED_MASK,
                        equals = RIGHT_MARGIN_UNDEFINED_MASK, name = "RIGHT_MARGIN_UNDEFINED_MASK"),
                @ViewDebug.FlagToString(mask = RTL_COMPATIBILITY_MODE_MASK,
                        equals = RTL_COMPATIBILITY_MODE_MASK, name = "RTL_COMPATIBILITY_MODE_MASK"),
                @ViewDebug.FlagToString(mask = NEED_RESOLUTION_MASK,
                        equals = NEED_RESOLUTION_MASK, name = "NEED_RESOLUTION_MASK")
        }, formatToHexString = true)
        byte mMarginFlags;

        private static final int LAYOUT_DIRECTION_MASK = 0x00000003;
        private static final int LEFT_MARGIN_UNDEFINED_MASK = 0x00000004;
        private static final int RIGHT_MARGIN_UNDEFINED_MASK = 0x00000008;
        private static final int RTL_COMPATIBILITY_MODE_MASK = 0x00000010;
        private static final int NEED_RESOLUTION_MASK = 0x00000020;

        private static final int DEFAULT_MARGIN_RESOLVED = 0;
        private static final int UNDEFINED_MARGIN = DEFAULT_MARGIN_RELATIVE;

        /**
         * Creates a new set of layout parameters. The values are extracted from
         * the supplied attributes set and context.
         *
         * @param c the application environment
         * @param attrs the set of attributes from which to extract the layout
         *              parameters' values
         */
        public MarginLayoutParams(Context c, AttributeSet attrs) {
            super();

            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.ViewGroup_MarginLayout);
            setBaseAttributes(a,
                    R.styleable.ViewGroup_MarginLayout_layout_width,
                    R.styleable.ViewGroup_MarginLayout_layout_height);

            int margin = a.getDimensionPixelSize(
                    com.android.internal.R.styleable.ViewGroup_MarginLayout_layout_margin, -1);
            if (margin >= 0) {
                leftMargin = margin;
                topMargin = margin;
                rightMargin= margin;
                bottomMargin = margin;
            } else {
                leftMargin = a.getDimensionPixelSize(
                        R.styleable.ViewGroup_MarginLayout_layout_marginLeft,
                        UNDEFINED_MARGIN);
                if (leftMargin == UNDEFINED_MARGIN) {
                    mMarginFlags |= LEFT_MARGIN_UNDEFINED_MASK;
                    leftMargin = DEFAULT_MARGIN_RESOLVED;
                }
                rightMargin = a.getDimensionPixelSize(
                        R.styleable.ViewGroup_MarginLayout_layout_marginRight,
                        UNDEFINED_MARGIN);
                if (rightMargin == UNDEFINED_MARGIN) {
                    mMarginFlags |= RIGHT_MARGIN_UNDEFINED_MASK;
                    rightMargin = DEFAULT_MARGIN_RESOLVED;
                }

                topMargin = a.getDimensionPixelSize(
                        R.styleable.ViewGroup_MarginLayout_layout_marginTop,
                        DEFAULT_MARGIN_RESOLVED);
                bottomMargin = a.getDimensionPixelSize(
                        R.styleable.ViewGroup_MarginLayout_layout_marginBottom,
                        DEFAULT_MARGIN_RESOLVED);

                startMargin = a.getDimensionPixelSize(
                        R.styleable.ViewGroup_MarginLayout_layout_marginStart,
                        DEFAULT_MARGIN_RELATIVE);
                endMargin = a.getDimensionPixelSize(
                        R.styleable.ViewGroup_MarginLayout_layout_marginEnd,
                        DEFAULT_MARGIN_RELATIVE);

                if (isMarginRelative()) {
                   mMarginFlags |= NEED_RESOLUTION_MASK;
                }
            }

            final boolean hasRtlSupport = c.getApplicationInfo().hasRtlSupport();
            final int targetSdkVersion = c.getApplicationInfo().targetSdkVersion;
            if (targetSdkVersion < JELLY_BEAN_MR1 || !hasRtlSupport) {
                mMarginFlags |= RTL_COMPATIBILITY_MODE_MASK;
            }

            // Layout direction is LTR by default
            mMarginFlags |= LAYOUT_DIRECTION_LTR;

            a.recycle();
        }

        /**
         * {@inheritDoc}
         */
        public MarginLayoutParams(int width, int height) {
            super(width, height);

            mMarginFlags |= LEFT_MARGIN_UNDEFINED_MASK;
            mMarginFlags |= RIGHT_MARGIN_UNDEFINED_MASK;

            mMarginFlags &= ~NEED_RESOLUTION_MASK;
            mMarginFlags &= ~RTL_COMPATIBILITY_MODE_MASK;
        }

        /**
         * Copy constructor. Clones the width, height and margin values of the source.
         *
         * @param source The layout params to copy from.
         */
        public MarginLayoutParams(MarginLayoutParams source) {
            this.width = source.width;
            this.height = source.height;

            this.leftMargin = source.leftMargin;
            this.topMargin = source.topMargin;
            this.rightMargin = source.rightMargin;
            this.bottomMargin = source.bottomMargin;
            this.startMargin = source.startMargin;
            this.endMargin = source.endMargin;

            this.mMarginFlags = source.mMarginFlags;
        }

        /**
         * {@inheritDoc}
         */
        public MarginLayoutParams(LayoutParams source) {
            super(source);

            mMarginFlags |= LEFT_MARGIN_UNDEFINED_MASK;
            mMarginFlags |= RIGHT_MARGIN_UNDEFINED_MASK;

            mMarginFlags &= ~NEED_RESOLUTION_MASK;
            mMarginFlags &= ~RTL_COMPATIBILITY_MODE_MASK;
        }

        /**
         * @hide Used internally.
         */
        public final void copyMarginsFrom(MarginLayoutParams source) {
            this.leftMargin = source.leftMargin;
            this.topMargin = source.topMargin;
            this.rightMargin = source.rightMargin;
            this.bottomMargin = source.bottomMargin;
            this.startMargin = source.startMargin;
            this.endMargin = source.endMargin;

            this.mMarginFlags = source.mMarginFlags;
        }

        /**
         * Sets the margins, in pixels. A call to {@link android.view.View#requestLayout()} needs
         * to be done so that the new margins are taken into account. Left and right margins may be
         * overriden by {@link android.view.View#requestLayout()} depending on layout direction.
         * Margin values should be positive.
         *
         * @param left the left margin size
         * @param top the top margin size
         * @param right the right margin size
         * @param bottom the bottom margin size
         *
         * @attr ref android.R.styleable#ViewGroup_MarginLayout_layout_marginLeft
         * @attr ref android.R.styleable#ViewGroup_MarginLayout_layout_marginTop
         * @attr ref android.R.styleable#ViewGroup_MarginLayout_layout_marginRight
         * @attr ref android.R.styleable#ViewGroup_MarginLayout_layout_marginBottom
         */
        public void setMargins(int left, int top, int right, int bottom) {
            leftMargin = left;
            topMargin = top;
            rightMargin = right;
            bottomMargin = bottom;
            mMarginFlags &= ~LEFT_MARGIN_UNDEFINED_MASK;
            mMarginFlags &= ~RIGHT_MARGIN_UNDEFINED_MASK;
            if (isMarginRelative()) {
                mMarginFlags |= NEED_RESOLUTION_MASK;
            } else {
                mMarginFlags &= ~NEED_RESOLUTION_MASK;
            }
        }

        /**
         * Sets the relative margins, in pixels. A call to {@link android.view.View#requestLayout()}
         * needs to be done so that the new relative margins are taken into account. Left and right
         * margins may be overriden by {@link android.view.View#requestLayout()} depending on layout
         * direction. Margin values should be positive.
         *
         * @param start the start margin size
         * @param top the top margin size
         * @param end the right margin size
         * @param bottom the bottom margin size
         *
         * @attr ref android.R.styleable#ViewGroup_MarginLayout_layout_marginStart
         * @attr ref android.R.styleable#ViewGroup_MarginLayout_layout_marginTop
         * @attr ref android.R.styleable#ViewGroup_MarginLayout_layout_marginEnd
         * @attr ref android.R.styleable#ViewGroup_MarginLayout_layout_marginBottom
         *
         * @hide
         */
        public void setMarginsRelative(int start, int top, int end, int bottom) {
            startMargin = start;
            topMargin = top;
            endMargin = end;
            bottomMargin = bottom;
            mMarginFlags |= NEED_RESOLUTION_MASK;
        }

        /**
         * Sets the relative start margin. Margin values should be positive.
         *
         * @param start the start margin size
         *
         * @attr ref android.R.styleable#ViewGroup_MarginLayout_layout_marginStart
         */
        public void setMarginStart(int start) {
            startMargin = start;
            mMarginFlags |= NEED_RESOLUTION_MASK;
        }

        /**
         * Returns the start margin in pixels.
         *
         * @attr ref android.R.styleable#ViewGroup_MarginLayout_layout_marginStart
         *
         * @return the start margin in pixels.
         */
        public int getMarginStart() {
            if (startMargin != DEFAULT_MARGIN_RELATIVE) return startMargin;
            if ((mMarginFlags & NEED_RESOLUTION_MASK) == NEED_RESOLUTION_MASK) {
                doResolveMargins();
            }
            switch(mMarginFlags & LAYOUT_DIRECTION_MASK) {
                case View.LAYOUT_DIRECTION_RTL:
                    return rightMargin;
                case View.LAYOUT_DIRECTION_LTR:
                default:
                    return leftMargin;
            }
        }

        /**
         * Sets the relative end margin. Margin values should be positive.
         *
         * @param end the end margin size
         *
         * @attr ref android.R.styleable#ViewGroup_MarginLayout_layout_marginEnd
         */
        public void setMarginEnd(int end) {
            endMargin = end;
            mMarginFlags |= NEED_RESOLUTION_MASK;
        }

        /**
         * Returns the end margin in pixels.
         *
         * @attr ref android.R.styleable#ViewGroup_MarginLayout_layout_marginEnd
         *
         * @return the end margin in pixels.
         */
        public int getMarginEnd() {
            if (endMargin != DEFAULT_MARGIN_RELATIVE) return endMargin;
            if ((mMarginFlags & NEED_RESOLUTION_MASK) == NEED_RESOLUTION_MASK) {
                doResolveMargins();
            }
            switch(mMarginFlags & LAYOUT_DIRECTION_MASK) {
                case View.LAYOUT_DIRECTION_RTL:
                    return leftMargin;
                case View.LAYOUT_DIRECTION_LTR:
                default:
                    return rightMargin;
            }
        }

        /**
         * Check if margins are relative.
         *
         * @attr ref android.R.styleable#ViewGroup_MarginLayout_layout_marginStart
         * @attr ref android.R.styleable#ViewGroup_MarginLayout_layout_marginEnd
         *
         * @return true if either marginStart or marginEnd has been set.
         */
        public boolean isMarginRelative() {
            return (startMargin != DEFAULT_MARGIN_RELATIVE || endMargin != DEFAULT_MARGIN_RELATIVE);
        }

        /**
         * Set the layout direction
         * @param layoutDirection the layout direction.
         *        Should be either {@link View#LAYOUT_DIRECTION_LTR}
         *                     or {@link View#LAYOUT_DIRECTION_RTL}.
         */
        public void setLayoutDirection(int layoutDirection) {
            if (layoutDirection != View.LAYOUT_DIRECTION_LTR &&
                    layoutDirection != View.LAYOUT_DIRECTION_RTL) return;
            if (layoutDirection != (mMarginFlags & LAYOUT_DIRECTION_MASK)) {
                mMarginFlags &= ~LAYOUT_DIRECTION_MASK;
                mMarginFlags |= (layoutDirection & LAYOUT_DIRECTION_MASK);
                if (isMarginRelative()) {
                    mMarginFlags |= NEED_RESOLUTION_MASK;
                } else {
                    mMarginFlags &= ~NEED_RESOLUTION_MASK;
                }
            }
        }

        /**
         * Retuns the layout direction. Can be either {@link View#LAYOUT_DIRECTION_LTR} or
         * {@link View#LAYOUT_DIRECTION_RTL}.
         *
         * @return the layout direction.
         */
        public int getLayoutDirection() {
            return (mMarginFlags & LAYOUT_DIRECTION_MASK);
        }

        /**
         * This will be called by {@link android.view.View#requestLayout()}. Left and Right margins
         * may be overridden depending on layout direction.
         */
        @Override
        public void resolveLayoutDirection(int layoutDirection) {
            setLayoutDirection(layoutDirection);

            // No relative margin or pre JB-MR1 case or no need to resolve, just dont do anything
            // Will use the left and right margins if no relative margin is defined.
            if (!isMarginRelative() ||
                    (mMarginFlags & NEED_RESOLUTION_MASK) != NEED_RESOLUTION_MASK) return;

            // Proceed with resolution
            doResolveMargins();
        }

        private void doResolveMargins() {
            if ((mMarginFlags & RTL_COMPATIBILITY_MODE_MASK) == RTL_COMPATIBILITY_MODE_MASK) {
                // if left or right margins are not defined and if we have some start or end margin
                // defined then use those start and end margins.
                if ((mMarginFlags & LEFT_MARGIN_UNDEFINED_MASK) == LEFT_MARGIN_UNDEFINED_MASK
                        && startMargin > DEFAULT_MARGIN_RELATIVE) {
                    leftMargin = startMargin;
                }
                if ((mMarginFlags & RIGHT_MARGIN_UNDEFINED_MASK) == RIGHT_MARGIN_UNDEFINED_MASK
                        && endMargin > DEFAULT_MARGIN_RELATIVE) {
                    rightMargin = endMargin;
                }
            } else {
                // We have some relative margins (either the start one or the end one or both). So use
                // them and override what has been defined for left and right margins. If either start
                // or end margin is not defined, just set it to default "0".
                switch(mMarginFlags & LAYOUT_DIRECTION_MASK) {
                    case View.LAYOUT_DIRECTION_RTL:
                        leftMargin = (endMargin > DEFAULT_MARGIN_RELATIVE) ?
                                endMargin : DEFAULT_MARGIN_RESOLVED;
                        rightMargin = (startMargin > DEFAULT_MARGIN_RELATIVE) ?
                                startMargin : DEFAULT_MARGIN_RESOLVED;
                        break;
                    case View.LAYOUT_DIRECTION_LTR:
                    default:
                        leftMargin = (startMargin > DEFAULT_MARGIN_RELATIVE) ?
                                startMargin : DEFAULT_MARGIN_RESOLVED;
                        rightMargin = (endMargin > DEFAULT_MARGIN_RELATIVE) ?
                                endMargin : DEFAULT_MARGIN_RESOLVED;
                        break;
                }
            }
            mMarginFlags &= ~NEED_RESOLUTION_MASK;
        }

        /**
         * @hide
         */
        public boolean isLayoutRtl() {
            return ((mMarginFlags & LAYOUT_DIRECTION_MASK) == View.LAYOUT_DIRECTION_RTL);
        }

        /**
         * @hide
         */
        @Override
        public void onDebugDraw(View view, Canvas canvas, Paint paint) {
            Insets oi = isLayoutModeOptical(view.mParent) ? view.getOpticalInsets() : Insets.NONE;

            fillDifference(canvas,
                    view.getLeft()   + oi.left,
                    view.getTop()    + oi.top,
                    view.getRight()  - oi.right,
                    view.getBottom() - oi.bottom,
                    leftMargin,
                    topMargin,
                    rightMargin,
                    bottomMargin,
                    paint);
        }

        /** @hide */
        @Override
        protected void encodeProperties(@NonNull ViewHierarchyEncoder encoder) {
            super.encodeProperties(encoder);
            encoder.addProperty("leftMargin", leftMargin);
            encoder.addProperty("topMargin", topMargin);
            encoder.addProperty("rightMargin", rightMargin);
            encoder.addProperty("bottomMargin", bottomMargin);
            encoder.addProperty("startMargin", startMargin);
            encoder.addProperty("endMargin", endMargin);
        }
    }