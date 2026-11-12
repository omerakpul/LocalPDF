package com.omerakpul.localpdf.di

import android.content.Context
import androidx.room.Room
import com.omerakpul.localpdf.data.local.dao.PdfDao
import com.omerakpul.localpdf.data.local.database.PdfDatabase
import com.omerakpul.localpdf.data.repository.PdfRepositoryImpl
import com.omerakpul.localpdf.data.service.PdfService
import com.omerakpul.localpdf.domain.repository.PdfRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PdfDatabase {
        return Room.databaseBuilder(
            context,
            PdfDatabase::class.java,
            "pdf_database"
        )
            // Only the pre-release schema (v1) may be wiped. Every later version bump must
            // come with an explicit Migration so users keep their data.
            .fallbackToDestructiveMigrationFrom(1)
            .build()
    }

    @Provides
    @Singleton
    fun provideDao(db: PdfDatabase): PdfDao {
        return db.pdfDao()
    }

    @Provides
    @Singleton
    fun provideRepository(dao: PdfDao): PdfRepository {
        return PdfRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun providePdfService(@ApplicationContext context: Context): PdfService {
        return PdfService(context)
    }
}