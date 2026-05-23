package org.agrfesta.sh.ui.home

import org.agrfesta.sh.ui.api.Area
import org.agrfesta.sh.ui.api.AreaMeasurements
import org.agrfesta.sh.ui.api.FieldResult
import org.agrfesta.sh.ui.api.GlobalState
import org.agrfesta.sh.ui.api.HeatingMeasurements
import org.agrfesta.sh.ui.api.HomeResponse
import org.agrfesta.sh.ui.api.HumidityMeasurements

fun aHomeResponse() = HomeResponse(
    globalState = aGlobalState(),
    areas = emptyList()
)

fun aGlobalState(
    heatingActive: FieldResult<Boolean> = FieldResult.Success(false),
    strategy: FieldResult<String?> = FieldResult.Success(null),
) = GlobalState(heatingActive = heatingActive, strategy = strategy)

fun anArea(
    id: String = "test-area-id",
    name: String = "Test Area",
    measurements: AreaMeasurements = anAreaMeasurements(),
) = Area(id = id, name = name, measurements = measurements)

fun anAreaMeasurements(
    heating: HeatingMeasurements? = aHeatingMeasurements(),
    humidity: HumidityMeasurements? = aHumidityMeasurements(),
) = AreaMeasurements(heating = heating, humidity = humidity)

fun aHeatingMeasurements(
    currentTemperature: FieldResult<Double?> = FieldResult.Success(20.0),
) = HeatingMeasurements(currentTemperature = currentTemperature)

fun aHumidityMeasurements(
    relative: FieldResult<Double?> = FieldResult.Success(0.5),
) = HumidityMeasurements(relative = relative)
