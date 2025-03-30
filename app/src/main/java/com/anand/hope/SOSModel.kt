package com.anand.hope

data class SOSModel(
    val latitude: String,
    val longitude: String,
    val disasterType: String,
    val needs: List<String>
)

