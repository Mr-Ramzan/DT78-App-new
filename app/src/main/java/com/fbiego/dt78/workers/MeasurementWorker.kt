package com.fbiego.dt78.workers

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.concurrent.futures.CallbackToFutureAdapter.Completer
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.fbiego.dt78.app.DataListener
import com.fbiego.dt78.app.DataReceiver
import com.fbiego.dt78.app.ForegroundService
import com.fbiego.dt78.data.*
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
            val callback: SuccessFailCallback =
                object : SuccessFailCallback {
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


    fun measureAndUpload(callback: SuccessFailCallback) {
        try{
            DataReceiver.bindListener(this)
        }catch (e:Exception){
            e.printStackTrace()
            Log.w("Error Finding data","==================>Closing Worker")

        }
        ForegroundService().sendData(byteArrayOfInts(0xAB, 0x00, 0x04, 0xFF, 0x31, 0x0A, 0x00))

        try{
            appContext.runOnUiThread { Toast.makeText(appContext, "Passing measure command", Toast.LENGTH_LONG).show() }
            val foregroundService = ForegroundService()
            foregroundService.sendData(byteArrayOfInts(0xAB, 0x00, 0x04, 0xFF, 0x31, 0x0A, 0x01))

        }catch(e :Exception){
            e.printStackTrace()
            callback.onError()
            Log.w("Error Finding data","==================>Closing Worker")
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
                    val dataH  =HeartData(calendar.get(Calendar.YEAR) - 2000, calendar.get(Calendar.MONTH) + 1,
                        calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), bp)
                    dbHandler.insertHeart(dataH)
                    saveHeartData(dataH)
                }
            }
            if (data.getByte(5) == (0x12).toByte()) {
                val sp = data.getByte(6)!!.toPInt()
                if (sp != 0) {
                    val dataO  = OxygenData(calendar.get(Calendar.YEAR) - 2000, calendar.get(Calendar.MONTH) + 1,
                        calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), sp)
                    dbHandler.insertSp02(dataO)
                    saveOxygenData(dataO)
                }
            }

            if (data.getByte(5) == (0x22).toByte()) {
                val bph = data.getByte(6)!!.toPInt()
                val bpl = data.getByte(7)!!.toPInt()

                if (bph != 0) {
                     val dataP =  PressureData(calendar.get(Calendar.YEAR) - 2000, calendar.get(Calendar.MONTH) + 1,
                         calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), bph, bpl)
                    dbHandler.insertBp( dataP)
                    savePresureData(dataP)
                }
            }
        }


    }




    private fun saveHeartData(data :HeartData){
        val firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!
        FirebaseDatabase.getInstance().reference
            .child("fit_users")
            .child(firebaseUser.uid)
            .child("healthData")
            .push()
            .setValue(data)
            .addOnSuccessListener {
                appContext.runOnUiThread {
                    Toast.makeText(
                        appContext,
                        "user heart Data  saved",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }.addOnFailureListener {
                appContext.runOnUiThread {
                    Toast.makeText(appContext,"user health Data not saved",Toast.LENGTH_SHORT).show()
                }
            }
    }


    private fun savePresureData(data :PressureData ){
        val firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!
        FirebaseDatabase.getInstance().reference
            .child("fit_users")
            .child(firebaseUser.uid)
            .child("pressureData")
            .push()
            .setValue(data)
            .addOnSuccessListener {
                appContext.runOnUiThread {
                    Toast.makeText(
                        appContext,
                        "user heart Data  saved",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }.addOnFailureListener {
                appContext.runOnUiThread {
                    Toast.makeText(appContext,"user health Data not saved",Toast.LENGTH_SHORT).show()
                }
            }
    }


    private fun saveOxygenData(data :OxygenData){
        val firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!
        FirebaseDatabase.getInstance().reference
            .child("fit_users")
            .child(firebaseUser.uid)
            .child("oxygenData")
            .push()
            .setValue(data)
            .addOnSuccessListener {
                appContext.runOnUiThread {
                    Toast.makeText(
                        appContext,
                        "user heart Data  saved",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }.addOnFailureListener {
                appContext.runOnUiThread {
                    Toast.makeText(appContext,"user health Data not saved",Toast.LENGTH_SHORT).show()
                }
            }
    }

    companion object {
        var retryCount = 0
        var Key =0;
        lateinit var query : Query
    }

}


interface SuccessFailCallback {
    fun onSuccess()
    fun onError()
}
