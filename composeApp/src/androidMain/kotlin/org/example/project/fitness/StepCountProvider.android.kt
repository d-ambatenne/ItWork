package org.example.project.fitness

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.DataPoint
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.data.Bucket
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.data.Value
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.result.DataReadResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit

actual class StepCountProvider(private val context: Context) {
    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .build()
    
    private val _dailyStepCount = MutableStateFlow<Int?>(null)
    actual val dailyStepCount: Flow<Int?> = _dailyStepCount.asStateFlow()
    
    private val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .addExtension(fitnessOptions)
        .build()
    
    private val googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)
    
    /**
     * Get the sign-in intent with fitness scopes.
     * This intent should be launched to sign in and request fitness permissions.
     */
    fun getSignInIntent(): android.content.Intent {
        return googleSignInClient.signInIntent
    }
    
    actual suspend fun requestPermission(): Boolean = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account != null && GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                refresh()
                true
            } else {
                // Permission not granted, user needs to sign in
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("StepCountProvider", "Error requesting permission", e)
            false
        }
    }
    
    actual suspend fun hasPermission(): Boolean = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            account != null && GoogleSignIn.hasPermissions(account, fitnessOptions)
        } catch (e: Exception) {
            false
        }
    }
    
    actual suspend fun refresh() = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null || !GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                _dailyStepCount.value = null
                return@withContext
            }
            
            // Get the start and end of the current day
            val calendar = Calendar.getInstance()
            val endTime = calendar.timeInMillis
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startTime = calendar.timeInMillis
            
            // Create a request to read step count data
            val readRequest = DataReadRequest.Builder()
                .aggregate(DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()
            
            // Read the data
            val response = Fitness.getHistoryClient(context, account)
                .readData(readRequest)
                .await()
            
            // Extract step count from response
            var totalSteps = 0L
            val buckets: List<Bucket> = response.buckets.toList()
            for (bucket in buckets) {
                val dataSets: List<DataSet> = bucket.dataSets.toList()
                for (dataSet in dataSets) {
                    val dataPoints: List<DataPoint> = dataSet.dataPoints.toList()
                    for (dataPoint in dataPoints) {
                        // For aggregated step count, get the value directly
                        try {
                            val stepField = Field.FIELD_STEPS
                            val value: Value = dataPoint.getValue(stepField)
                            totalSteps += value.asInt().toLong()
                        } catch (e: Exception) {
                            // Field doesn't exist or can't be read, skip
                            android.util.Log.d("StepCountProvider", "Could not read step field from data point", e)
                        }
                    }
                }
            }
            
            // If no aggregated data, try reading raw step count data
            if (totalSteps == 0L) {
                val rawReadRequest = DataReadRequest.Builder()
                    .read(DataType.TYPE_STEP_COUNT_DELTA)
                    .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                    .build()
                
                val rawResponse = Fitness.getHistoryClient(context, account)
                    .readData(rawReadRequest)
                    .await()
                
                val rawDataSets: List<DataSet> = rawResponse.dataSets.toList()
                for (dataSet in rawDataSets) {
                    val rawDataPoints: List<DataPoint> = dataSet.dataPoints.toList()
                    for (dataPoint in rawDataPoints) {
                        // For raw step count delta, get the value directly
                        try {
                            val stepField = Field.FIELD_STEPS
                            val value: Value = dataPoint.getValue(stepField)
                            totalSteps += value.asInt().toLong()
                        } catch (e: Exception) {
                            // Field doesn't exist or can't be read, skip
                            android.util.Log.d("StepCountProvider", "Could not read step field from raw data point", e)
                        }
                    }
                }
            }
            
            _dailyStepCount.value = totalSteps.toInt()
        } catch (e: Exception) {
            android.util.Log.e("StepCountProvider", "Error refreshing step count", e)
            _dailyStepCount.value = null
        }
    }
    
}

