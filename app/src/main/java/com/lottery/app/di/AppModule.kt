package com.lottery.app.di

import android.content.Context
import androidx.room.Room
import com.lottery.app.data.local.DrawResultDao
import com.lottery.app.data.local.LotteryDatabase
import com.lottery.app.data.local.LotteryRecordDao
import com.lottery.app.data.repository.DrawRepositoryImpl
import com.lottery.app.data.repository.LotteryRepositoryImpl
import com.lottery.app.data.repository.PredictionRepositoryImpl
import com.lottery.app.domain.repository.DrawRepository
import com.lottery.app.domain.repository.LotteryRepository
import com.lottery.app.domain.repository.PredictionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ============== Database ==============
    
    @Provides
    @Singleton
    fun provideLotteryDatabase(@ApplicationContext context: Context): LotteryDatabase {
        return Room.databaseBuilder(
            context,
            LotteryDatabase::class.java,
            LotteryDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideLotteryRecordDao(database: LotteryDatabase): LotteryRecordDao {
        return database.lotteryRecordDao()
    }

    @Provides
    @Singleton
    fun provideDrawResultDao(database: LotteryDatabase): DrawResultDao {
        return database.drawResultDao()
    }

    // ============== Repositories ==============

    @Provides
    @Singleton
    fun provideLotteryRepository(
        lotteryRecordDao: LotteryRecordDao
    ): LotteryRepository {
        return LotteryRepositoryImpl(lotteryRecordDao)
    }

    @Provides
    @Singleton
    fun provideDrawRepository(
        drawResultDao: DrawResultDao
    ): DrawRepository {
        return DrawRepositoryImpl(drawResultDao)
    }

    @Provides
    @Singleton
    fun providePredictionRepository(): PredictionRepository {
        return PredictionRepositoryImpl()
    }
}