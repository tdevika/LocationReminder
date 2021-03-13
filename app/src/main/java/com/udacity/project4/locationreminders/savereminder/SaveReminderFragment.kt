package com.udacity.project4.locationreminders.savereminder

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnFailureListener
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceHelper
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject


class SaveReminderFragment : BaseFragment() {

    //Get the view model this time as a single to be shared with the another fragment
    override val viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var geofenceHelper: GeofenceHelper
    private val GEOFENCE_RADIUS = 1000f


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)
        setDisplayHomeAsUpEnabled(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }
        geofenceHelper = GeofenceHelper(requireContext())
        geofencingClient = LocationServices.getGeofencingClient(requireContext())
        binding.saveReminder.setOnClickListener {
            onSaveReminder(view)
        }
        viewModel.navigateToReminderList.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it) {
                view.findNavController()
                    .navigate(R.id.action_saveReminderFragment_to_reminderListFragment)
                viewModel.navigateToReminderList()
            }
        })
    }

    private fun onSaveReminder(view: View) {
        val title = viewModel.reminderTitle.value
        val description = viewModel.reminderDescription.value
        val location = viewModel.reminderSelectedLocationStr.value
        val latitude = viewModel.selectedPOI.value?.latLng?.latitude
        val longitude = viewModel.selectedPOI.value?.latLng?.longitude

        val reminderDataItem = ReminderDataItem(
            title,
            description,
            location,
            latitude,
            longitude
        )
        viewModel.validateAndSaveReminder(reminderDataItem)

        if (latitude != null && longitude != null && !TextUtils.isEmpty(title))
            addGeofence(LatLng(latitude, longitude), GEOFENCE_RADIUS, reminderDataItem.id)

    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        viewModel.onClear()
    }

    @SuppressLint("MissingPermission")
    private fun addGeofence(
        latLng: LatLng,
        radius: Float,
        geofenceId: String
    ) {

        val geofence: Geofence = geofenceHelper.getGeofence(
            geofenceId,
            latLng,
            radius,
            Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT
        )
        val geofencingRequest: GeofencingRequest = geofenceHelper.getGeofencingRequest(geofence)
        val pendingIntent: PendingIntent? = geofenceHelper.getGeofencePendingIntent()
        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
            .addOnSuccessListener {
                Toast.makeText(context, "geofence added", Toast.LENGTH_LONG).show()
                Log.d("SaveReminderFragment", "Geofence Added")
            }
            .addOnFailureListener(OnFailureListener { e ->
                val errorMessage: String = geofenceHelper.getErrorString(e)
                Toast.makeText(
                    context,
                    "Please give background location permission",
                    Toast.LENGTH_LONG
                ).show()
                Log.d("SaveReminderFragment", "fail in creating geofence: $errorMessage")
            })
    }

}
