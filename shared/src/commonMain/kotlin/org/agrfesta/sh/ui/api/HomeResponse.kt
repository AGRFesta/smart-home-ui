package org.agrfesta.sh.ui.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class FieldResult<out T> {
    @Serializable
    @SerialName("success")
    data class Success<out T>(val value: T) : FieldResult<T>()

    @Serializable
    @SerialName("failure")
    data class Failure(val error: String) : FieldResult<Nothing>()
}

@Serializable
data class GlobalState(
    val heatingActive: FieldResult<Boolean>,
    val strategy: FieldResult<String?>,
)

@Serializable
data class HeatingMeasurements(
    val currentTemperature: FieldResult<Double?>,
)

@Serializable
data class HumidityMeasurements(
    val relative: FieldResult<Double?>,
)

@Serializable
data class AreaMeasurements(
    val heating: HeatingMeasurements?,
    val humidity: HumidityMeasurements?,
)

@Serializable
data class Area(
    val id: String,
    val name: String,
    val measurements: AreaMeasurements,
)

@Serializable
data class HomeResponse(
    val globalState: GlobalState,
    val areas: List<Area>,
)
