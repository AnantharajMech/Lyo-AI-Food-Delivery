package com.example.data.repository

import android.content.Context
import android.os.Environment
import android.util.Log
import com.example.data.database.User
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File

object LyoLocalBackupHelper {
    private const val TAG = "LyoLocalBackupHelper"
    private const val FILE_NAME = ".lyo_accounts_backup.json"

    private fun getBackupFiles(context: Context): List<File> {
        val files = mutableListOf<File>()
        try {
            val internalDir = context.filesDir
            if (!internalDir.exists()) internalDir.mkdirs()
            files.add(File(internalDir, FILE_NAME))
        } catch (e: Exception) {
            Log.e(TAG, "Error getting internal files dir: ${e.message}")
        }
        return files.distinctBy { it.absolutePath }
    }

    fun backupUsers(users: List<User>, context: Context) {
        if (users.isEmpty()) return
        
        try {
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
            val type = Types.newParameterizedType(List::class.java, User::class.java)
            val adapter = moshi.adapter<List<User>>(type)
            val jsonString = adapter.toJson(users)
            
            val files = getBackupFiles(context)
            for (file in files) {
                try {
                    val parent = file.parentFile
                    if (parent != null && !parent.exists()) {
                        parent.mkdirs()
                    }
                    file.writeText(jsonString)
                    Log.d(TAG, "Users backed up successfully to: ${file.absolutePath}")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed backing up to ${file.absolutePath}: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed serialization or overall backup: ${e.message}")
        }
    }

    fun restoreUsers(context: Context): List<User> {
        val files = getBackupFiles(context)
        for (file in files) {
            if (file.exists()) {
                try {
                    val jsonString = file.readText()
                    if (jsonString.isNotBlank()) {
                        val moshi = Moshi.Builder()
                            .add(KotlinJsonAdapterFactory())
                            .build()
                        val type = Types.newParameterizedType(List::class.java, User::class.java)
                        val adapter = moshi.adapter<List<User>>(type)
                        val users = adapter.fromJson(jsonString)
                        if (users != null && users.isNotEmpty()) {
                            Log.d(TAG, "Restored ${users.size} users from backup: ${file.absolutePath}")
                            return users
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed restoring from ${file.absolutePath}: ${e.message}")
                }
            }
        }
        Log.d(TAG, "No valid user backup found to restore.")
        return emptyList()
    }
}
