package com.melonheadstudios.kanjispotter.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.melonheadstudios.kanjispotter.R
import com.melonheadstudios.kanjispotter.extensions.verticalFadingEdge
import com.melonheadstudios.kanjispotter.models.BlacklistApp

//    val mainIntent = Intent(Intent.ACTION_MAIN, null)
//    mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
//    val pkgAppsList = packageManager.queryIntentActivities(mainIntent, 0)
//    pkgAppsList.map {
//        val appLabel = it.loadLabel(packageManager).toString()
//        val packageName = it.activityInfo.taskAffinity
//        val packageIcon = it.loadIcon(packageManager)
//        BlackListableApp(name = appLabel, packageName = packageName, icon = packageIcon)
//    }

@Composable
fun Blacklist(
        blacklistApps: Set<BlacklistApp>,
        blacklistedPackages: Set<String>,
        blackListValueToggled: (packageName: String, isBlackListed: Boolean) -> Unit) {
    val scrollState = rememberScrollState()
    Column(Modifier.padding(24.dp)) {
        Text(text = "Blacklist Apps", style = MaterialTheme.typography.h5)
        Text(text = "Prevent Kanji Spotter from triggering from certain apps", style = MaterialTheme.typography.body2)
        Column(verticalArrangement = Arrangement.spacedBy(15.dp),
               modifier = Modifier
                       .verticalFadingEdge(scrollState, length = 150.dp)
                       .fillMaxWidth()
                       .padding(vertical = 15.dp)) {
            blacklistApps.forEach { app ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                            bitmap = app.icon.toBitmap().asImageBitmap(),
                            contentDescription = app.packageName,
                            modifier = Modifier
                                    .size(50.dp)
                    )
                    Column(modifier = Modifier.padding(horizontal = 15.dp).weight(1f)) {
                        Text(text = app.name, style = MaterialTheme.typography.subtitle2)
                        Text(text = app.packageName, style = MaterialTheme.typography.caption)
                    }
                    Switch(checked = blacklistedPackages.contains(app.packageName), onCheckedChange = { checked ->
                        blackListValueToggled(app.packageName, checked)
                    })
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xffffff)
@Composable
fun PreviewBlackList() {
    MaterialTheme {
        val resources = LocalContext.current.resources
        val icon = ResourcesCompat.getDrawable(resources, R.mipmap.ic_launcher, null)!!
        Blacklist(setOf(
            BlacklistApp("Test 1", "com.test.test1", icon),
            BlacklistApp("Test 2", "com.test.test2", icon),
            BlacklistApp("Test3", "com.test.test3", icon),
        ), blacklistedPackages = setOf("com.test.test3"), blackListValueToggled = { _, _ ->
        })
    }
}