package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.TaskFilter
import com.example.ui.theme.BluePrimary

@Composable
fun TaskFilterChips(
    selectedFilter: TaskFilter,
    pendingCount: Int,
    completedCount: Int,
    totalCount: Int,
    onFilterSelected: (TaskFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            FilterTab(
                text = "${stringResource(id = R.string.filter_all)} ($totalCount)",
                isSelected = selectedFilter == TaskFilter.ALL,
                onClick = { onFilterSelected(TaskFilter.ALL) },
                testTag = "filter_all_tab",
                modifier = Modifier.weight(1f)
            )
            FilterTab(
                text = "${stringResource(id = R.string.filter_pending)} ($pendingCount)",
                isSelected = selectedFilter == TaskFilter.PENDING,
                onClick = { onFilterSelected(TaskFilter.PENDING) },
                testTag = "filter_pending_tab",
                modifier = Modifier.weight(1f)
            )
            FilterTab(
                text = "${stringResource(id = R.string.filter_completed)} ($completedCount)",
                isSelected = selectedFilter == TaskFilter.COMPLETED,
                onClick = { onFilterSelected(TaskFilter.COMPLETED) },
                testTag = "filter_completed_tab",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun FilterTab(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
        label = "filterTabBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) BluePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "filterTabText"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp
            ),
            color = textColor,
            maxLines = 1
        )
    }
}
