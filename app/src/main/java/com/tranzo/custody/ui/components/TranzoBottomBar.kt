package com.tranzo.custody.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.tranzo.custody.ui.util.glassCard
import com.tranzo.custody.ui.theme.LocalTranzoTheme

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem("home", "Home", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem("card", "Card", Icons.Filled.CreditCard, Icons.Outlined.CreditCard),
    BottomNavItem("activity", "Activity", Icons.Filled.History, Icons.Outlined.History),
    BottomNavItem("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
)

@Composable
fun TranzoBottomBar(
    currentRoute: String?,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .height(64.dp)
                .fillMaxWidth()
                .glassCard(cornerRadius = 32.dp, alpha = 0.12f)
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(Color.White.copy(0.2f), Color.Transparent, Color.White.copy(0.1f))
                    ),
                    shape = RoundedCornerShape(32.dp)
                ),
            color = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                bottomNavItems.forEach { item ->
                    val selected = currentRoute == item.route
                    
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable { onItemSelected(item.route) },
                        contentAlignment = Alignment.Center
                    ) {
                        // Background glow for selected item
                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(MaterialTheme.colorScheme.primary.copy(0.2f), Color.Transparent)
                                        ),
                                        shape = CircleShape
                                    )
                            )
                        }

                        Icon(
                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label,
                            modifier = Modifier.size(26.dp),
                            tint = if (selected) MaterialTheme.colorScheme.primary else Color.White.copy(0.4f)
                        )
                    }
                }
            }
        }
    }
}
