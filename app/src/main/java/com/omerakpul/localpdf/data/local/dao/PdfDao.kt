package com.omerakpul.localpdf.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.omerakpul.localpdf.data.local.entity.PdfEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PdfDao {

    @Query("SELECT * FROM pdfs ORDER BY createdAt DESC") // Sort by createdAt in descending order
    fun getAllPdfs(): Flow<List<PdfEntity>>

    @Query("SELECT * FROM pdfs WHERE id = :id") // Query to get a specific PDF by its ID
    suspend fun getPdfById(id: Int): PdfEntity?

    @Query("SELECT * FROM pdfs WHERE name LIKE '%' || :query || '%' ORDER BY createdAt DESC") // Query to search PDFs by name
    fun searchPdfs(query: String): Flow<List<PdfEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPdf(pdf: PdfEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPdfs(pdfs: List<PdfEntity>) : List<Long>

    @Update
    suspend fun updatePdf(pdf: PdfEntity)

    @Query("DELETE FROM pdfs WHERE id = :id")
    suspend fun deletePdfById(id: Int)

    @Query("DELETE FROM pdfs")
    suspend fun deleteAllPdfs()
}