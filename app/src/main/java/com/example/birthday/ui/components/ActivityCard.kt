package com.example.birthday.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.birthday.R
import com.example.birthday.data.model.ActivityLockReason
import com.example.birthday.data.model.ActivityTimelineState
import com.example.birthday.data.model.ActivityTimelineStatus
import com.example.birthday.util.TimeUtils
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource

@Composable
fun ActivityCard(
    state: ActivityTimelineState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onSkipWait: (() -> Unit)? = null
) {
    val activity = state.activity
    val icon = ActivityIcons.forId(activity.id)
    val backgroundColor = when (state.status) {
        ActivityTimelineStatus.COMPLETED -> MaterialTheme.colorScheme.tertiaryContainer
        ActivityTimelineStatus.AVAILABLE -> MaterialTheme.colorScheme.primaryContainer
        ActivityTimelineStatus.PENDING_PHOTO -> MaterialTheme.colorScheme.secondaryContainer
        ActivityTimelineStatus.BLOCKED_TIME -> MaterialTheme.colorScheme.surfaceVariant
    }
    val foregroundColor = when (state.status) {
        ActivityTimelineStatus.COMPLETED -> MaterialTheme.colorScheme.onTertiaryContainer
        ActivityTimelineStatus.AVAILABLE -> MaterialTheme.colorScheme.onPrimaryContainer
        ActivityTimelineStatus.PENDING_PHOTO -> MaterialTheme.colorScheme.onSecondaryContainer
        ActivityTimelineStatus.BLOCKED_TIME -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val primaryStatusText: String? = when (state.status) {
        ActivityTimelineStatus.BLOCKED_TIME ->
            stringResource(
                id = R.string.activity_status_locked_time_with_hour,
                TimeUtils.formatUnlockTime(state.unlockAt)
            )
        ActivityTimelineStatus.PENDING_PHOTO -> null
        ActivityTimelineStatus.AVAILABLE -> stringResource(id = R.string.activity_status_ready)
        ActivityTimelineStatus.COMPLETED -> stringResource(id = R.string.activity_status_completed)
    }

    val secondaryMessage = when (state.lockReason) {
        ActivityLockReason.PreviousIncomplete -> stringResource(id = R.string.activity_status_previous_incomplete)
        else -> null
    }

    val countdown = remember(state.timeRemaining, state.status, state.previousCompleted) {
        if (
            state.status == ActivityTimelineStatus.BLOCKED_TIME &&
            state.timeRemaining != null &&
            state.previousCompleted
        ) {
            TimeUtils.formatDuration(state.timeRemaining)
        } else {
            null
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        onClick = onClick,
        enabled = state.isAvailable,
        shape = RoundedCornerShape(28.dp),
        color = backgroundColor,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.35f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(56.dp)
                        .padding(14.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = foregroundColor
                )
                primaryStatusText?.let { statusText ->
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.55f)
                    ) {
                        Text(
                            text = statusText,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                secondaryMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = foregroundColor
                    )
                }
                countdown?.let { remaining ->
                    Text(
                        text = remaining,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium),
                        color = foregroundColor
                    )
                }
                if (onSkipWait != null && state.status == ActivityTimelineStatus.BLOCKED_TIME && state.previousCompleted) {
                    androidx.compose.material3.TextButton(onClick = onSkipWait) {
                        Text(
                            text = stringResource(id = R.string.skip_wait_debug),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
