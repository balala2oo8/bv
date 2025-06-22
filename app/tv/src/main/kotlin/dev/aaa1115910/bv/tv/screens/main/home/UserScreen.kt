package dev.aaa1115910.bv.tv.screens.main.home

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import dev.aaa1115910.bv.tv.component.TopNav
import dev.aaa1115910.bv.tv.component.TopNavItem
import dev.aaa1115910.bv.tv.screens.main.DrawerItem
import dev.aaa1115910.bv.tv.screens.main.drawerItemFocusRequesters
import dev.aaa1115910.bv.tv.screens.user.FavoriteScreen
import dev.aaa1115910.bv.tv.screens.user.FollowingSeasonScreen
import dev.aaa1115910.bv.tv.screens.user.HistoryScreen
import dev.aaa1115910.bv.tv.screens.user.ToViewScreen
import dev.aaa1115910.bv.util.requestFocus
import dev.aaa1115910.bv.viewmodel.user.FavoriteViewModel
import dev.aaa1115910.bv.viewmodel.user.FollowingSeasonViewModel
import dev.aaa1115910.bv.viewmodel.user.HistoryViewModel
import dev.aaa1115910.bv.viewmodel.user.ToViewViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

enum class UserTabItem(val displayName: String) : TopNavItem {
    History("历史"),
    Favorite("收藏"),
    ToView("稍后再看"),
    FollowingSeason("追番");

    override fun getDisplayName(context: Context) = displayName
}

@Composable
fun UserScreen(
    modifier: Modifier = Modifier,
    historyViewModel: HistoryViewModel = koinViewModel(),
    favoriteViewModel: FavoriteViewModel = koinViewModel(),
    toViewViewModel: ToViewViewModel = koinViewModel(),
    followingSeasonViewModel: FollowingSeasonViewModel = koinViewModel(),
    contentFocusRequester: FocusRequester = remember { FocusRequester() },
    topNavFocusRequester: FocusRequester = remember { FocusRequester() }
) {
    val scope = rememberCoroutineScope()

    var focusOnContent by remember { mutableStateOf(false) }
    var topNavHasFocus by remember { mutableStateOf(false) }
    val items = UserTabItem.entries.toList()
    val initialSelectedTab = items.first()
    var selectedTab by remember { mutableStateOf(initialSelectedTab) }

    val historyState = rememberLazyGridState()
    // val favoriteState = rememberLazyGridState()
    val toViewState = rememberLazyGridState()
    val followingSeasonState = rememberLazyGridState()

    val innerNavFocusRequester = remember { FocusRequester() }

    BackHandler(focusOnContent || topNavHasFocus) {
        if (topNavHasFocus) {
            topNavFocusRequester.requestFocus(scope)
            return@BackHandler
        }
        innerNavFocusRequester.requestFocus(scope)
        // scroll to top
        scope.launch(Dispatchers.Main) {
            when (selectedTab) {
                UserTabItem.History -> historyState.animateScrollToItem(0)
                UserTabItem.Favorite -> {}//favoriteState.animateScrollToItem(0)
                UserTabItem.ToView -> toViewState.animateScrollToItem(0)
                UserTabItem.FollowingSeason -> followingSeasonState.animateScrollToItem(0)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopNav(
                modifier = Modifier
                    .focusRequester(innerNavFocusRequester)
                    .padding(end = 60.dp)
                    .onFocusChanged { topNavHasFocus = it.hasFocus },
                items = items,
                isLargePadding = false,
                initialSelectedItem = initialSelectedTab,
                onSelectedChanged = { nav ->
                    selectedTab = nav as UserTabItem
                },
                onClick = { nav ->
                    when (nav) {
                        UserTabItem.History -> {
                            historyViewModel.clearData()
                            historyViewModel.update()
                        }

                        UserTabItem.Favorite -> {
                            favoriteViewModel.clearData()
                            favoriteViewModel.updateFoldersInfo(true)
                        }

                        UserTabItem.ToView -> {
                            toViewViewModel.clearData()
                            toViewViewModel.update()
                        }

                        UserTabItem.FollowingSeason -> {
                            followingSeasonViewModel.clearData()
                            followingSeasonViewModel.loadMore()
                        }
                    }
                },
                onLeftKeyEvent = {
                    // 顶部栏最左侧按左键时，跳转到左侧导航栏
                    drawerItemFocusRequesters[DrawerItem.Home]?.requestFocus(scope)
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .focusRequester(contentFocusRequester)
                .onFocusChanged { focusOnContent = it.hasFocus }
        ) {
            AnimatedContent(
                targetState = selectedTab,
                label = "user animated content",
                transitionSpec = {
                    val coefficient = 10
                    if (targetState.ordinal < initialState.ordinal) {
                        fadeIn() + slideInHorizontally { -it / coefficient } togetherWith
                                fadeOut() + slideOutHorizontally { it / coefficient }
                    } else {
                        fadeIn() + slideInHorizontally { it / coefficient } togetherWith
                                fadeOut() + slideOutHorizontally { -it / coefficient }
                    }
                }
            ) { screen ->
                when (screen) {
                    UserTabItem.History -> HistoryScreen(
                        historyViewModel = historyViewModel,
                        lazyGridState = historyState,
                        onlyShowContent = true
                    )

                    UserTabItem.Favorite -> FavoriteScreen(
                        favoriteViewModel = favoriteViewModel,
                        onlyShowContent = true
                    )

                    UserTabItem.ToView -> ToViewScreen(
                        ToViewViewModel = toViewViewModel,
                        lazyGridState = toViewState,
                        onlyShowContent = true
                    )

                    UserTabItem.FollowingSeason -> FollowingSeasonScreen(
                        followingSeasonViewModel = followingSeasonViewModel,
                        lazyGridState = followingSeasonState,
                        onlyShowContent = true
                    )
                }
            }
        }
    }
}
