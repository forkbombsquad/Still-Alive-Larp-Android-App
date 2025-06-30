package com.forkbombsquad.stillalivelarp

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.CharacterType
import com.forkbombsquad.stillalivelarp.services.models.CheckInOutBarcodeModel
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.CaptureActivityPortrait
import com.forkbombsquad.stillalivelarp.utils.LoadingLayout
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlack
import com.forkbombsquad.stillalivelarp.utils.globalFromJson
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions


class AdminPanelActivity : NoStatusBarActivity() {

    // TODO make sure all activities that are shown while offline don't have internet enabled features

    private lateinit var title: TextView
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
    private lateinit var researchProjects: NavArrowButtonBlack

    private lateinit var pullToRefresh: SwipeRefreshLayout

    private lateinit var loadingLayout: LoadingLayout

    private var checkInOutState = 0

    private val CHECKIN_STATE = 1
    private val CHECKOUT_STATE = 2

    private val barcodeScanner: ActivityResultLauncher<ScanOptions> = registerForActivityResult(
        ScanContract()
    ) { result ->
        if(result.contents != null) {
            if (checkInOutState == CHECKIN_STATE) {
                globalFromJson<CheckInOutBarcodeModel>(result.contents).ifLet({
                    DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.BARCODE, it)
                    DataManager.shared.setUpdateCallback(this::class) {
                        reload()
                    }
                    val intent = Intent(this, CheckInPlayerActivity::class.java)
                    startActivity(intent)
                }, {
                    AlertUtils.displayError(this, "Unable to parse barcode data!")
                })
            } else if (checkInOutState == CHECKOUT_STATE) {
                globalFromJson<CheckInOutBarcodeModel>(result.contents).ifLet({
                    DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.BARCODE, it)
                    DataManager.shared.setUpdateCallback(this::class) {
                        reload()
                    }
                    val intent = Intent(this, CheckOutPlayerActivity::class.java)
                    startActivity(intent)
                }, {
                    AlertUtils.displayError(this, "Unable to parse barcode data!")
                })
            }
        }
        checkInOutState = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_panel)
        setupView()
    }

    private fun setupView() {
        loadingLayout = findViewById(R.id.loadinglayout)

        title = findViewById(R.id.adminpanel_title)
        prereg = findViewById(R.id.adminpanel_viewPrereg)
        eventManagement = findViewById(R.id.adminpanel_eventManagement)
        playerCheckIn = findViewById(R.id.adminpanel_playerCheckIn)
        playerCheckOut = findViewById(R.id.adminpanel_playerCheckOut)
        giveClassXpRed = findViewById(R.id.adminpanel_giveClassXpRed)
        awardPlayer = findViewById(R.id.adminpanel_awardPlayer)
        awardChar = findViewById(R.id.adminpanel_awardChar)
        manageGear = findViewById(R.id.adminpanel_manageGear)
        createAnnouncement = findViewById(R.id.adminpanel_createAnnouncement)
        manageIntrigue = findViewById(R.id.adminpanel_manageIntrigue)
        approveBios = findViewById(R.id.adminpanel_approveBios)
        contactRequests = findViewById(R.id.adminpanel_contactRequests)
        updatePass = findViewById(R.id.adminpanel_updatePass)
        featureFlagManagement = findViewById(R.id.adminpanel_featureFlagManagement)
        refundSkills = findViewById(R.id.adminpanel_refundSkills)
        manageNPCs = findViewById(R.id.adminpanel_manageNPCs)
        researchProjects = findViewById(R.id.adminpanel_research)

        pullToRefresh = findViewById(R.id.pulltorefresh_admin)
        pullToRefresh.setOnRefreshListener {
            reload()
        }

        prereg.setOnClick {
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.EVENT_LIST, DataManager.shared.events)
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.DESTINATION_CLASS, ViewPreregsForEventActivity::class)
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.VIEW_TITLE, "Select Event To View PreRegistrations")
            val intent = Intent(this, EventsListActivity::class.java)
            startActivity(intent)
        }
        eventManagement.setOnClick {
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.EVENT_LIST, DataManager.shared.events)
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.DESTINATION_CLASS, ManageEventActivity::class)
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.ADDITIONAL_DESTINATION_CLASS, CreateNewEventActivity::class)
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.VIEW_TITLE, "Event Management")
            DataManager.shared.setUpdateCallback(this::class) {
                reload()
            }
            val intent = Intent(this, EventsListActivity::class.java)
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
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.CHARACTER_LIST, DataManager.shared.getAllCharacters(CharacterType.STANDARD))
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.DESTINATION_CLASS, SelectSkillForClassXpReductionActivity::class)
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.VIEW_TITLE, "Select Character for Xp Reduction")
            DataManager.shared.setUpdateCallback(this::class) {
                reload()
            }
            val intent = Intent(this, CharactersListActivity::class.java)
            startActivity(intent)
        }
        awardPlayer.setOnClick {
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.PLAYER_LIST, DataManager.shared.players)
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.DESTINATION_CLASS, AwardPlayerActivity::class)
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.VIEW_TITLE, "Select Player To Award")
            DataManager.shared.setUpdateCallback(this::class) {
                reload()
            }
            val intent = Intent(this, PlayersListActivity::class.java)
            startActivity(intent)
        }
        awardChar.setOnClick {
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.CHARACTER_LIST, DataManager.shared.getAllCharacters(CharacterType.STANDARD))
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.DESTINATION_CLASS, AwardCharacterActivity::class)
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.VIEW_TITLE, "Select Character to Award")
            DataManager.shared.setUpdateCallback(this::class) {
                reload()
            }
            val intent = Intent(this, CharactersListActivity::class.java)
            startActivity(intent)
        }
        manageGear.setOnClick {
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.CHARACTER_LIST, DataManager.shared.getAllCharacters(CharacterType.STANDARD))
            if (DataManager.shared.offlineMode) {
                DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.DESTINATION_CLASS, ViewGearActivity::class)
            } else {
                DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.DESTINATION_CLASS, ManageGearActivty::class)
            }
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.VIEW_TITLE, "Select Character for Gear Management")
            DataManager.shared.setUpdateCallback(this::class) {
                reload()
            }
            val intent = Intent(this, CharactersListActivity::class.java)
            startActivity(intent)
        }
        createAnnouncement.setOnClick {
            DataManager.shared.setUpdateCallback(this::class) {
                reload()
            }
            val intent = Intent(this, CreateAnnouncementActivity::class.java)
            startActivity(intent)
        }
        manageIntrigue.setOnClick {
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.EVENT_LIST, DataManager.shared.events)
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.DESTINATION_CLASS, ManageIntrigueActivity::class)
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.VIEW_TITLE, "Select Event To Manage Intrigue")
            DataManager.shared.setUpdateCallback(this::class) {
                reload()
            }
            val intent = Intent(this, EventsListActivity::class.java)
            startActivity(intent)
        }
        approveBios.setOnClick {
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.CHARACTER_LIST, DataManager.shared.getAllCharacters(CharacterType.STANDARD).filter { !it.approvedBio && it.bio.trim().isNotEmpty() })
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.DESTINATION_CLASS, ApproveBioActivity::class)
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.VIEW_TITLE, "Select Character To Approve Bio For")
            DataManager.shared.setUpdateCallback(this::class) {
                reload()
            }
            val intent = Intent(this, CharactersListActivity::class.java)
            startActivity(intent)
        }
        contactRequests.setOnClick {
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.CONTACT_REQUEST_LIST, DataManager.shared.contactRequests)
            DataManager.shared.setUpdateCallback(this::class) {
                reload()
            }
            val intent = Intent(this, ContactListActivity::class.java)
            startActivity(intent)
        }
        updatePass.setOnClick {
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.PLAYER_LIST, DataManager.shared.players)
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.DESTINATION_CLASS, ChangePlayerPasswordActivity::class)
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.VIEW_TITLE, "Select Player To Change Password For")
            DataManager.shared.setUpdateCallback(this::class) {
                reload()
            }
            val intent = Intent(this, PlayersListActivity::class.java)
            startActivity(intent)
        }
        featureFlagManagement.setOnClick {
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.FEATURE_FLAG_LIST, DataManager.shared.featureFlags)
            DataManager.shared.setUpdateCallback(this::class) {
                reload()
            }
            val intent = Intent(this, FeatureFlagManagementActivity::class.java)
            startActivity(intent)
        }
        refundSkills.setOnClick {
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.CHARACTER_LIST, DataManager.shared.getAllCharacters(CharacterType.STANDARD))
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.DESTINATION_CLASS, RefundSkillsActivity::class)
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.VIEW_TITLE, "Select Character to Refund Skills For")
            DataManager.shared.setUpdateCallback(this::class) {
                reload()
            }
            val intent = Intent(this, CharactersListActivity::class.java)
            startActivity(intent)
        }
        manageNPCs.setOnClick {
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.CHARACTER_LIST, DataManager.shared.getAllCharacters(CharacterType.NPC))
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.DESTINATION_CLASS, ManageNPCActivity::class)
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.VIEW_TITLE, "Select NPC To Manage")
            DataManager.shared.setUpdateCallback(this::class) {
                reload()
            }
            val intent = Intent(this, NPCListActivity::class.java)
            startActivity(intent)
        }
        researchProjects.setOnClick {
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.RESEARCH_PROJECT_LIST, DataManager.shared.researchProjects)
            DataManager.shared.setUpdateCallback(this::class) {
                reload()
            }
            val intent = Intent(this, ManageResearchProjectsActivity::class.java)
            startActivity(intent)
        }

        reload()
    }

    private fun reload() {
        DataManager.shared.load(lifecycleScope, stepFinished = {
            buildView()
        }, finished = {
            buildView()
            pullToRefresh.isRefreshing = false
        })
        buildView()
    }

    private fun buildView() {
        DataManager.shared.setTitleTextPotentiallyOffline(title, "Admin Panel")
        DataManager.shared.handleLoadingTextAndHidingViews(loadingLayout) {}

        val offline = DataManager.shared.offlineMode

        playerCheckIn.isGone = offline
        playerCheckOut.isGone = offline
        giveClassXpRed.isGone = offline
        awardPlayer.isGone = offline
        awardChar.isGone = offline
        createAnnouncement.isGone = offline
        approveBios.isGone = offline
        updatePass.isGone = offline
        refundSkills.isGone = offline

        val bioApprovalCount = DataManager.shared.getCharactersWhoNeedBiosApproved().count()
        if (bioApprovalCount > 0) {
            approveBios.setNotificationBubble(bioApprovalCount.toString())
        } else {
            approveBios.setNotificationBubble(null)
        }

        val contactRequestCount = DataManager.shared.contactRequests.count { !it.read.toBoolean() }
        if (contactRequestCount > 0) {
            contactRequests.setNotificationBubble(contactRequestCount.toString())
        } else {
            contactRequests.setNotificationBubble(null)
        }
    }
}