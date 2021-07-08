package com.fbiego.dt78.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.MediaStore
import android.provider.Settings
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.db.williamchart.data.Scale
import com.db.williamchart.view.DonutChartView
import com.fbiego.dt78.*
import com.fbiego.dt78.app.*
import com.fbiego.dt78.data.*
import com.fbiego.dt78.databinding.FragmentMainHomeBinding
import no.nordicsemi.android.ble.data.Data
import org.jetbrains.anko.support.v4.runOnUiThread
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() ,ConnectionListener,View.OnClickListener {
    private lateinit var mBinding  : FragmentMainHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mBinding = FragmentMainHomeBinding.inflate(layoutInflater,container,false)
        return mBinding.root
    }






        private lateinit var menu: Menu
        private var alertDialog: AlertDialog? = null

        private lateinit var timer: Timer
        private val noDelay = 500L
        private val duration = 1000L * 30
        private lateinit var btAdapter: BluetoothAdapter

        private val REQUEST_ENABLE_BT = 37
        private lateinit var pref: SharedPreferences



        private var sleepList = ArrayList<SleepData>()
        private var sleep = ArrayList<SleepData>()
        private var daysList = ArrayList<String>()
        private var daily = ArrayList<SleepData>()
        private var current = 0
        private var maxDay = 0
        private var start = 0
        private var end = 0




        companion object{

            const val PERMISSIONS_CONTACTS = 100
            const val PERMISSION_CONTACT = 101
            const val PERMISSION_SMS = 42
            const val PERMISSION_CALL = 52
            const val PERMISSION_CALL_LOG = 54
            const val PERMISSION_STORAGE = 57
            const val PERMISSION_CAMERA = 58

            var target = 1000


            var bat: TextView? = null
            var watch: TextView? = null
            var bt: ImageView? = null
            var per: ImageView? = null
            var chrg: ImageView? = null
            var step: TextView? = null
            var cal: TextView? = null
            var dis: TextView? = null
            var progress: TextView? = null
            var donut: DonutChartView? = null
            var contxt: Context? = null


        }

        private fun barChart(){
            val dbHandler = MyDBHandler(requireContext(), null, null, 1)
            val todaySteps = dbHandler.getStepsToday()
            var max = 2000
            val data = ArrayList<Pair<String, Float>>()

            todaySteps.forEach {
                data.add(Pair("",
                        (it.steps).toFloat(),))
                if (it.steps > max){
                    max = it.steps
                }
            }
//        val mChart = findViewById<ChartProgressBar>(R.id.ChartProgressBar)
//        mChart.setMaxValue(max.toFloat())
//        mChart.setDataList(dataList)
//        mChart.build()

            mBinding.barChart.fillColor = requireContext().getColorFromAttr(R.attr.colorIcons)
            //mBinding.barChart.gradientFillColors = intArrayOf(this.getColorFromAttr(R.attr.colorIcons), ContextCompat.getColor(this, R.color.colorTransparent))
            mBinding.barChart .scale = Scale((max * -0.03).toFloat(), max.toFloat()+500)
            //mBinding.barChart.labelsFormatter = { "${it.roundToInt()}" }
            mBinding.barChart

        }

        private fun updateDonut(context: Context, current: Int, target: Int, update: Boolean){
            val plot = ((current.toFloat()/target)*100)
            val donutData = arrayListOf(plot)
            //donutChart.
            Timber.w("Current: $current, Plot: $plot")
            progress?.text = "${plot.toInt()}%"
            donut?.donutColors = intArrayOf(ContextCompat.getColor(requireContext() ,R.color.primaryColor))
            if (update){
                donut?.show(donutData)
            } else {
                donut?.animate(donutData)
            }




        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

            pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

//            changeNavigationButtonBg()
            btAdapter = BluetoothAdapter.getDefaultAdapter()
            if (!btAdapter.isEnabled){
                val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT)
            }

            val theme = pref.getInt(SettingsActivity.PREF_THEME, AppCompatDelegate.MODE_NIGHT_NO)

            if (theme != AppCompatDelegate.getDefaultNightMode()){
                AppCompatDelegate.setDefaultNightMode(theme)
            }

            bat = view.findViewById(R.id.battery)
            watch = view.findViewById(R.id.watchName)
            bt = view.findViewById(R.id.connect)
            per = view.findViewById(R.id.batIcon)
            step = view.findViewById(R.id.stepsText)
            chrg = view.findViewById(R.id.charging)
            cal = view.findViewById(R.id.caloriesText)
            dis = view.findViewById(R.id.distanceText)
            progress = view.findViewById(R.id.targetSteps)
            donut = view.findViewById(R.id.donutChart)
            contxt = requireContext()
            setListeners()
            ConnectionReceiver.bindListener(this)


            Timber.d("Main Activity onCreate ")



        }


        private fun appsList(){

            val enabled = NotificationManagerCompat.getEnabledListenerPackages(requireContext()).contains(
                    BuildConfig.APPLICATION_ID
            )
            Timber.d("Notification Listener Enabled $enabled")

            if (alertDialog == null || !(alertDialog!!.isShowing)) {
                if (enabled) {

                    startActivity(Intent(requireContext(), AppsActivity::class.java))
                    requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

                } else {
                    val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
                            .setTitle(R.string.choose_app)
                            .setMessage(R.string.grant_notification)
                            .setNegativeButton(android.R.string.no, null)
                            .setPositiveButton(android.R.string.yes) { _: DialogInterface?, _: Int ->
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                                    startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                                } else {
                                    startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                                }
                            }
                            .setOnDismissListener { alertDialog = null }
                            .setOnCancelListener { alertDialog = null }
                    alertDialog = builder.create()
                    alertDialog!!.show()

                }

            }
        }

        public fun showDialog(context: Context){
            val alert = AlertDialog.Builder(context)
            alert.setTitle(context.getString(R.string.self_test_fail))
            alert.setMessage(context.getString(R.string.re_enable))
            alert.setPositiveButton(android.R.string.yes) { _: DialogInterface?, _: Int ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                } else {
                    context.startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                }
            }
            alert.setNegativeButton(android.R.string.no, null)
            alert.show()
        }

