package com.fbiego.dt78.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fbiego.dt78.R
import com.fbiego.dt78.databinding.FragmentExploreBinding


class ExploreFragment : Fragment() {

    private lateinit var mBinding  : FragmentExploreBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mBinding = FragmentExploreBinding.inflate(inflater,container,false)
        return mBinding.root
    }






}