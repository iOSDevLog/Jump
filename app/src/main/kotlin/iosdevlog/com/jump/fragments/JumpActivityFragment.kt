package iosdevlog.com.jump.fragments

import android.support.v4.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import iosdevlog.com.jump.R

/**
 * A placeholder fragment containing a simple view.
 */
class JumpActivityFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_jump, container, false)
    }
}
