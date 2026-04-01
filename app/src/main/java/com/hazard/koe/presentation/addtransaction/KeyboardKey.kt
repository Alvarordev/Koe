package com.hazard.koe.presentation.addtransaction

sealed class KeyboardKey {
    data class Digit(val char: Char) : KeyboardKey()
    object Dot : KeyboardKey()
    object Delete : KeyboardKey()
    object CurrencySelector : KeyboardKey()
    object Location : KeyboardKey()
    object Submit : KeyboardKey()
}
