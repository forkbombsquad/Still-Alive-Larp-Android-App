package com.forkbombsquad.stillalivelarp.jetpack_compose_view_components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forkbombsquad.stillalivelarp.R

// TODO FUTURE start using this one day
enum class NavArrowColor {
    BLACK, RED, GREEN, BLUE
}

@Composable
fun NavigationArrow(
    modifier: Modifier = Modifier,
    text: String,
    loading: Boolean,
    color: NavArrowColor,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                if (!loading) {
                    onClick()
                }
            }
            .clip(RoundedCornerShape(50))
            .background(color = Color.White)
            .border(2.dp, getColor(navArrowColor = color), shape = RoundedCornerShape(50))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text, fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(2f)
                    .padding(start = 8.dp, end = 8.dp)
            )
            Spacer(modifier = Modifier)
            if (loading) {
                CircularProgressIndicator(color = getColor(navArrowColor = color))
            } else {
                Icon(painter = painterResource(id = R.drawable.ic_arrow_forward), contentDescription = "Nav Arrow")
            }

        }
    }

}

@Composable
private fun getColor(navArrowColor: NavArrowColor): Color {
    return when (navArrowColor) {
        NavArrowColor.BLACK -> colorResource(id = R.color.black)
        NavArrowColor.RED -> colorResource(id = R.color.mid_red)
        NavArrowColor.GREEN -> colorResource(id = R.color.green)
        NavArrowColor.BLUE -> colorResource(id = R.color.blue)
    }
}

// Preview
data class PREVIEWPARAMS_NavigationArrow(
    val text: String,
    val loading: Boolean,
    val color: NavArrowColor,
    val onClick: () -> Unit
)

internal class PREVIEWPROVIDER_NavigationArrow: PreviewParameterProvider<PREVIEWPARAMS_NavigationArrow> {
    override val values = sequenceOf(
        PREVIEWPARAMS_NavigationArrow("Preview Test Text Black", false, NavArrowColor.BLACK) {},
        PREVIEWPARAMS_NavigationArrow("Preview Test Text Blue", false, NavArrowColor.BLUE) {},
        PREVIEWPARAMS_NavigationArrow("Preview Test Text Green", false, NavArrowColor.GREEN) {},
        PREVIEWPARAMS_NavigationArrow("Preview Test Text Red", false, NavArrowColor.RED) {},
        PREVIEWPARAMS_NavigationArrow("Preview Test Text Black Loading", true, NavArrowColor.BLACK) {},
        PREVIEWPARAMS_NavigationArrow("Preview Test Text Blue Loading", true, NavArrowColor.BLUE) {},
        PREVIEWPARAMS_NavigationArrow("Preview Test Text Green Loading", true, NavArrowColor.GREEN) {},
        PREVIEWPARAMS_NavigationArrow("Preview Test Text Red Loading", true, NavArrowColor.RED) {},
        PREVIEWPARAMS_NavigationArrow("Preview Test Text Black LONG BOI test with lots of things in it.", false, NavArrowColor.BLACK) {}
    )
}

@Preview
@Composable
fun PREVIEW_NavigationArrow(@PreviewParameter(PREVIEWPROVIDER_NavigationArrow::class) params: PREVIEWPARAMS_NavigationArrow) {
    NavigationArrow(text = params.text, loading = params.loading, color = params.color, onClick = params.onClick)
}