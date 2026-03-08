package com.lottery.app.di

import android.content.Context
import androidx.room.Room
import com.lottery.app.data.local.DrawResultDao
import com.lottery.app.data.local.LotteryDatabase
import com.lottery.app.data.local.LotteryRecordDao
import com.lottery.app.data.remote.DrawApiService
import com.lottery.app.data.repository.DrawRepositoryImpl
import com.lottery.app.data.repository.FeishuRepositoryImpl
import com.lottery.app.data.repository.LotteryRepositoryImpl
import com.lottery.app.data.repository.PredictionRepositoryImpl
import com.lottery.app.domain.repository.DrawRepository
import com.lottery.app.domain.repository.FeishuRepository
import com.lottery.app.domain.repository.LotteryRepository
import com.lottery.app.domain.repository.PredictionRepository
import com.lottery.app.feishu.FishFlipper
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
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

    // ============== Network ==============

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.caipiao.163.com/")  // 示例域名
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideDrawApiService(retrofit: Retrofit): DrawApiService {
        return retrofit.create(DrawApiService::class.java)
    }

    // ============== Feishu ==============

    @Provides
    @Singleton
    fun provideFishFlipper(): FishFlipper {
        return FishFlipper()
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
        drawResultDao: DrawResultDao,
        drawApiService: DrawApiService
    ): DrawRepository {
        return DrawRepositoryImpl(drawResultDao, drawApiService)
    }

    @Provides
    @Singleton
    fun providePredictionRepository(): PredictionRepository {
        return PredictionRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideFeishuRepository(
        fishFlipper: FishFlipper
    ): FeishuRepository {
        return FeishuRepositoryImpl(fishFlipper)
    }
}