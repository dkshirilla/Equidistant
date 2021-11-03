package edu.fsu.equidistant.fragments

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import edu.fsu.equidistant.R
import edu.fsu.equidistant.databinding.FragmentJoingroupBinding


class JoinGroupFragment : Fragment(R.layout.fragment_joingroup){

    private val args: JoinGroupFragmentArgs by navArgs()

    private var user: FirebaseUser? = null
    private var binding: FragmentJoingroupBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        binding = FragmentJoingroupBinding.bind(view)
        binding!!.apply {

            btnHome.setOnClickListener {
                val action = JoinGroupFragmentDirections.actionJoinGroupFragmentToHomeFragment(args.userId,args.email)
                findNavController().navigate(action)
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.option_logout) {
            FirebaseAuth.getInstance().signOut()

            val action = HomeFragmentDirections.actionHomeFragmentToLoginFragment()
            findNavController().navigate(action)


        } else if (id == R.id.option_editProfile) {
            val action = HomeFragmentDirections.actionHomeFragmentToProfileFragment(args.userId)
            findNavController().navigate(action)
        }
        else if (id == R.id.option_home) {
            //val action = CreateGroupFragment.actioncreateGroupFragmentToHomeFragment(args.userId)
            val action = JoinGroupFragmentDirections.actionJoinGroupFragmentToHomeFragment(args.userId,args.email)
            findNavController().navigate(action)
        }
        return super.onOptionsItemSelected(item)

    }
}