package com.forkbombsquad.stillalivelarp.tabbar_fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
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
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey

import com.forkbombsquad.stillalivelarp.services.models.CharacterBarcodeModel
import com.forkbombsquad.stillalivelarp.services.models.EventModel
import com.forkbombsquad.stillalivelarp.services.models.PlayerCheckOutBarcodeModel
import com.forkbombsquad.stillalivelarp.services.models.SkillBarcodeModel
import com.forkbombsquad.stillalivelarp.utils.Constants
import com.forkbombsquad.stillalivelarp.utils.LoadingButton
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlue
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonGreen
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonRed
import com.forkbombsquad.stillalivelarp.utils.fragmentName
import com.forkbombsquad.stillalivelarp.utils.getFragmentOrActivityName
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

    private val TAG = "HOME_FRAGMENT"

    private var eventIndex = 0
    private var showAllEvents = false
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
    private lateinit var eventShowbutton: Button

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
                DataManager.shared.getCurrentPlayer().ifLet { player ->
                    player.eventAttendees.firstOrNull { it.isCheckedIn.toBoolean() }.ifLet({ eventAttendee ->
                        val barcodeModel = player.getCheckOutBarcodeModel(eventAttendee)
                        DataManager.shared.setUpdateCallback(HomeFragment::class) {
                            this@HomeFragment.buildViews(v)
                        }
                        DataManager.shared.setPassedData(HomeFragment::class, DataManagerPassedDataKey.CHECKOUT_BARCODE, barcodeModel)
                        val intent = Intent(v.context, CheckOutBarcodeActivity::class.java)
                        startActivity(intent)
                    }, {
                        checkoutButton.setLoading(false)
                    })
                }
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
        eventShowbutton = v.findViewById(R.id.event_show_button)

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
        eventShowbutton.setOnClickListener {
            eventIndex = 0
            showAllEvents = !showAllEvents
            buildViews(v)
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
        DataManager.shared.setTitleTextPotentiallyOffline(announcementsViewTitle, "Announcements")
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
        DataManager.shared.setTitleTextPotentiallyOffline(intrigueViewTitle, "Intrigue")

        DataManager.shared.getOngoingOrTodayEvent()?.intrigue.ifLet { intrigue ->
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
        currentCharacterLayout.isGone = !showCurrentCharSection()
        DataManager.shared.setTitleTextPotentiallyOffline(currentCharacterViewTitle, "Current Character")

        DataManager.shared.getActiveCharacter().ifLet({ char ->
            currentCharacterNameLayout.isGone = false
            currentCharacterNoneLayout.isGone = true
            createCharacterButton.isGone = true
            currentCharacterNameText.text = char.fullName
        }, {
            currentCharacterNameLayout.isGone = true
            currentCharacterNoneLayout.isGone = false
            createCharacterButton.isGone = false
        })
    }

    private fun prepareEventsSection() {
        eventLayout.isGone = !showEventsSection()
        DataManager.shared.setTitleTextPotentiallyOffline(eventTodayViewTitle, "Event Today!")
        DataManager.shared.setTitleTextPotentiallyOffline(eventListViewTitle, "Events")

        DataManager.shared.getOngoingOrTodayEvent().ifLet({ event ->
            eventTodayLayout.isGone = false
            eventListLayout.isGone = true
            eventTodayTitle.text = event.title
            eventTodayDate.text = "${event.date.yyyyMMddToMonthDayYear()}\n${event.startTime} to ${event.endTime}"
            eventTodayDesc.text = event.description

            if (event.isOngoing()) {

                if (DataManager.shared.getCurrentPlayer()?.isCheckedIn == true) {
                    checkedInAs.isGone = false
                    event.attendees.firstOrNull { it.playerId == DataManager.shared.getCurrentPlayer()?.id }.ifLet { attendee ->
                        DataManager.shared.getCurrentPlayer()?.characters?.firstOrNull { it.id == attendee.characterId }.ifLet({ character ->
                            checkedInAs.text = "Checked in as ${character.fullName}"
                        }, {
                            checkedInAs.text = "Checked in as NPC"
                        })
                    }
                } else {
                    checkedInAs.isGone = true
                    checkInAsCharButton.isGone = DataManager.shared.getActiveCharacter() == null
                    checkInAsNpcButton.isGone = false
                }
            } else {
                checkInAsCharButton.isGone = true
                checkInAsNpcButton.isGone = true
                checkedInAs.isGone = true
            }
        }, {
            eventTodayLayout.isGone = false
            eventListLayout.isGone = true
            val events = showAllEvents.ternary(DataManager.shared.events, DataManager.shared.getRelevantEvents())
            events.getOrNull(eventIndex).ifLet { event ->
                eventListTitle.text = event.title
                eventListDate.text = "${event.date.yyyyMMddToMonthDayYear()}\n${event.startTime} to ${event.endTime}"
                eventListDesc.text = event.description

                if (event.isRelevant()) {
                    preregButton.isGone = false
                    DataManager.shared.getCurrentPlayer()?.preregs?.firstOrNull { it.eventId == event.id }.ifLet({ prereg ->
                        preregButton.textView.text = "Edit Your Pre-Registration"

                        eventListPreregDesc.isGone = false
                        val char = DataManager.shared.getCurrentPlayer()?.characters?.firstOrNull { it.id == prereg.getCharId() }
                        val regType = prereg.eventRegType().getAttendingText()
                        eventListPreregDesc.text = "You are pre-registered for this event as:\n\n${char?.fullName ?: "NPC"} - ${regType}"
                    }, {
                        preregButton.textView.text = "Pre-Register For This Event"
                        eventListPreregDesc.isGone = true
                    })
                } else {
                    preregButton.isGone = true
                    eventListPreregDesc.isGone = true
                }
                eventShowbutton.text = showAllEvents.ternary("Show Only\nRelevant\nEvents", "Show\nAll\nEvents")
            }
            nextEventButton.isGone = eventIndex + 1 == events.size
            previousEventButton.isGone = eventIndex == 0
        })
    }

    private fun prepareAwardsSection(v: View) {
        val awards = DataManager.shared.getCurrentPlayer()?.getAwardsSorted() ?: listOf()
        DataManager.shared.setTitleTextPotentiallyOffline(awardsTitle, "Awards")
        awardsInnerLayout.isGone = awards.isEmpty()
        noAwardsLayout.isGone = awards.isNotEmpty()

        if (awards.isNotEmpty()) {
            awardsInnerLayout.removeAllViews()
            awards.forEachIndexed { index, award ->
                if (index != 0) {
                    val divider = MaterialDivider(v.context)
                    divider.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    awardsInnerLayout.addView(divider)
                }
                val horLayout = LinearLayout(v.context)
                horLayout.setPadding(0, 8, 0, 0)
                horLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                horLayout.orientation = LinearLayout.HORIZONTAL

                val nameView = TextView(v.context)
                nameView.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                nameView.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
                DataManager.shared.getCurrentPlayer()?.characters?.firstOrNull { it.id == award.characterId }.ifLet({ char ->
                    nameView.text = char.fullName
                }, {
                    nameView.text = DataManager.shared.getCurrentPlayer()?.fullName ?: ""
                })

                val dateView = TextView(v.context)
                dateView.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                dateView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                dateView.text = award.date.yyyyMMddToMonthDayYear()

                val amountView = TextView(v.context)
                amountView.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                amountView.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_END
                amountView.text = "${award.amount} ${award.getDisplayText()}"

                horLayout.addView(nameView)
                horLayout.addView(dateView)
                horLayout.addView(amountView)

                val reasonView = TextView(v.context)
                reasonView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                reasonView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                reasonView.setPadding(0, 0, 0, 8)
                reasonView.text = award.reason

                awardsInnerLayout.addView(horLayout)
                awardsInnerLayout.addView(reasonView)
            }
        }
    }

    private fun showIntrigue(): Boolean {
        return (DataManager.shared.getActiveCharacter()?.getIntrigueSkills()?.count() ?: 0) > 0 && DataManager.shared.getOngoingOrTodayEvent()?.intrigue != null
    }

    private fun showCheckout(): Boolean {
        return !DataManager.shared.offlineMode && DataManager.shared.getOngoingEvent() == null && DataManager.shared.getCurrentPlayer()?.isCheckedIn == true
    }

    private fun showCurrentCharSection(): Boolean {
        return !(DataManager.shared.getCurrentPlayer()?.isCheckedIn ?: true)
    }

    private fun showEventsSection(): Boolean {
        return if (showAllEvents) {
            DataManager.shared.events.isNotEmpty()
        } else {
            DataManager.shared.getRelevantEvents().isNotEmpty()
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