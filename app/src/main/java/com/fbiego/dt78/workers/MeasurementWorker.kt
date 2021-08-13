package com.fbiego.dt78.workers

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.concurrent.futures.CallbackToFutureAdapter.Completer
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.fbiego.dt78.app.ForegroundService
import com.fbiego.dt78.data.MyDBHandler
import com.fbiego.dt78.data.byteArrayOfInts
import com.fbiego.dt78.model.HealthRate
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import org.jetbrains.anko.runOnUiThread

class RecipesListUpdateWorker(var appContext: Context, var workerParams: WorkerParameters) :
    ListenableWorker(appContext, workerParams) {
    override fun startWork(): ListenableFuture<Result?> {
        Key = workerParams.inputData.getString("key")!!.toInt()
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
            if(Key!=-1) {
                measureAndUpload(callback)
            }else{
                completer.setCancelled()
            }
            callback
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
                        val firebaseUser: FirebaseUser =
                            FirebaseAuth.getInstance().getCurrentUser()!!
//                        FirebaseDatabase.getInstance().reference
//                            .child("users")
//                            .child(firebaseUser.getUid())
//                            .child("health")
//                            .setValue(healthRate)
                        FirebaseDatabase.getInstance().getReference()
                            .child("User")
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
