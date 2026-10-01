package io.github.halilozel1903.wearkit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.pager.HorizontalPager
import androidx.wear.compose.foundation.pager.PagerState
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material3.HorizontalPageIndicator
import androidx.wear.compose.material3.HorizontalPagerScaffold
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.SwipeToDismissBox

/**
 * Horizontally swiped pages with a page indicator, where swiping right on the first page
 * dismisses the whole stack (the Wear OS back gesture). Later pages swipe back to the previous
 * page as usual; only the first page lets the dismiss gesture through.
 *
 * ```kotlin
 * SwipeDismissPages(pageCount = 3, onDismiss = { navigateBack() }) { page ->
 *     when (page) {
 *         0 -> HeartRatePage()
 *         1 -> StepsPage()
 *         else -> SummaryPage()
 *     }
 * }
 * ```
 *
 * @param background drawn behind the pages while they are swiped away, usually the previous screen.
 * @param dismissEnabled turns the dismiss gesture off, for example while a workout is running.
 */
@Composable
public fun SwipeDismissPages(
    pageCount: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    pagerState: PagerState = rememberPagerState(pageCount = { pageCount }),
    dismissEnabled: Boolean = true,
    showPageIndicator: Boolean = true,
    background: @Composable () -> Unit = { DefaultDismissBackground() },
    page: @Composable (index: Int) -> Unit,
) {
    require(pageCount >= 1) { "pageCount must be at least 1" }
    val indicator: (@Composable BoxScope.() -> Unit)? = if (showPageIndicator) {
        { HorizontalPageIndicator(pagerState) }
    } else {
        null
    }
    SwipeToDismissBox(
        onDismissed = onDismiss,
        modifier = modifier,
        userSwipeEnabled = dismissEnabled && pagerState.currentPage == 0,
    ) { isBackground ->
        if (isBackground) {
            background()
        } else {
            HorizontalPagerScaffold(
                pagerState = pagerState,
                pageIndicator = indicator,
            ) {
                HorizontalPager(state = pagerState) { index ->
                    page(index)
                }
            }
        }
    }
}

@Composable
private fun DefaultDismissBackground() {
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    )
}
