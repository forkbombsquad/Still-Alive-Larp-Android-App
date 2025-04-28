package com.forkbombsquad.stillalivelarp.tabbar_fragments.account.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.forkbombsquad.stillalivelarp.CharacterBioListActivity
import com.forkbombsquad.stillalivelarp.CheckInPlayerActivity
import com.forkbombsquad.stillalivelarp.CheckOutPlayerActivity
import com.forkbombsquad.stillalivelarp.ContactListActivity
import com.forkbombsquad.stillalivelarp.CreateAnnouncementActivity
import com.forkbombsquad.stillalivelarp.FeatureFlagManagementActivity
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.SelectCharacterForClassXpReductionActivity
import com.forkbombsquad.stillalivelarp.SelectCharacterToAwardActivity
import com.forkbombsquad.stillalivelarp.SelectCharacterToManageGearActivity
import com.forkbombsquad.stillalivelarp.SelectCharacterToRefundSkillsActivity
import com.forkbombsquad.stillalivelarp.SelectEventForEventManagementActivity
import com.forkbombsquad.stillalivelarp.SelectEventForIntrigueActivty
import com.forkbombsquad.stillalivelarp.SelectEventForPreregViewActivity
import com.forkbombsquad.stillalivelarp.SelectNPCToManageActivity
import com.forkbombsquad.stillalivelarp.SelectPlayerToAwardActivity
import com.forkbombsquad.stillalivelarp.SelectPlayerToChangePassActivity
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerType
import com.forkbombsquad.stillalivelarp.services.models.CharacterModel
import com.forkbombsquad.stillalivelarp.services.models.ContactRequestModel
import com.forkbombsquad.stillalivelarp.services.models.PlayerCheckInBarcodeModel
import com.forkbombsquad.stillalivelarp.services.models.PlayerCheckOutBarcodeModel
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.CaptureActivityPortrait
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlack
import com.forkbombsquad.stillalivelarp.utils.decompress
import com.forkbombsquad.stillalivelarp.utils.globalFromJson
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.ternary
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class AdminPanelFragment : Fragment() {
    private val TAG = "ADMIN_PANEL_FRAGMENT"

    private lateinit var v: View

    private lateinit var prereg: NavArrowButtonBlack
    private lateinit var eventManagement: NavArrowButtonBlack
    private lateinit var playerCheckIn: NavArrowButtonBlack
    private lateinit var playerCheckOut: NavArrowButtonBlack
    private lateinit var giveClassXpRed: NavArrowButtonBlack
    private lateinit var awardPlayer: NavArrowButtonBlack
    private lateinit var awardChar: NavArrowButtonBlack
    private lateinit var manageGear: NavArrowButtonBlack
    private lateinit var createAnnouncement: NavArrowButtonBlack
    private lateinit var manageIntrigue: NavArrowButtonBlack
    private lateinit var approveBios: NavArrowButtonBlack
    private lateinit var contactRequests: NavArrowButtonBlack
    private lateinit var updatePass: NavArrowButtonBlack
    private lateinit var refundSkills: NavArrowButtonBlack
    private lateinit var featureFlagManagement: NavArrowButtonBlack
    private lateinit var manageNPCs: NavArrowButtonBlack

    private lateinit var pullToRefresh: SwipeRefreshLayout

    private var checkInOutState = 0

    private val CHECKIN_STATE = 1
    private val CHECKOUT_STATE = 2

    private val barcodeScanner: ActivityResultLauncher<ScanOptions> = registerForActivityResult(
        ScanContract()
    ) { result ->
        if(result.contents != null) {
            if (checkInOutState == CHECKIN_STATE) {
                globalFromJson<PlayerCheckInBarcodeModel>(result.contents.decompress()).ifLet({
                    DataManager.shared.playerCheckInModel = it
                    val intent = Intent(v.context, CheckInPlayerActivity::class.java)
                    startActivity(intent)
                }, {
                  AlertUtils.displayError(v.context, "Unable to parse barcode data!")
                })
            } else if (checkInOutState == CHECKOUT_STATE) {
                globalFromJson<PlayerCheckOutBarcodeModel>(result.contents.decompress()).ifLet({
                    DataManager.shared.playerCheckOutModel = it
                    val intent = Intent(v.context, CheckOutPlayerActivity::class.java)
                    startActivity(intent)
                }, {
                    AlertUtils.displayError(v.context, "Unable to parse barcode data!")
                })
            }
        }
        checkInOutState = 0
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_admin_panel, container, false)
        setupView(v)
        return v
    }

    private fun setupView(v: View) {
        prereg = v.findViewById(R.id.adminpanel_viewPrereg)
        eventManagement = v.findViewById(R.id.adminpanel_eventManagement)
        playerCheckIn = v.findViewById(R.id.adminpanel_playerCheckIn)
        playerCheckOut = v.findViewById(R.id.adminpanel_playerCheckOut)
        giveClassXpRed = v.findViewById(R.id.adminpanel_giveClassXpRed)
        awardPlayer = v.findViewById(R.id.adminpanel_awardPlayer)
        awardChar = v.findViewById(R.id.adminpanel_awardChar)
        manageGear = v.findViewById(R.id.adminpanel_manageGear)
        createAnnouncement = v.findViewById(R.id.adminpanel_createAnnouncement)
        manageIntrigue = v.findViewById(R.id.adminpanel_manageIntrigue)
        approveBios = v.findViewById(R.id.adminpanel_approveBios)
        contactRequests = v.findViewById(R.id.adminpanel_contactRequests)
        updatePass = v.findViewById(R.id.adminpanel_updatePass)
        featureFlagManagement = v.findViewById(R.id.adminpanel_featureFlagManagement)
        refundSkills = v.findViewById(R.id.adminpanel_refundSkills)
        manageNPCs = v.findViewById(R.id.adminpanel_manageNPCs)

        pullToRefresh = v.findViewById(R.id.pulltorefresh_admin)
        pullToRefresh.setOnRefreshListener {
            DataManager.shared.load(lifecycleScope, listOf(DataManagerType.ALL_PLAYERS, DataManagerType.ALL_CHARACTERS, DataManagerType.EVENTS, DataManagerType.CONTACT_REQUESTS), true, finishedStep = {
                buildView()
            }) {
                buildView()
                pullToRefresh.isRefreshing = false
            }
            buildView()
        }

        prereg.setOnClick {
            val intent = Intent(v.context, SelectEventForPreregViewActivity::class.java)
            startActivity(intent)
        }
        eventManagement.setOnClick {
            val intent = Intent(v.context, SelectEventForEventManagementActivity::class.java)
            startActivity(intent)
        }
        playerCheckIn.setOnClick {
            checkInOutState = CHECKIN_STATE
            val sc = ScanOptions()
            sc.setOrientationLocked(true)
            sc.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            sc.captureActivity = CaptureActivityPortrait::class.java
            barcodeScanner.launch(sc)
        }
        playerCheckOut.setOnClick {
            checkInOutState = CHECKOUT_STATE
            val sc = ScanOptions()
            sc.setOrientationLocked(true)
            sc.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            sc.captureActivity = CaptureActivityPortrait::class.java
            barcodeScanner.launch(sc)
        }
        giveClassXpRed.setOnClick {
            val intent = Intent(v.context, SelectCharacterForClassXpReductionActivity::class.java)
            startActivity(intent)
        }
        awardPlayer.setOnClick {
            val intent = Intent(v.context, SelectPlayerToAwardActivity::class.java)
            startActivity(intent)
        }
        awardChar.setOnClick {
            val intent = Intent(v.context, SelectCharacterToAwardActivity::class.java)
            startActivity(intent)
        }
        manageGear.setOnClick {
            val intent = Intent(v.context, SelectCharacterToManageGearActivity::class.java)
            startActivity(intent)
        }
        createAnnouncement.setOnClick {
            val intent = Intent(v.context, CreateAnnouncementActivity::class.java)
            startActivity(intent)
        }
        manageIntrigue.setOnClick {
            val intent = Intent(v.context, SelectEventForIntrigueActivty::class.java)
            startActivity(intent)
        }
        approveBios.setOnClick {
            val intent = Intent(v.context, CharacterBioListActivity::class.java)
            startActivity(intent)
        }
        contactRequests.setOnClick {
            val intent = Intent(v.context, ContactListActivity::class.java)
            startActivity(intent)
        }
        updatePass.setOnClick {
            val intent = Intent(v.context, SelectPlayerToChangePassActivity::class.java)
            startActivity(intent)
        }
        featureFlagManagement.setOnClick {
            val intent = Intent(v.context, FeatureFlagManagementActivity::class.java)
            startActivity(intent)
        }
        refundSkills.setOnClick {
            val intent = Intent(v.context, SelectCharacterToRefundSkillsActivity::class.java)
            startActivity(intent)
        }
        manageNPCs.setOnClick {
            val intent = Intent(v.context, SelectNPCToManageActivity::class.java)
            startActivity(intent)
        }

        DataManager.shared.load(lifecycleScope, listOf(DataManagerType.ALL_PLAYERS, DataManagerType.ALL_CHARACTERS, DataManagerType.EVENTS, DataManagerType.CONTACT_REQUESTS, DataManagerType.FEATURE_FLAGS, DataManagerType.ALL_NPC_CHARACTERS), true, finishedStep = {
            buildView()
        }) {
            buildView()
        }
        buildView()
    }

    private fun buildView() {
        prereg.setLoading(DataManager.shared.loadingEvents)
        eventManagement.setLoading(DataManager.shared.loadingEvents)
        giveClassXpRed.setLoading(DataManager.shared.loadingAllCharacters)
        awardPlayer.setLoading(DataManager.shared.loadingAllPlayers)
        awardChar.setLoading(DataManager.shared.loadingAllCharacters)
        manageGear.setLoading(DataManager.shared.loadingAllCharacters)
        manageIntrigue.setLoading(DataManager.shared.loadingEvents)
        approveBios.setLoading(DataManager.shared.loadingAllCharacters)
        contactRequests.setLoading(DataManager.shared.loadingContactRequests)
        updatePass.setLoading(DataManager.shared.loadingAllCharacters)
        featureFlagManagement.setLoading(DataManager.shared.loadingFeatureFlags)
        refundSkills.setLoading(DataManager.shared.loadingAllCharacters)
        manageNPCs.setLoading(DataManager.shared.loadingAllNPCCharacters)

        DataManager.shared.allCharacters.ifLet({
            approveBios.setNotificationBubble(getNumberOfBiosThatNeedApproval(it))
        }, {
            approveBios.setNotificationBubble(null)
        })

        DataManager.shared.contactRequests.ifLet({
            contactRequests.setNotificationBubble(getNumberUnreadContacts(it))
        }, {
            contactRequests.setNotificationBubble(null)
        })
    }

    private fun getNumberOfBiosThatNeedApproval(chars: List<CharacterModel>): String? {
        val filtered = chars.filter { !it.approvedBio.toBoolean() && it.bio.isNotEmpty() }
        return (filtered.isNotEmpty()).ternary(filtered.count().toString(), null)
    }

    private fun getNumberUnreadContacts(contactRequests: List<ContactRequestModel>): String? {
        val filtered = contactRequests.filter { !it.read.toBoolean() }
        return (filtered.isNotEmpty()).ternary(filtered.count().toString(), null)
    }

    companion object {
        @JvmStatic
        fun newInstance() = AdminPanelFragment()
    }
}
