package com.reelia.app.ui.backup

import com.reelia.app.data.backup.BackupImportReport
import com.reelia.app.data.backup.LibraryBackup

data class LibraryBackupUiState(
    val importPhase: ImportPhase = ImportPhase.PickFile,
    val isExporting: Boolean = false,
    val exportMessage: ExportMessage? = null,
)

sealed interface ExportMessage {
    data object Success : ExportMessage
    data class Failure(val message: String?) : ExportMessage
}

sealed interface ImportPhase {
    data object PickFile : ImportPhase
    data object Parsing : ImportPhase
    data class ParseFailed(val isUnsupportedVersion: Boolean) : ImportPhase
    data class ReadyToImport(val backup: LibraryBackup) : ImportPhase
    data object Importing : ImportPhase
    data class ImportFailed(val message: String?) : ImportPhase
    data class Done(val report: BackupImportReport) : ImportPhase
}
