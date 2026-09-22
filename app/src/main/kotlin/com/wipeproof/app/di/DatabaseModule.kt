package com.wipeproof.app.di

import android.content.Context
import androidx.room.Room
import com.wipeproof.app.data.db.WipeProofDatabase
import com.wipeproof.app.data.db.dao.CaseDao
import com.wipeproof.app.data.db.dao.CertificateDao
import com.wipeproof.app.data.db.dao.EventDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WipeProofDatabase {
        return Room.databaseBuilder(
            context,
            WipeProofDatabase::class.java,
            "wipeproof.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideCaseDao(db: WipeProofDatabase): CaseDao = db.caseDao()

    @Provides
    fun provideEventDao(db: WipeProofDatabase): EventDao = db.eventDao()

    @Provides
    fun provideCertificateDao(db: WipeProofDatabase): CertificateDao = db.certificateDao()
}
