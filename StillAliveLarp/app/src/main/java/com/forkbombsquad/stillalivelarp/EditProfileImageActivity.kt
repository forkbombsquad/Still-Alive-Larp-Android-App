package com.forkbombsquad.stillalivelarp

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Bundle
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.ProfileImageService

import com.forkbombsquad.stillalivelarp.services.models.ProfileImageCreateModel
import com.forkbombsquad.stillalivelarp.services.models.ProfileImageModel
import com.forkbombsquad.stillalivelarp.services.utils.CreateModelSP
import com.forkbombsquad.stillalivelarp.services.utils.IdSP
import com.forkbombsquad.stillalivelarp.services.utils.UpdateModelSP
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.LoadingButton
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlack
import com.forkbombsquad.stillalivelarp.utils.base64String
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.toBitmap
import kotlinx.coroutines.launch

class EditProfileImageActivity : NoStatusBarActivity() {

    private lateinit var profileImage: ImageView
    private lateinit var profileImageProgressBar: ProgressBar
    private lateinit var selectImage: NavArrowButtonBlack
    private lateinit var deleteButton: LoadingButton
    private lateinit var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile_image)
        setupView()
    }

    private fun setupView() {
        profileImage = findViewById(R.id.editProfileImage_imageView)
        profileImageProgressBar = findViewById(R.id.editProfileImage_loadingBar)
        selectImage = findViewById(R.id.editProfileImage_selectImage)
        deleteButton = findViewById(R.id.editProfileImage_delete)

        pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            startLoading()
            if (uri != null) {
                val source = ImageDecoder.createSource(this.contentResolver, uri)
                val bitmap = ImageDecoder.decodeBitmap(source)

                setImage(bitmap)
            } else {
                stopLoading()
            }
        }

        selectImage.setOnClick {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        deleteButton.setOnClick {
            OldDataManager.shared.selectedPlayer.ifLet {
                startLoading()
                val request = ProfileImageService.DeleteProfileImages()
                lifecycleScope.launch {
                    request.successfulResponse(IdSP(it.id), true).ifLet({
                        OldDataManager.shared.profileImage = null
                        AlertUtils.displaySuccessMessage(this@EditProfileImageActivity, "Profile Image Deleted")
                        stopLoading()
                        buildView()
                    }, {
                        AlertUtils.displaySomethingWentWrong(this@EditProfileImageActivity)
                        stopLoading()
                        buildView()
                    })
                }
            }
        }

        OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.PROFILE_IMAGE), false) {
            buildView()
        }
        buildView()
    }

    private fun startLoading() {
        profileImageProgressBar.isGone = false
        selectImage.setLoading(true)
        deleteButton.setLoading(true)
    }

    private fun stopLoading() {
        profileImageProgressBar.isGone = true
        selectImage.setLoading(false)
        deleteButton.setLoading(false)
    }

    private fun buildView() {
        profileImageProgressBar.isGone = !OldDataManager.shared.loadingProfileImage

        OldDataManager.shared.profileImage.ifLet {
            profileImage.setImageBitmap(it.image.toBitmap())
        }
    }
    private fun setImage(bitmap: Bitmap) {
        startLoading()
        OldDataManager.shared.selectedPlayer.ifLet {
            if (OldDataManager.shared.profileImage == null || OldDataManager.shared.profileImage?.playerId != it.id) {
                // Create
                val request = ProfileImageService.CreateProfileImage()
                lifecycleScope.launch {
                    request.successfulResponse(CreateModelSP(ProfileImageCreateModel(it.id, bitmap.base64String()))).ifLet({ profileImage ->
                        OldDataManager.shared.profileImage = profileImage
                        OldDataManager.shared.loadingProfileImage = false
                        stopLoading()
                        buildView()
                    }, {
                        AlertUtils.displaySomethingWentWrong(this@EditProfileImageActivity)
                        stopLoading()
                        buildView()
                    })
                }
            } else {
                // Update
                val request = ProfileImageService.UpdateProfileImage()
                lifecycleScope.launch {
                    request.successfulResponse(UpdateModelSP(ProfileImageModel(OldDataManager.shared.profileImage?.id ?: 0, it.id, bitmap.base64String()))).ifLet({ profileImage ->
                        OldDataManager.shared.profileImage = profileImage
                        OldDataManager.shared.loadingProfileImage = false
                        stopLoading()
                        buildView()
                    }, {
                        AlertUtils.displaySomethingWentWrong(this@EditProfileImageActivity)
                        stopLoading()
                        buildView()
                    })
                }
            }
        }
    }
    override fun onBackPressed() {
        OldDataManager.shared.unrelaltedUpdateCallback()
        super.onBackPressed()
    }
}