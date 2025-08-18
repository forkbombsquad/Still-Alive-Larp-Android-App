package com.forkbombsquad.stillalivelarp.services.models

import com.fasterxml.jackson.annotation.JsonProperty
import com.forkbombsquad.stillalivelarp.utils.ifLet
import java.io.Serializable

data class LDAwardModels(
    @JsonProperty("playerAwards") val playerAwards: Map<Int, List<AwardModel>>,
    @JsonProperty("characterAwards") val characterAwards: Map<Int, List<AwardModel>>
): Serializable {

    companion object {
        fun empty(): LDAwardModels {
            return LDAwardModels(mapOf(), mapOf())
        }
    }

}

data class LDEventAttendeeModels(
    @JsonProperty("byEvent") val byEvent: Map<Int, List<EventAttendeeModel>>,
    @JsonProperty("byPlayer") val byPlayer: Map<Int, List<EventAttendeeModel>>,
    @JsonProperty("byCharacter") val byCharacter: Map<Int, List<EventAttendeeModel>>
): Serializable {

    companion object {
        fun empty(): LDEventAttendeeModels {
            return LDEventAttendeeModels(mapOf(), mapOf(), mapOf())
        }
    }

}

data class LDPreregModels(
    @JsonProperty("byEvent") val byEvent: Map<Int, List<EventPreregModel>>,
    @JsonProperty("byPlayer") val byPlayer: Map<Int, List<EventPreregModel>>,
    @JsonProperty("byCharacter") val byCharacter: Map<Int, List<EventPreregModel>>,
    @JsonProperty("byRegType") val byRegType: Map<EventRegType, List<EventPreregModel>>
): Serializable {

    companion object {
        fun empty(): LDPreregModels {
            return LDPreregModels(mapOf(), mapOf(), mapOf(), mapOf())
        }
    }

}

data class LDSkillPrereqModels(
    @JsonProperty("all") val all: List<SkillPrereqModel>,
    @JsonProperty("byBaseSkill") val byBaseSkill: Map<Int, List<SkillPrereqModel>>,
    @JsonProperty("byPrereqSkill") val byPrereqSkill: Map<Int, List<SkillPrereqModel>>
): Serializable {

    companion object {
        fun empty(): LDSkillPrereqModels {
            return LDSkillPrereqModels(listOf(), mapOf(), mapOf())
        }
    }

}