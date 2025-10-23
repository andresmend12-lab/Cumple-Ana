package com.example.birthday.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.birthday.R
import com.example.birthday.data.model.ActivityLockReason
import com.example.birthday.data.repo.CumpleRepository
import com.example.birthday.ui.components.ActivityIcons
import com.example.birthday.util.TimeUtils

@Composable
fun LockedActivityScreen(
    activityId: Int,
    repository: CumpleRepository,
    onBack: () -> Unit
) {
    val timeline by repository.observeTimelineState().collectAsState(initial = emptyList())
    val state = timeline.firstOrNull { it.activity.id == activityId }

    if (state == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Button(onClick = onBack) {
                Text(text = stringResource(id = R.string.back))
            }
        }
        return
    }

    val icon = ActivityIcons.forId(state.activity.id)
    val reasonText = when (val reason = state.lockReason) {
        is ActivityLockReason.WaitingTime -> stringResource(
            id = R.string.locked_activity_waiting_time,
            TimeUtils.formatUnlockTime(reason.unlockAt)
        )
        ActivityLockReason.MissingPhoto -> stringResource(id = R.string.locked_activity_missing_photo)
        ActivityLockReason.PreviousIncomplete -> stringResource(id = R.string.activity_status_previous_incomplete)
        null -> stringResource(id = R.string.locked_activity_generic)
    }
    val countdown = if (
        state.lockReason is ActivityLockReason.WaitingTime &&
        state.previousCompleted &&
        state.timeRemaining != null
    ) {
        TimeUtils.formatDuration(state.timeRemaining)
    } else {
        null
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp),
            shape = RoundedCornerShape(32.dp),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 8.dp)
                        .size(48.dp)
                )
                Text(
                    text = state.activity.title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = reasonText,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                countdown?.let {
                    Text(
                        text = it,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Button(onClick = onBack) {
                    Text(text = stringResource(id = R.string.back))
                }
            }
        }
    }
}
