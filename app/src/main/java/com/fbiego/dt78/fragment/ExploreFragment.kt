package com.fbiego.dt78.fragment

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import com.fbiego.dt78.CameraActivity
import com.fbiego.dt78.R
import com.fbiego.dt78.app.ForegroundService
import com.fbiego.dt78.app.RootUtil
import com.fbiego.dt78.app.SettingsActivity
import com.fbiego.dt78.data.NotifyAdapter
import com.fbiego.dt78.data.Watch
import com.fbiego.dt78.databinding.FragmentExploreBinding
import timber.log.Timber


class ExploreFragment : Fragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentExploreBinding
    private lateinit var pref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        mBinding = FragmentExploreBinding.inflate(inflater, container, false)
        return mBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        setListeners()
    }

    private fun setListeners() {
        mBinding.testNotify.setOnClickListener(this)
        mBinding.findWatch.setOnClickListener(this)
        mBinding.shakeCamera.setOnClickListener(this)
        mBinding.shakeCamera.setOnLongClickListener {
            val cur = pref.getBoolean(SettingsActivity.PREF_CAMERA, false)
            pref.edit().putBoolean(SettingsActivity.PREF_CAMERA, !cur).apply()
            setCamera(!cur, RootUtil.isDeviceRooted)
            if (!cur && !RootUtil.isDeviceRooted) {
                Toast.makeText(requireContext(), R.string.not_rooted, Toast.LENGTH_SHORT).show()
                pref.edit().putBoolean(SettingsActivity.PREF_CAMERA, false).apply()
            }
            true
        }

        Timber.d("Main Activity onCreate ")


    }


    private fun setCamera(external: Boolean, rooted: Boolean) {
        if (external && rooted) {
            mBinding.cameraIcon.setImageResource(R.drawable.ic_camera_ext)
            mBinding.cameraText.setText(R.string.ext_camera)
        } else {
            mBinding.cameraIcon.setImageResource(R.drawable.ic_camera)
            mBinding.cameraText.setText(R.string.camera)
        }
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onClick(view: View?) {

        when (view?.id) {
            R.id.findWatch -> {
                if (ForegroundService().findWatch()) {
                    Toast.makeText(requireContext(), R.string.find_watch, Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(requireContext(), R.string.not_connect, Toast.LENGTH_SHORT).show()
                }
            }
            R.id.shakeCamera -> {
                if (pref.getBoolean(SettingsActivity.PREF_CAMERA, false) && RootUtil.isDeviceRooted) {
                    if (ForegroundService().shakeCamera()) {
                        //start camera
                        val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
                        this.startActivity(intent)
                    } else {
                        Toast.makeText(requireContext(), R.string.not_connect, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    if (startCamera()) {
                        startActivity(Intent(requireContext(), CameraActivity::class.javaObjectType))
                        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    }
                }
            }
            R.id.testNotify -> {
                testNotify()
            }
        }
    }



    private fun testNotify(){
        val builder = AlertDialog.Builder(requireContext())
        val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val tp = pref.getInt(SettingsActivity.PREF_WATCH_ID, -1)
        builder.setTitle(R.string.test_notification)
        builder.setMessage(R.string.test_notification_desc)
        val inflater = layoutInflater
        val dialogInflater = inflater.inflate(R.layout.notify_layout, null)
        val editText = dialogInflater.findViewById<EditText>(R.id.editText)
        val spinner = dialogInflater.findViewById<Spinner>(R.id.spinner)
        val adapter = NotifyAdapter(requireContext(), true, Watch(tp).iconSet)
        spinner.adapter = adapter

        builder.setView(dialogInflater)
        builder.setPositiveButton(R.string.send){ _, _ ->
            if (!ForegroundService().testNotification(
                            editText.text.toString(),
                            spinner.selectedItem as Int,
                            requireContext().applicationContext
                    )){
                Toast.makeText(requireContext(), R.string.not_connect, Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton(R.string.cancel){ _, _ ->

        }
        builder.setNeutralButton(R.string.self_test){ _, _ ->
            ForegroundService().selfTest(requireContext(), spinner.selectedItem as Int)
        }
        builder.show()

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
    private fun requestCameraPermissions(){
        ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.CAMERA), HomeFragment.PERMISSION_CAMERA
        )
    }
    private fun requestWriteStoragePermission(){
        ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), HomeFragment.PERMISSION_STORAGE
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


}