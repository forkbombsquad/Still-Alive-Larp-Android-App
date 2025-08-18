package com.forkbombsquad.stillalivelarp.utils

import androidx.fragment.app.Fragment

inline fun <reified T : Fragment> fragmentName(): String {
    return T::class.simpleName ?: "UnnamedFragment"
}