//        override fun onCreateOptionsMenu(menu: Menu): Boolean {
//            menuInflater.inflate(R.menu.main_menu, menu)
//            this.menu = menu
//            return super.onCreateOptionsMenu(menu)
//        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.menu_item_prefs -> {
                    startActivity(Intent(requireContext(), SettingsActivity::class.java))
                    requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    true
                }
                R.id.menu_item_kill -> {
                    ConnectionReceiver().notifyStatus(false)
                    Toast.makeText(requireContext(), R.string.stop_service, Toast.LENGTH_SHORT).show()
                    requireActivity().stopService(Intent(requireContext(), ForegroundService::class.java))
                    item.isVisible = false
                    menu.findItem(R.id.menu_item_start)?.isVisible = true
                    true
                }
                R.id.menu_item_start -> {
                    item.isVisible = false
                    menu.findItem(R.id.menu_item_kill)?.isVisible = true

                    val remoteMacAddress = pref.getString(
                            SettingsActivity.PREF_KEY_REMOTE_MAC_ADDRESS,
                            ForegroundService.VESPA_DEVICE_ADDRESS
                    )
                    //val id = pref.getInt(ST.PREF_WATCH_ID, UNKNOWN)
                    if (btAdapter.isEnabled) {
                        if (remoteMacAddress != ForegroundService.VESPA_DEVICE_ADDRESS) {

//                        if (isConnected(btAdapter.getRemoteDevice(remoteMacAddress)) && id == UNKNOWN){
//                            Toast.makeText(this, "Device already connected", Toast.LENGTH_SHORT).show()
//                            deviceConnected()
//                        } else {
//                            Toast.makeText(this, R.string.start_service, Toast.LENGTH_SHORT).show()
//                            startService(Intent(this, FG::class.java))
//                        }

                            Toast.makeText(requireContext(), R.string.start_service, Toast.LENGTH_SHORT).show()
                            requireContext().startService(Intent(requireContext(), ForegroundService::class.java))
                        }

                    } else {
                        //enable bt
                        val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT)
                    }
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }

        }


        override fun onStart() {
            super.onStart()

            Timber.d("Mainactivity on start")
            val remoteMacAddress = pref.getString(
                    SettingsActivity.PREF_KEY_REMOTE_MAC_ADDRESS,
                    ForegroundService.VESPA_DEVICE_ADDRESS
            )
            val id = pref.getInt(SettingsActivity.PREF_WATCH_ID, UNKNOWN)
            if (btAdapter.isEnabled){

                if (remoteMacAddress != ForegroundService.VESPA_DEVICE_ADDRESS){
//                if (isConnected(btAdapter.getRemoteDevice(remoteMacAddress)) && id == UNKNOWN){
//                    Toast.makeText(this, "Device already connected", Toast.LENGTH_SHORT).show()
//                    deviceConnected()
//                } else {
//                    startService(Intent(this, FG::class.java))
//                }
                    requireContext().startService(Intent(requireContext(), ForegroundService::class.java))

                }

            }
            if (requireActivity().intent.hasExtra("RING")){

                Timber.w("Stopping service due to ring")
                requireActivity().stopService(Intent(requireContext(), ForegroundService::class.java))
                requireActivity().intent.removeExtra("RING")
            }

            Timber.w("onStart")
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, result: Intent?) {
            super.onActivityResult(requestCode, resultCode, result)
            if (requestCode == REQUEST_ENABLE_BT) {  // Match the request code
                if (resultCode == Activity.RESULT_OK) {
                    val remoteMacAddress = pref.getString(
                            SettingsActivity.PREF_KEY_REMOTE_MAC_ADDRESS,
                            ForegroundService.VESPA_DEVICE_ADDRESS
                    )
                    //val id = pref.getInt(ST.PREF_WATCH_ID, UNKNOWN)
                    if (remoteMacAddress != ForegroundService.VESPA_DEVICE_ADDRESS){
                        //Toast.makeText(this, "Bluetooth Turned on", Toast.LENGTH_LONG).show()
//                    if (isConnected(btAdapter.getRemoteDevice(remoteMacAddress)) && id == UNKNOWN){
//                        Toast.makeText(this, "Device already connected", Toast.LENGTH_SHORT).show()
//                        deviceConnected()
//                    } else {
//                        startService(Intent(this, FG::class.java))
//                    }
                        requireActivity().startService(Intent(requireContext(), ForegroundService::class.java))
                    }
                }
            }
        }

        @SuppressLint("SetTextI18n")
        @RequiresApi(Build.VERSION_CODES.M)
        override fun onResume() {
            super.onResume()
            Timber.d("onResume")

            setIcon(ForegroundService.connected)


            if (ForegroundService.camera){
                ForegroundService().shakeCamera()
            }

            if (ForegroundService.connected){
                watch?.text = ForegroundService.deviceName
            }
            checkPermission()
            checkOptimization()
            val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
            val tp = pref.getInt(SettingsActivity.PREF_WATCH_ID, -1)
//            title = Watch(tp).name
            checkEnabled(tp)
            ForegroundService.lst_sync = pref.getLong(SettingsActivity.PREF_SYNC, System.currentTimeMillis() - 604800000)
            if (System.currentTimeMillis() > ForegroundService.lst_sync + (3600000 * 3) && tp != ESP32 && tp != UNKNOWN){
                if (ForegroundService().syncData()){
                    Toast.makeText(requireContext(), R.string.sync_watch, Toast.LENGTH_SHORT).show()
                    val editor: SharedPreferences.Editor = pref.edit()
                    val time = System.currentTimeMillis()
                    editor.putLong(SettingsActivity.PREF_SYNC, time)
                    editor.apply()
                    editor.commit()
                }
//            else  {
//                Toast.makeText(this, R.string.unable_sync, Toast.LENGTH_SHORT).show()
//            }

            }
//            setCamera(pref.getBoolean(SettingsActivity.PREF_CAMERA, false), RootUtil.isDeviceRooted)

            bat?.text = "${ForegroundService.bat}%"
            watch?.text = ForegroundService.deviceName
            per?.setImageResource(battery(ForegroundService.bat, 1))
            per?.imageTintList = ColorStateList.valueOf(color(ForegroundService.bat, requireContext()))



            mBinding. appsNo.text = MainApplication.sharedPrefs.getStringSet(
                    MainApplication.PREFS_KEY_ALLOWED_PACKAGES,
                    mutableSetOf()
            )?.size.toString()


            val dbHandler = MyDBHandler(requireContext(), null, null, 1)
            target = dbHandler.getUser().target
            val stepsCal = dbHandler.getStepCalToday()
            val stepSize = dbHandler.getUser().step

            updateDash(dbHandler.getHeartToday(), dbHandler.getBpToday()[0], dbHandler.getBpToday()[1], dbHandler.getSp02Today())

            step?.text = stepsCal.steps.toString()
            cal?.text = "${stepsCal.calories} "+this.resources.getString(R.string.kcal)
            dis?.text = distance(stepsCal.steps * stepSize, ForegroundService.unit != 0, requireContext())

            updateDonut(requireContext(), stepsCal.steps, target, false)

            barChart()



            val timerTask = object: TimerTask(){
                override fun run() {
                    if (tp != ESP32 && tp != UNKNOWN) {
                        ForegroundService().stepRQ()
                        Timber.w("Timer Task: Steps requested")
                    }

                }
            }


            dailySleep()
            val cal = Calendar.getInstance(Locale.getDefault())
            val today = String.format(
                    "%02d-%02d-%04d", cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(
                    Calendar.YEAR
            )
            )
            Timber.w(today)
            sleepList.clear()
            sleepList = getSleepDay(today)

//        sleepList.add(SleepData(21, 2, 11, 1, 5, 1, 69))
//        sleepList.add(SleepData(21, 2, 11, 5, 5, 2, 354))

            mBinding.textSleep.text = if (sleepList.isNotEmpty()){
                val lightS = sleepList.sumBy { if (it.type == 1) it.duration else 0 }
                val deepS = sleepList.sumBy { if (it.type == 2) it.duration else 0 }

                mBinding. sleepDonut.donutColors= intArrayOf(requireContext().getColorFromAttr(R.attr.colorButtonEnabled), requireContext().getColorFromAttr(R.attr.colorIcons))
                mBinding. sleepDonut.animate(arrayListOf(((lightS.toFloat()/(lightS+deepS))*100), ((deepS.toFloat()/(lightS+deepS))*100)))
                time(lightS + deepS) +"\n"+this.getString(R.string.sleep_txt)
            } else {
                mBinding. sleepDonut.donutColors= intArrayOf(requireContext().getColorFromAttr(R.attr.colorButtonEnabled), requireContext().getColorFromAttr(R.attr.colorIcons))
                mBinding. sleepDonut.animate(arrayListOf(0f, 0f))
                "0h 0m\n"+this.getString(R.string.sleep_txt)
            }

            mBinding.quietActive.visibility = if (isQuietA(dbHandler.getSet(2))) View.VISIBLE else View.GONE

//        if (::menu.isInitialized){
//            menu.findItem(R.id.menu_item_kill)?.isVisible = FG.serviceRunning
//            menu.findItem(R.id.menu_item_start)?.isVisible = !FG.serviceRunning
//        }


            timer = Timer()
            timer.schedule(timerTask, noDelay, duration)
        }


        private fun updateDash(hrm: Int, bpH: Int, bpL: Int, sp02: Int){
            mBinding.textHrm.text = "$hrm\n"+this.getString(R.string.bpm)
            mBinding.textBp.text = "$bpH/$bpL\n"+this.getString(R.string.mmHg)
            mBinding. textSp.text = "$sp02%\nO²"

            mBinding. hrmDonut.donutColors = intArrayOf(requireContext().getColorFromAttr(R.attr.colorIcons))
            mBinding. hrmDonut.animate(arrayListOf(map(hrm, 40, 100).toFloat()))
            mBinding. bpDonut.donutColors = intArrayOf(requireContext().getColorFromAttr(R.attr.colorIcons), requireContext().getColorFromAttr(R.attr.colorButtonEnabled))
            mBinding. bpDonut.animate(arrayListOf((bpH.toFloat()/(bpH+bpL)*100), (bpL.toFloat()/(bpH+bpL)*100)))
            mBinding. spDonut.donutColors = intArrayOf(requireContext().getColorFromAttr(R.attr.colorIcons))
            mBinding.  spDonut.animate(arrayListOf(map(sp02, 80, 100).toFloat()))

        }


        override fun onPause() {
            super.onPause()

            timer.cancel()
            timer.purge()
        }

