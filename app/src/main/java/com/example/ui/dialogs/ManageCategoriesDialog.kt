package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsSubway
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Icecream
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Toll
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.MainCategory
import com.example.data.model.SubcategoryItem
import com.example.data.model.TransactionType
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.FinanceViewModel

@Composable
fun ManageCategoriesDialog(
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit
) {
    val categories by viewModel.categories.collectAsState()

    // 0: Pengeluaran, 1: Pendapatan
    var selectedTab by remember { mutableIntStateOf(0) }

    // Dialog states
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showAddSubcategoryDialog by remember { mutableStateOf<MainCategory?>(null) }
    var categoryToEdit by remember { mutableStateOf<MainCategory?>(null) }
    var subcategoryToEdit by remember { mutableStateOf<Pair<MainCategory, SubcategoryItem>?>(null) }
    var categoryToDelete by remember { mutableStateOf<MainCategory?>(null) }
    var subcategoryToDelete by remember { mutableStateOf<Pair<MainCategory, SubcategoryItem>?>(null) }

    val expenseCategories = categories.filter { it.type == TransactionType.EXPENSE }
    val incomeCategories = categories.filter { it.type == TransactionType.INCOME }

    val totalExpenseSubcategories = expenseCategories.sumOf { it.subcategories.size }
    val totalIncomeSubcategories = incomeCategories.sumOf { it.subcategories.size }

    val displayedCategories = if (selectedTab == 0) expenseCategories else incomeCategories

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header Bar matching Screenshot
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_close_categories_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = TextWhite
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "Kategori",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )

                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.size(36.dp))
                }

                // Two Segment Tabs: [ ↓ Pengeluaran (N) ]  [ ↑ Pendapatan (N) ]
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF131720)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Pengeluaran Tab
                        val isExpenseSelected = selectedTab == 0
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = 0 }
                                .testTag("tab_categories_expense"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isExpenseSelected) Color(0xFF1C273C) else Color.Transparent,
                            border = if (isExpenseSelected) BorderStroke(1.dp, Color(0xFF2B3D5B)) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                    contentDescription = null,
                                    tint = if (isExpenseSelected) AccentBlue else TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Pengeluaran ($totalExpenseSubcategories)",
                                    fontSize = 13.sp,
                                    fontWeight = if (isExpenseSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isExpenseSelected) AccentBlue else TextMuted
                                )
                            }
                        }

                        // Pendapatan Tab
                        val isIncomeSelected = selectedTab == 1
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = 1 }
                                .testTag("tab_categories_income"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isIncomeSelected) Color(0xFF162B28) else Color.Transparent,
                            border = if (isIncomeSelected) BorderStroke(1.dp, Color(0xFF21463F)) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = if (isIncomeSelected) IncomeGreen else TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Pendapatan ($totalIncomeSubcategories)",
                                    fontSize = 13.sp,
                                    fontWeight = if (isIncomeSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isIncomeSelected) IncomeGreen else TextMuted
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // List of Categories and Subcategories
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(displayedCategories, key = { it.id }) { cat ->
                        var isExpanded by remember { mutableStateOf(true) }
                        var showMenu by remember { mutableStateOf(false) }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Main Category Item Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF171D27))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { isExpanded = !isExpanded }
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Rounded Category Icon with Amber/Warm container
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(getCategoryColor(cat.name, cat.type)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getCategoryIcon(cat.iconName.ifBlank { cat.name }),
                                            contentDescription = cat.name,
                                            tint = Color(0xFFFBBF24),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = cat.name,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (cat.isVisible) TextWhite else TextMuted
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${cat.subcategories.size} subkategori",
                                            fontSize = 12.sp,
                                            color = TextMuted
                                        )
                                    }

                                    // Action icons matching Screenshot: Eye, Dots Menu, Drag Handle
                                    IconButton(
                                        onClick = { viewModel.toggleCategoryVisibility(cat.id) },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (cat.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = "Visibilitas",
                                            tint = if (cat.isVisible) TextWhite.copy(alpha = 0.8f) else TextMuted,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Box {
                                        IconButton(
                                            onClick = { showMenu = true },
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = "Menu",
                                                tint = TextWhite.copy(alpha = 0.8f),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        DropdownMenu(
                                            expanded = showMenu,
                                            onDismissRequest = { showMenu = false },
                                            modifier = Modifier.background(DarkSurfaceElevated)
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Tambah Subkategori", color = TextWhite) },
                                                leadingIcon = { Icon(Icons.Default.Add, null, tint = AccentBlue) },
                                                onClick = {
                                                    showMenu = false
                                                    showAddSubcategoryDialog = cat
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Ubah Kategori", color = TextWhite) },
                                                leadingIcon = { Icon(Icons.Default.Edit, null, tint = TextMuted) },
                                                onClick = {
                                                    showMenu = false
                                                    categoryToEdit = cat
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Hapus Kategori", color = ExpenseRed) },
                                                leadingIcon = { Icon(Icons.Default.Delete, null, tint = ExpenseRed) },
                                                onClick = {
                                                    showMenu = false
                                                    categoryToDelete = cat
                                                }
                                            )
                                        }
                                    }

                                    Icon(
                                        imageVector = Icons.Default.DragHandle,
                                        contentDescription = "Urutkan",
                                        tint = TextMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Subcategories underneath
                            if (isExpanded) {
                                cat.subcategories.forEach { sub ->
                                    var showSubMenu by remember { mutableStateOf(false) }

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 14.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131822))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Vertical Orange/Amber Indicator Bar `|`
                                            Box(
                                                modifier = Modifier
                                                    .width(3.dp)
                                                    .height(26.dp)
                                                    .clip(RoundedCornerShape(2.dp))
                                                    .background(Color(0xFFD97706))
                                            )

                                            Spacer(modifier = Modifier.width(10.dp))

                                            // Subcategory Icon container
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(Color(0xFF281C10)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = getSubcategoryIcon(sub.iconName.ifBlank { sub.name }),
                                                    contentDescription = sub.name,
                                                    tint = Color(0xFFFBBF24),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Text(
                                                text = sub.name,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = if (sub.isVisible) TextWhite else TextMuted,
                                                modifier = Modifier.weight(1f)
                                            )

                                            // Action icons: Eye, Menu, Drag Handle
                                            IconButton(
                                                onClick = { viewModel.toggleSubcategoryVisibility(cat.id, sub.id) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (sub.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                    contentDescription = "Visibilitas",
                                                    tint = if (sub.isVisible) TextWhite.copy(alpha = 0.8f) else TextMuted,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            Box {
                                                IconButton(
                                                    onClick = { showSubMenu = true },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.MoreVert,
                                                        contentDescription = "Menu",
                                                        tint = TextWhite.copy(alpha = 0.8f),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }

                                                DropdownMenu(
                                                    expanded = showSubMenu,
                                                    onDismissRequest = { showSubMenu = false },
                                                    modifier = Modifier.background(DarkSurfaceElevated)
                                                ) {
                                                    DropdownMenuItem(
                                                        text = { Text("Ubah Nama", color = TextWhite) },
                                                        leadingIcon = { Icon(Icons.Default.Edit, null, tint = TextMuted) },
                                                        onClick = {
                                                            showSubMenu = false
                                                            subcategoryToEdit = Pair(cat, sub)
                                                        }
                                                    )
                                                    DropdownMenuItem(
                                                        text = { Text("Hapus Subkategori", color = ExpenseRed) },
                                                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = ExpenseRed) },
                                                        onClick = {
                                                            showSubMenu = false
                                                            subcategoryToDelete = Pair(cat, sub)
                                                        }
                                                    )
                                                }
                                            }

                                            Icon(
                                                imageVector = Icons.Default.DragHandle,
                                                contentDescription = "Urutkan",
                                                tint = TextMuted,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(90.dp))
                    }
                }
            }

            // Floating Action Button (+) matching Screenshot
            FloatingActionButton(
                onClick = { showAddCategoryDialog = true },
                containerColor = AccentBlue,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 32.dp, end = 20.dp)
                    .size(56.dp)
                    .testTag("fab_add_category")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Kategori",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }

    // Modal 1: Tambah Kategori Baru
    if (showAddCategoryDialog) {
        var newCatName by remember { mutableStateOf("") }
        var newCatType by remember { mutableStateOf(if (selectedTab == 0) TransactionType.EXPENSE else TransactionType.INCOME) }
        var selectedIcon by remember { mutableStateOf("Category") }

        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            containerColor = DarkSurface,
            title = {
                Text("Tambah Kategori Baru", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 17.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newCatName,
                        onValueChange = { newCatName = it },
                        label = { Text("Nama Kategori", color = TextSubtle) },
                        placeholder = { Text("Contoh: Makanan, Transportasi", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedContainerColor = DarkSurfaceElevated,
                            unfocusedContainerColor = DarkSurfaceElevated
                        )
                    )

                    // Type Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurfaceElevated, RoundedCornerShape(10.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { newCatType = TransactionType.EXPENSE },
                            shape = RoundedCornerShape(8.dp),
                            color = if (newCatType == TransactionType.EXPENSE) ExpenseRed else Color.Transparent
                        ) {
                            Text(
                                "Pengeluaran",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { newCatType = TransactionType.INCOME },
                            shape = RoundedCornerShape(8.dp),
                            color = if (newCatType == TransactionType.INCOME) IncomeGreen else Color.Transparent
                        ) {
                            Text(
                                "Pendapatan",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCatName.isNotBlank()) {
                            viewModel.addCategory(
                                MainCategory(
                                    name = newCatName.trim(),
                                    type = newCatType,
                                    iconName = selectedIcon,
                                    subcategories = listOf(
                                        SubcategoryItem(name = "Umum")
                                    )
                                )
                            )
                            showAddCategoryDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Text("Simpan", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("Batal", color = TextMuted)
                }
            }
        )
    }

    // Modal 2: Tambah Subkategori
    showAddSubcategoryDialog?.let { parentCat ->
        var subName by remember { mutableStateOf("") }
        var subIcon by remember { mutableStateOf("Category") }

        AlertDialog(
            onDismissRequest = { showAddSubcategoryDialog = null },
            containerColor = DarkSurface,
            title = {
                Text("Tambah Subkategori untuk ${parentCat.name}", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Kategori Induk: ${parentCat.name}", fontSize = 12.sp, color = TextMuted)
                    OutlinedTextField(
                        value = subName,
                        onValueChange = { subName = it },
                        label = { Text("Nama Subkategori", color = TextSubtle) },
                        placeholder = { Text("Contoh: Kopi, Bensin, Restoran", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedContainerColor = DarkSurfaceElevated,
                            unfocusedContainerColor = DarkSurfaceElevated
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (subName.isNotBlank()) {
                            viewModel.addSubcategory(
                                parentCat.id,
                                SubcategoryItem(name = subName.trim(), iconName = subIcon)
                            )
                            showAddSubcategoryDialog = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Text("Tambah", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSubcategoryDialog = null }) {
                    Text("Batal", color = TextMuted)
                }
            }
        )
    }

    // Modal 3: Ubah Nama Kategori
    categoryToEdit?.let { cat ->
        var editName by remember { mutableStateOf(cat.name) }
        AlertDialog(
            onDismissRequest = { categoryToEdit = null },
            containerColor = DarkSurface,
            title = { Text("Ubah Nama Kategori", fontWeight = FontWeight.Bold, color = TextWhite) },
            text = {
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text("Nama Kategori", color = TextSubtle) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedContainerColor = DarkSurfaceElevated,
                        unfocusedContainerColor = DarkSurfaceElevated
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editName.isNotBlank()) {
                            viewModel.updateCategory(cat.copy(name = editName.trim()))
                            categoryToEdit = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Text("Simpan", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToEdit = null }) {
                    Text("Batal", color = TextMuted)
                }
            }
        )
    }

    // Modal 4: Ubah Subkategori
    subcategoryToEdit?.let { (parentCat, sub) ->
        var editSubName by remember { mutableStateOf(sub.name) }
        AlertDialog(
            onDismissRequest = { subcategoryToEdit = null },
            containerColor = DarkSurface,
            title = { Text("Ubah Subkategori", fontWeight = FontWeight.Bold, color = TextWhite) },
            text = {
                OutlinedTextField(
                    value = editSubName,
                    onValueChange = { editSubName = it },
                    label = { Text("Nama Subkategori", color = TextSubtle) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedContainerColor = DarkSurfaceElevated,
                        unfocusedContainerColor = DarkSurfaceElevated
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editSubName.isNotBlank()) {
                            viewModel.updateSubcategory(parentCat.id, sub.copy(name = editSubName.trim()))
                            subcategoryToEdit = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Text("Simpan", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { subcategoryToEdit = null }) {
                    Text("Batal", color = TextMuted)
                }
            }
        )
    }

    // Modal 5: Konfirmasi Hapus Kategori
    categoryToDelete?.let { cat ->
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            containerColor = DarkSurface,
            title = { Text("Hapus Kategori?", fontWeight = FontWeight.Bold, color = TextWhite) },
            text = {
                Text(
                    "Apakah Anda yakin ingin menghapus kategori \"${cat.name}\" beserta seluruh ${cat.subcategories.size} subkategorinya?",
                    color = TextMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCategory(cat.id)
                        categoryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("Hapus", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) {
                    Text("Batal", color = TextMuted)
                }
            }
        )
    }

    // Modal 6: Konfirmasi Hapus Subkategori
    subcategoryToDelete?.let { (parentCat, sub) ->
        AlertDialog(
            onDismissRequest = { subcategoryToDelete = null },
            containerColor = DarkSurface,
            title = { Text("Hapus Subkategori?", fontWeight = FontWeight.Bold, color = TextWhite) },
            text = {
                Text("Hapus subkategori \"${sub.name}\" dari kategori ${parentCat.name}?", color = TextMuted)
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSubcategory(parentCat.id, sub.id)
                        subcategoryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("Hapus", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { subcategoryToDelete = null }) {
                    Text("Batal", color = TextMuted)
                }
            }
        )
    }
}

fun getCategoryColor(name: String, type: TransactionType): Color {
    return when {
        type == TransactionType.INCOME -> Color(0xFF1B382F)
        name.contains("Food", ignoreCase = true) || name.contains("Makan", ignoreCase = true) -> Color(0xFF3B2510)
        name.contains("Transport", ignoreCase = true) -> Color(0xFF183028)
        name.contains("Shop", ignoreCase = true) || name.contains("Belanja", ignoreCase = true) -> Color(0xFF331D38)
        name.contains("Bill", ignoreCase = true) || name.contains("Tagihan", ignoreCase = true) -> Color(0xFF3B1E1E)
        name.contains("Entertain", ignoreCase = true) || name.contains("Hiburan", ignoreCase = true) -> Color(0xFF1B2B3E)
        name.contains("Sehat", ignoreCase = true) || name.contains("Health", ignoreCase = true) -> Color(0xFF1D3528)
        else -> Color(0xFF26201B)
    }
}

fun getSubcategoryIcon(name: String): ImageVector {
    return when {
        name.contains("Resto", ignoreCase = true) -> Icons.Default.Restaurant
        name.contains("Cafe", ignoreCase = true) || name.contains("Coffee", ignoreCase = true) || name.contains("Kopi", ignoreCase = true) -> Icons.Default.LocalCafe
        name.contains("Delivery", ignoreCase = true) -> Icons.Default.TwoWheeler
        name.contains("Snack", ignoreCase = true) || name.contains("Jajan", ignoreCase = true) -> Icons.Default.Icecream
        name.contains("Fuel", ignoreCase = true) || name.contains("Bensin", ignoreCase = true) -> Icons.Default.LocalGasStation
        name.contains("Toll", ignoreCase = true) || name.contains("Tol", ignoreCase = true) -> Icons.Default.Toll
        name.contains("Taxi", ignoreCase = true) || name.contains("Mobil", ignoreCase = true) -> Icons.Default.LocalTaxi
        name.contains("Transit", ignoreCase = true) || name.contains("Kereta", ignoreCase = true) -> Icons.Default.DirectionsSubway
        name.contains("Pakaian", ignoreCase = true) || name.contains("Baju", ignoreCase = true) -> Icons.Default.Checkroom
        name.contains("Elektronik", ignoreCase = true) -> Icons.Default.Devices
        name.contains("Rumah", ignoreCase = true) -> Icons.Default.Home
        name.contains("Listrik", ignoreCase = true) -> Icons.Default.Bolt
        name.contains("Air", ignoreCase = true) -> Icons.Default.WaterDrop
        name.contains("Wifi", ignoreCase = true) || name.contains("Internet", ignoreCase = true) -> Icons.Default.Wifi
        name.contains("Pulsa", ignoreCase = true) -> Icons.Default.PhoneAndroid
        name.contains("Bioskop", ignoreCase = true) || name.contains("Movie", ignoreCase = true) -> Icons.Default.Movie
        name.contains("Game", ignoreCase = true) -> Icons.Default.SportsEsports
        name.contains("Langganan", ignoreCase = true) -> Icons.Default.Subscriptions
        name.contains("Liburan", ignoreCase = true) -> Icons.Default.Flight
        name.contains("Dokter", ignoreCase = true) -> Icons.Default.LocalHospital
        name.contains("Obat", ignoreCase = true) -> Icons.Default.Medication
        name.contains("Asuransi", ignoreCase = true) -> Icons.Default.Shield
        name.contains("Buku", ignoreCase = true) -> Icons.Default.MenuBook
        name.contains("SPP", ignoreCase = true) || name.contains("Kursus", ignoreCase = true) -> Icons.Default.School
        name.contains("Groceries", ignoreCase = true) || name.contains("Pasar", ignoreCase = true) -> Icons.Default.Storefront
        else -> Icons.Default.Category
    }
}
