package com.example.wizardlydo.screens.customization.comps

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wizardlydo.R


@Composable
fun HairStyleSelector(
    gender: String,
    selectedStyle: Int,
    onHairStyleSelected: (Int) -> Unit
) {
    val hairStyles = remember(gender) {
        if (gender == "Male") {
            listOf(
                Triple("Short", R.drawable.creator_hair_bangs_1_black, Color.White),
                Triple("Wavy", R.drawable.creator_hair_bangs_1_white, Color.White),
            )
        } else {
            listOf(
                Triple("Wavy", R.drawable.creator_hair_bangs_1_white, Color.White),
                Triple("Classic", R.drawable.creator_hair_bangs_2_blond, Color.White)
            )
        }
    }

    Column {
        Text(
            text = "Hair Style",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(hairStyles) { index, (styleName, resourceId, bgColor) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(80.dp)
                        .clickable { onHairStyleSelected(index) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = 2.dp,
                                color = if (selectedStyle == index)
                                    MaterialTheme.colorScheme.primary
                                else
                                    Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(bgColor)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (resourceId != 0) {
                            Image(
                                painter = painterResource(id = resourceId),
                                contentDescription = styleName,
                                modifier = Modifier.fillMaxSize(),
                                alignment = Alignment.Center
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.LightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Missing",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center,
                                    fontSize = 8.sp
                                )
                            }
                        }
                    }
                    Text(
                        text = styleName,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}