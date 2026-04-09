package com.example.hugbunadarver2.theater

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point

private const val STYLE_URL = "https://tiles.openfreemap.org/styles/liberty"
private const val DEFAULT_LAT = 64.1355
private const val DEFAULT_LNG = -21.8954

@SuppressLint("MissingPermission")
@Composable
fun TheaterMapScreen(viewModel: TheaterMapViewModel = viewModel()) {
    val context = LocalContext.current
    val theaters by viewModel.theaters.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var locationGranted by remember { mutableStateOf(false) }
    val mapRef = remember { mutableStateOf<MapLibreMap?>(null) }
    var styleLoaded by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        locationGranted = granted
    }

    // Initialize MapLibre (no API key needed for OSM)
    remember(context) { MapLibre.getInstance(context) }

    // Check / request location permission
    LaunchedEffect(Unit) {
        locationGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!locationGranted) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Fetch user's last known location when permission is granted
    LaunchedEffect(locationGranted) {
        if (!locationGranted) return@LaunchedEffect
        // Enable the blue location dot if the style loaded before permission was granted
        mapRef.value?.getStyle { style -> enableLocationComponent(context, mapRef.value!!, style) }
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { loc ->
                if (loc != null) {
                    viewModel.updateUserLocation(loc.latitude, loc.longitude)
                    mapRef.value?.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(LatLng(loc.latitude, loc.longitude), 12.0)
                    )
                }
            }
    }

    // Redraw theater markers whenever the style is ready or theaters list changes
    LaunchedEffect(styleLoaded, theaters) {
        if (!styleLoaded) return@LaunchedEffect
        val map = mapRef.value ?: return@LaunchedEffect
        updateTheaterMarkers(map, theaters)
    }

    val mapView = remember { MapView(context) }

    DisposableEffect(Unit) {
        onDispose {
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Map — top half
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            AndroidView(
                factory = {
                    mapView.apply {
                        // Lifecycle must be called before getMapAsync
                        onCreate(null)
                        onStart()
                        onResume()
                        getMapAsync { map ->
                            mapRef.value = map
                            map.setStyle(STYLE_URL) { style ->
                                map.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(DEFAULT_LAT, DEFAULT_LNG), 10.0
                                    )
                                )
                                // Only enable location dot if permission is already granted
                                if (ContextCompat.checkSelfPermission(
                                        context, Manifest.permission.ACCESS_FINE_LOCATION
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    enableLocationComponent(context, map, style)
                                }
                                styleLoaded = true
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }

        // Theater list — bottom half
        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (theaters.isEmpty() && !isLoading) {
                item {
                    Text(
                        text = "No theaters with location data available.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            items(theaters) { item ->
                TheaterListItem(item)
                HorizontalDivider()
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun enableLocationComponent(
    context: android.content.Context,
    map: MapLibreMap,
    style: org.maplibre.android.maps.Style
) {
    val locationComponent = map.locationComponent
    if (!locationComponent.isLocationComponentActivated) {
        locationComponent.activateLocationComponent(
            LocationComponentActivationOptions.builder(context, style).build()
        )
    }
    locationComponent.isLocationComponentEnabled = true
    locationComponent.cameraMode = CameraMode.NONE
    locationComponent.renderMode = RenderMode.COMPASS
}

private fun updateTheaterMarkers(map: MapLibreMap, theaters: List<TheaterWithDistance>) {
    map.getStyle { style ->
        runCatching { style.removeLayer("theaters-layer") }
        runCatching { style.removeSource("theaters-source") }

        if (theaters.isEmpty()) return@getStyle

        val features = theaters.mapNotNull { t ->
            val lat = t.hall.latitude ?: return@mapNotNull null
            val lng = t.hall.longitude ?: return@mapNotNull null
            Feature.fromGeometry(Point.fromLngLat(lng, lat))
        }

        runCatching {
            style.addSource(GeoJsonSource("theaters-source", FeatureCollection.fromFeatures(features)))
        }
        runCatching {
            style.addLayer(
                CircleLayer("theaters-layer", "theaters-source").withProperties(
                    PropertyFactory.circleRadius(12f),
                    PropertyFactory.circleColor("#E53935"),
                    PropertyFactory.circleStrokeWidth(2f),
                    PropertyFactory.circleStrokeColor("#FFFFFF")
                )
            )
        }
    }
}

@Composable
private fun TheaterListItem(item: TheaterWithDistance) {
    val context = LocalContext.current
    val lat = item.hall.latitude
    val lng = item.hall.longitude

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = lat != null && lng != null) {
                val uri = Uri.parse("google.navigation:q=$lat,$lng")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                context.startActivity(intent)
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.hall.name,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = item.hall.location,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item.distanceMeters?.let { meters ->
            val distText = if (meters < 1000) {
                "${meters.toInt()} m"
            } else {
                "${"%.1f".format(meters / 1000f)} km"
            }
            Text(
                text = distText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        if (lat != null && lng != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Navigation,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
