package com.fbiego.dt78.workers

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.concurrent.futures.CallbackToFutureAdapter.Completer
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.fbiego.dt78.app.DataListener
import com.fbiego.dt78.app.ForegroundService
import com.fbiego.dt78.data.*
import com.fbiego.dt78.model.HealthRate
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import no.nordicsemi.android.ble.data.Data
import org.jetbrains.anko.runOnUiThread
import timber.log.Timber
import java.util.*

class RecipesListUpdateWorker(var appContext: Context, var workerParams: WorkerParameters) : DataListener
   , ListenableWorker(appContext, workerParams) {
    override fun startWork(): ListenableFuture<Result?> {
        retryCount = 0
        return CallbackToFutureAdapter.getFuture { completer: Completer<Result?> ->
            val callback: MonthlyReportsCallback =
                object : MonthlyReportsCallback {
                    override fun onSuccess() {
                        completer.set(Result.success())
                    }
                    override fun onError() {
                        if(retryCount <2){
                            completer.set(Result.retry())
                            retryCount +=1
                        }else{
                            completer.setCancelled()
                        }
                    }
                }
                measureAndUpload(callback)
            callback
        }
    }


    @SuppressLint("SetTextI18n")
    override fun onDataReceived(data: Data) {
        val dbHandler = MyDBHandler(appContext, null, null, 1)
        val calendar = Calendar.getInstance(Locale.getDefault())
        if (data.getByte(4) == (0x31).toByte()) {
            Timber.d("Type = ${data.getByte(5)!!.toPInt()} and value = ${data.getByte(6)!!.toPInt()}")
            if (data.getByte(5) == (0x0A).toByte()) {
                val bp = data.getByte(6)!!.toPInt()
                if (bp != 0) {
                    dbHandler.insertHeart(
                            HeartData(calendar.get(Calendar.YEAR) - 2000, calendar.get(Calendar.MONTH) + 1,
                                    calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), bp)
                    )
                }
            }
            if (data.getByte(5) == (0x12).toByte()) {
                val sp = data.getByte(6)!!.toPInt()
                if (sp != 0) {
                    dbHandler.insertSp02(
                            OxygenData(calendar.get(Calendar.YEAR) - 2000, calendar.get(Calendar.MONTH) + 1,
                                    calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), sp)
                    )
                }
            }

            if (data.getByte(5) == (0x22).toByte()) {
                val bph = data.getByte(6)!!.toPInt()
                val bpl = data.getByte(7)!!.toPInt()

                if (bph != 0) {

                    dbHandler.insertBp(
                            PressureData(calendar.get(Calendar.YEAR) - 2000, calendar.get(Calendar.MONTH) + 1,
                                    calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), bph, bpl)
                    )
                }
            }
        }


    }

    fun measureAndUpload(callback: MonthlyReportsCallback) {
        ForegroundService().sendData(byteArrayOfInts(0xAB, 0x00, 0x04, 0xFF, 0x31, 0x0A, 0x00))

        try{
           appContext.runOnUiThread {
                Toast.makeText(
                    appContext,
                    "Passing measure command",
                    Toast.LENGTH_LONG
                ).show()
            }
            val foregroundService = ForegroundService()
            foregroundService.sendData(byteArrayOfInts(0xAB, 0x00, 0x04, 0xFF, 0x31, 0x0A, 0x01))
            Handler(Looper.getMainLooper()).postDelayed(   {
                fun run() {
                    if (foregroundService.syncData()) {
                        appContext.runOnUiThread {
                            Toast.makeText(
                                appContext,
                                "Getting data from DB",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        val healthRate = HealthRate()
                        val dbHandler = MyDBHandler(
                            appContext, null, null, 1
                        )
                        healthRate.heartRate = (dbHandler.getHeartToday())
                        healthRate.bpHRate = (dbHandler.getBpToday()[0])
                        healthRate.bpLRate = (dbHandler.getBpToday()[1])
                        healthRate.spRate =(dbHandler.getSp02Today())
                        Log.w("Data from db","=====>${healthRate.heartRate}")
                        val firebaseUser: FirebaseUser =
                            FirebaseAuth.getInstance().getCurrentUser()!!
//                        FirebaseDatabase.getInstance().reference
//                            .child("users")
//                            .child(firebaseUser.getUid())
//                            .child("health")
//                            .setValue(healthRate)
                        FirebaseDatabase.getInstance().getReference()
                            .child("fit_users")
                            .child(firebaseUser.getUid())
                            .child("healthData")
                            .push()
                            .setValue(healthRate)
                            .addOnSuccessListener {
                                appContext.runOnUiThread {
                                    Toast.makeText(
                                        appContext,
                                        "user health Data not saved",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }.addOnFailureListener {
                                appContext.runOnUiThread {
                                Toast.makeText(appContext,"user health Data not saved",Toast.LENGTH_SHORT).show()
                            }
                            }

                    }
                }
            },15000)
        }catch(e :Exception){
            e.printStackTrace()
            callback.onError()
            Log.w("Error Finding data","==================>Closing Worker")
        }
    }







    companion object {
        var retryCount = 0
        var Key =0;
        lateinit var query : Query
    }


}


interface MonthlyReportsCallback {
    fun onSuccess()
    fun onError()
}
