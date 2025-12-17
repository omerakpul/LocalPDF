package com.omerakpul.localpdf.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.omerakpul.localpdf.data.local.dao.PdfDao
import com.omerakpul.localpdf.data.local.entity.PdfEntity

@Database(
    entities = [PdfEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PdfDatabase : RoomDatabase() {
    abstract fun pdfDao(): PdfDao
}