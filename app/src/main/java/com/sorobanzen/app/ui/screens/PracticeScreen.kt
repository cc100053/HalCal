package com.sorobanzen.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sorobanzen.app.R
import com.sorobanzen.app.ui.components.ZenBackground
import com.sorobanzen.app.ui.components.ZenCard
import com.sorobanzen.app.ui.components.ZenMark
import com.sorobanzen.app.ui.components.ZenMetric
import com.sorobanzen.app.ui.components.ZenScreenHeader
import com.sorobanzen.app.viewmodel.ZenViewModel

@Composable
fun PracticeScreen(
    viewModel: ZenViewModel,
    modifier: Modifier = Modifier
) {
    val isActive by viewModel.isPracticeActive.collectAsState()
    val timeLeft by viewModel.practiceTimeLeft.collectAsState()
    val score by viewModel.practiceScore.collectAsState()
    val total by viewModel.practiceTotal.collectAsState()
    val problemText by viewModel.currentProblemText.collectAsState()
    val inputVal by viewModel.practiceInput.collectAsState()
    val feedback by viewModel.practiceFeedback.collectAsState()
    val isAnswerLocked by viewModel.isPracticeAnswerLocked.collectAsState()

    ZenBackground(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ZenScreenHeader(
                title = stringResource(id = R.string.practice_mode),
                eyebrow = stringResource(id = R.string.practice_eyebrow),
                subtitle = stringResource(id = R.string.practice_subtitle)
            )

            Spacer(modifier = Modifier.height(22.dp))

            AnimatedContent(
                targetState = isActive,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "PracticeState"
            ) { active ->
                if (active) {
                    ActivePracticeContent(
                        timeLeft = timeLeft,
                        score = score,
                        total = total,
                        problemText = problemText,
                        inputVal = inputVal,
                        feedback = feedback,
                        isAnswerLocked = isAnswerLocked,
                        onInputChange = viewModel::updatePracticeInput,
                        onSubmit = viewModel::submitPracticeAnswer,
                        onStop = viewModel::stopPractice
                    )
                } else {
                    PracticeWelcome(onStart = viewModel::startPractice)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PracticeWelcome(onStart: () -> Unit) {
    ZenCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ZenMark(modifier = Modifier.size(58.dp))
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = stringResource(id = R.string.practice_intro_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(id = R.string.practice_intro_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(stringResource(id = R.string.start_practice))
            }
        }
    }
}

@Composable
private fun ActivePracticeContent(
    timeLeft: Int,
    score: Int,
    total: Int,
    problemText: String,
    inputVal: String,
    feedback: String,
    isAnswerLocked: Boolean,
    onInputChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onStop: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ZenMetric(
                label = stringResource(id = R.string.score_label),
                value = "$score / $total",
                modifier = Modifier.weight(1f)
            )
            ZenMetric(
                label = stringResource(id = R.string.time_label),
                value = stringResource(id = R.string.seconds_short, timeLeft),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { (timeLeft / 60f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        ZenCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = 28.dp
        ) {
            Text(
                text = stringResource(id = R.string.current_problem),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = problemText,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            contentAlignment = Alignment.Center
        ) {
            if (feedback.isNotEmpty()) {
                val isCorrect = feedback == "正解"
                val feedbackText = if (isCorrect) {
                    stringResource(id = R.string.correct)
                } else {
                    stringResource(id = R.string.incorrect, feedback.substringAfter("："))
                }
                Text(
                    text = feedbackText,
                    color = if (isCorrect) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.tertiary
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        OutlinedTextField(
            value = inputVal,
            onValueChange = onInputChange,
            label = { Text(stringResource(id = R.string.answer)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (inputVal.isNotBlank() && !isAnswerLocked) onSubmit()
                }
            ),
            singleLine = true,
            enabled = !isAnswerLocked,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.68f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.48f)
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onSubmit,
                enabled = inputVal.isNotBlank() && !isAnswerLocked,
                modifier = Modifier
                    .weight(1.25f)
                    .heightIn(min = 52.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(stringResource(id = R.string.submit))
            }
            Button(
                onClick = onStop,
                modifier = Modifier
                    .weight(0.75f)
                    .heightIn(min = 52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(stringResource(id = R.string.stop_practice))
            }
        }
    }
}
