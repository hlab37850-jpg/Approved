package info.aalmoghalis.approved

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = PageBackground
                ) {
                    androidx.compose.runtime.CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        AccountantHome()
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
private fun AccountantHome() {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedModule by remember { mutableIntStateOf(-1) }
    val tabs = listOf("الرئيسية", "الأصناف", "الحسابات", "التقارير", "المزيد")
    val tabIcons = listOf(Icons.Filled.AccountBalance, Icons.Filled.Inventory2, Icons.Filled.Groups, Icons.Filled.Assessment, Icons.Filled.MoreHoriz)

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
                ModuleDetail(modules[selectedModule], onBack = { selectedModule = -1 })
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                }
                if (selectedTab == 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandBlue)
                    ) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("ملخص الحسابات", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Text("لا توجد بيانات مسجلة بعد", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                            Divider(color = Color.White.copy(alpha = 0.28f))
                            Text("ابدأ بإعداد بيانات المنشأة وإضافة السجلات الفعلية.", color = Color.White, fontSize = 12.sp)
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
                    items(visibleModules) { module ->
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
        Column(
            modifier = Modifier.fillMaxSize().padding(13.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(Color(0xFFE6F2F9), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(module.icon, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(23.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(module.title, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(module.subtitle, color = Muted, fontSize = 10.sp, maxLines = 1)
            }
        }
    }
}

@Composable
private fun ModuleDetail(module: Module, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("رجوع إلى الأقسام", color = BrandBlue, fontSize = 13.sp, modifier = Modifier.clickable(onClick = onBack).padding(vertical = 8.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(14.dp)) {
            Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(module.icon, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(34.dp))
                Column {
                    Text(module.title, fontWeight = FontWeight.Bold, color = Ink, fontSize = 18.sp)
                    Text(module.subtitle, color = Muted, fontSize = 12.sp)
                }
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(14.dp)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("لا توجد سجلات", fontWeight = FontWeight.SemiBold, color = Ink)
                Text("هذه الشاشة هي نقطة بداية للربط بالوظائف وقاعدة البيانات. لم تتم إضافة سجلات تجريبية.", color = Muted, fontSize = 13.sp)
            }
        }
    }
}
