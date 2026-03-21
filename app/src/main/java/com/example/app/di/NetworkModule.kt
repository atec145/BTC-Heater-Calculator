package com.example.app.di

import com.example.app.data.remote.api.AwattarApi
import com.example.app.data.remote.api.CoinGeckoApi
import com.example.app.data.remote.api.MempoolApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .build()

    @Provides
    @Singleton
    @Named("awattar")
    fun provideAwattarRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.awattar.at/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    @Named("mempool")
    fun provideMempoolRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://mempool.space/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    @Named("coingecko")
    fun provideCoinGeckoRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.coingecko.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideAwattarApi(@Named("awattar") retrofit: Retrofit): AwattarApi =
        retrofit.create(AwattarApi::class.java)

    @Provides
    @Singleton
    fun provideMempoolApi(@Named("mempool") retrofit: Retrofit): MempoolApi =
        retrofit.create(MempoolApi::class.java)

    @Provides
    @Singleton
    fun provideCoinGeckoApi(@Named("coingecko") retrofit: Retrofit): CoinGeckoApi =
        retrofit.create(CoinGeckoApi::class.java)
}
