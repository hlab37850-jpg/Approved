package info.aalmoghalis.approved

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BrandBlue = Color(0xFF0A6EA9)
private val BrandBlueDark = Color(0xFF1B5088)
private val PageBackground = Color(0xFFF4F6F8)
private val Ink = Color(0xFF243746)
private val Muted = Color(0xFF71808D)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = AccountantDatabase(applicationContext)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize(), color = PageBackground) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        AccountantHome(database)
                    }
                }
            }
        }
    }
}

private data class Module(val title: String, val icon: ImageVector, val subtitle: String)
private val modules = listOf(
    Module("الأصناف والمخزون", Icons.Filled.Inventory2, "إدارة الأصناف والكميات"),
    Module("المبيعات", Icons.Filled.PointOfSale, "فواتير وحركة المبيعات"),
    Module("المشتريات", Icons.Filled.ShoppingCart, "فواتير وحركة المشتريات"),
    Module("العملاء", Icons.Filled.Groups, "حسابات وأرصدة العملاء"),
    Module("المجموعات", Icons.Filled.Category, "تصنيف الأصناف والحسابات"),
    Module("الحسابات", Icons.Filled.AccountBalance, "دليل الحسابات والأرصدة"),
    Module("التقارير", Icons.Filled.Assessment, "تقارير النشاط المالي"),
    Module("الإعدادات", Icons.Filled.Settings, "إعدادات التطبيق")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountantHome(database: AccountantDatabase) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedModule by remember { mutableIntStateOf(-1) }
    val tabs = listOf("الرئيسية", "الأصناف", "الحسابات", "التقارير", "المزيد")
    val tabIcons = listOf(Icons.Filled.Home, Icons.Filled.Inventory2, Icons.Filled.Groups, Icons.Filled.Assessment, Icons.Filled.MoreHoriz)

    Scaffold(
        containerColor = PageBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("المحاسب برو", fontWeight = FontWeight.Bold, fontSize = 19.sp, color = Color.White)
                        Text(if (selectedModule >= 0) modules[selectedModule].title else "نظام إدارة الحسابات والمخزون", fontSize = 11.sp, color = Color(0xFFD9EAF5))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { selectedModule = if (selectedModule == -1) 7 else -1 }) {
                        Icon(Icons.Filled.Menu, contentDescription = "القائمة", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandBlueDark)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                tabs.forEachIndexed { index, label ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index; selectedModule = -1 },
                        icon = { Icon(tabIcons[index], contentDescription = label) },
                        label = { Text(label, fontSize = 10.sp) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(2.dp))
            if (selectedModule >= 0) {
                ModuleDetail(modules[selectedModule], database, onBack = { selectedModule = -1 })
            } else {
                Column {
                    Text(when (selectedTab) {
                        1 -> "الأصناف والمخزون"
                        2 -> "الحسابات"
                        3 -> "التقارير"
                        4 -> "المزيد من الخيارات"
                        else -> "لوحة التحكم"
                    }, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Ink)
                    Text("اختر القسم لعرض تفاصيله", fontSize = 12.sp, color = Muted)
                }
                if (selectedTab == 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandBlue)
                    ) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("ملخص الحسابات", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Text("تُحفظ بياناتك محلياً على الجهاز", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                            HorizontalDivider(color = Color.White.copy(alpha = 0.28f))
                            Text("أضف العملاء والأصناف من الأقسام المخصصة. لا توجد بيانات تجريبية.", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
                val visibleModules = when (selectedTab) {
                    1 -> modules.filter { it.title.contains("الأصناف") || it.title.contains("المجموعات") }
                    2 -> modules.filter { it.title == "الحسابات" || it.title == "العملاء" }
                    3 -> modules.filter { it.title == "التقارير" }
                    4 -> modules.filter { it.title == "الإعدادات" || it.title == "المبيعات" || it.title == "المشتريات" }
                    else -> modules
                }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(bottom = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    gridItems(visibleModules) { module ->
                        ModuleCard(module) { selectedModule = modules.indexOf(module) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModuleCard(module: Module, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(124.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.fillMaxSize().padding(13.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Box(
                modifier = Modifier.size(40.dp).background(Color(0xFFE6F2F9), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) { Icon(module.icon, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(23.dp)) }
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(module.title, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(module.subtitle, color = Muted, fontSize = 10.sp, maxLines = 1)
            }
        }
    }
}

@Composable
private fun ModuleDetail(module: Module, database: AccountantDatabase, onBack: () -> Unit) {
    var refresh by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    var companyName by remember { mutableStateOf(database.companyName()) }
    val isCustomers = module.title == "العملاء"
    val isItems = module.title == "الأصناف والمخزون"
    val records = remember(module.title, refresh) {
        when {
            isCustomers -> database.listCustomers()
            isItems -> database.listItems()
            else -> emptyList()
        }
    }

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("رجوع إلى الأقسام", color = BrandBlue, fontSize = 13.sp,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 5.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(14.dp)) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(module.icon, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(32.dp))
                Column(Modifier.weight(1f)) {
                    Text(module.title, fontWeight = FontWeight.Bold, color = Ink, fontSize = 18.sp)
                    Text(module.subtitle, color = Muted, fontSize = 12.sp)
                }
                if (isCustomers || isItems) {
                    FilledTonalButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Text("إضافة")
                    }
                }
            }
        }

        when {
            isCustomers || isItems -> {
                if (records.isEmpty()) {
                    EmptyState(if (isCustomers) "لا يوجد عملاء بعد" else "لا توجد أصناف بعد",
                        "اضغط «إضافة» لتسجيل أول سجل. البيانات تحفظ في قاعدة SQLite على جهازك.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                        items(records, key = { it.id }) { record ->
                            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
                                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(record.name, color = Ink, fontWeight = FontWeight.SemiBold)
                                        Text(if (isCustomers) record.detail else "رمز: ${record.detail.ifBlank { "—" }}  •  الكمية: ${record.quantity}",
                                            color = Muted, fontSize = 12.sp)
                                    }
                                    IconButton(onClick = {
                                        database.deleteRecord(if (isCustomers) "customers" else "items", record.id)
                                        refresh++
                                    }) { Icon(Icons.Filled.DeleteOutline, contentDescription = "حذف", tint = Color(0xFFB3261E)) }
                                }
                            }
                        }
                    }
                }
            }
            module.title == "الإعدادات" -> {
                var editName by remember { mutableStateOf(companyName) }
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(14.dp)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("بيانات المنشأة", fontWeight = FontWeight.Bold, color = Ink)
                        OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("اسم المنشأة") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        Button(onClick = {
                            database.saveCompanyName(editName)
                            companyName = database.companyName()
                        }, modifier = Modifier.fillMaxWidth()) { Text("حفظ البيانات") }
                        if (companyName.isNotBlank()) Text("الاسم المحفوظ: $companyName", color = Muted, fontSize = 12.sp)
                    }
                }
                Text("قاعدة البيانات: SQLite محلية • لا يتم إنشاء بيانات افتراضية", color = Muted, fontSize = 12.sp)
            }
            else -> EmptyState("القسم قيد التطوير", "لم تُنفذ وظيفة هذا القسم بعد. لن نعرض بيانات وهمية أو نعتبره مكتملًا.")
        }
    }

    if (showAddDialog) {
        RecordDialog(
            title = if (isCustomers) "إضافة عميل" else "إضافة صنف",
            customer = isCustomers,
            onDismiss = { showAddDialog = false },
            onSave = { name, detail, quantity ->
                if (name.isNotBlank()) {
                    if (isCustomers) database.addCustomer(name, detail)
                    else database.addItem(name, detail, quantity)
                    refresh++
                    showAddDialog = false
                }
            }
        )
    }
}

@Composable
private fun EmptyState(title: String, description: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Filled.Inventory2, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(32.dp))
            Text(title, fontWeight = FontWeight.SemiBold, color = Ink)
            Text(description, color = Muted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun RecordDialog(
    title: String,
    customer: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("0") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(if (customer) "اسم العميل *" else "اسم الصنف *") }, singleLine = true)
                OutlinedTextField(value = detail, onValueChange = { detail = it }, label = { Text(if (customer) "رقم الهاتف" else "رمز الصنف / SKU") }, singleLine = true)
                if (!customer) OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("الكمية") }, singleLine = true)
            }
        },
        confirmButton = { TextButton(onClick = { onSave(name, detail, quantity.toDoubleOrNull() ?: 0.0) }, enabled = name.isNotBlank()) { Text("حفظ") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
