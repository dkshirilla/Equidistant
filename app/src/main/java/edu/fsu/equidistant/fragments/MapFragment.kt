package edu.fsu.equidistant.fragments

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import edu.fsu.equidistant.R
import edu.fsu.equidistant.databinding.FragmentMapBinding
import edu.fsu.equidistant.places.*
import kotlinx.coroutines.flow.collect


class MapFragment : Fragment(R.layout.fragment_map), OnMapReadyCallback {

    private var map: GoogleMap? = null
    private val args: MapFragmentArgs by navArgs()
    private lateinit var centerLocation: Location
    private val locationViewModel: LocationViewModel by viewModels()
    private lateinit var googlePlaceList: ArrayList<GooglePlaceModel>
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var binding: FragmentMapBinding
    private var currentMarker: Marker? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        googlePlaceList = ArrayList()
        binding = FragmentMapBinding.bind(view)
        centerLocation = args.location
        loadingDialog = LoadingDialog(requireActivity())

        val mapFragment =
            (childFragmentManager.findFragmentById(R.id.homeMap) as SupportMapFragment?)
        mapFragment?.getMapAsync(this)

    }

    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap
        setupMap()
        getNearbyPlace()
    }

    private fun getNearbyPlace() {
        val url = ("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
                + centerLocation.latitude + "," + centerLocation.longitude
                + "&radius=7500&type=restaurant&key=AIzaSyCUNlT9fS_FgParpYQXGkIO_LMdX7jvEHA")

        lifecycleScope.launchWhenStarted {
            locationViewModel.getNearbyPlace(url).collect {
                when (it) {
                    is State.Loading -> {
                        if (it.flag == true) {
                            loadingDialog.startLoading()
                        }
                    }

                    is State.Success -> {
                        loadingDialog.stopLoading()

                        val googleResponseModel: GoogleResponseModel =
                            it.data as GoogleResponseModel

                        if (googleResponseModel.googlePlaceModelList != null &&
                            googleResponseModel.googlePlaceModelList.isNotEmpty()) {

                            googlePlaceList.clear()
                            map?.clear()

                            for (i in googleResponseModel.googlePlaceModelList.indices) {
                                googlePlaceList.add(googleResponseModel.googlePlaceModelList[i])
                                addMarker(googleResponseModel.googlePlaceModelList[i], i)
                            }

                            Log.d(ContentValues.TAG, "googlePlaceList array: $googlePlaceList")
                            addCurrentMarker()
                        } else {
                            googlePlaceList.clear()
                            map?.clear()
                        }
                    }

                    is State.Failed -> {
                        loadingDialog.stopLoading()
                        Snackbar.make(
                            binding.root, it.error,
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun addMarker(googlePlaceModel: GooglePlaceModel, position: Int) {
        val markerOptions = MarkerOptions()
            .position(
                LatLng(
                    googlePlaceModel.geometry?.location?.lat!!,
                    googlePlaceModel.geometry.location.lng!!
                )
            )
            .title(googlePlaceModel.name)
            .snippet(googlePlaceModel.vicinity)

        markerOptions.icon(getCustomIcon())
        map?.addMarker(markerOptions)?.tag = position
    }

    private fun getCustomIcon(): BitmapDescriptor {

        val background = ContextCompat.getDrawable(requireContext(), R.drawable.ic_location)
        background?.setTint(resources.getColor(R.color.quantum_teal300, null))
        background?.setBounds(0, 0, background.intrinsicWidth, background.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(
            background?.intrinsicWidth!!, background.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        background.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun addCurrentMarker() {
        val markerOption = MarkerOptions()
            .position(LatLng(centerLocation.latitude, centerLocation.longitude))
            .title("Center Location")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))

        currentMarker?.remove()
        currentMarker = map?.addMarker(markerOption)
        currentMarker?.tag = 703
    }

    private fun setupMap() {
        moveCameraToLocation()
    }

    private fun moveCameraToLocation() {
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
            LatLng(
                centerLocation.latitude,
                centerLocation.longitude
            ), 13f
        )

        map?.animateCamera(cameraUpdate)
    }

}