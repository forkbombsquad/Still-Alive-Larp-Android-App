package com.forkbombsquad.stillalivelarp.services.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.forkbombsquad.stillalivelarp.utils.AwardCharType
import com.forkbombsquad.stillalivelarp.utils.AwardPlayerType
import com.forkbombsquad.stillalivelarp.utils.yyyyMMddFormatted
import java.io.Serializable
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class AwardModel(
    @JsonProperty("id") val id: Int,
    @JsonProperty("playerId") val playerId: Int,
    @JsonProperty("characterId") val characterId: Int?,
    @JsonProperty("awardType") val awardType: String,
    @JsonProperty("reason") val reason: String,
    @JsonProperty("date") val date: String,
    @JsonProperty("amount") val amount: String
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class AwardCreateModel(
    @JsonProperty("playerId") val playerId: Int,
    @JsonProperty("characterId") val characterId: Int?,
    @JsonProperty("awardType") val awardType: String,
    @JsonProperty("reason") val reason: String,
    @JsonProperty("date") val date: String,
    @JsonProperty("amount") val amount: String
) : Serializable {
    companion object {
        fun createPlayerAward(player: PlayerModel, awardType: AwardPlayerType, reason: String, amount: String): AwardCreateModel {
            return AwardCreateModel(
                player.id,
                null,
                awardType.text,
                reason,
                LocalDate.now().yyyyMMddFormatted(),
                amount
            )
        }

        fun createPlayerAward(playerId: Int, awardType: AwardPlayerType, reason: String, amount: String): AwardCreateModel {
            return AwardCreateModel(
                playerId,
                null,
                awardType.text,
                reason,
                LocalDate.now().yyyyMMddFormatted(),
                amount
            )
        }

        fun createCharacterAward(char: CharacterModel, awardType: AwardCharType, reason: String, amount: String): AwardCreateModel {
            return AwardCreateModel(
                char.playerId,
                char.id,
                awardType.text,
                reason,
                LocalDate.now().yyyyMMddFormatted(),
                amount
            )
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class AwardListModel(
    @JsonProperty("awards") val awards: Array<AwardModel>
) : Serializable