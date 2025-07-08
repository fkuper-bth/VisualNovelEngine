package scene.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
internal fun InfoTextView(text: String) {
    val bubbleShape = RoundedCornerShape(16.dp)
    val borderColor = MaterialTheme.colorScheme.primaryContainer
    val textStyle = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.onPrimaryContainer
    )
    val modifier = Modifier
        .padding(12.dp)
        .fillMaxWidth()
        .background(color = borderColor, shape = bubbleShape)
        .padding(16.dp)

    Text(text = text, textAlign = TextAlign.Center, style = textStyle, modifier = modifier)
}

@Composable
internal fun PlayerTextView(text: String) {
    val bubbleShape = RoundedCornerShape(16.dp, 2.dp, 16.dp, 16.dp)
    val borderColor = MaterialTheme.colorScheme.secondaryContainer
    val textStyle = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.onSecondaryContainer
    )
    val modifier = Modifier
        .padding(start = 48.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)
        .fillMaxWidth()
        .background(color = borderColor, shape = bubbleShape)
        .padding(16.dp)

    Text(text = text, textAlign = TextAlign.Start, style = textStyle, modifier = modifier)
}

@Composable
internal fun CharacterTextView(text: String) {
    val bubbleShape = RoundedCornerShape(2.dp, 16.dp, 16.dp, 16.dp)
    val borderColor = MaterialTheme.colorScheme.tertiaryContainer
    val textStyle = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.onTertiaryContainer
    )
    val modifier = Modifier
        .padding(start = 12.dp, end = 48.dp, top = 12.dp, bottom = 12.dp)
        .fillMaxWidth()
        .background(color = borderColor, shape = bubbleShape)
        .padding(16.dp)

    Text(text = text, textAlign = TextAlign.Start, style = textStyle, modifier = modifier)
}

@Composable
internal fun LinkView(onClick: () -> Unit, text: String) {
    val bubbleShape = RoundedCornerShape(16.dp, 2.dp, 16.dp, 16.dp)
    val borderColor = MaterialTheme.colorScheme.surface
    val textStyle = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.onSurface
    )
    val textButtonModifier = Modifier
        .padding(start = 48.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)
        .fillMaxWidth()
        .background(color = borderColor, shape = bubbleShape)
    val textModifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)

    TextButton(
        onClick = onClick,
        shape = bubbleShape,
        contentPadding = PaddingValues(0.dp),
        modifier = textButtonModifier
    ) {
        Text(text = text, textAlign = TextAlign.Start, style = textStyle, modifier = textModifier)
    }
}