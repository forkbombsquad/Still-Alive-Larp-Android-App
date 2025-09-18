package com.forkbombsquad.stillalivelarp.utils.mockdata

import java.io.File

class MockData {
    class TokenResponses {
        companion object {
            private const val PATH = "Token"
            val NEVER_EXPIRE: String
                get() = loadJsonFile(PATH, "validNeverExpire")

            val EXPIRED: String
                get() = loadJsonFile(PATH, "validExpired")
        }
    }

    class VersionResponses {
        companion object {
            private const val PATH = "Version"
            val ANDROID_23_RULEBOOK_2206: String
                get() = loadJsonFile(PATH, "android23Rulebook2206")
        }
    }

    class SignInResponses {
        companion object {
            private const val PATH = "SignIn"
            val ADMIN: String
                get() = loadJsonFile(PATH, "admin")

            val REGULAR: String
                get() = loadJsonFile(PATH, "regular")
        }
    }
    class UpdateTrackerResponses {
        companion object {
            private const val PATH = "UpdateTracker"
            val ALL_1S: String
                get() = loadJsonFile(PATH, "all1s")

            val ALL_2S: String
                get() = loadJsonFile(PATH, "all2s")

            val SOME_2S_REST_1S: String
                get() = loadJsonFile(PATH, "some2sRest1s")
        }
    }

    class GetAllAnnouncementsResponses {
        companion object {
            private const val PATH = "Announcements"
            val FIVE_ANNOUNCEMENTS: String
                get() = loadJsonFile(PATH, "five")
        }
    }

    class GetAllContactRequestResponses {
        companion object {
            private const val PATH = "Contact"
            val ONE_UNREAD: String
                get() = loadJsonFile(PATH, "1unread")

            val ONE_READ: String
                get() = loadJsonFile(PATH, "1read")

            val ONE_READ_ONE_UNREAD: String
                get() = loadJsonFile(PATH, "1read1unread")
        }
    }

    class GetAllCharacterGearResponses {
        companion object {
            private const val PATH = "Gear"
            val STANDARD: String
                get() = loadJsonFile(PATH)
        }
    }

    class GetAllAwardsResponses {
        companion object {
            private const val PATH = "Awards"
            val STANDARD: String
                get() = loadJsonFile(PATH)
        }
    }

    class GetAllCharacterResponses {
        companion object {
            private const val PATH = "Characters"
            val STANDARD: String
                get() = loadJsonFile(PATH)
        }
    }

    class GetAllPlayersResponses {
        companion object {
            private const val PATH = "Players"
            val STANDARD: String
                get() = loadJsonFile(PATH)
        }
    }

    class GetAllEventsResponses {
        companion object {
            private const val PATH = "Events"
            val STANDARD: String
                get() = loadJsonFile(PATH)
        }
    }

    class GetFeatureFlagsResponses {
        companion object {
            private const val PATH = "FeatureFlags"
            val ONE_ON_ONE_OFF: String
                get() = loadJsonFile(PATH, "1on1off")

            val TWO_OFF: String
                get() = loadJsonFile(PATH, "2off")

            val TWO_ON: String
                get() = loadJsonFile(PATH, "2on")
        }
    }

    class GetAllEventAttendeesResponses {
        companion object {
            private const val PATH = "EventAttendees"
            val STANDARD: String
                get() = loadJsonFile(PATH)
        }
    }

    class GetAllCharacterSkillsResponses {
        companion object {
            private const val PATH = "CharSkills"
            val STANDARD: String
                get() = loadJsonFile(PATH)
        }
    }

    class GetAllPreregResponses {
        companion object {
            private const val PATH = "Preregs"
            val STANDARD: String
                get() = loadJsonFile(PATH)
        }
    }

    class GetAllIntrigueResponses {
        companion object {
            private const val PATH = "Gear"
            val STANDARD: String
                get() = loadJsonFile(PATH)
        }
    }

    class GetAllResearchProjectResponses {
        companion object {
            private const val PATH = "Research"
            val STANDARD: String
                get() = loadJsonFile(PATH)
        }
    }

    class GetAllSkillsResponses {
        companion object {
            private const val PATH = "Skills"
            val STANDARD: String
                get() = loadJsonFile(PATH)
        }
    }

    class GetAllSkillCategoriesResponses {
        companion object {
            private const val PATH = "SkillCat"
            val STANDARD: String
                get() = loadJsonFile(PATH)
        }
    }

    class GetAllSkillPrereqsResponses {
        companion object {
            private const val PATH = "SkillPrereqs"
            val STANDARD: String
                get() = loadJsonFile(PATH)
        }
    }

    class GetAllXpReductionResponses {
        companion object {
            private const val PATH = "XpRed"
            val STANDARD: String
                get() = loadJsonFile(PATH)
        }
    }

    class GetCampStatusResponses {
        companion object {
            private const val PATH = "CampStatus"
            val STANDARD: String
                get() = loadJsonFile(PATH)
        }
    }

    class GetAllProfileImageResponses {
        companion object {
            private const val PATH = "ProfileImage"
            val STANDARD: String
                get() = loadJsonFile(PATH)
        }
    }

    companion object {
        fun loadJsonFile(path: String, pathComponent: String = "standard"): String {
            val url = javaClass.getResource("/${path}_${pathComponent}.json")!!
            return File(url.toURI()).readText()
        }
    }

}