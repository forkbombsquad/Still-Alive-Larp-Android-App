package com.forkbombsquad.stillalivelarp.services.managers

import com.forkbombsquad.stillalivelarp.services.models.PlayerModel

class PlayerManager private constructor() {

    private var player: PlayerModel? = null

    fun forceReset() {
        player = null
    }

    fun setPlayer(player: PlayerModel) {
        this.player = player
        SharedPrefsManager.shared.storePlayer(player)
    }

    fun getPlayer(): PlayerModel? {
        return player
    }

    fun updatePlayer(updatedPlayer: PlayerModel) {
        this.player = updatedPlayer
        SharedPrefsManager.shared.storePlayer(updatedPlayer)
    }

    companion object {
        val shared = PlayerManager()
    }

}