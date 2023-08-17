package com.example.baseandroid.features.main.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.request.RequestOptions
import com.example.baseandroid.R
import com.example.baseandroid.features.main.models.UserResponse

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AdapterItem(modifier: Modifier, userData: UserResponse) {
    Card(modifier = modifier
        .padding(10.dp)
        .fillMaxWidth()
        .height(100.dp),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 10.dp, pressedElevation = 15.dp
        ),
        onClick = {
            // write onClick functionality here
        }) {


        Row {
            GlideImage(
                model = "your_url",
                contentDescription = null,
                modifier = modifier
                    .fillMaxWidth(0.2f)
                    .fillMaxHeight()
                    .padding(5.dp),
            ){
                it.apply(
                    RequestOptions().placeholder(R.drawable.ic_placeholder)
                )
            }

            Column(
                modifier = modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .padding(5.dp)
            ) {
                userData.title?.let {
                    Text(
                        text = it,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = modifier.padding(vertical = 5.dp)
                    )
                }
                userData.body?.let {
                    Text(
                        text = it, modifier = Modifier.alpha(0.3f)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAdapterItem() {
    AdapterItem(modifier = Modifier, userData = UserResponse(0, 0, "Hello", "World"))
}
