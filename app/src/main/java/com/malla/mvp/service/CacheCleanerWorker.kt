package com.malla.mvp.service

import android.content.Context
import android.util.Log
import androidx.work.*
import coil.ImageLoader
import coil.request.ImageRequest
import com.malla.mvp.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class CacheCleanerWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getInstance(applicationContext)
            if (db != null) {
                // Eliminar mensajes de más de 30 días
                val threshold = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)
                db.messageDao().deleteMessagesOlderThan(threshold)
                Log.d("CacheCleaner", "Mensajes antiguos eliminados")
            }
            // Limpiar caché de Coil (si existe)
            val imageLoader = coil.Coil.imageLoader(applicationContext)
            imageLoader.diskCache?.clear()
            imageLoader.memoryCache?.clear()
            Log.d("CacheCleaner", "Caché de imágenes limpiada")
            Result.success()
        } catch (e: Exception) {
            Log.e("CacheCleaner", "Error en limpieza", e)
            Result.retry()
        }
    }

    companion object {
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()
            val request = PeriodicWorkRequestBuilder<CacheCleanerWorker>(24, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "cache_cleaner",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
