package edu.utap.sharein.ui.newpost


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetFileDescriptor
import android.location.*
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.gms.location.*
import edu.utap.sharein.*
import edu.utap.sharein.R
import kotlinx.android.synthetic.main.fragment_new_post.*
import java.io.IOException
import java.lang.Exception
import java.util.*

class NewPostFragment : Fragment(){

    private lateinit var newPostViewModel: NewPostViewModel
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var pictureUUIDs: List<String>
    private var musicRawID: Int = -1
    private lateinit var profilePhotoUUID: String
    private var position = -1
    private val viewModel: MainViewModel by activityViewModels()
    private val args: NewPostFragmentArgs by navArgs()
    private lateinit var player: MediaPlayer
    private val songReposit = SongReposit()
    private var songResources = songReposit.fetchSongs()

    private val LOCATION_REQUEST_CODE = 101
    private var address: String = ""
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient




    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        newPostViewModel =
                ViewModelProvider(this).get(NewPostViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_new_post, container, false)
//        val textView: TextView = root.findViewById(R.id.text_dashboard)
//        dashboardViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })
        setHasOptionsMenu(true)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        position = args.position
        if (position == -1) {
            // new post
            pictureUUIDs = listOf()
            // XXX update music info

            updateMusic()

            // update location info
            updateLocation()
            clearLocation()
            profilePhotoUUID = viewModel.observeUser().value?.profilePhotoUUID ?: ""
        }
        else {
            // edit post
            val post = viewModel.getPost(position)
            enterTitleET.text.insert(0, post.title)
            enterPostET.text.insert(0, post.text)
            enterPostET.movementMethod = ScrollingMovementMethod()
            pictureUUIDs = post.pictureUUIDs
            // XXX update music info
            musicRawID = post.musicRawID
            if (post.musicRawID != -1) {
                chooseMusicBut.text = songResources[post.musicRawID]?.name + " >"
            }


            updateMusic()
            if (post.address != "") {
                currLocationTV.text = post.address
            }

            // update location info
            address = post.address
            updateLocation()
            clearLocation()
            profilePhotoUUID = viewModel.observeUser().value?.profilePhotoUUID ?: ""

        }
        enterTitleET.requestFocus()

