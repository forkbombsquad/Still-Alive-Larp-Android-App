package com.forkbombsquad.stillalivelarp.tabbar_fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.forkbombsquad.stillalivelarp.CheckInBarcodeActivity
import com.forkbombsquad.stillalivelarp.CheckOutBarcodeActivity
import com.forkbombsquad.stillalivelarp.CreateCharacterActivity
import com.forkbombsquad.stillalivelarp.PreregActivity
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.OldDataManager
import com.forkbombsquad.stillalivelarp.services.managers.OldDataManagerType
import com.forkbombsquad.stillalivelarp.services.models.CharacterBarcodeModel
import com.forkbombsquad.stillalivelarp.services.models.EventModel
import com.forkbombsquad.stillalivelarp.services.models.PlayerCheckInBarcodeModel
import com.forkbombsquad.stillalivelarp.services.models.PlayerCheckOutBarcodeModel
import com.forkbombsquad.stillalivelarp.services.models.SkillBarcodeModel
import com.forkbombsquad.stillalivelarp.utils.Constants
import com.forkbombsquad.stillalivelarp.utils.LoadingButton
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlue
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonGreen
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonRed
import com.forkbombsquad.stillalivelarp.utils.globalGetContext
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.inChronologicalOrder
import com.forkbombsquad.stillalivelarp.utils.ternary
import com.forkbombsquad.stillalivelarp.utils.yyyyMMddToMonthDayYear
import com.google.android.material.divider.MaterialDivider

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    // TODO get rid of all of the old data manger stuff an use the new stuff

    private val TAG = "HOME_FRAGMENT"

    private var eventIndex = 0
    private var currentAnnouncementIndex = 0

    private var prevButton = Button(globalGetContext())
    private var nextButton = Button(globalGetContext())

    private var eventPrevButton = Button(globalGetContext())
    private var eventNextButton = Button(globalGetContext())

    private lateinit var pullToRefresh: SwipeRefreshLayout

    private lateinit var preregisterButton: NavArrowButtonBlue

    private lateinit var preregInfo: TextView

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

            DataManager.shared.load(lifecycleScope, stepFinished = {
                setupViews(v)
            }, finished = {
                setupViews(v)
                pullToRefresh.isRefreshing = false
            })
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

        val announcements = OldDataManager.shared.announcements ?: listOf()

        prevButton.setOnClickListener {
            announcements.ifLet {
                if (currentAnnouncementIndex > 0) {
                    currentAnnouncementIndex--
                    AnnouncementManager.shared.getAnnouncement(lifecycleScope, it[currentAnnouncementIndex].id) { an ->
                        OldDataManager.shared.currentAnnouncement = an
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
                        OldDataManager.shared.currentAnnouncement = an
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

        OldDataManager.shared.currentAnnouncement.ifLet({
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

        val investigator = v.findViewById<TextView>(R.id.intrigue_investigatorText)
        val interrogator = v.findViewById<TextView>(R.id.intrigue_interrogatorText)

        OldDataManager.shared.intrigue.ifLet({
            if (showIntrigue()) {
                val intrigueSkills: IntArray = OldDataManager.shared.character?.getIntrigueSkills() ?: IntArray(0)
                investigator.text = it.investigatorMessage
                interrogator.text = it.interrogatorMessage
                investigatorView.isGone = intrigueSkills.firstOrNull { id -> id == Constants.SpecificSkillIds.investigator } == null
                interrogatorView.isGone = intrigueSkills.firstOrNull { id -> id == Constants.SpecificSkillIds.interrogator } == null
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
                OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.EVENT_ATTENDEES), true) {
                    checkoutButton.setLoading(false)
                    OldDataManager.shared.eventAttendeesForPlayer.ifLet({ eventAttendees ->
                        eventAttendees.firstOrNull { it.isCheckedIn.toBoolean() }.ifLet({

                            var char: CharacterBarcodeModel? = null
                            var relevantSkills: Array<SkillBarcodeModel> = arrayOf()

                            if (!it.asNpc.toBoolean()) {
                                char = OldDataManager.shared.character?.getBarcodeModel()
                                relevantSkills = OldDataManager.shared.character?.getRelevantBarcodeSkills() ?: arrayOf()
                            }

                            OldDataManager.shared.checkoutBarcodeModel = PlayerCheckOutBarcodeModel(
                                player = OldDataManager.shared.player!!.getBarcodeModel(),
                                character = char,
                                eventAttendeeId = it.id,
                                eventId = it.eventId,
                                relevantSkills = relevantSkills
                            )
                            OldDataManager.shared.unrelaltedUpdateCallback = {
                                OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.PLAYER, OldDataManagerType.CHARACTER, OldDataManagerType.INTRIGUE, OldDataManagerType.EVENTS), true, finishedStep = {
                                    setupViews(v)
                                }) {
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
            if (OldDataManager.shared.loadingCharacter) {
                currentCharLoadingView.isGone = false
                currentCharNameView.isGone = true
                currentCharNoneView.isGone = true
            } else if (OldDataManager.shared.character == null) {
                currentCharLoadingView.isGone = true
                currentCharNameView.isGone = true
                currentCharNoneView.isGone = false
                charCreateButton.setOnClick {
                    OldDataManager.shared.unrelaltedUpdateCallback = {
                        OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.PLAYER, OldDataManagerType.CHARACTER), true) {
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

                currentCharNameText.text = OldDataManager.shared.character?.fullName
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

        preregInfo = v.findViewById(R.id.prereg_info)

        if (showEventsSection()) {
            eventView.isGone = false
            val event = OldDataManager.shared.events?.firstOrNull { it.isToday() } ?: OldDataManager.shared.events?.firstOrNull { it.isStarted.toBoolean() && !it.isFinished.toBoolean() }
            if (OldDataManager.shared.loadingEvents) {
                eventLoadingView.isGone = false
                eventTodayView.isGone = true
                eventListView.isGone = true
            } else if (event != null) {
                eventLoadingView.isGone = true
                eventTodayView.isGone = false
                eventListView.isGone = true

                event.ifLet {
                    eventTodayTitle.text = it.title
                    eventTodayDate.text = "${it.date.yyyyMMddToMonthDayYear()}\n${it.startTime} to ${it.endTime}"
                    eventTodayDesc.text = it.description

                    if (it.isStarted.toBoolean() && !it.isFinished.toBoolean()) {
                        if (!OldDataManager.shared.player?.isCheckedIn.toBoolean()) {
                            checkInAsCharButton.isGone = OldDataManager.shared.character == null
                            checkInAsCharButton.textView.text = "Check in as ${OldDataManager.shared.character?.fullName ?: ""}"
                            checkInAsNpcButton.isGone = false
                            eventTodayCheckedInAs.isGone = true
                            checkInAsCharButton.setOnClick {
                                OldDataManager.shared.checkinBarcodeModel = PlayerCheckInBarcodeModel(
                                    player = OldDataManager.shared.player?.getBarcodeModel()!!,
                                    character = OldDataManager.shared.character?.getBarcodeModel(),
                                    event = it.barcodeModel(),
                                    relevantSkills = OldDataManager.shared.character?.getRelevantBarcodeSkills() ?: arrayOf(),
                                    gear = OldDataManager.shared.selectedCharacterGear?.firstOrNull()
                                )
                                OldDataManager.shared.unrelaltedUpdateCallback = {
                                    OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.PLAYER, OldDataManagerType.CHARACTER, OldDataManagerType.INTRIGUE, OldDataManagerType.EVENTS), true, finishedStep = {
                                        setupViews(v)
                                    }) {
                                        setupViews(v)
                                    }
                                    setupViews(v)
                                }
                                val intent = Intent(v.context, CheckInBarcodeActivity::class.java)
                                startActivity(intent)
                            }
                            checkInAsNpcButton.setOnClick {
                                OldDataManager.shared.checkinBarcodeModel = PlayerCheckInBarcodeModel(
                                    player = OldDataManager.shared.player?.getBarcodeModel()!!,
                                    character = null,
                                    event = it.barcodeModel(),
                                    relevantSkills = arrayOf(),
                                    gear = null
                                )
                                OldDataManager.shared.unrelaltedUpdateCallback = {
                                    OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.PLAYER, OldDataManagerType.EVENTS), true) {
                                        setupViews(v)
                                    }
                                    setupViews(v)
                                }
                                val intent = Intent(v.context, CheckInBarcodeActivity::class.java)
                                startActivity(intent)
                            }
                        } else if (!OldDataManager.shared.loadingCharacter) {
                            checkInAsCharButton.isGone = true
                            checkInAsNpcButton.isGone = true
                            eventTodayCheckedInAs.isGone = false
                            var name = "NPC"
                            if (!OldDataManager.shared.player?.isCheckedInAsNpc.toBoolean()) {
                                name = OldDataManager.shared.character?.fullName ?: "UNKNOWN"
                            }
                            eventTodayCheckedInAs.text = "Checked in as $name"
                        } else {
                            eventTodayCheckedInAs.isGone = false
                            eventTodayCheckedInAs.text = "Loading Check In Information..."
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
                OldDataManager.shared.currentEvent = OldDataManager.shared.events?.inChronologicalOrder()?.firstOrNull()
                OldDataManager.shared.currentEvent.ifLet { ce ->
                    eventTitle.text = ce.title
                    eventDate.text = "${ce.date.yyyyMMddToMonthDayYear()}\n${ce.startTime} to ${ce.endTime}"
                    eventDesc.text = ce.description

                    buildPreregSection(ce)

                    preregisterButton.setOnClick {
                        OldDataManager.shared.unrelaltedUpdateCallback = {
                            OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.EVENT_PREREGS), true) {
                                setupViews(v)
                            }
                        }
                        OldDataManager.shared.selectedEvent = ce
                        val intent = Intent(v.context, PreregActivity::class.java)
                        startActivity(intent)
                    }
                }

                eventPrevButton.setOnClickListener {
                    if (eventIndex > 0) {
                        eventIndex--
                        OldDataManager.shared.currentEvent = OldDataManager.shared.events?.inChronologicalOrder()?.get(eventIndex)

                        OldDataManager.shared.currentEvent.ifLet {
                            eventTitle.text = it.title
                            eventDate.text = "${it.date.yyyyMMddToMonthDayYear()}\n${it.startTime} to ${it.endTime}"
                            eventDesc.text = it.description

                            buildPreregSection(it)
                        }

                        showHideEventNavButtons()
                    }
                }
                eventNextButton.setOnClickListener {
                    if (eventIndex + 1 < (OldDataManager.shared.events?.inChronologicalOrder()?.size ?: 0)) {
                        eventIndex++
                        OldDataManager.shared.currentEvent = OldDataManager.shared.events?.inChronologicalOrder()?.get(eventIndex)

                        OldDataManager.shared.currentEvent.ifLet {
                            eventTitle.text = it.title
                            eventDate.text = "${it.date.yyyyMMddToMonthDayYear()}\n${it.startTime} to ${it.endTime}"
                            eventDesc.text = it.description

                            buildPreregSection(it)
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

    private fun buildPreregSection(event: EventModel) {
        preregisterButton.isGone = !event.isInFuture()
        preregisterButton.setLoading(OldDataManager.shared.loadingEventPreregs)
        if (OldDataManager.shared.loadingEventPreregs) {
            preregisterButton.textView.text = "Loading Preregistrations..."
            preregInfo.isGone = true
        } else {
            OldDataManager.shared.eventPreregs[event.id].ifLet({ preregs ->
                preregs.firstOrNull { prereg -> prereg.playerId == OldDataManager.shared.player?.id }.ifLet({ eventPrereg ->
                    preregisterButton.textView.text = "Edit Your Pre-Registration"
                    preregInfo.isGone = false
                    preregInfo.text = "You are pre-registered for this event as:\n\n${(eventPrereg.getCharId() == null).ternary("NPC", "${OldDataManager.shared.character?.fullName ?: ""}")} - ${eventPrereg.eventRegType()}"
                }, {
                    preregisterButton.textView.text = "Pre-Register\nFor This Event"
                    preregInfo.isGone = true
                })
            }, {
                preregInfo.isGone = true
                preregisterButton.textView.text = "Pre-Register\nFor This Event"
            })
        }
    }

    private fun prepareAwardsSection(v: View) {
        val awardsLoadingView = v.findViewById<LinearLayout>(R.id.awardsLoadingView)
        val awardsContainerView = v.findViewById<LinearLayout>(R.id.awardsContainerView)
        val noAwardsView = v.findViewById<LinearLayout>(R.id.noAwardsView)

        if (OldDataManager.shared.loadingAwards) {
            awardsLoadingView.isGone = false
            awardsContainerView.isGone = true
            noAwardsView.isGone = true
        } else if (OldDataManager.shared.awards?.isEmpty() == true) {
            awardsLoadingView.isGone = true
            awardsContainerView.isGone = true
            noAwardsView.isGone = false
        } else {
            awardsLoadingView.isGone = true
            awardsContainerView.isGone = false
            noAwardsView.isGone = true
            awardsContainerView.removeAllViews()

            OldDataManager.shared.awards.ifLet {
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
        return OldDataManager.shared.player?.isCheckedIn.toBoolean() && !OldDataManager.shared.player?.isCheckedInAsNpc.toBoolean() && (OldDataManager.shared.character?.getIntrigueSkills()?.size ?: 0) > 0 && !OldDataManager.shared.loadingIntrigue && OldDataManager.shared.intrigue != null
    }

    private fun showCheckout(): Boolean {
        return getCurrentEvent() == null && OldDataManager.shared.player?.isCheckedIn.toBoolean() && !OldDataManager.shared.loadingCharacter
    }

    private fun showCurrentCharSection(): Boolean {
        var value = true
        OldDataManager.shared.player.ifLet{ player ->
            if (player.isCheckedIn.toBoolean()) {
                if (!OldDataManager.shared.loadingCharacter && OldDataManager.shared.character == null) {
                    value = false
                }
            }
        }
        return value
    }

    private fun showEventsSection(): Boolean {
        return OldDataManager.shared.loadingEvents || (OldDataManager.shared.events?.size ?: 0) > 0
    }

    private fun getCurrentEvent(): EventModel? {
        OldDataManager.shared.events?.let {
            return it.firstOrNull { ev -> ev.isStarted.toBoolean() && !ev.isFinished.toBoolean() }
        } ?: run {
            return null
        }
    }

    private fun showHideAnnouncementNavButtons() {
        if (OldDataManager.shared.currentAnnouncement != null) {
            prevButton.isGone = currentAnnouncementIndex == 0
            nextButton.isGone = currentAnnouncementIndex + 1 == (OldDataManager.shared.announcements?.size ?: 0)
        } else {
            prevButton.isGone = true
            nextButton.isGone = true
        }
    }

    private fun showHideEventNavButtons() {
        if (OldDataManager.shared.currentEvent != null) {
            eventPrevButton.isGone = eventIndex == 0
            eventNextButton.isGone = eventIndex + 1 == (OldDataManager.shared.events?.inChronologicalOrder()?.size ?: 0)
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