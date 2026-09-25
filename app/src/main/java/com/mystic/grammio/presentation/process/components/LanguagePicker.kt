package com.mystic.grammio.presentation.process.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.mystic.grammio.R
import com.mystic.grammio.presentation.process.model.TranslationLanguages
import java.util.Locale

/** Target-language dropdown shown while Translate is selected. */
@Composable
fun LanguagePicker(
    selectedTag: String,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val tags = remember { TranslationLanguages.tags(Locale.getDefault().language) }
    Box {
        TextButton(onClick = { expanded = true }) {
            Text(stringResource(R.string.process_target_language, TranslationLanguages.displayName(selectedTag)))
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            tags.forEach { tag ->
                DropdownMenuItem(
                    text = { Text(TranslationLanguages.displayName(tag)) },
                    onClick = {
                        expanded = false
                        onSelect(tag)
                    },
                )
            }
        }
    }
}
