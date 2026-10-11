package com.example.softbackgrounds;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FocusGridLayoutManager extends RecyclerView.LayoutManager {

    // ---------------------------------------------------------
    // Configuration
    // ---------------------------------------------------------

    private final Context context;

    // Adaptive grid
    private static final int MIN_ITEM_WIDTH_DP = 150;

    // Row dimensions
    private static final int MAX_ROW_HEIGHT_DP = 240;
    private static final int MIN_ROW_HEIGHT_DP = 90;

    // Spacing
    private static final int HORIZONTAL_GAP_DP = 12;
    private static final int VERTICAL_GAP_DP = 14;

    /*
     * This determines how far the focus travels for one
     * complete row of scrolling.
     *
     * It is deliberately independent from the current
     * dynamic row height.
     */
    private static final int FOCUS_STEP_DP = 190;

    // ---------------------------------------------------------
    // Runtime values
    // ---------------------------------------------------------

    private int columnCount = 2;

    /*
     * IMPORTANT:
     *
     * This value never changes because of vertical scrolling.
     */
    private int itemWidth;

    private int horizontalGap;
    private int verticalGap;

    private int maxRowHeight;
    private int minRowHeight;

    private int focusStep;

    /*
     * Actual RecyclerView scroll position.
     */
    private int scrollOffset = 0;

    /*
     * Continuous focus coordinate.
     *
     * 0.0 = row 0 focused
     * 0.5 = halfway between row 0 and row 1
     * 1.0 = row 1 focused
     * 1.5 = halfway between row 1 and row 2
     */
    private float focusPosition = 0f;

    /*
     * We use a stable virtual content coordinate for the
     * focus. This prevents row height and focus from
     * depending on each other.
     */
    private float focusOrigin = 0f;

    private int estimatedContentHeight = 0;

    // ---------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------

    public FocusGridLayoutManager(Context context) {

        this.context = context;

        horizontalGap = dp(HORIZONTAL_GAP_DP);
        verticalGap = dp(VERTICAL_GAP_DP);

        maxRowHeight = dp(MAX_ROW_HEIGHT_DP);
        minRowHeight = dp(MIN_ROW_HEIGHT_DP);

        focusStep = dp(FOCUS_STEP_DP);
    }

    // =========================================================
    // RecyclerView required methods
    // =========================================================

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {

        return new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
        );
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    // =========================================================
    // Layout
    // =========================================================

    @Override
    public void onLayoutChildren(
            @NonNull RecyclerView.Recycler recycler,
            @NonNull RecyclerView.State state
    ) {

        if (state.isPreLayout()) {
            return;
        }

        if (getItemCount() == 0) {

            removeAndRecycleAllViews(recycler);

            scrollOffset = 0;
            focusPosition = 0;

            return;
        }

        calculateGrid();

        clampScrollOffset();

        updateFocusPosition();

        calculateEstimatedContentHeight();

        fill(recycler);
    }

    // =========================================================
    // Adaptive column calculation
    // =========================================================

    private void calculateGrid() {

        int availableWidth =
                getWidth()
                        - getPaddingLeft()
                        - getPaddingRight();

        int minimumItemWidth =
                dp(MIN_ITEM_WIDTH_DP);

        int columns =
                availableWidth / minimumItemWidth;

        /*
         * Minimum two columns.
         */
        columns = Math.max(2, columns);

        /*
         * Maximum four columns.
         */
        columns = Math.min(4, columns);

        columnCount = columns;

        /*
         * IMPORTANT:
         *
         * This width is calculated from the screen width.
         *
         * It does NOT depend on scroll position.
         */
        itemWidth =
                (
                        availableWidth
                                - (
                                columnCount - 1
                        ) * horizontalGap
                ) / columnCount;
    }

    // =========================================================
    // Row count
    // =========================================================

    private int getRowCount() {

        return (int) Math.ceil(
                (double) getItemCount()
                        / columnCount
        );
    }

    // =========================================================
    // Focus
    // =========================================================

    private void updateFocusPosition() {

        /*
         * Focus is based ONLY on scrollOffset.
         *
         * Therefore:
         *
         * scroll → focus
         *
         * and NOT:
         *
         * row height → focus → row height
         */
        focusPosition =
                (float) scrollOffset
                        / focusStep;

        int lastRow =
                Math.max(
                        0,
                        getRowCount() - 1
                );

        focusPosition =
                Math.max(
                        0f,
                        Math.min(
                                focusPosition,
                                lastRow
                        )
                );
    }

    // =========================================================
    // Dynamic row height
    // =========================================================

    private int getRowHeight(int row) {

        float distance =
                Math.abs(
                        row - focusPosition
                );

        /*
         * Two row distances represent the complete
         * transition from maximum to minimum.
         *
         * distance 0 → max
         * distance 1 → still fairly large
         * distance 2 → min
         */
        float normalized =
                Math.min(
                        distance / 2f,
                        1f
                );

        /*
         * SmoothStep:
         *
         * 3t² - 2t³
         *
         * This makes the transition feel much more
         * natural than linear interpolation.
         */
        float smooth =
                normalized
                        * normalized
                        * (
                        3f
                                - 2f
                                * normalized
                );

        float height =
                maxRowHeight
                        - (
                        maxRowHeight
                                - minRowHeight
                ) * smooth;

        return Math.round(height);
    }

    // =========================================================
    // Row position
    // =========================================================

    private int getRowTop(int targetRow) {

        int y = getPaddingTop();

        for (int row = 0;
             row < targetRow;
             row++) {

            y +=
                    getRowHeight(row)
                            + verticalGap;
        }

        return y - scrollOffset;
    }

    // =========================================================
    // Fill
    // =========================================================

    private void fill(
            RecyclerView.Recycler recycler
    ) {

        detachAndScrapAttachedViews(recycler);

        if (getItemCount() == 0) {
            return;
        }

        int rowCount =
                getRowCount();

        /*
         * Only walk through rows that could potentially
         * appear on screen.
         *
         * For a small catalogue this is already cheap.
         * RecyclerView will still recycle the views.
         */
        for (int row = 0;
             row < rowCount;
             row++) {

            int rowHeight =
                    getRowHeight(row);

            int rowTop =
                    getRowTop(row);

            int rowBottom =
                    rowTop + rowHeight;

            /*
             * Completely above the viewport.
             */
            if (rowBottom < 0) {
                continue;
            }

            /*
             * Completely below the viewport.
             */
            if (rowTop > getHeight()) {
                break;
            }

            layoutRow(
                    recycler,
                    row,
                    rowTop,
                    rowHeight
            );
        }
    }

    // =========================================================
    // Layout one row
    // =========================================================

    private void layoutRow(
            RecyclerView.Recycler recycler,
            int row,
            int rowTop,
            int rowHeight
    ) {

        int firstPosition =
                row * columnCount;

        for (int column = 0;
             column < columnCount;
             column++) {

            int position =
                    firstPosition + column;

            if (position >= getItemCount()) {
                break;
            }

            View child =
                    recycler.getViewForPosition(
                            position
                    );

            addView(child);

            /*
             * CONSTANT ITEM WIDTH
             */
            int left =
                    getPaddingLeft()
                            + column
                            * (
                            itemWidth
                                    + horizontalGap
                    );

            int right =
                    left + itemWidth;

            /*
             * ONLY HEIGHT CHANGES
             */
            int top =
                    rowTop;

            int bottom =
                    top + rowHeight;

            child.measure(
                    View.MeasureSpec.makeMeasureSpec(
                            itemWidth,
                            View.MeasureSpec.EXACTLY
                    ),
                    View.MeasureSpec.makeMeasureSpec(
                            rowHeight,
                            View.MeasureSpec.EXACTLY
                    )
            );

            layoutDecorated(
                    child,
                    left,
                    top,
                    right,
                    bottom
            );
        }
    }

    // =========================================================
    // Vertical scrolling
    // =========================================================

    @Override
    public int scrollVerticallyBy(
            int dy,
            @NonNull RecyclerView.Recycler recycler,
            @NonNull RecyclerView.State state
    ) {

        if (getChildCount() == 0) {
            return 0;
        }

        int oldOffset =
                scrollOffset;

        scrollOffset += dy;

        clampScrollOffset();

        int consumed =
                scrollOffset - oldOffset;

        if (consumed != 0) {

            updateFocusPosition();

            /*
             * Recalculate all visible row dimensions.
             */
            fill(recycler);
        }

        return consumed;
    }

    // =========================================================
    // Scroll bounds
    // =========================================================

    private void clampScrollOffset() {

        int maxScroll =
                getMaxScroll();

        scrollOffset =
                Math.max(
                        0,
                        Math.min(
                                scrollOffset,
                                maxScroll
                        )
                );
    }

    // =========================================================
    // Content height
    // =========================================================

    private void calculateEstimatedContentHeight() {

        int rowCount =
                getRowCount();

        /*
         * We use a stable virtual row size for the
         * RecyclerView's scroll range.
         *
         * Actual visual row heights are still dynamic.
         */
        estimatedContentHeight =
                getPaddingTop()
                        + getPaddingBottom()
                        + rowCount
                        * (
                        focusStep
                                + verticalGap
                )
                        + dp(100);
    }

    private int getMaxScroll() {

        return Math.max(
                0,
                estimatedContentHeight
                        - getHeight()
        );
    }

    // =========================================================
    // Programmatic scrolling
    // =========================================================

    @Override
    public void scrollToPosition(
            int position
    ) {

        if (position < 0 ||
                position >= getItemCount()) {
            return;
        }

        int row =
                position / columnCount;

        scrollOffset =
                row * focusStep;

        clampScrollOffset();

        requestLayout();
    }

    // =========================================================
    // Adapter changes
    // =========================================================

    @Override
    public void onItemsChanged(
            @NonNull RecyclerView recyclerView
    ) {

        super.onItemsChanged(recyclerView);

        calculateGrid();

        calculateEstimatedContentHeight();

        clampScrollOffset();

        recyclerView.requestLayout();
    }

    // =========================================================
    // Density helper
    // =========================================================

    private int dp(int value) {

        DisplayMetrics metrics =
                context
                        .getResources()
                        .getDisplayMetrics();

        return Math.round(
                value * metrics.density
        );
    }
}