//    override fun onDestroy() {
//        super.onDestroy()
//        // stop the service
//
////        val isRunAsAService = PreferenceManager.getDefaultSharedPreferences(this)
////                .getBoolean(ST.PREF_KEY_RUN_AS_A_SERVICE, true)
////        Timber.w("onDestroy {isService=$isRunAsAService}")
////        if (!isRunAsAService) {
////            stopService(Intent(this, FG::class.java))
////        }
//    }


        private fun setListeners(){



            mBinding. notificationApps.setOnClickListener(this)
            mBinding. layoutSteps.setOnClickListener(this)
            mBinding. barChart.setOnClickListener(this)
            mBinding. settings.setOnClickListener(this)
            mBinding. cardInfo.setOnClickListener(this)
            mBinding. layoutSteps.setOnClickListener(this)
            mBinding. hrmDonut.setOnClickListener(this)
            mBinding. bpDonut.setOnClickListener(this)
            mBinding. spDonut.setOnClickListener(this)
            mBinding. reminder.setOnClickListener(this)

            mBinding.  sleepDonut.setOnClickListener(this)

        }


        private fun checkEnabled(id: Int){
            if (id == ESP32){
                mBinding. cardInfo.isClickable = false
                mBinding. layoutSteps.isClickable = false
                mBinding. hrmDonut.isClickable = false
                mBinding. bpDonut.isClickable = false
                mBinding. spDonut.isClickable = false
                mBinding. reminder.isClickable = false
                mBinding.  sleepDonut.isClickable = false
                mBinding.  layoutSteps.backgroundTintList = ColorStateList.valueOf(requireContext().getColorFromAttr(R.attr.colorCardBackgroundDark))
                mBinding.  linearLayout.backgroundTintList = ColorStateList.valueOf(requireContext().getColorFromAttr(R.attr.colorCardBackgroundDark))
                mBinding. reminder.backgroundTintList = ColorStateList.valueOf(requireContext().getColorFromAttr(R.attr.colorCardBackgroundDark))

            } else {
                mBinding.   cardInfo.isClickable = true
                mBinding.  layoutSteps.isClickable = true
                mBinding. hrmDonut.isClickable = true
                mBinding.  bpDonut.isClickable = true
                mBinding. spDonut.isClickable = true
                mBinding.  reminder.isClickable = true

                mBinding.  sleepDonut.isClickable = true
                // layoutSteps.setCardBackgroundColor(ContextCompat.getColor(this, R.color.colorCardBackground))
                //  linearLayout.setCardBackgroundColor(ContextCompat.getColor(this, R.color.colorCardBackground))
                mBinding.  reminder.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorCardBackground))

            }
        }







        @SuppressLint("SetTextI18n")
        fun onDataReceived(data: Data, context: Context, stepsZ: Int){


            run {}

            Timber.w("Data received")
            if (data.size() == 8) {
                if (data.getByte(4) == (0x91).toByte()) {
                    ForegroundService.bat = data.getByte(7)!!.toPInt()
                    val chg = (data.getByte(6)!!.toPInt() == 1)

                    if (chg){
                        chrg?.visibility = View.VISIBLE
                    } else {
                        chrg?.visibility = View.GONE
                    }
                    Timber.w("Battery: ${ForegroundService.bat}%")
                    bat?.text = "${ForegroundService.bat}%"
                    watch?.text = ForegroundService.deviceName
                    per?.setImageResource(battery(ForegroundService.bat, 1))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        per?.imageTintList = ColorStateList.valueOf(color(ForegroundService.bat, context))
                    }
                }
            }

            if (data.size() == 17 && data.getByte(4) == (0x51).toByte() && data.getByte(5) == (0x08).toByte()) {
                val steps = ((data.getByte(7)!!.toPInt() * 256) + (data.getByte(8)!!).toPInt())
                step?.text = steps.toString()
                val cl = (((data.getByte(10)!!).toPInt() * 256) + (data.getByte(11)!!).toPInt())
                cal?.text = "$cl " + context.resources.getString(R.string.kcal)
                dis?.text = distance(steps * stepsZ, ForegroundService.unit != 0, context)

                //updateDonut(this@MainActivity, steps, target, true)

            }



        }

        override fun onConnectionChanged(state: Boolean) {

            runOnUiThread{
                ForegroundService.connected = state
                setIcon(ForegroundService.connected)
                if (ForegroundService.connected){
                    watch?.text = ForegroundService.deviceName
                } else {
                    chrg?.visibility = View.GONE
                }


                val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
                val id = pref.getInt(SettingsActivity.PREF_WATCH_ID, -1)
                ForegroundService.lst_sync = pref.getLong(SettingsActivity.PREF_SYNC, System.currentTimeMillis() - 604800000)
                if (System.currentTimeMillis() > ForegroundService.lst_sync + (3600000 * 3) && id != ESP32 ){
                    if (ForegroundService().syncData()){
                        Toast.makeText(requireContext(), R.string.sync_watch, Toast.LENGTH_SHORT).show()
                        val editor: SharedPreferences.Editor = pref.edit()
                        val time = System.currentTimeMillis()
                        editor.putLong(SettingsActivity.PREF_SYNC, time)
                        editor.apply()
                        editor.commit()
                    }
                }
            }


        }

    override fun onClick(view: View?) {

            when (view?.id) {
                R.id.cardInfo -> {
                    if (ForegroundService().syncData()) {
                        Toast.makeText(requireContext(), R.string.sync_watch, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), R.string.not_connect, Toast.LENGTH_SHORT).show()
                    }
                }
                R.id.hrmDonut -> {
                    HealthActivity.viewH = 0
                    startActivity(Intent(requireContext(), HealthActivity::class.javaObjectType))
                    requireActivity(). overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
                R.id.bpDonut -> {
                    HealthActivity.viewH = 1
                    startActivity(Intent(requireContext(), HealthActivity::class.javaObjectType))
                    requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
                R.id.spDonut -> {
                    HealthActivity.viewH = 2
                    startActivity(Intent(requireContext(), HealthActivity::class.javaObjectType))
                    requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
                R.id.notificationApps -> {
                    appsList()
                }
                R.id.reminder -> {
                    startActivity(Intent(requireContext(), ReminderActivity::class.javaObjectType))
                    requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
                R.id.layoutSteps  -> {
                    startActivity(Intent(requireContext(), StepsActivity::class.javaObjectType))
                    requireActivity(). overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
                R.id.barChart  -> {
                    startActivity(Intent(requireContext(), StepsActivity::class.javaObjectType))
                    requireActivity().  overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
                R.id.sleepDonut -> {
                    startActivity(Intent(requireContext(), SleepActivity::class.javaObjectType))
                    requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
                R.id.userInfo -> {
                    startActivity(Intent(requireContext(), UserFragment::class.javaObjectType))
                    requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
                R.id.settings -> {
                    startActivity(Intent(requireContext(), SettingsWatchActivity::class.javaObjectType))
                    requireActivity() .overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }


            }


        }

        private fun setIcon(state: Boolean){
            if (state){
                bt?.setImageResource(R.drawable.ic_bt)
                bt?.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.colorBluetooth))
            } else {
                bt?.setImageResource(R.drawable.ic_disc)
                bt?.imageTintList = ColorStateList.valueOf(Color.DKGRAY)
                per?.imageTintList = ColorStateList.valueOf(Color.DKGRAY)
            }
        }

//    private fun deviceConnected(){
//        val builder = AlertDialog.Builder(this)
//        builder.setTitle("First time connect")
//        builder.setMessage("It is recommended to disconnect the device from bluetooth settings first before connecting the app")
//        builder.setPositiveButton(R.string.bt_settings){_, _ ->
//            val intentOpenBluetoothSettings = Intent()
//            intentOpenBluetoothSettings.action = Settings.ACTION_BLUETOOTH_SETTINGS
//            startActivity(intentOpenBluetoothSettings)
//        }
//        builder.setNegativeButton(R.string.cancel, null)
//        builder.show()
//    }

        @SuppressLint("BatteryLife")
        private fun checkOptimization(){
            val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
            val optimize = pref.getBoolean(SettingsActivity.PREF_OPTIMIZE, true)


            val packageName = requireActivity().packageName
            val pm = requireActivity().getSystemService(Context.POWER_SERVICE) as PowerManager

            if (optimize && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !pm.isIgnoringBatteryOptimizations(
                            packageName
                    )){
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle(R.string.ignore_optimize)
                builder.setMessage(R.string.request_optimize)
                val editor: SharedPreferences.Editor = pref.edit()
                builder.setNeutralButton(R.string.never_ask){ _, _ ->
                    editor.putBoolean(SettingsActivity.PREF_OPTIMIZE, false)
                    editor.apply()
                    editor.commit()
                }
                builder.setNegativeButton(android.R.string.no, null)
                builder.setPositiveButton(R.string.yes){ _, _ ->
                    val intent = Intent()
                    intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                }
                builder.show()
            }
        }

//    private fun isConnected(device: BluetoothDevice): Boolean {
//        return try {
//            val m: Method = device.javaClass.getMethod("isConnected")
//            m.invoke(device) as Boolean
//        } catch (e: Exception) {
//            throw IllegalStateException(e)
//        }
//    }
//
//    private fun getProfiles(device: BluetoothDevice){
//        val manager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
//        val connectedDevices = manager.getConnectedDevices(BluetoothProfile.GATT)
//        connectedDevices.contains(device)
//    }

        private fun checkPermission(){
            val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
            val remoteMacAddress = pref.getString(
                    SettingsActivity.PREF_KEY_REMOTE_MAC_ADDRESS,
                    ForegroundService.VESPA_DEVICE_ADDRESS
            )
            val later = pref.getBoolean("later", false)
            if (remoteMacAddress == "00:00:00:00:00:00" && !later){
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle(R.string.mac_addr)
                builder.setMessage(R.string.setup_desc)
                val editor: SharedPreferences.Editor = pref.edit()
                builder.setNegativeButton(R.string.later){ _, _ ->
                    editor.putBoolean("later", true)
                    editor.apply()
                    editor.commit()
                }
                builder.setPositiveButton(R.string.setup_now){ _, _ ->
                    startActivity(Intent(requireActivity(), SettingsActivity::class.java))
                    requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
                builder.show()
            }



        }

        //sleep
        private fun dailySleep(){
            val dbHandler = MyDBHandler(requireContext(), null, null, 1)
            val slp = dbHandler.getSleepData()
            val current = dbHandler.getSet(1)
            start = current[1]*100 + current[2]
            end = current[3]*100 +  current[4]


            sleep.clear()
            sleep = slp

            val qr = ArrayList<String>()

            slp.forEach {

                if (start > end) {
                    if (it.hour * 100 + it.minute >= start || it.hour * 100 + it.minute <= end) {

                        if (it.hour * 100 + it.minute >= start){
                            val cal = Calendar.getInstance(Locale.getDefault())
                            cal.set(Calendar.DAY_OF_MONTH, it.day)
                            cal.set(Calendar.MONTH, it.month - 1)
                            cal.set(Calendar.YEAR, it.year + 2000)
                            cal.add(Calendar.DATE, 1)
                            val el = String.format(
                                    "%02d-%02d-20%02d", cal.get(Calendar.DAY_OF_MONTH), cal.get(
                                    Calendar.MONTH
                            ) + 1, cal.get(Calendar.YEAR) - 2000
                            )
                            if (!qr.contains(el)){
                                qr.add(el)
                            }
                        } else {
                            val el = String.format("%02d-%02d-20%02d", it.day, it.month, it.year)
                            if (!qr.contains(el)){
                                qr.add(el)
                            }
                        }
                    }
                } else {
                    if (it.hour * 100 + it.minute in start..end){
                        val el = String.format("%02d-%02d-20%02d", it.day, it.month, it.year)
                        if (!qr.contains(el)){
                            qr.add(el)
                        }
                    }
                }
            }

            daysList.clear()
            daysList = qr

        }

        private fun getSleepDay(day: String): ArrayList<SleepData>{
            val today = ArrayList<SleepData>()
            if (sleep.isNotEmpty()){
                sleep.forEach {
                    if (start > end) {
                        if (it.hour * 100 + it.minute >= start || it.hour * 100 + it.minute <= end) {

                            if (it.hour * 100 + it.minute >= start){
                                val cal = Calendar.getInstance(Locale.getDefault())
                                cal.set(Calendar.DAY_OF_MONTH, it.day)
                                cal.set(Calendar.MONTH, it.month - 1)
                                cal.set(Calendar.YEAR, it.year + 2000)
                                cal.add(Calendar.DATE, 1)
                                val date = String.format(
                                        "%02d-%02d-20%02d", cal.get(Calendar.DAY_OF_MONTH), cal.get(
                                        Calendar.MONTH
                                ) + 1, cal.get(Calendar.YEAR) - 2000
                                )
                                if (day == date){
                                    today.add(it)
                                }
                            } else {
                                val date = String.format("%02d-%02d-20%02d", it.day, it.month, it.year)
                                if (day == date){
                                    today.add(it)
                                }
                            }
                        }
                    } else {
                        if (it.hour * 100 + it.minute in start..end){
                            val date = String.format("%02d-%02d-20%02d", it.day, it.month, it.year)
                            if (day == date){
                                today.add(it)
                            }
                        }
                    }
                }
            }
            return SleepActivity().parseData(today)
        }

        private fun requestCameraPermissions(){
            ActivityCompat.requestPermissions(requireActivity(),
                    arrayOf(Manifest.permission.CAMERA), PERMISSION_CAMERA
            )
        }

        private fun requestWriteStoragePermission(){
            ActivityCompat.requestPermissions(requireActivity(),
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_STORAGE
            )
        }

        private fun checkStoragePermission(): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ActivityCompat.checkSelfPermission( requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
            return true
        }

        private fun checkCameraPermission(): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ActivityCompat.checkSelfPermission( requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
            return true
        }

        private fun startCamera(): Boolean{
            return if (!checkCameraPermission() && !checkStoragePermission()){
                ActivityCompat.requestPermissions(requireActivity(),
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA), 60
                )
                false
            } else if (!checkStoragePermission()){
                requestWriteStoragePermission()
                false
            } else if (!checkCameraPermission()){
                requestCameraPermissions()
                false
            } else {
                true
            }
        }

    }


