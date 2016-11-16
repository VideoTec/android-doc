
    /**
     * Per-child layout information associated with RelativeLayout.
     *
     * @attr ref android.R.styleable#RelativeLayout_Layout_layout_alignWithParentIfMissing
     * @attr ref android.R.styleable#RelativeLayout_Layout_layout_toLeftOf
     * @attr ref android.R.styleable#RelativeLayout_Layout_layout_toRightOf
     * @attr ref android.R.styleable#RelativeLayout_Layout_layout_above
     * @attr ref android.R.styleable#RelativeLayout_Layout_layout_below
     * @attr ref android.R.styleable#RelativeLayout_Layout_layout_alignBaseline
     * @attr ref android.R.styleable#RelativeLayout_Layout_layout_alignLeft
     * @attr ref android.R.styleable#RelativeLayout_Layout_layout_alignTop
     * @attr ref android.R.styleable#RelativeLayout_Layout_layout_alignRight
     * @attr ref android.R.styleable#RelativeLayout_Layout_layout_alignBottom
     * @attr ref android.R.styleable#RelativeLayout_Layout_layout_alignParentLeft
     * @attr ref android.R.styleable#RelativeLayout_Layout_layout_alignParentTop
     * @attr ref android.R.styleable#RelativeLayout_Layout_layout_alignParentRight
     * @attr ref android.R.styleable#RelativeLayout_Layout_layout_alignParentBottom
     * @attr ref android.R.styleable#RelativeLayout_Layout_layout_centerInParent
     * @attr ref android.R.styleable#RelativeLayout_Layout_layout_centerHorizontal
     * @attr ref android.R.styleable#RelativeLayout_Layout_layout_centerVertical
     * @attr ref android.R.styleable#RelativeLayout_Layout_layout_toStartOf
     * @attr ref android.R.styleable#RelativeLayout_Layout_layout_toEndOf
     * @attr ref android.R.styleable#RelativeLayout_Layout_layout_alignStart
     * @attr ref android.R.styleable#RelativeLayout_Layout_layout_alignEnd
     * @attr ref android.R.styleable#RelativeLayout_Layout_layout_alignParentStart
     * @attr ref android.R.styleable#RelativeLayout_Layout_layout_alignParentEnd
     */
    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        @ViewDebug.ExportedProperty(category = "layout", resolveId = true, indexMapping = {
            @ViewDebug.IntToString(from = ABOVE,               to = "above"),
            @ViewDebug.IntToString(from = ALIGN_BASELINE,      to = "alignBaseline"),
            @ViewDebug.IntToString(from = ALIGN_BOTTOM,        to = "alignBottom"),
            @ViewDebug.IntToString(from = ALIGN_LEFT,          to = "alignLeft"),
            @ViewDebug.IntToString(from = ALIGN_PARENT_BOTTOM, to = "alignParentBottom"),
            @ViewDebug.IntToString(from = ALIGN_PARENT_LEFT,   to = "alignParentLeft"),
            @ViewDebug.IntToString(from = ALIGN_PARENT_RIGHT,  to = "alignParentRight"),
            @ViewDebug.IntToString(from = ALIGN_PARENT_TOP,    to = "alignParentTop"),
            @ViewDebug.IntToString(from = ALIGN_RIGHT,         to = "alignRight"),
            @ViewDebug.IntToString(from = ALIGN_TOP,           to = "alignTop"),
            @ViewDebug.IntToString(from = BELOW,               to = "below"),
            @ViewDebug.IntToString(from = CENTER_HORIZONTAL,   to = "centerHorizontal"),
            @ViewDebug.IntToString(from = CENTER_IN_PARENT,    to = "center"),
            @ViewDebug.IntToString(from = CENTER_VERTICAL,     to = "centerVertical"),
            @ViewDebug.IntToString(from = LEFT_OF,             to = "leftOf"),
            @ViewDebug.IntToString(from = RIGHT_OF,            to = "rightOf"),
            @ViewDebug.IntToString(from = ALIGN_START,         to = "alignStart"),
            @ViewDebug.IntToString(from = ALIGN_END,           to = "alignEnd"),
            @ViewDebug.IntToString(from = ALIGN_PARENT_START,  to = "alignParentStart"),
            @ViewDebug.IntToString(from = ALIGN_PARENT_END,    to = "alignParentEnd"),
            @ViewDebug.IntToString(from = START_OF,            to = "startOf"),
            @ViewDebug.IntToString(from = END_OF,              to = "endOf")
        }, mapping = {
            @ViewDebug.IntToString(from = TRUE, to = "true"),
            @ViewDebug.IntToString(from = 0,    to = "false/NO_ID")
        })

        private int[] mRules = new int[VERB_COUNT];
        private int[] mInitialRules = new int[VERB_COUNT];

        private int mLeft, mTop, mRight, mBottom;

        private boolean mRulesChanged = false;
        private boolean mIsRtlCompatibilityMode = false;

        /**
         * When true, uses the parent as the anchor if the anchor doesn't exist or if
         * the anchor's visibility is GONE.
         */
        @ViewDebug.ExportedProperty(category = "layout")
        public boolean alignWithParent;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);

            TypedArray a = c.obtainStyledAttributes(attrs,
                    com.android.internal.R.styleable.RelativeLayout_Layout);

            final int targetSdkVersion = c.getApplicationInfo().targetSdkVersion;
            mIsRtlCompatibilityMode = (targetSdkVersion < JELLY_BEAN_MR1 ||
                    !c.getApplicationInfo().hasRtlSupport());

            final int[] rules = mRules;
            //noinspection MismatchedReadAndWriteOfArray
            final int[] initialRules = mInitialRules;

            final int N = a.getIndexCount();
            for (int i = 0; i < N; i++) {
                int attr = a.getIndex(i);
                switch (attr) {
                    case com.android.internal.R.styleable.RelativeLayout_Layout_layout_alignWithParentIfMissing:
                        alignWithParent = a.getBoolean(attr, false);
                        break;
                    case com.android.internal.R.styleable.RelativeLayout_Layout_layout_toLeftOf:
                        rules[LEFT_OF] = a.getResourceId(attr, 0);
                        break;
                    case com.android.internal.R.styleable.RelativeLayout_Layout_layout_toRightOf:
                        rules[RIGHT_OF] = a.getResourceId(attr, 0);
                        break;
                    case com.android.internal.R.styleable.RelativeLayout_Layout_layout_above:
                        rules[ABOVE] = a.getResourceId(attr, 0);
                        break;
                    case com.android.internal.R.styleable.RelativeLayout_Layout_layout_below:
                        rules[BELOW] = a.getResourceId(attr, 0);
                        break;
                    case com.android.internal.R.styleable.RelativeLayout_Layout_layout_alignBaseline:
                        rules[ALIGN_BASELINE] = a.getResourceId(attr, 0);
                        break;
                    case com.android.internal.R.styleable.RelativeLayout_Layout_layout_alignLeft:
                        rules[ALIGN_LEFT] = a.getResourceId(attr, 0);
                        break;
                    case com.android.internal.R.styleable.RelativeLayout_Layout_layout_alignTop:
                        rules[ALIGN_TOP] = a.getResourceId(attr, 0);
                        break;
                    case com.android.internal.R.styleable.RelativeLayout_Layout_layout_alignRight:
                        rules[ALIGN_RIGHT] = a.getResourceId(attr, 0);
                        break;
                    case com.android.internal.R.styleable.RelativeLayout_Layout_layout_alignBottom:
                        rules[ALIGN_BOTTOM] = a.getResourceId(attr, 0);
                        break;
                    case com.android.internal.R.styleable.RelativeLayout_Layout_layout_alignParentLeft:
                        rules[ALIGN_PARENT_LEFT] = a.getBoolean(attr, false) ? TRUE : 0;
                        break;
                    case com.android.internal.R.styleable.RelativeLayout_Layout_layout_alignParentTop:
                        rules[ALIGN_PARENT_TOP] = a.getBoolean(attr, false) ? TRUE : 0;
                        break;
                    case com.android.internal.R.styleable.RelativeLayout_Layout_layout_alignParentRight:
                        rules[ALIGN_PARENT_RIGHT] = a.getBoolean(attr, false) ? TRUE : 0;
                        break;
                    case com.android.internal.R.styleable.RelativeLayout_Layout_layout_alignParentBottom:
                        rules[ALIGN_PARENT_BOTTOM] = a.getBoolean(attr, false) ? TRUE : 0;
                        break;
                    case com.android.internal.R.styleable.RelativeLayout_Layout_layout_centerInParent:
                        rules[CENTER_IN_PARENT] = a.getBoolean(attr, false) ? TRUE : 0;
                        break;
                    case com.android.internal.R.styleable.RelativeLayout_Layout_layout_centerHorizontal:
                        rules[CENTER_HORIZONTAL] = a.getBoolean(attr, false) ? TRUE : 0;
                        break;
                    case com.android.internal.R.styleable.RelativeLayout_Layout_layout_centerVertical:
                        rules[CENTER_VERTICAL] = a.getBoolean(attr, false) ? TRUE : 0;
                       break;
                    case com.android.internal.R.styleable.RelativeLayout_Layout_layout_toStartOf:
                        rules[START_OF] = a.getResourceId(attr, 0);
                        break;
                    case com.android.internal.R.styleable.RelativeLayout_Layout_layout_toEndOf:
                        rules[END_OF] = a.getResourceId(attr, 0);
                        break;
                    case com.android.internal.R.styleable.RelativeLayout_Layout_layout_alignStart:
                        rules[ALIGN_START] = a.getResourceId(attr, 0);
                        break;
                    case com.android.internal.R.styleable.RelativeLayout_Layout_layout_alignEnd:
                        rules[ALIGN_END] = a.getResourceId(attr, 0);
                        break;
                    case com.android.internal.R.styleable.RelativeLayout_Layout_layout_alignParentStart:
                        rules[ALIGN_PARENT_START] = a.getBoolean(attr, false) ? TRUE : 0;
                        break;
                    case com.android.internal.R.styleable.RelativeLayout_Layout_layout_alignParentEnd:
                        rules[ALIGN_PARENT_END] = a.getBoolean(attr, false) ? TRUE : 0;
                        break;
                }
            }
            mRulesChanged = true;
            System.arraycopy(rules, LEFT_OF, initialRules, LEFT_OF, VERB_COUNT);

            a.recycle();
        }

        public LayoutParams(int w, int h) {
            super(w, h);
        }

        /**
         * {@inheritDoc}
         */
        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        /**
         * {@inheritDoc}
         */
        public LayoutParams(ViewGroup.MarginLayoutParams source) {
            super(source);
        }

        /**
         * Copy constructor. Clones the width, height, margin values, and rules
         * of the source.
         *
         * @param source The layout params to copy from.
         */
        public LayoutParams(LayoutParams source) {
            super(source);

            this.mIsRtlCompatibilityMode = source.mIsRtlCompatibilityMode;
            this.mRulesChanged = source.mRulesChanged;
            this.alignWithParent = source.alignWithParent;

            System.arraycopy(source.mRules, LEFT_OF, this.mRules, LEFT_OF, VERB_COUNT);
            System.arraycopy(
                    source.mInitialRules, LEFT_OF, this.mInitialRules, LEFT_OF, VERB_COUNT);
        }

        @Override
        public String debug(String output) {
            return output + "ViewGroup.LayoutParams={ width=" + sizeToString(width) +
                    ", height=" + sizeToString(height) + " }";
        }

        /**
         * Adds a layout rule to be interpreted by the RelativeLayout. This
         * method should only be used for constraints that don't refer to another sibling
         * (e.g., CENTER_IN_PARENT) or take a boolean value ({@link RelativeLayout#TRUE}
         * for true or 0 for false). To specify a verb that takes a subject, use
         * {@link #addRule(int, int)} instead.
         *
         * @param verb One of the verbs defined by
         *        {@link android.widget.RelativeLayout RelativeLayout}, such as
         *        ALIGN_WITH_PARENT_LEFT.
         * @see #addRule(int, int)
         * @see #getRule(int)
         */
        public void addRule(int verb) {
            mRules[verb] = TRUE;
            mInitialRules[verb] = TRUE;
            mRulesChanged = true;
        }

        /**
         * Adds a layout rule to be interpreted by the RelativeLayout. Use this for
         * verbs that take a target, such as a sibling (ALIGN_RIGHT) or a boolean
         * value (VISIBLE).
         *
         * @param verb One of the verbs defined by
         *        {@link android.widget.RelativeLayout RelativeLayout}, such as
         *         ALIGN_WITH_PARENT_LEFT.
         * @param anchor The id of another view to use as an anchor,
         *        or a boolean value (represented as {@link RelativeLayout#TRUE}
         *        for true or 0 for false).  For verbs that don't refer to another sibling
         *        (for example, ALIGN_WITH_PARENT_BOTTOM) just use -1.
         * @see #addRule(int)
         * @see #getRule(int)
         */
        public void addRule(int verb, int anchor) {
            mRules[verb] = anchor;
            mInitialRules[verb] = anchor;
            mRulesChanged = true;
        }

        /**
         * Removes a layout rule to be interpreted by the RelativeLayout.
         *
         * @param verb One of the verbs defined by
         *        {@link android.widget.RelativeLayout RelativeLayout}, such as
         *         ALIGN_WITH_PARENT_LEFT.
         * @see #addRule(int)
         * @see #addRule(int, int)
         * @see #getRule(int)
         */
        public void removeRule(int verb) {
            mRules[verb] = 0;
            mInitialRules[verb] = 0;
            mRulesChanged = true;
        }

        /**
         * Returns the layout rule associated with a specific verb.
         *
         * @param verb one of the verbs defined by {@link RelativeLayout}, such
         *             as ALIGN_WITH_PARENT_LEFT
         * @return the id of another view to use as an anchor, a boolean value
         *         (represented as {@link RelativeLayout#TRUE} for true
         *         or 0 for false), or -1 for verbs that don't refer to another
         *         sibling (for example, ALIGN_WITH_PARENT_BOTTOM)
         * @see #addRule(int)
         * @see #addRule(int, int)
         */
        public int getRule(int verb) {
            return mRules[verb];
        }

        private boolean hasRelativeRules() {
            return (mInitialRules[START_OF] != 0 || mInitialRules[END_OF] != 0 ||
                    mInitialRules[ALIGN_START] != 0 || mInitialRules[ALIGN_END] != 0 ||
                    mInitialRules[ALIGN_PARENT_START] != 0 || mInitialRules[ALIGN_PARENT_END] != 0);
        }

        // The way we are resolving rules depends on the layout direction and if we are pre JB MR1
        // or not.
        //
        // If we are pre JB MR1 (said as "RTL compatibility mode"), "left"/"right" rules are having
        // predominance over any "start/end" rules that could have been defined. A special case:
        // if no "left"/"right" rule has been defined and "start"/"end" rules are defined then we
        // resolve those "start"/"end" rules to "left"/"right" respectively.
        //
        // If we are JB MR1+, then "start"/"end" rules are having predominance over "left"/"right"
        // rules. If no "start"/"end" rule is defined then we use "left"/"right" rules.
        //
        // In all cases, the result of the resolution should clear the "start"/"end" rules to leave
        // only the "left"/"right" rules at the end.
        private void resolveRules(int layoutDirection) {
            final boolean isLayoutRtl = (layoutDirection == View.LAYOUT_DIRECTION_RTL);

            // Reset to initial state
            System.arraycopy(mInitialRules, LEFT_OF, mRules, LEFT_OF, VERB_COUNT);

            // Apply rules depending on direction and if we are in RTL compatibility mode
            if (mIsRtlCompatibilityMode) {
                if (mRules[ALIGN_START] != 0) {
                    if (mRules[ALIGN_LEFT] == 0) {
                        // "left" rule is not defined but "start" rule is: use the "start" rule as
                        // the "left" rule
                        mRules[ALIGN_LEFT] = mRules[ALIGN_START];
                    }
                    mRules[ALIGN_START] = 0;
                }

                if (mRules[ALIGN_END] != 0) {
                    if (mRules[ALIGN_RIGHT] == 0) {
                        // "right" rule is not defined but "end" rule is: use the "end" rule as the
                        // "right" rule
                        mRules[ALIGN_RIGHT] = mRules[ALIGN_END];
                    }
                    mRules[ALIGN_END] = 0;
                }

                if (mRules[START_OF] != 0) {
                    if (mRules[LEFT_OF] == 0) {
                        // "left" rule is not defined but "start" rule is: use the "start" rule as
                        // the "left" rule
                        mRules[LEFT_OF] = mRules[START_OF];
                    }
                    mRules[START_OF] = 0;
                }

                if (mRules[END_OF] != 0) {
                    if (mRules[RIGHT_OF] == 0) {
                        // "right" rule is not defined but "end" rule is: use the "end" rule as the
                        // "right" rule
                        mRules[RIGHT_OF] = mRules[END_OF];
                    }
                    mRules[END_OF] = 0;
                }

                if (mRules[ALIGN_PARENT_START] != 0) {
                    if (mRules[ALIGN_PARENT_LEFT] == 0) {
                        // "left" rule is not defined but "start" rule is: use the "start" rule as
                        // the "left" rule
                        mRules[ALIGN_PARENT_LEFT] = mRules[ALIGN_PARENT_START];
                    }
                    mRules[ALIGN_PARENT_START] = 0;
                }

                if (mRules[ALIGN_PARENT_END] != 0) {
                    if (mRules[ALIGN_PARENT_RIGHT] == 0) {
                        // "right" rule is not defined but "end" rule is: use the "end" rule as the
                        // "right" rule
                        mRules[ALIGN_PARENT_RIGHT] = mRules[ALIGN_PARENT_END];
                    }
                    mRules[ALIGN_PARENT_END] = 0;
                }
            } else {
                // JB MR1+ case
                if ((mRules[ALIGN_START] != 0 || mRules[ALIGN_END] != 0) &&
                        (mRules[ALIGN_LEFT] != 0 || mRules[ALIGN_RIGHT] != 0)) {
                    // "start"/"end" rules take precedence over "left"/"right" rules
                    mRules[ALIGN_LEFT] = 0;
                    mRules[ALIGN_RIGHT] = 0;
                }
                if (mRules[ALIGN_START] != 0) {
                    // "start" rule resolved to "left" or "right" depending on the direction
                    mRules[isLayoutRtl ? ALIGN_RIGHT : ALIGN_LEFT] = mRules[ALIGN_START];
                    mRules[ALIGN_START] = 0;
                }
                if (mRules[ALIGN_END] != 0) {
                    // "end" rule resolved to "left" or "right" depending on the direction
                    mRules[isLayoutRtl ? ALIGN_LEFT : ALIGN_RIGHT] = mRules[ALIGN_END];
                    mRules[ALIGN_END] = 0;
                }

                if ((mRules[START_OF] != 0 || mRules[END_OF] != 0) &&
                        (mRules[LEFT_OF] != 0 || mRules[RIGHT_OF] != 0)) {
                    // "start"/"end" rules take precedence over "left"/"right" rules
                    mRules[LEFT_OF] = 0;
                    mRules[RIGHT_OF] = 0;
                }
                if (mRules[START_OF] != 0) {
                    // "start" rule resolved to "left" or "right" depending on the direction
                    mRules[isLayoutRtl ? RIGHT_OF : LEFT_OF] = mRules[START_OF];
                    mRules[START_OF] = 0;
                }
                if (mRules[END_OF] != 0) {
                    // "end" rule resolved to "left" or "right" depending on the direction
                    mRules[isLayoutRtl ? LEFT_OF : RIGHT_OF] = mRules[END_OF];
                    mRules[END_OF] = 0;
                }

                if ((mRules[ALIGN_PARENT_START] != 0 || mRules[ALIGN_PARENT_END] != 0) &&
                        (mRules[ALIGN_PARENT_LEFT] != 0 || mRules[ALIGN_PARENT_RIGHT] != 0)) {
                    // "start"/"end" rules take precedence over "left"/"right" rules
                    mRules[ALIGN_PARENT_LEFT] = 0;
                    mRules[ALIGN_PARENT_RIGHT] = 0;
                }
                if (mRules[ALIGN_PARENT_START] != 0) {
                    // "start" rule resolved to "left" or "right" depending on the direction
                    mRules[isLayoutRtl ? ALIGN_PARENT_RIGHT : ALIGN_PARENT_LEFT] = mRules[ALIGN_PARENT_START];
                    mRules[ALIGN_PARENT_START] = 0;
                }
                if (mRules[ALIGN_PARENT_END] != 0) {
                    // "end" rule resolved to "left" or "right" depending on the direction
                    mRules[isLayoutRtl ? ALIGN_PARENT_LEFT : ALIGN_PARENT_RIGHT] = mRules[ALIGN_PARENT_END];
                    mRules[ALIGN_PARENT_END] = 0;
                }
            }
            mRulesChanged = false;
        }

        /**
         * Retrieves a complete list of all supported rules, where the index is the rule
         * verb, and the element value is the value specified, or "false" if it was never
         * set. If there are relative rules defined (*_START / *_END), they will be resolved
         * depending on the layout direction.
         *
         * @param layoutDirection the direction of the layout.
         *                        Should be either {@link View#LAYOUT_DIRECTION_LTR}
         *                        or {@link View#LAYOUT_DIRECTION_RTL}
         * @return the supported rules
         * @see #addRule(int, int)
         *
         * @hide
         */
        public int[] getRules(int layoutDirection) {
            if (hasRelativeRules() &&
                    (mRulesChanged || layoutDirection != getLayoutDirection())) {
                resolveRules(layoutDirection);
                if (layoutDirection != getLayoutDirection()) {
                    setLayoutDirection(layoutDirection);
                }
            }
            return mRules;
        }

        /**
         * Retrieves a complete list of all supported rules, where the index is the rule
         * verb, and the element value is the value specified, or "false" if it was never
         * set. There will be no resolution of relative rules done.
         *
         * @return the supported rules
         * @see #addRule(int, int)
         */
        public int[] getRules() {
            return mRules;
        }

        @Override
        public void resolveLayoutDirection(int layoutDirection) {
            final boolean isLayoutRtl = isLayoutRtl();
            if (hasRelativeRules() && layoutDirection != getLayoutDirection()) {
                resolveRules(layoutDirection);
            }
            // This will set the layout direction
            super.resolveLayoutDirection(layoutDirection);
        }

        /** @hide */
        @Override
        protected void encodeProperties(@NonNull ViewHierarchyEncoder encoder) {
            super.encodeProperties(encoder);
            encoder.addProperty("layout:alignWithParent", alignWithParent);
        }
    }