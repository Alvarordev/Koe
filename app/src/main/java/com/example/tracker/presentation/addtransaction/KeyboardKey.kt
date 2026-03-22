package com.example.tracker.presentation.addtransaction

sealed class KeyboardKey {
    data class Digit(val char: Char) : KeyboardKey()
    object Dot : KeyboardKey()
    object Delete : KeyboardKey()
    object CurrencySelector : KeyboardKey()
    object MapPinPlaceholder : KeyboardKey()
    object Submit : KeyboardKey()
}
