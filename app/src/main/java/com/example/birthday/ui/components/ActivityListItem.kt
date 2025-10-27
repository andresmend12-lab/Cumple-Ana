package com.example.birthday.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.birthday.R
import com.example.birthday.data.model.ActivityLockReason
import com.example.birthday.data.model.ActivityTimelineState
import com.example.birthday.data.model.ActivityTimelineStatus
import com.example.birthday.util.TimeUtils

@Composable
fun ActivityListItem(
    state: ActivityTimelineState,
    onClick: () -> Unit,
    onSkipTimer: (() -> Unit)? = null
) {
    val title = state.activity.title
    val painter = activityIconPainter(title)
    val enabled = state.isAvailable
    val tint = if (enabled) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val stateLabel = when (state.status) {
        ActivityTimelineStatus.COMPLETED -> stringResource(R.string.activity_status_completed)
        ActivityTimelineStatus.AVAILABLE -> stringResource(R.string.activity_status_ready)
        ActivityTimelineStatus.PENDING_PHOTO -> when (state.lockReason) {
            ActivityLockReason.PreviousIncomplete -> stringResource(R.string.activity_status_previous_incomplete)
            ActivityLockReason.MissingPhoto -> stringResource(R.string.activity_status_pending_photo)
            else -> stringResource(R.string.activity_status_pending_photo)
        }

        ActivityTimelineStatus.BLOCKED_TIME -> stringResource(
            R.string.activity_status_locked_time_with_hour,
            TimeUtils.formatUnlockTime(state.unlockAt)
        )
    }

    val countdown = if (
        state.status == ActivityTimelineStatus.BLOCKED_TIME &&
        state.timeRemaining != null &&
        state.previousCompleted
    ) {
        TimeUtils.formatDuration(state.timeRemaining)
    } else {
        null
    }

    val showSkip = onSkipTimer != null &&
        state.status == ActivityTimelineStatus.BLOCKED_TIME &&
        state.previousCompleted

    val baseModifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 4.dp)

    val itemModifier = if (enabled) {
        baseModifier.clickable(onClick = onClick)
    } else {
        baseModifier
    }

    ListItem(
        modifier = itemModifier,
        headlineContent = {
            Text(text = title)
        },
        supportingContent = {
            Column {
                Text(
                    text = stateLabel,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                countdown?.let { remaining ->
                    Text(
                        text = remaining,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        leadingContent = {
            Icon(
                painter = painter,
                contentDescription = "Icono de $title",
                tint = tint,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .size(24.dp)
            )
        },
        trailingContent = {
            if (showSkip) {
                TextButton(onClick = { onSkipTimer?.invoke() }) {
                    Text(text = stringResource(id = R.string.skip_wait))
                }
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
    Divider()
}
