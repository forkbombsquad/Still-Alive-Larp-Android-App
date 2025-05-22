package com.forkbombsquad.stillalivelarp.services.models

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

data class AwardModels_LD(
    @JsonProperty("playerAwards") val playerAwards: Map<Int, List<AwardModel>>,
    @JsonProperty("characterAwards") val characterAwards: Map<Int, List<AwardModel>>
): Serializable