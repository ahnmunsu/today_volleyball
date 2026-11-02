package com.example.todayvolleyball.di

import android.content.Context
import androidx.room.Room
import com.example.todayvolleyball.data.local.SettingsDataStore
import com.example.todayvolleyball.data.local.VolleyballDatabase
import com.example.todayvolleyball.data.remote.KovoApi
import com.example.todayvolleyball.data.repository.VolleyballRepositoryImpl
import com.example.todayvolleyball.domain.repository.VolleyballRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideKovoApi(): KovoApi {
        return Retrofit.Builder()
            .baseUrl("https://user-api.kovo.co.kr/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(KovoApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): VolleyballDatabase {
        return Room.databaseBuilder(
            context,
            VolleyballDatabase::class.java,
            "volleyball.db"
        )
        .fallbackToDestructiveMigration(true)
        .build()
    }

    @Provides
    fun provideVolleyballDao(database: VolleyballDatabase): com.example.todayvolleyball.data.local.VolleyballDao {
        return database.volleyballDao()
    }

    @Provides
    @Singleton
    fun provideVolleyballRepository(
        api: KovoApi,
        db: VolleyballDatabase
    ): VolleyballRepository {
        return VolleyballRepositoryImpl(api, db.volleyballDao())
    }
    
    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): SettingsDataStore {
        return SettingsDataStore(context)
    }
}
