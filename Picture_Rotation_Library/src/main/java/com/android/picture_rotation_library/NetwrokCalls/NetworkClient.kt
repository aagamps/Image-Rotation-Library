package com.kredily.web.api

import com.android.picture_rotation_library.BuildConfig
import com.android.picture_rotation_library.Constants.Companion.APP_ID
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkClient {

    fun provideGson() : Gson = GsonBuilder().setLenient().create()

    private fun provideConverterFactory() : GsonConverterFactory = GsonConverterFactory.create(provideGson())

    private fun provideInterceptor() : Interceptor {
        return Interceptor { chain ->
            var request = chain.request()
            val requestBuilder = request.newBuilder()
            requestBuilder.addHeader(APP_ID, BuildConfig.LIBRARY_PACKAGE_NAME)
            requestBuilder.method(request.method(), request.body())
            request = requestBuilder.build()
            chain.proceed(request)
        }
    }

    private fun provideOkHttpClient() : OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if(BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        })
        .addInterceptor(provideInterceptor())
        .build()

//    private fun getRetrofit() : Retrofit = Retrofit.Builder()
//        .baseUrl(BuildConfig.BASE_URL)
//        .client(provideOkHttpClient())
//        .addConverterFactory(provideConverterFactory())
//        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//        .build()
//
//    fun getApiService() : KredilyApi = getRetrofit().create(KredilyApi::class.java)
}