package com.waypoint.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Lightweight launcher: native map rendering begins only when the user enters a world. */
@Composable fun WelcomeScreen(world:World,onOpen:()->Unit,onWorlds:()->Unit,onSettings:()->Unit) {
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit){entered=true}
    Scaffold(containerColor=Ink,contentWindowInsets=WindowInsets(0),bottomBar={
        Column(Modifier.background(Ink).navigationBarsPadding().padding(horizontal=24.dp).padding(top=12.dp,bottom=16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
            Button(onClick=onOpen,modifier=Modifier.fillMaxWidth().heightIn(min=56.dp),shape=CutCornerShape(topStart=12.dp,bottomEnd=12.dp),colors=ButtonDefaults.buttonColors(containerColor=world.accent,contentColor=Ink)) {
                Icon(Icons.Default.Navigation,null,Modifier.size(21.dp));Spacer(Modifier.width(12.dp))
                Text("Open map",Modifier.weight(1f),fontSize=18.sp,fontWeight=FontWeight.SemiBold)
                Icon(Icons.AutoMirrored.Filled.ArrowForward,null)
            }
            Text("Car · Motorcycle · Walk",color=Muted,fontSize=13.sp,modifier=Modifier.align(Alignment.CenterHorizontally))
        }
    }) { inset ->
        AnimatedVisibility(entered,enter=fadeIn(tween(360))+slideInVertically(tween(420,easing=FastOutSlowInEasing)){height->(height*.035f).toInt()}) {
            Column(Modifier.fillMaxSize().padding(inset).statusBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal=24.dp).padding(top=14.dp,bottom=16.dp),verticalArrangement=Arrangement.spacedBy(24.dp)) {
                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                    Icon(Icons.Default.Explore,null,tint=world.accent,modifier=Modifier.size(30.dp))
                    Text("WAYPOINT",fontFamily=DisplayFont,fontSize=29.sp,color=Cream,modifier=Modifier.weight(1f).padding(start=10.dp))
                    IconButton(onClick=onSettings,modifier=Modifier.size(48.dp)){Icon(Icons.Default.Tune,"Settings",tint=Cream)}
                }
                Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
                    Text("REAL STREETS.\nGAME WORLDS.",fontFamily=DisplayFont,fontSize=48.sp,lineHeight=49.sp,color=Cream)
                    Text("Your everyday journey, through the worlds you love.",fontSize=16.sp,lineHeight=23.sp,color=Muted)
                }
                Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
                    Crossfade(world,animationSpec=tween(240),label="Selected world") { selected ->
                        val p=hudPalette(selected)
                        Surface(onClick=onWorlds,color=p.panel,contentColor=p.foreground,shape=CutCornerShape(topStart=16.dp,bottomEnd=16.dp)) {
                            Column {
                                Box(Modifier.fillMaxWidth().height(168.dp).clip(CutCornerShape(topStart=16.dp))) {
                                    MapSwatch(selected,Modifier.fillMaxSize())
                                    Surface(color=p.panel,modifier=Modifier.align(Alignment.BottomStart).padding(12.dp)) {
                                        Text("Style preview",color=p.foreground,fontSize=11.sp,modifier=Modifier.padding(horizontal=8.dp,vertical=4.dp))
                                    }
                                }
                                Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                                    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(5.dp)) {
                                        Text(selected.title,fontFamily=hudFont(selected),fontSize=22.sp,lineHeight=28.sp)
                                        Text(selected.inspiration(),color=p.muted,fontSize=12.sp,lineHeight=17.sp)
                                    }
                                    Icon(Icons.Default.Layers,"Change world",Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                    TextButton(onClick=onWorlds,modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)) {
                        Text("Explore all ${World.entries.size} game worlds",color=world.accent,fontSize=15.sp)
                        Spacer(Modifier.width(8.dp));Icon(Icons.AutoMirrored.Filled.ArrowForward,null,tint=world.accent,modifier=Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
