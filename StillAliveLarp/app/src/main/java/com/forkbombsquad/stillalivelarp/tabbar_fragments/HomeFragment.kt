package com.forkbombsquad.stillalivelarp.tabbar_fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.forkbombsquad.stillalivelarp.*
import com.forkbombsquad.stillalivelarp.services.managers.AnnouncementManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerType
import com.forkbombsquad.stillalivelarp.services.models.*
import com.forkbombsquad.stillalivelarp.utils.*
import com.google.android.material.divider.MaterialDivider

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    private val TAG = "HOME_FRAGMENT"

    private var eventIndex = 0
    private var currentAnnouncementIndex = 0

    private var prevButton = Button(globalGetContext())
    private var nextButton = Button(globalGetContext())

    private var eventPrevButton = Button(globalGetContext())
    private var eventNextButton = Button(globalGetContext())

    private lateinit var pullToRefresh: SwipeRefreshLayout

    private lateinit var preregisterButton: NavArrowButtonBlue

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_home, container, false)
        setupViews(v)
        return v
    }

    private fun setupViews(v: View) {
        // Pull To Refresh
        preparePullToRefresh(v)

        // Announcements
        prepareAnnouncementsSection(v)

        // Intrigue
        prepareIntrigueSection(v)

        // Checkout
        prepareCheckoutSection(v)

        // Current Char View
        prepareCurrentCharSection(v)

        // Events View
        prepareEventsSection(v)

        // Awards View
        prepareAwardsSection(v)
    }

    private fun preparePullToRefresh(v: View) {
        pullToRefresh = v.findViewById(R.id.pulltorefresh_home)
        pullToRefresh.setOnRefreshListener {
            DataManager.shared.load(lifecycleScope, listOf(DataManagerType.PLAYER, DataManagerType.CHARACTER, DataManagerType.ANNOUNCEMENTS, DataManagerType.EVENTS, DataManagerType.AWARDS, DataManagerType.INTRIGUE, DataManagerType.SKILLS), true) {
                DataManager.shared.selectedChar = DataManager.shared.character?.getBaseModel()
                DataManager.shared.load(lifecycleScope, listOf(DataManagerType.EVENT_PREREGS, DataManagerType.SELECTED_CHARACTER_GEAR), true) {
                    setupViews(v)
                    pullToRefresh.isRefreshing = false
                }
            }
            setupViews(v)
        }
    }

    private fun prepareAnnouncementsSection(v: View) {
        val sectionTitle = v.findViewById<TextView>(R.id.announcement_section_title)
        val announcementsProgressBar = v.findViewById<ProgressBar>(R.id.announcement_list_progress_bar)

        val title = v.findViewById<TextView>(R.id.announcement_title)
        val date = v.findViewById<TextView>(R.id.announcement_date)
        val desc = v.findViewById<TextView>(R.id.announcement_desc)
        prevButton = v.findViewById<Button>(R.id.announcement_prev_button)
        nextButton = v.findViewById<Button>(R.id.announcement_next_button)

        val announcements = DataManager.shared.announcements ?: listOf()

        prevButton.setOnClickListener {
            announcements.ifLet {
                if (currentAnnouncementIndex > 0) {
                    currentAnnouncementIndex--
                    AnnouncementManager.shared.getAnnouncement(lifecycleScope, it[currentAnnouncementIndex].id) { an ->
                        DataManager.shared.currentAnnouncement = an
                        an.ifLet { am ->
                            title.text = am.title
                            date.text = am.date.yyyyMMddToMonthDayYear()
                            desc.text = am.text
                            showHideAnnouncementNavButtons()
                        }
                    }
                }
            }
        }

        nextButton.setOnClickListener {
            announcements.ifLet {
                if (currentAnnouncementIndex + 1 < it.size) {
                    currentAnnouncementIndex++
                    AnnouncementManager.shared.getAnnouncement(lifecycleScope, it[currentAnnouncementIndex].id) { an ->
                        DataManager.shared.currentAnnouncement = an
                        an.ifLet { am ->
                            title.text = am.title
                            date.text = am.date.yyyyMMddToMonthDayYear()
                            desc.text = am.text
                            showHideAnnouncementNavButtons()
                        }
                    }
                }
            }
        }

        DataManager.shared.currentAnnouncement.ifLet({
            announcementsProgressBar.isGone = true
            title.text = it.title
            date.text = it.date.yyyyMMddToMonthDayYear()
            desc.text = it.text

            title.isGone = false
            date.isGone = false
            desc.isGone = false

            showHideAnnouncementNavButtons()

        }, {
            announcementsProgressBar.isGone = false
            title.isGone = true
            date.isGone = true
            desc.isGone = true
            showHideAnnouncementNavButtons()
        })
    }

    private fun prepareIntrigueSection(v: View) {
        val intrigueSection = v.findViewById<LinearLayout>(R.id.intrigueView)
        val investigatorView = v.findViewById<LinearLayout>(R.id.intrigue_investigatorView)
        val interrogatorView = v.findViewById<LinearLayout>(R.id.intrigue_interrogatorView)
        val webView = v.findViewById<LinearLayout>(R.id.intrigue_webOfInfView)

        val investigator = v.findViewById<TextView>(R.id.intrigue_investigatorText)
        val interrogator = v.findViewById<TextView>(R.id.intrigue_interrogatorText)
        val web = v.findViewById<TextView>(R.id.intrigue_webOfInfText)

        DataManager.shared.intrigue.ifLet({
            if (showIntrigue()) {
                val intrigueSkills: IntArray = DataManager.shared.character?.getIntrigueSkills() ?: IntArray(0)
                investigator.text = it.investigatorMessage
                interrogator.text = it.interrogatorMessage
                web.text = it.webOfInformantsMessage
                investigatorView.isGone = intrigueSkills.firstOrNull { id -> id == Constants.SpecificSkillIds.investigator } == null
                interrogatorView.isGone = intrigueSkills.firstOrNull { id -> id == Constants.SpecificSkillIds.interrogator } == null
                webView.isGone = intrigueSkills.firstOrNull { id -> id == Constants.SpecificSkillIds.webOfInformants } == null
                intrigueSection.isGone = false
            } else {
                intrigueSection.isGone = true
            }
        }, {
            intrigueSection.isGone = true
        })

    }

    private fun prepareCheckoutSection(v: View) {
        val checkoutSection = v.findViewById<LinearLayout>(R.id.checkoutView)
        val checkoutButton = v.findViewById<NavArrowButtonRed>(R.id.checkout_navarrow)
        if (showCheckout()) {
            checkoutSection.isGone = false
            checkoutButton.textView.text = "Checkout"
            checkoutButton.setOnClick {
                checkoutButton.setLoading(true)
                DataManager.shared.load(lifecycleScope, listOf(DataManagerType.EVENT_ATTENDEES), true) {
                    checkoutButton.setLoading(false)
                    DataManager.shared.eventAttendeesForPlayer.ifLet({ eventAttendees ->
                        eventAttendees.firstOrNull { it.isCheckedIn.toBoolean() }.ifLet({

                            var char: CharacterBarcodeModel? = null
                            var relevantSkills: Array<SkillBarcodeModel> = arrayOf()

                            if (!it.asNpc.toBoolean()) {
                                char = DataManager.shared.character?.getBarcodeModel()
                                relevantSkills = DataManager.shared.character?.getRelevantBarcodeSkills() ?: arrayOf()
                            }

                            DataManager.shared.checkoutBarcodeModel = PlayerCheckOutBarcodeModel(
                                player = DataManager.shared.player!!.getBarcodeModel(),
                                character = char,
                                eventAttendeeId = it.id,
                                eventId = it.eventId,
                                relevantSkills = relevantSkills
                            )
                            DataManager.shared.unrelaltedUpdateCallback = {
                                DataManager.shared.load(lifecycleScope, listOf(DataManagerType.PLAYER, DataManagerType.CHARACTER, DataManagerType.INTRIGUE, DataManagerType.EVENTS), true) {
                                    setupViews(v)
                                }
                                setupViews(v)
                            }
                            val intent = Intent(v.context, CheckOutBarcodeActivity::class.java)
                            startActivity(intent)

                        }, {
                            checkoutButton.setLoading(true)
                        })
                    }, {
                        checkoutButton.setLoading(true)
                    })
                }
            }
        } else {
            checkoutSection.isGone = true
        }
    }

    private fun prepareCurrentCharSection(v: View) {
        val currentCharView = v.findViewById<LinearLayout>(R.id.currentCharacterView)
        val currentCharLoadingView = v.findViewById<LinearLayout>(R.id.currentCharLoadingView)
        val currentCharNameView = v.findViewById<LinearLayout>(R.id.currentCharNameView)
        val currentCharNoneView = v.findViewById<LinearLayout>(R.id.currentCharNoneView)

        val currentCharNameText = v.findViewById<TextView>(R.id.currentCharNameText)
        val charCreateButton = v.findViewById<LoadingButton>(R.id.currentCharCreateNewCharButton)

        if (showCurrentCharSection()) {
            currentCharView.isGone = false
            if (DataManager.shared.loadingCharacter) {
                currentCharLoadingView.isGone = false
                currentCharNameView.isGone = true
                currentCharNoneView.isGone = true
            } else if (DataManager.shared.character == null) {
                currentCharLoadingView.isGone = true
                currentCharNameView.isGone = true
                currentCharNoneView.isGone = false
                charCreateButton.setOnClick {
                    DataManager.shared.unrelaltedUpdateCallback = {
                        DataManager.shared.load(lifecycleScope, listOf(DataManagerType.PLAYER, DataManagerType.CHARACTER), true) {
                            setupViews(v)
                        }
                        setupViews(v)
                    }
                    val intent = Intent(v.context, CreateCharacterActivity::class.java)
                    startActivity(intent)
                }
            } else {
                currentCharLoadingView.isGone = true
                currentCharNameView.isGone = false
                currentCharNoneView.isGone = true

                currentCharNameText.text = DataManager.shared.character?.fullName
            }
        } else {
            currentCharView.isGone = true
        }
    }

    private fun prepareEventsSection(v: View) {
        val eventView = v.findViewById<LinearLayout>(R.id.eventView)
        val eventLoadingView = v.findViewById<LinearLayout>(R.id.eventLoadingView)
        val eventTodayView = v.findViewById<LinearLayout>(R.id.eventTodayView)
        val eventListView = v.findViewById<LinearLayout>(R.id.eventListView)

        val eventTodayTitle = v.findViewById<TextView>(R.id.eventTodayTitle)
        val eventTodayDate = v.findViewById<TextView>(R.id.eventTodayDate)
        val eventTodayDesc = v.findViewById<TextView>(R.id.eventTodayDesc)

        val checkInAsCharButton = v.findViewById<NavArrowButtonGreen>(R.id.checkInAsCharButton)
        val checkInAsNpcButton = v.findViewById<NavArrowButtonBlue>(R.id.checkInAsNPCButton)

        val eventTodayCheckedInAs = v.findViewById<TextView>(R.id.eventTodayCheckedInAs)

        val eventTitle = v.findViewById<TextView>(R.id.eventTitle)
        val eventDate = v.findViewById<TextView>(R.id.eventDate)
        val eventDesc = v.findViewById<TextView>(R.id.eventDesc)

        eventPrevButton = v.findViewById(R.id.event_prev_button)
        eventNextButton = v.findViewById(R.id.event_next_button)

        preregisterButton = v.findViewById(R.id.preregister_button)

        if (showEventsSection()) {
            eventView.isGone = false
            if (DataManager.shared.loadingEvents) {
                eventLoadingView.isGone = false
                eventTodayView.isGone = true
                eventListView.isGone = true
            } else if (DataManager.shared.events != null && DataManager.shared.events?.firstOrNull { it.isToday() } != null) {
                eventLoadingView.isGone = true
                eventTodayView.isGone = false
                eventListView.isGone = true

                DataManager.shared.events?.firstOrNull { it.isToday() }.ifLet {
                    eventTodayTitle.text = it.title
                    eventTodayDate.text = "${it.date.yyyyMMddToMonthDayYear()}\n${it.startTime} to ${it.endTime}"
                    eventTodayDesc.text = it.description

                    if (it.isStarted.toBoolean() && !it.isFinished.toBoolean()) {
                        if (!DataManager.shared.player?.isCheckedIn.toBoolean()) {
                            checkInAsCharButton.isGone = DataManager.shared.character == null
                            checkInAsCharButton.textView.text = "Check in as ${DataManager.shared.character?.fullName ?: ""}"
                            checkInAsNpcButton.isGone = false
                            eventTodayCheckedInAs.isGone = true
                            checkInAsCharButton.setOnClick {
                                DataManager.shared.checkinBarcodeModel = PlayerCheckInBarcodeModel(
                                    player = DataManager.shared.player?.getBarcodeModel()!!,
                                    character = DataManager.shared.character?.getBarcodeModel(),
                                    event = it.barcodeModel(),
                                    relevantSkills = DataManager.shared.character?.getRelevantBarcodeSkills() ?: arrayOf(),
                                    primaryWeapon = DataManager.shared.selectedCharacterGear?.primaryWeapon()
                                )
                                DataManager.shared.unrelaltedUpdateCallback = {
                                    DataManager.shared.load(lifecycleScope, listOf(DataManagerType.PLAYER, DataManagerType.CHARACTER, DataManagerType.INTRIGUE, DataManagerType.EVENTS), true) {
                                        setupViews(v)
                                    }
                                    setupViews(v)
                                }
                                val intent = Intent(v.context, CheckInBarcodeActivity::class.java)
                                startActivity(intent)
                            }
                            checkInAsNpcButton.setOnClick {
                                DataManager.shared.checkinBarcodeModel = PlayerCheckInBarcodeModel(
                                    player = DataManager.shared.player?.getBarcodeModel()!!,
                                    character = null,
                                    event = it.barcodeModel(),
                                    relevantSkills = arrayOf(),
                                    primaryWeapon = null
                                )
                                DataManager.shared.unrelaltedUpdateCallback = {
                                    DataManager.shared.load(lifecycleScope, listOf(DataManagerType.PLAYER, DataManagerType.EVENTS), true) {
                                        setupViews(v)
                                    }
                                    setupViews(v)
                                }
                                val intent = Intent(v.context, CheckInBarcodeActivity::class.java)
                                startActivity(intent)
                            }
                        } else {
                            checkInAsCharButton.isGone = true
                            checkInAsNpcButton.isGone = true
                            eventTodayCheckedInAs.isGone = false
                            var name = "NPC"
                            if (!DataManager.shared.player?.isCheckedInAsNpc.toBoolean()) {
                                name = DataManager.shared.character?.fullName ?: "UNKNOWN"
                            }
                            eventTodayCheckedInAs.text = "Checked in as $name"
                        }
                    } else {
                        checkInAsCharButton.isGone = true
                        checkInAsNpcButton.isGone = true
                        eventTodayCheckedInAs.isGone = true
                    }
                }
            } else {
                eventLoadingView.isGone = true
                eventTodayView.isGone = true
                eventListView.isGone = false
                DataManager.shared.currentEvent = DataManager.shared.events?.inChronologicalOrder()?.firstOrNull()
                DataManager.shared.currentEvent.ifLet {
                    eventTitle.text = it.title
                    eventDate.text = "${it.date.yyyyMMddToMonthDayYear()}\n${it.startTime} to ${it.endTime}"
                    eventDesc.text = it.description

                    preregisterButton.isGone = !it.isInFuture()
                    DataManager.shared.eventPreregs[it.id].ifLet({ preregs ->
                        preregs.firstOrNull { prereg -> prereg.playerId == DataManager.shared.player?.id }.ifLet({
                            preregisterButton.textView.text = "Edit Your Pre-Registration"
                        }, {
                            preregisterButton.textView.text = "Pre-Register\nFor This Event"
                        })
                    }, {
                        preregisterButton.textView.text = "Pre-Register\nFor This Event"
                    })
                    preregisterButton.setOnClick {
                        DataManager.shared.unrelaltedUpdateCallback = {
                            DataManager.shared.load(lifecycleScope, listOf(DataManagerType.EVENT_PREREGS), true) {
                                setupViews(v)
                            }
                        }
                        DataManager.shared.selectedEvent = it
                        val intent = Intent(v.context, PreregActivity::class.java)
                        startActivity(intent)
                    }

                }

                eventPrevButton.setOnClickListener {
                    if (eventIndex > 0) {
                        eventIndex--
                        DataManager.shared.currentEvent = DataManager.shared.events?.inChronologicalOrder()?.get(eventIndex)

                        DataManager.shared.currentEvent.ifLet {
                            eventTitle.text = it.title
                            eventDate.text = "${it.date.yyyyMMddToMonthDayYear()}\n${it.startTime} to ${it.endTime}"
                            eventDesc.text = it.description
                            preregisterButton.isGone = !it.isInFuture()
                            DataManager.shared.eventPreregs[it.id].ifLet({ preregs ->
                                preregs.firstOrNull { prereg -> prereg.playerId == DataManager.shared.player?.id }.ifLet({
                                    preregisterButton.textView.text = "Edit Your Pre-Registration"
                                }, {
                                    preregisterButton.textView.text = "Pre-Register\nFor This Event"
                                })
                            }, {
                                preregisterButton.textView.text = "Pre-Register\nFor This Event"
                            })
                            preregisterButton.setOnClick {
                                DataManager.shared.unrelaltedUpdateCallback = {
                                    DataManager.shared.load(lifecycleScope, listOf(DataManagerType.EVENT_PREREGS), true) {
                                        setupViews(v)
                                    }
                                }
                                DataManager.shared.selectedEvent = it
                                val intent = Intent(v.context, PreregActivity::class.java)
                                startActivity(intent)
                            }
                        }
                        showHideEventNavButtons()
                    }
                }
                eventNextButton.setOnClickListener {
                    if (eventIndex + 1 < (DataManager.shared.events?.inChronologicalOrder()?.size ?: 0)) {
                        eventIndex++
                        DataManager.shared.currentEvent = DataManager.shared.events?.inChronologicalOrder()?.get(eventIndex)

                        DataManager.shared.currentEvent.ifLet {
                            eventTitle.text = it.title
                            eventDate.text = "${it.date.yyyyMMddToMonthDayYear()}\n${it.startTime} to ${it.endTime}"
                            eventDesc.text = it.description
                            preregisterButton.isGone = !it.isInFuture()
                            DataManager.shared.eventPreregs[it.id].ifLet({ preregs ->
                                preregs.firstOrNull { prereg -> prereg.playerId == DataManager.shared.player?.id }.ifLet({
                                    preregisterButton.textView.text = "Edit Your Pre-Registration"
                                }, {
                                    preregisterButton.textView.text = "Pre-Register\nFor This Event"
                                })
                            }, {
                                preregisterButton.textView.text = "Pre-Register\nFor This Event"
                            })
                            preregisterButton.setOnClick {
                                DataManager.shared.unrelaltedUpdateCallback = {
                                    DataManager.shared.load(lifecycleScope, listOf(DataManagerType.EVENT_PREREGS), true) {
                                        setupViews(v)
                                    }
                                }
                                DataManager.shared.selectedEvent = it
                                val intent = Intent(v.context, PreregActivity::class.java)
                                startActivity(intent)
                            }
                        }
                        showHideEventNavButtons()
                    }
                }
                showHideEventNavButtons()
            }

        } else {
            eventView.isGone = true
        }
    }

    private fun prepareAwardsSection(v: View) {
        val awardsLoadingView = v.findViewById<LinearLayout>(R.id.awardsLoadingView)
        val awardsContainerView = v.findViewById<LinearLayout>(R.id.awardsContainerView)
        val noAwardsView = v.findViewById<LinearLayout>(R.id.noAwardsView)

        if (DataManager.shared.loadingAwards) {
            awardsLoadingView.isGone = false
            awardsContainerView.isGone = true
            noAwardsView.isGone = true
        } else if (DataManager.shared.awards?.isEmpty() == true) {
            awardsLoadingView.isGone = true
            awardsContainerView.isGone = true
            noAwardsView.isGone = false
        } else {
            awardsLoadingView.isGone = true
            awardsContainerView.isGone = false
            noAwardsView.isGone = true
            awardsContainerView.removeAllViews()

            DataManager.shared.awards.ifLet {
                it.forEachIndexed { index, awardModel ->
                    if (index != 0) {
                        val divider = MaterialDivider(v.context)
                        divider.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                        awardsContainerView.addView(divider)
                    }
                    val horLayout = LinearLayout(v.context)
                    horLayout.setPadding(0, 8, 0, 0)
                    horLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    horLayout.orientation = LinearLayout.HORIZONTAL

                    val dateView = TextView(v.context)
                    dateView.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    dateView.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
                    dateView.text = awardModel.date.yyyyMMddToMonthDayYear()

                    val amountView = TextView(v.context)
                    amountView.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    amountView.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_END
                    amountView.text = "${awardModel.amount} ${awardModel.awardType}"

                    horLayout.addView(dateView)
                    horLayout.addView(amountView)

                    val reasonView = TextView(v.context)
                    reasonView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    reasonView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                    reasonView.setPadding(0, 0, 0, 8)
                    reasonView.text = awardModel.reason

                    awardsContainerView.addView(horLayout)
                    awardsContainerView.addView(reasonView)
                }
            }

        }

    }

    private fun showIntrigue(): Boolean {
        return DataManager.shared.player?.isCheckedIn.toBoolean() && !DataManager.shared.player?.isCheckedInAsNpc.toBoolean() && (DataManager.shared.character?.getIntrigueSkills()?.size ?: 0) > 0 && !DataManager.shared.loadingIntrigue && DataManager.shared.intrigue != null
    }

    private fun showCheckout(): Boolean {
        return getCurrentEvent() == null && DataManager.shared.player?.isCheckedIn.toBoolean() && !DataManager.shared.loadingCharacter
    }

    private fun showCurrentCharSection(): Boolean {
        return !showIntrigue() && !showCheckout()
    }

    private fun showEventsSection(): Boolean {
        return DataManager.shared.loadingEvents || (DataManager.shared.events?.size ?: 0) > 0
    }

    private fun getCurrentEvent(): EventModel? {
        DataManager.shared.events?.let {
            return it.firstOrNull { ev -> ev.isStarted.toBoolean() && !ev.isFinished.toBoolean() }
        } ?: run {
            return null
        }
    }

    private fun showHideAnnouncementNavButtons() {
        if (DataManager.shared.currentAnnouncement != null) {
            prevButton.isGone = currentAnnouncementIndex == 0
            nextButton.isGone = currentAnnouncementIndex + 1 == (DataManager.shared.announcements?.size ?: 0)
        } else {
            prevButton.isGone = true
            nextButton.isGone = true
        }
    }

    private fun showHideEventNavButtons() {
        if (DataManager.shared.currentEvent != null) {
            eventPrevButton.isGone = eventIndex == 0
            eventNextButton.isGone = eventIndex + 1 == (DataManager.shared.events?.inChronologicalOrder()?.size ?: 0)
        } else {
            eventPrevButton.isGone = true
            eventNextButton.isGone = true
        }
    }

    companion object {

        @JvmStatic
        fun newInstance() =
            HomeFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}