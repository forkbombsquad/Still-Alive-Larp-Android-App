package com.forkbombsquad.stillalivelarp.services.managers

class DataManager private constructor() {
    companion object {
        var shared = DataManager()
            private set

        fun forceReset() {
            // TODO
        }
    }
}