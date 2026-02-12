package org.example.project.ui.navigation

import androidx.compose.runtime.*

@Composable
fun rememberSimpleNavController(initialRoute: String = Screen.Progress.route): SimpleNavController {
    val currentRoute = remember { mutableStateOf(initialRoute) }
    return remember {
        SimpleNavController(
            currentRoute = currentRoute,
            onNavigate = { route -> currentRoute.value = route }
        )
    }
}

class SimpleNavController(
    private val currentRoute: MutableState<String>,
    private val onNavigate: (String) -> Unit
) {
    fun navigate(route: String) {
        onNavigate(route)
    }
    
    val currentRouteValue: String
        get() = currentRoute.value
}


