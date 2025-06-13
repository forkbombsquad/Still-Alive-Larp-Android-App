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

    fun getGearOrganized(): Map<String, List<GearJsonModel>> {
        val gear = jsonModels
        if (gear != null) {
            var firearms: MutableList<GearJsonModel> = mutableListOf()
            var melee: MutableList<GearJsonModel> = mutableListOf()
            val clothing: MutableList<GearJsonModel> = mutableListOf()
            var accessory: MutableList<GearJsonModel> = mutableListOf()
            var bag: MutableList<GearJsonModel> = mutableListOf()
            val other: MutableList<GearJsonModel> = mutableListOf()
            gear.forEach { jg ->
                when (jg.gearType) {
                    Constants.GearTypes.firearm -> firearms.add(jg)
                    Constants.GearTypes.meleeWeapon -> melee.add(jg)
                    Constants.GearTypes.clothing -> clothing.add(jg)
                    Constants.GearTypes.accessory -> accessory.add(jg)
                    Constants.GearTypes.bag -> bag.add(jg)
                    Constants.GearTypes.other -> other.add(jg)
                }
            }

            // Sorting
            firearms = firearms.sortedWith(
                compareBy(
                    { if (it.isPrimaryFirearm()) 0 else 1 },
                    {
                        when (it.primarySubtype) {
                            Constants.GearPrimarySubtype.lightFirearm -> 0
                            Constants.GearPrimarySubtype.mediumFirearm -> 1
                            Constants.GearPrimarySubtype.heavyFirearm -> 2
                            Constants.GearPrimarySubtype.advancedFirearm -> 3
                            Constants.GearPrimarySubtype.militaryGradeFirearm -> 4
                            else -> Int.MAX_VALUE
                        }
                    }
                )
            ).toMutableList()

            melee = melee.sortedWith(
                compareBy {
                    when (it.primarySubtype) {
                        Constants.GearPrimarySubtype.superLightMeleeWeapon -> 0
                        Constants.GearPrimarySubtype.lightMeleeWeapon -> 1
                        Constants.GearPrimarySubtype.mediumMeleeWeapon -> 2
                        Constants.GearPrimarySubtype.heavyMeleeWeapon -> 3
                        else -> Int.MAX_VALUE
                    }
                }
            ).toMutableList()

            accessory = accessory.sortedWith(
                compareBy {
                    when (it.primarySubtype) {
                        Constants.GearPrimarySubtype.blacklightFlashlight -> 0
                        Constants.GearPrimarySubtype.flashlight -> 1
                        Constants.GearPrimarySubtype.other -> 2
                        else -> Int.MAX_VALUE
                    }
                }
            ).toMutableList()

            bag = bag.sortedWith(
                compareBy {
                    when (it.primarySubtype) {
                        Constants.GearPrimarySubtype.smallBag -> 0
                        Constants.GearPrimarySubtype.mediumBag -> 1
                        Constants.GearPrimarySubtype.largeBag -> 2
                        Constants.GearPrimarySubtype.extraLargeBag -> 3
                        else -> Int.MAX_VALUE
                    }
                }
            ).toMutableList()

            // Adding to map
            return mapOf(
                Pair(Constants.GearTypes.firearm, firearms),
                Pair(Constants.GearTypes.meleeWeapon, melee),
                Pair(Constants.GearTypes.clothing, clothing),
                Pair(Constants.GearTypes.accessory, accessory),
                Pair(Constants.GearTypes.bag, bag),
                Pair(Constants.GearTypes.other, other)
            )

        } else {
            return mapOf()
        }
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