package com.example.fetchfilescompose

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val fileUris = remember { mutableStateOf<List<Uri>>(emptyList()) }

    val folderPickerLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
            uri?.let {
                fileUris.value = getFilesFromFolder(context, it)
            }
        }

    val permissionLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                folderPickerLauncher.launch(null)
            } else {
                Toast.makeText(context, "Permission denied...", Toast.LENGTH_SHORT).show()
            }
        }

    Column(modifier.fillMaxSize()) {
        Button(onClick = {
            checkAndRequestPermission(context, permissionLauncher, folderPickerLauncher)
        }) {
            Text("Pick folder")
        }
        Spacer(modifier.height(8.dp))
        LazyColumn {
            items(fileUris.value) { uri ->
                val fileType = context.contentResolver.getType(uri)

                Column {
                    when {
                        fileType != null && fileType.startsWith("image/") -> {
                            Image(
                                painter = painterResource(id = R.drawable.image_24px),
                                contentDescription = "Image File",
                                modifier = Modifier.height(100.dp)
                            )
                            Text(text = uri.lastPathSegment ?: "Unknown Image")
                        }
                        fileType != null && fileType.startsWith("audio/") -> {
                            Image(
                                painter = painterResource(id = R.drawable.music_note_24px),
                                contentDescription = "Audio File",
                                modifier = Modifier.height(100.dp)
                            )
                            Text(text = uri.lastPathSegment ?: "Unknown Audio")
                        }
                        fileType != null && fileType.startsWith("video/") -> {
                            Image(
                                painter = painterResource(id = R.drawable.play_arrow_24px),
                                contentDescription = "Video File",
                                modifier = Modifier.height(100.dp)
                            )
                            Text(text = uri.lastPathSegment ?: "Unknown Video")
                        }
                        else -> {
                            Text(text = "Unknown File Type: ${uri.lastPathSegment ?: "Unknown File"}")
                        }
                    }
                }
            }
        }
    }
}

fun checkAndRequestPermission(
    context: Context,
    permissionLauncher: ManagedActivityResultLauncher<String, Boolean>,
    folderPickerLauncher: ManagedActivityResultLauncher<Uri?, Uri?>
) {
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.MANAGE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(android.Manifest.permission.MANAGE_EXTERNAL_STORAGE)
            } else {
                folderPickerLauncher.launch(null)
            }
        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            } else {
                folderPickerLauncher.launch(null)
            }
        }
        else -> {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            } else {
                folderPickerLauncher.launch(null)
            }
        }
    }
}

fun getFilesFromFolder(context: Context, uri: Uri): List<Uri> {
    val files = mutableListOf<Uri>()
    val documentFiles = DocumentFile.fromTreeUri(context, uri)

    documentFiles?.listFiles()?.forEach { file ->
        if (file.isFile) {
            files.add(file.uri)
        }
    }
    return files
}