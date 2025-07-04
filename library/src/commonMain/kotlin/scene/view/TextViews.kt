package scene.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
internal fun InfoTextView(text: String) {
    val bubbleShape = RoundedCornerShape(16.dp)
    val borderColor = Color.Blue
    val modifier = Modifier
        .padding(12.dp)
        .fillMaxWidth()
        .border(width = 2.dp, color = borderColor, shape = bubbleShape)
        .background(color = borderColor.copy(alpha = 0.3F), shape = bubbleShape)
        .padding(16.dp)

    Text(text = text, textAlign = TextAlign.Center, modifier = modifier)
}

@Composable
internal fun PlayerTextView(text: String) {
    val bubbleShape = RoundedCornerShape(16.dp, 2.dp, 16.dp, 16.dp)
    val borderColor = Color.Green
    val modifier = Modifier
        .padding(start = 48.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)
        .fillMaxWidth()
        .border(width = 2.dp, color = borderColor, shape = bubbleShape)
        .background(color = borderColor.copy(alpha = 0.3F), shape = bubbleShape)
        .padding(16.dp)

    Text(text = text, textAlign = TextAlign.Start, modifier = modifier)
}

@Composable
internal fun CharacterTextView(text: String) {
    val bubbleShape = RoundedCornerShape(2.dp, 16.dp, 16.dp, 16.dp)
    val borderColor = Color.Yellow
    val modifier = Modifier
        .padding(start = 12.dp, end = 48.dp, top = 12.dp, bottom = 12.dp)
        .fillMaxWidth()
        .border(width = 2.dp, color = borderColor, shape = bubbleShape)
        .background(color = borderColor.copy(alpha = 0.3F), shape = bubbleShape)
        .padding(16.dp)

    Text(text = text, textAlign = TextAlign.Start, modifier = modifier)
}

@Composable
internal fun LinkView(onClick: () -> Unit, text: String) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(text = text, modifier = Modifier.padding(12.dp))
    }
}