        photosRV.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        imageAdapter = ImageAdapter(viewModel) {
            // long click to remove image
            val shorterList = pictureUUIDs.toMutableList()
            val removedPictureUUID: String = shorterList.removeAt(it)
            viewModel.deleteImage(removedPictureUUID)
            pictureUUIDs = shorterList
            imageAdapter.submitList(pictureUUIDs)

        }
        photosRV.adapter = imageAdapter
        imageAdapter.submitList(pictureUUIDs)




    }

    private fun updateMusic(){

        chooseMusicBut.setOnClickListener {

            val chooseMusicView = LayoutInflater.from(requireContext()).inflate(R.layout.choose_song, null)
            val dialogueBuilder = AlertDialog.Builder(requireContext())
            dialogueBuilder.setCancelable(false)
                .setView(chooseMusicView)
            val alert = dialogueBuilder.create()
            alert.show()

            player = MediaPlayer.create(requireContext(), R.raw.american_dream)
            musicRawID = R.raw.american_dream
            player.start()

            val musicRadioGroup = chooseMusicView.findViewById<RadioGroup>(R.id.musicRadioGroup)



            musicRadioGroup.setOnCheckedChangeListener { radioGroup, i ->
                musicRawID = when (i) {
                    R.id.radioAmericanDream -> {
                        R.raw.american_dream
                    }
                    R.id.radioCinematic -> {
                        R.raw.cinematic
                    }
                    R.id.radioNocturne -> {
                        R.raw.nocturne
                    }
                    else -> -1
                }
                if (musicRawID != -1 ) {

                    stopMusic()


                    playMusic(musicRawID)
                }

            }


            val chooseMusicOKBut = chooseMusicView.findViewById<Button>(R.id.chooseMusicOKBut)
            val chooseMusicCancelBut = chooseMusicView.findViewById<Button>(R.id.chooseMusicCancelBut)
            chooseMusicOKBut.setOnClickListener {
                stopMusic()
                alert.cancel()
                chooseMusicBut.text = songResources[musicRawID]?.name + " >"
            }
            chooseMusicCancelBut.setOnClickListener {
                musicRawID = -1
                chooseMusicBut.text = ">"
                stopMusic()
                alert.cancel()
            }

        }

    }

    private fun playMusic(musicRawID: Int) {
        val fd: AssetFileDescriptor = resources.openRawResourceFd(musicRawID)
        player.setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
        player.prepare()
        player.start()

    }

    private fun stopMusic() {
        player.reset()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.new_post_menu, menu)


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.postBut -> {
                // XXX post it
                if (TextUtils.isEmpty(enterTitleET.text.toString())) {
                    Toast.makeText(activity, "Enter title!", Toast.LENGTH_LONG).show()
                }
                else {
                    if (position == -1) {
                        // new post
                        val postID = viewModel.createPost(enterTitleET.text.toString(), enterPostET.text.toString(), pictureUUIDs, musicRawID, address)
                        Log.d(javaClass.simpleName, "fetch status is ${viewModel.observeFetchStatus().value} ")
                        val user = viewModel.observeUser().value
                        if (user != null) {
                            Log.d(javaClass.simpleName, "when post, user is not null")
                            var tempList = user.postsList.toMutableList()
                            tempList.add(postID)
                            user.postsList = tempList
                            Log.d(javaClass.simpleName, "the postslist is ${user.postsList}")
                            viewModel.updateUser(user)
                        }

                    }
                    else {
                        // edit post
                        viewModel.updatePost(position, enterTitleET.text.toString(), enterPostET.text.toString(), pictureUUIDs, musicRawID, address)

                    }
                    (activity as MainActivity?)?.hideKeyboard()
                    val status = viewModel.observeFetchStatus().value
                    if (status == Constants.FETCH_FOLLOW || status == Constants.FETCH_ALL || status == Constants.FETCH_TREND) {
                        val action = NewPostFragmentDirections.actionNavigationNewPostToNavigationHome()
                        findNavController().navigate(action)
                    }
                    else {
                        val action = NewPostFragmentDirections.actionNavigationNewPostToNavigationMe(-1, "Me", viewModel.observeUser().value!!.uid)
                        findNavController().navigate(action)
                    }
                }


                true
            }
            R.id.cameraBut -> {
                viewModel.takePhoto(::pictureSuccess)
                true
            }
            R.id.cancelBut -> {
                (activity as MainActivity?)?.hideKeyboard()
                val status = viewModel.observeFetchStatus().value
                if (status == Constants.FETCH_FOLLOW || status == Constants.FETCH_ALL || status == Constants.FETCH_TREND) {
                    val action = NewPostFragmentDirections.actionNavigationNewPostToNavigationHome()
                    findNavController().navigate(action)
                }
                else {
                    val action = NewPostFragmentDirections.actionNavigationNewPostToNavigationMe(-1, "Me", viewModel.observeUser().value!!.uid)
                    findNavController().navigate(action)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun pictureSuccess(pictureUUID: String) {
        pictureUUIDs.toMutableList().apply {
            add(pictureUUID)
            Log.d(javaClass.simpleName, "photo added $pictureUUID len ${this.size}")
            pictureUUIDs = this
            imageAdapter.submitList(pictureUUIDs)
        }
    }


    private fun grantPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_REQUEST_CODE)
        }
    }

    private fun checkLocationEnabled() {
        var lm: LocationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gpsEnabled: Boolean = false
        var networkEnabled: Boolean = false

        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }
        catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
        catch (e: Exception) {
            e.printStackTrace()
        }

        if (!gpsEnabled && !networkEnabled) {
            val dialogueBuilder = AlertDialog.Builder(requireContext())
            dialogueBuilder.setCancelable(false)
                .setTitle("Please enable GPS Service")
                .setPositiveButton("Enable") { dialogInterface, i ->
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

    }



    @SuppressLint("MissingPermission")
    private fun updateLocation() {
        currLocationTV.setOnClickListener{
            Log.d(javaClass.simpleName, "choose location is clicked")
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
            grantPermission()
            checkLocationEnabled()
            fusedLocationProviderClient.lastLocation.addOnCompleteListener {
                var location: Location? = it.result
                if (location == null) {
                    newLocationData()
                }
                else {
                    currLocationTV.text = getCityAndCountry(location.latitude, location.longitude)
                }
                address = currLocationTV.text.toString()
            }

        }
    }

    @SuppressLint("MissingPermission")
    private fun newLocationData() {
        var locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 1
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        fusedLocationProviderClient!!.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    }

    private val locationCallback = object: LocationCallback() {
        override fun onLocationResult(p0: LocationResult?) {
            var location: Location = p0!!.lastLocation
            currLocationTV.text = getCityAndCountry(location.latitude, location.longitude)
        }
    }

    private fun getCityAndCountry(lat: Double, long: Double): String {
        var city: String = ""
        var country: String = ""
        var geocoder = Geocoder(requireContext(), Locale.getDefault())
        var addressList = geocoder.getFromLocation(lat, long, 1)
        city = addressList[0].locality
        country = addressList[0].countryName

        return city + ", " + country

    }


    private fun clearLocation() {
        if (currLocationTV.text != ">") {
            currLocationTV.setOnLongClickListener {
                val clearLocationView = LayoutInflater.from(requireContext()).inflate(R.layout.clear_location_alert, null)
                val dialogueBuilder = AlertDialog.Builder(requireContext())
                dialogueBuilder.setCancelable(false)
                    .setView(clearLocationView)
                val alert = dialogueBuilder.create()
                alert.show()

                val clearLocationConfirmBut = clearLocationView.findViewById<Button>(R.id.clearLocationConfirmBut)
                val clearLocationCancelBut = clearLocationView.findViewById<Button>(R.id.clearLocationCancelBut)
                clearLocationConfirmBut.setOnClickListener {
                    address = ""

                    currLocationTV.text = ">"
                    alert.cancel()
                }
                clearLocationCancelBut.setOnClickListener {
                    alert.cancel()
                }
                true
            }
        }
    }


}