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

    private lateinit var pullToRefresh: SwipeRefreshLayout

    private lateinit var loadingLayout: LinearLayout
    private lateinit var loadingTextView: TextView

    private lateinit var announcementsLayout: LinearLayout
    private lateinit var announcementsViewTitle: TextView
    private lateinit var announcementTitle: TextView
    private lateinit var announcementDate: TextView
    private lateinit var announcementDesc: TextView
    private lateinit var announcementNextButton: Button
    private lateinit var announcementPreviousButton: Button

    private lateinit var intrigueLayout: LinearLayout
    private lateinit var intrigueViewTitle: TextView
    private lateinit var intrigueInvestigatorLayout: LinearLayout
    private lateinit var intrigueInvestigator: TextView
    private lateinit var intrigueInterrogatorLayout: LinearLayout
    private lateinit var intrigueInterrogator: TextView

    private lateinit var checkoutLayout: LinearLayout
    private lateinit var checkoutButton: NavArrowButtonRed

    private lateinit var currentCharacterLayout: LinearLayout
    private lateinit var currentCharacterViewTitle: TextView
    private lateinit var currentCharacterNameLayout: LinearLayout
    private lateinit var currentCharacterNameText: TextView
    private lateinit var currentCharacterNoneLayout: LinearLayout
    private lateinit var createCharacterButton: LoadingButton

    private lateinit var eventLayout: LinearLayout
    private lateinit var eventTodayLayout: LinearLayout
    private lateinit var eventTodayViewTitle: TextView
    private lateinit var eventTodayTitle: TextView
    private lateinit var eventTodayDate: TextView
    private lateinit var eventTodayDesc: TextView
    private lateinit var checkInAsCharButton: NavArrowButtonGreen
    private lateinit var checkInAsNpcButton: NavArrowButtonBlue
    private lateinit var checkedInAs: TextView

    private lateinit var eventListLayout: LinearLayout
    private lateinit var eventListViewTitle: TextView
    private lateinit var eventListTitle: TextView
    private lateinit var eventListDate: TextView
    private lateinit var eventListDesc: TextView
    private lateinit var preregButton: NavArrowButtonBlue
    private lateinit var eventListPreregDesc: TextView
    private lateinit var previousEventButton: Button
    private lateinit var nextEventButton: Button

    private lateinit var awardsLayout: LinearLayout
    private lateinit var awardsTitle: TextView
    private lateinit var awardsInnerLayout: LinearLayout
    private lateinit var noAwardsLayout: LinearLayout

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
        pullToRefresh = v.findViewById(R.id.pulltorefresh_home)
        pullToRefresh.setOnRefreshListener {

            DataManager.shared.load(lifecycleScope, stepFinished = {
                buildViews(v)
            }, finished = {
                buildViews(v)
                pullToRefresh.isRefreshing = false
            })
            buildViews(v)
        }

        // Setup Loading View
        loadingLayout = v.findViewById(R.id.loadingView)
        loadingTextView = v.findViewById(R.id.loadingText)

        // Announcements
        announcementsLayout = v.findViewById(R.id.announcementslayout)
        announcementsViewTitle = v.findViewById(R.id.announcement_section_title)

        announcementTitle = v.findViewById(R.id.announcement_title)
        announcementDate = v.findViewById(R.id.announcement_date)
        announcementDesc = v.findViewById(R.id.announcement_desc)
        announcementPreviousButton = v.findViewById(R.id.announcement_prev_button)
        announcementNextButton = v.findViewById(R.id.announcement_next_button)

        announcementPreviousButton.setOnClickListener {
            if (currentAnnouncementIndex > 0) {
                currentAnnouncementIndex--
                buildViews(v)
            }
        }

        announcementNextButton.setOnClickListener {
            if (currentAnnouncementIndex + 1 < DataManager.shared.announcements.size) {
                currentAnnouncementIndex++
                buildViews(v)
            }
        }

        // Intrigue
        intrigueLayout = v.findViewById(R.id.intrigueView)
        intrigueViewTitle = v.findViewById(R.id.intriguetitle)
        intrigueInvestigatorLayout = v.findViewById(R.id.intrigue_investigatorView)
        intrigueInterrogatorLayout = v.findViewById(R.id.intrigue_interrogatorView)

        intrigueInvestigator = v.findViewById(R.id.intrigue_investigatorText)
        intrigueInterrogator = v.findViewById(R.id.intrigue_interrogatorText)

        // Checkout
        checkoutLayout = v.findViewById<LinearLayout>(R.id.checkoutView)
        checkoutButton = v.findViewById<NavArrowButtonRed>(R.id.checkout_navarrow)

        checkoutButton.setOnClick {
            checkoutButton.setLoading(true)
            DataManager.shared.load(lifecycleScope) {
                DataManager.shared.getCurrentPlayer()?.eventAttendees?.firstOrNull { it.isCheckedIn.toBoolean() }.ifLet({ eventAttendee ->
                    var char: CharacterBarcodeModel? = null
                    var relevantSkills: Array<SkillBarcodeModel> = arrayOf()

                    if (!eventAttendee.asNpc.toBoolean()) {
                        DataManager.shared.getCurrentPlayer()?.characters?.firstOrNull { it.id == eventAttendee.characterId }.ifLet { char ->
                            // TODO
//                              char = char.barcodeModel()
//                              relevantSkills = char.getRelevantBarcodeSkills()
                        }
                    }

                    // TODO
//                        OldDataManager.shared.checkoutBarcodeModel = PlayerCheckOutBarcodeModel(
//                            player = OldDataManager.shared.player!!.getBarcodeModel(),
//                            character = char,
//                            eventAttendeeId = it.id,
//                            eventId = it.eventId,
//                            relevantSkills = relevantSkills
//                        )
                    // TODO
//                        OldDataManager.shared.unrelaltedUpdateCallback = {
//                            OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.PLAYER, OldDataManagerType.CHARACTER, OldDataManagerType.INTRIGUE, OldDataManagerType.EVENTS), true, finishedStep = {
//                                buildViews(v)
//                            }) {
//                                buildViews(v)
//                            }
//                            buildViews(v)
//                        }
                    val intent = Intent(v.context, CheckOutBarcodeActivity::class.java)
                    startActivity(intent)
                }, {
                    checkoutButton.setLoading(false)
                })
            }
        }

        // Current Character View
        currentCharacterLayout = v.findViewById(R.id.currentCharacterView)
        currentCharacterViewTitle = v.findViewById(R.id.currentchartitle)
        currentCharacterNameLayout = v.findViewById(R.id.currentCharNameView)
        currentCharacterNameText = v.findViewById(R.id.currentCharNameText)
        currentCharacterNoneLayout = v.findViewById(R.id.currentCharNoneView)
        createCharacterButton = v.findViewById(R.id.currentCharCreateNewCharButton)

        createCharacterButton.setOnClick {
            // TODO
//            OldDataManager.shared.unrelaltedUpdateCallback = {
//                OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.PLAYER, OldDataManagerType.CHARACTER), true) {
//                    buildViews(v)
//                }
//                buildViews(v)
//            }
            val intent = Intent(v.context, CreateCharacterActivity::class.java)
            startActivity(intent)
        }

        // Events View
        eventLayout = v.findViewById(R.id.eventView)
        eventTodayLayout = v.findViewById(R.id.eventTodayView)
        eventTodayViewTitle = v.findViewById(R.id.eventstodayviewtitle)
        eventTodayTitle = v.findViewById(R.id.eventTodayTitle)
        eventTodayDate = v.findViewById(R.id.eventTodayDate)
        eventTodayDesc = v.findViewById(R.id.eventTodayDesc)
        checkInAsCharButton = v.findViewById(R.id.checkInAsCharButton)
        checkInAsNpcButton = v.findViewById(R.id.checkInAsNPCButton)
        checkedInAs = v.findViewById(R.id.eventTodayCheckedInAs)

        eventListLayout = v.findViewById(R.id.eventListView)
        eventListViewTitle = v.findViewById(R.id.eventslistviewtitle)
        eventListTitle = v.findViewById(R.id.eventTitle)
        eventListDate = v.findViewById(R.id.eventDate)
        eventListDesc = v.findViewById(R.id.eventDesc)
        preregButton = v.findViewById(R.id.preregister_button)
        eventListPreregDesc = v.findViewById(R.id.prereg_info)
        previousEventButton = v.findViewById(R.id.event_prev_button)
        nextEventButton = v.findViewById(R.id.event_next_button)

        checkInAsCharButton.setOnClick {
            // TODO
//            OldDataManager.shared.checkinBarcodeModel = PlayerCheckInBarcodeModel(
//                player = OldDataManager.shared.player?.getBarcodeModel()!!,
//                character = OldDataManager.shared.character?.getBarcodeModel(),
//                event = it.barcodeModel(),
//                relevantSkills = OldDataManager.shared.character?.getRelevantBarcodeSkills() ?: arrayOf(),
//                gear = OldDataManager.shared.selectedCharacterGear?.firstOrNull()
//            )
//            OldDataManager.shared.unrelaltedUpdateCallback = {
//                OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.PLAYER, OldDataManagerType.CHARACTER, OldDataManagerType.INTRIGUE, OldDataManagerType.EVENTS), true, finishedStep = {
//                    buildViews(v)
//                }) {
//                    buildViews(v)
//                }
//                buildViews(v)
//            }
            val intent = Intent(v.context, CheckInBarcodeActivity::class.java)
            startActivity(intent)
        }
        checkInAsNpcButton.setOnClick {
            // TODO
//            OldDataManager.shared.checkinBarcodeModel = PlayerCheckInBarcodeModel(
//                player = OldDataManager.shared.player?.getBarcodeModel()!!,
//                character = null,
//                event = it.barcodeModel(),
//                relevantSkills = arrayOf(),
//                gear = null
//            )
//            OldDataManager.shared.unrelaltedUpdateCallback = {
//                OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.PLAYER, OldDataManagerType.EVENTS), true) {
//                    buildViews(v)
//                }
//                buildViews(v)
//            }
            val intent = Intent(v.context, CheckInBarcodeActivity::class.java)
            startActivity(intent)
        }

        preregButton.setOnClick {
            // TODO
//            OldDataManager.shared.unrelaltedUpdateCallback = {
//                OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.EVENT_PREREGS), true) {
//                    buildViews(v)
//                }
//            }
//            OldDataManager.shared.selectedEvent = ce
            val intent = Intent(v.context, PreregActivity::class.java)
            startActivity(intent)
        }

        previousEventButton.setOnClickListener {
            if (eventIndex > 0) {
                eventIndex--
                buildViews(v)
            }
        }
        nextEventButton.setOnClickListener {
            if (eventIndex + 1 < DataManager.shared.events.count()) {
                eventIndex++
                buildViews(v)
            }
        }

        // Awards
        awardsLayout = v.findViewById(R.id.awardView)
        awardsTitle = v.findViewById(R.id.awardsviewtitle)
        awardsInnerLayout = v.findViewById(R.id.awardsContainerView)
        noAwardsLayout = v.findViewById(R.id.noAwardsView)

        buildViews(v)
    }

    private fun buildViews(v: View) {
        // TODO this won't work because view aren't actually hidden, They're just redrawn here. Hide them in loading and show them when not
        if (DataManager.shared.loading) {
            // Loading Stuff
            loadingLayout.isGone = false
            announcementsLayout.isGone = true
            intrigueLayout.isGone = true
            checkoutLayout.isGone = true
            currentCharacterLayout.isGone = true
            eventLayout.isGone = true
            awardsLayout.isGone = true
            prepareLoadingSection()
        } else {
            loadingLayout.isGone = true
            announcementsLayout.isGone = false
            intrigueLayout.isGone = false
            checkoutLayout.isGone = false
            currentCharacterLayout.isGone = false
            eventLayout.isGone = false
            awardsLayout.isGone = false
            // Announcements
            prepareAnnouncementsSection()

            // Intrigue
            prepareIntrigueSection()

            // Checkout
            prepareCheckoutSection()

            // Current Char View
            prepareCurrentCharSection()

            // Events View
            prepareEventsSection()

            // Awards View
            prepareAwardsSection(v)
        }
    }

    private fun prepareLoadingSection() {
        loadingTextView.text = DataManager.shared.loadingText
    }

    private fun prepareAnnouncementsSection() {
        announcementsViewTitle.text = DataManager.shared.offlineMode.ternary("Offline Announcements", "Announcements")

        DataManager.shared.announcements.getOrNull(currentAnnouncementIndex).ifLet({
            announcementTitle.text = it.title
            announcementDate.text = it.date.yyyyMMddToMonthDayYear()
            announcementDesc.text = it.text
            announcementNextButton.isGone = currentAnnouncementIndex + 1 == DataManager.shared.announcements.size
            announcementPreviousButton.isGone = currentAnnouncementIndex == 0
        }, {
            announcementsLayout.isGone = true
        })
    }

    private fun prepareIntrigueSection() {
        intrigueLayout.isGone = !showIntrigue()
        intrigueViewTitle.text = DataManager.shared.offlineMode.ternary("Offline Intrigue", "Intrigue")

        DataManager.shared.getStartedOrTodayEvent()?.intrigue.ifLet { intrigue ->
            val intrigueSkills: List<Int> = DataManager.shared.getActiveCharacter()?.getIntrigueSkills() ?: listOf()
            intrigueInvestigator.text = intrigue.investigatorMessage
            intrigueInterrogator.text = intrigue.interrogatorMessage
            intrigueInvestigatorLayout.isGone = intrigueSkills.firstOrNull { id -> id == Constants.SpecificSkillIds.investigator } == null
            intrigueInterrogatorLayout.isGone = intrigueSkills.firstOrNull { id -> id == Constants.SpecificSkillIds.interrogator } == null
        }
    }

    private fun prepareCheckoutSection() {
        checkoutLayout.isGone = !showCheckout()
    }

    private fun prepareCurrentCharSection() {
        // TODO
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

    private fun prepareEventsSection() {
        // TODO

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
        // TODO

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
        return (DataManager.shared.getActiveCharacter()?.getIntrigueSkills()?.count() ?: 0) > 0 && DataManager.shared.getStartedOrTodayEvent()?.intrigue != null
    }

    private fun showCheckout(): Boolean {
        return !DataManager.shared.offlineMode && DataManager.shared.getStartedEvent() == null && DataManager.shared.getCurrentPlayer()?.isCheckedIn == true
    }

    private fun showCurrentCharSection(): Boolean {
        return !(DataManager.shared.getCurrentPlayer()?.isCheckedIn ?: true)
    }

    private fun showEventsSection(): Boolean {
        return  DataManager.shared.events.isNotEmpty()
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