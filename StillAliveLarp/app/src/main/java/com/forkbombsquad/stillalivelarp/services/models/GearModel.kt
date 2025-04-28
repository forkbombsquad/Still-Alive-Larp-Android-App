package com.forkbombsquad.stillalivelarp.services.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.forkbombsquad.stillalivelarp.utils.Constants
import com.forkbombsquad.stillalivelarp.utils.decompress
import com.forkbombsquad.stillalivelarp.utils.globalFromJson
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class GearModel(
    @JsonProperty("id") val id: Int,
    @JsonProperty("characterId") val characterId: Int,
    @JsonProperty("gearJson") var gearJson: String
) : Serializable {
    val jsonModels: List<GearJsonModel>?
        get() {
            return globalFromJson<GearJsonListModel>(gearJson)?.gearJson?.toList()
        }

    fun getPrimaryFirearm(): GearJsonModel? {
        return jsonModels?.firstOrNull { it.isPrimaryFirearm() }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class GearCreateModel(
    @JsonProperty("characterId") val characterId: Int,
    @JsonProperty("gearJson") val gearJson: String
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class GearListModel(
    @JsonProperty("charGear") val charGear: Array<GearModel>
) : Serializable

data class GearJsonModel(
    @JsonProperty("name") val name: String,
    @JsonProperty("gearType") val gearType: String,
    @JsonProperty("primarySubtype") val primarySubtype: String,
    @JsonProperty("secondarySubtype") val secondarySubtype: String,
    @JsonProperty("desc") val desc: String
) : Serializable {

    fun isPrimaryFirearm(): Boolean {
        return secondarySubtype == Constants.GearSecondarySubtype.primaryFirearm
    }

    fun isEqualTo(other: GearJsonModel): Boolean {
        return name == other.name && gearType == other.gearType && primarySubtype == other.primarySubtype && secondarySubtype == other.secondarySubtype && desc == other.desc
    }

    fun duplicateWithEdit(name: String = this.name, gearType: String = this.gearType, primarySubtype: String = this.primarySubtype, secondarySubtype: String = this.secondarySubtype, desc: String = this.desc): GearJsonModel {
        return GearJsonModel(name, gearType, primarySubtype, secondarySubtype, desc)
    }

}

data class GearJsonListModel(@JsonProperty("gearJson") val gearJson: Array<GearJsonModel>
) : Serializable