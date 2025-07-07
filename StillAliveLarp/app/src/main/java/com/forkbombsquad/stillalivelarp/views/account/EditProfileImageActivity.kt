package com.forkbombsquad.stillalivelarp.views.account

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.utils.NoStatusBarActivity
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.ProfileImageService
import com.forkbombsquad.stillalivelarp.services.managers.DataManager

import com.forkbombsquad.stillalivelarp.services.models.ProfileImageCreateModel
import com.forkbombsquad.stillalivelarp.services.models.ProfileImageModel
import com.forkbombsquad.stillalivelarp.services.utils.CreateModelSP
import com.forkbombsquad.stillalivelarp.services.utils.IdSP
import com.forkbombsquad.stillalivelarp.services.utils.UpdateModelSP
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.LoadingButton
import com.forkbombsquad.stillalivelarp.utils.LoadingLayout
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlack
import com.forkbombsquad.stillalivelarp.utils.base64String
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.toBitmap
import kotlinx.coroutines.launch

class EditProfileImageActivity : NoStatusBarActivity() {

    private lateinit var profileImage: ImageView
    private lateinit var loadingLayout: LoadingLayout
    private lateinit var selectImage: NavArrowButtonBlack
    private lateinit var deleteButton: LoadingButton
    private lateinit var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>

    private var bitmapImage: Bitmap? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile_image)
        setupView()
    }

    private fun setupView() {
        bitmapImage = DataManager.shared.getCurrentPlayer()?.profileImage?.image?.toBitmap()

        profileImage = findViewById(R.id.editProfileImage_imageView)
        loadingLayout = findViewById(R.id.loadinglayout)
        selectImage = findViewById(R.id.editProfileImage_selectImage)
        deleteButton = findViewById(R.id.editProfileImage_delete)

        pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            loadingLayout.setLoadingText("Converting Image...")
            if (uri != null) {
                val source = ImageDecoder.createSource(this.contentResolver, uri)
                val bitmap = ImageDecoder.decodeBitmap(source)
                setImage(bitmap)
            } else {
                loadingLayout.setLoading(false)
            }
        }

        selectImage.setOnClick {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        deleteButton.setOnClick {
            DataManager.shared.getCurrentPlayer().ifLet {
                loadingLayout.setLoadingText("Deleting Image...")
                val request = ProfileImageService.DeleteProfileImages()
                lifecycleScope.launch {
                    request.successfulResponse(IdSP(it.id), true).ifLet({
                        bitmapImage = null
                        reload()
                    }, {
                        AlertUtils.displaySomethingWentWrong(this@EditProfileImageActivity)
                        bitmapImage = null
                        reload()
                    })
                }
            }
        }

        reload()
    }

    private fun reload() {
        DataManager.shared.load(lifecycleScope, stepFinished = {
            buildView()
        }, finished = {
            buildView()
        })
        buildView()
    }

    private fun buildView() {
        DataManager.shared.handleLoadingTextAndHidingViews(loadingLayout, listOf(profileImage, selectImage, deleteButton)) {
            bitmapImage.ifLet {
                profileImage.setImageBitmap(it)
            }
        }
    }
    private fun setImage(bitmap: Bitmap) {
        bitmapImage = bitmap
        buildView()
        loadingLayout.setLoadingText("Beginning Upload...")
        DataManager.shared.getCurrentPlayer().ifLet {
            if (it.profileImage == null) {
                // Create
                val request = ProfileImageService.CreateProfileImage()
                lifecycleScope.launch {
                    request.successfulResponse(CreateModelSP(ProfileImageCreateModel(it.id, bitmap.base64String()))).ifLet({ profileImage ->
                        bitmapImage = profileImage.image.toBitmap()
                        reload()
                    }, {
                        AlertUtils.displaySomethingWentWrong(this@EditProfileImageActivity)
                        reload()
                    })
                }
            } else {
                // Update
                val request = ProfileImageService.UpdateProfileImage()
                lifecycleScope.launch {
                    request.successfulResponse(UpdateModelSP(ProfileImageModel(DataManager.shared.getCurrentPlayer()?.profileImage?.id ?: -1, it.id, bitmap.base64String()))).ifLet({ profileImage ->
                        bitmapImage = profileImage.image.toBitmap()
                        reload()
                    }, {
                        AlertUtils.displaySomethingWentWrong(this@EditProfileImageActivity)
                        reload()
                    })
                }
            }
        }
    }
    override fun onBackPressed() {
        DataManager.shared.callUpdateCallback(MyAccountFragment::class)
        super.onBackPressed()
    }
}