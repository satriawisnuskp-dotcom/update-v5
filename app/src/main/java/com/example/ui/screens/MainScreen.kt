package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DebtItem
import com.example.data.model.Transaction
import com.example.ui.dialogs.AddAssetDialog
import com.example.ui.dialogs.AddDebtDialog
import com.example.ui.dialogs.AddTransactionDialog
import com.example.ui.dialogs.AddWalletDialog
import com.example.ui.dialogs.BackupRestoreDialog
import com.example.ui.dialogs.BudgetManagementDialog
import com.example.ui.dialogs.CreateWalletDialog
import com.example.ui.dialogs.GamificationRoadmapDialog
import com.example.ui.dialogs.PayDebtDialog
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.FinanceViewModel

enum class NavigationTab(val title: String, val icon: ImageVector) {
    TRANSAKSI("Transaksi", Icons.Default.ReceiptLong),
    DASBOR("Dasbor", Icons.Default.GridView),
    RINGKASAN("Ringkasan", Icons.Default.ShowChart),
    AKUN("Akun", Icons.Default.Wallet),
    PENGATURAN("Pengaturan", Icons.Default.Settings)
}

@Composable
fun MainScreen(viewModel: FinanceViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }

    // Dialog state controllers
    var showAddTxDialog by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showPlannerScreen by remember { mutableStateOf(false) }
    var showLevelDialog by remember { mutableStateOf(false) }
    var showAddWalletDialog by remember { mutableStateOf(false) }
    var showAddAssetDialog by remember { mutableStateOf(false) }
    var showAddDebtDialog by remember { mutableStateOf(false) }
    var showBackupRestoreDialog by remember { mutableStateOf(false) }
    var activeDebtToPay by remember { mutableStateOf<DebtItem?>(null) }

    val accountSubTab by viewModel.accountSubTab.collectAsState()
    val selectedWalletIdForDetail by viewModel.selectedWalletIdForDetail.collectAsState()
    val selectedAssetIdForDetail by viewModel.selectedAssetIdForDetail.collectAsState()
    val isAnyDetailOpen = selectedWalletIdForDetail != null || selectedAssetIdForDetail != null

    if (showPlannerScreen) {
        PlannerScreen(
            viewModel = viewModel,
            onBack = { showPlannerScreen = false }
        )
    } else {
        // Handle back button: return to Transaksi tab if on other tabs, only when no detail view is open
        BackHandler(enabled = !isAnyDetailOpen && selectedTab != 0) {
            selectedTab = 0
        }

        // Determine FAB visibility:
        // Hide FAB completely when viewing account or asset detail.
        // Otherwise visible on Transaksi (tab 0), and on Akun (tab 3) ONLY for Akun (1), Aset (2), Utang (3).
        val isFabVisible = !isAnyDetailOpen && ((selectedTab == 0) || (selectedTab == 3 && accountSubTab != 0))

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = DarkBackground,
            floatingActionButton = {
                if (isFabVisible) {
                    FloatingActionButton(
                        onClick = {
                            if (selectedTab == 3) {
                                when (accountSubTab) {
                                    1 -> showAddWalletDialog = true
                                    2 -> showAddAssetDialog = true
                                    3 -> showAddDebtDialog = true
                                    else -> showAddWalletDialog = true
                                }
                            } else {
                                showAddTxDialog = true
                            }
                        },
                        containerColor = AccentBlue,
                        contentColor = Color.White,
                        shape = CircleShape,
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 10.dp
                        ),
                        modifier = Modifier
                            .testTag("fab_add_transaction")
                            .size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Tambah",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            },
            floatingActionButtonPosition = FabPosition.End,
            bottomBar = {
                // Sembunyikan toolbar bawah utama (transaksi, dasbor, ringkasan, akun, pengaturan) ketika membuka detil akun
                if (!isAnyDetailOpen) {
                    ModernBottomNavBar(
                        selectedTab = selectedTab,
                        onTabSelected = { tabIndex ->
                            selectedTab = tabIndex
                            if (tabIndex == 3) {
                                viewModel.setAccountSubTab(1) // langsung menuju toolbar akun bukan nilai bersih
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "ScreenTransition"
                ) { targetTab ->
                    when (targetTab) {
                        0 -> TransactionScreen(
                            viewModel = viewModel,
                            onQuickActionClick = { action ->
                                when (action) {
                                    "Anggaran" -> showBudgetDialog = true
                                    "Perencana" -> showPlannerScreen = true
                                    "Acara" -> showBudgetDialog = true
                                    "Aset" -> {
                                        viewModel.setAccountSubTab(2)
                                        selectedTab = 3 // navigate to Akun -> Aset
                                    }
                                    "Tabungan" -> {
                                        viewModel.setAccountSubTab(2)
                                        selectedTab = 3
                                    }
                                    "Level" -> showLevelDialog = true
                                }
                            },
                            onTransactionClick = { /* detail */ }
                        )
                    1 -> DashboardScreen(
                        viewModel = viewModel,
                        onAddUpcomingClick = { showAddTxDialog = true },
                        onLevelClick = { showLevelDialog = true }
                    )
                    2 -> SummaryScreen(
                        viewModel = viewModel
                    )
                    3 -> AccountScreen(
                        viewModel = viewModel,
                        onAddWalletClick = { showAddWalletDialog = true },
                        onAddAssetClick = { showAddAssetDialog = true },
                        onAddDebtClick = { showAddDebtDialog = true },
                        onPayDebtClick = { activeDebtToPay = it }
                    )
                    4 -> SettingsScreen(
                        viewModel = viewModel,
                        onBackupRestoreClick = { showBackupRestoreDialog = true }
                    )
                }
            }
        }
    }

    // Dialogs
    if (showBackupRestoreDialog) {
        BackupRestoreDialog(
            viewModel = viewModel,
            onDismiss = { showBackupRestoreDialog = false }
        )
    }

    if (showAddTxDialog) {
        AddTransactionDialog(
            viewModel = viewModel,
            onDismiss = { showAddTxDialog = false }
        )
    }

    if (showBudgetDialog) {
        BudgetManagementDialog(
            viewModel = viewModel,
            onDismiss = { showBudgetDialog = false }
        )
    }

    if (showLevelDialog) {
        GamificationRoadmapDialog(
            onDismiss = { showLevelDialog = false }
        )
    }

    if (showAddWalletDialog) {
        CreateWalletDialog(
            viewModel = viewModel,
            onDismiss = { showAddWalletDialog = false }
        )
    }

    if (showAddAssetDialog) {
        AddAssetDialog(
            viewModel = viewModel,
            onDismiss = { showAddAssetDialog = false }
        )
    }

    if (showAddDebtDialog) {
        AddDebtDialog(
            viewModel = viewModel,
            onDismiss = { showAddDebtDialog = false }
        )
    }

    activeDebtToPay?.let { debt ->
        PayDebtDialog(
            debt = debt,
            viewModel = viewModel,
            onDismiss = { activeDebtToPay = null }
        )
    }
    }
}

@Composable
fun ModernBottomNavBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = DarkSurface,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 0: Transaksi
            NavTabItem(
                title = "Transaksi",
                icon = Icons.Default.ReceiptLong,
                isSelected = selectedTab == 0,
                tag = "nav_tab_transaksi",
                onClick = { onTabSelected(0) },
                modifier = Modifier.weight(1f)
            )

            // Tab 1: Dasbor
            NavTabItem(
                title = "Dasbor",
                icon = Icons.Default.GridView,
                isSelected = selectedTab == 1,
                tag = "nav_tab_dasbor",
                onClick = { onTabSelected(1) },
                modifier = Modifier.weight(1f)
            )

            // Tab 2: Ringkasan (Riwayat)
            NavTabItem(
                title = "Ringkasan",
                icon = Icons.Default.ShowChart,
                isSelected = selectedTab == 2,
                tag = "nav_tab_ringkasan",
                onClick = { onTabSelected(2) },
                modifier = Modifier.weight(1f)
            )

            // Tab 3: Akun
            NavTabItem(
                title = "Akun",
                icon = Icons.Default.Wallet,
                isSelected = selectedTab == 3,
                tag = "nav_tab_akun",
                onClick = { onTabSelected(3) },
                modifier = Modifier.weight(1f)
            )

            // Tab 4: Pengaturan
            NavTabItem(
                title = "Pengaturan",
                icon = Icons.Default.Settings,
                isSelected = selectedTab == 4,
                tag = "nav_tab_pengaturan",
                onClick = { onTabSelected(4) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun NavTabItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    tag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable { onClick() }
            .padding(horizontal = 2.dp, vertical = 2.dp)
            .testTag(tag)
    ) {
        Box(
            modifier = Modifier
                .size(height = 28.dp, width = 42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (isSelected) Color(0x332563EB) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) AccentBlue else TextMuted,
                modifier = Modifier.size(19.dp)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) AccentBlue else TextMuted,
            maxLines = 1
        )
    }
}
