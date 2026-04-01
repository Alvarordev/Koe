package com.hazard.koe.data.enums

enum class SupportedCurrency(val code: String, val symbol: String, val currencyName: String) {
    USD("USD", "$", "US Dollar"),
    EUR("EUR", "€", "Euro"),
    GBP("GBP", "£", "British Pound"),
    PEN("PEN", "S/", "Peruvian Sol"),
    MXN("MXN", "$", "Mexican Peso"),
    COP("COP", "$", "Colombian Peso"),
    ARS("ARS", "$", "Argentine Peso"),
    BRL("BRL", "R$", "Brazilian Real"),
    CLP("CLP", "$", "Chilean Peso"),
    JPY("JPY", "¥", "Japanese Yen"),
    CAD("CAD", "C$", "Canadian Dollar"),
    AUD("AUD", "A$", "Australian Dollar"),
    CNY("CNY", "¥", "Chinese Yuan"),
    INR("INR", "₹", "Indian Rupee"),
    KRW("KRW", "₩", "South Korean Won")
}
