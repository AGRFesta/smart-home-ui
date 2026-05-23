package org.agrfesta.sh.ui.home

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import org.agrfesta.sh.ui.api.FieldResult
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class HomeContentTest {

    @Test
    fun `should show COMFORT strategy label when strategy value is COMFORT`() = runComposeUiTest {
        // Given
        val uiState = HomeUiState.Success(
            data = aHomeResponse().copy(
                globalState = aGlobalState(
                    heatingActive = FieldResult.Success(true),
                    strategy = FieldResult.Success("COMFORT")
                )
            )
        )

        // When
        setContent { HomeContent(uiState = uiState) }

        // Then
        onNodeWithText("COMFORT").assertIsDisplayed()
    }

    @Test
    fun `should show warning for heatingActive when FieldResult is Failure`() = runComposeUiTest {
        // Given
        val uiState = HomeUiState.Success(
            data = aHomeResponse().copy(
                globalState = aGlobalState(heatingActive = FieldResult.Failure("error"))
            )
        )

        // When
        setContent { HomeContent(uiState = uiState) }

        // Then
        onNodeWithTag("heating_active_warning").assertIsDisplayed()
    }

    @Test
    fun `should show warning for strategy when FieldResult is Failure`() = runComposeUiTest {
        // Given
        val uiState = HomeUiState.Success(
            data = aHomeResponse().copy(
                globalState = aGlobalState(
                    heatingActive = FieldResult.Success(true),
                    strategy = FieldResult.Failure("error")
                )
            )
        )

        // When
        setContent { HomeContent(uiState = uiState) }

        // Then
        onNodeWithTag("strategy_warning").assertIsDisplayed()
    }

    @Test
    fun `should hide strategy when heating is inactive`() = runComposeUiTest {
        // Given
        val uiState = HomeUiState.Success(
            data = aHomeResponse().copy(
                globalState = aGlobalState(
                    heatingActive = FieldResult.Success(false),
                    strategy = FieldResult.Success("COMFORT")
                )
            )
        )

        // When
        setContent { HomeContent(uiState = uiState) }

        // Then
        onNodeWithText("COMFORT").assertDoesNotExist()
    }

    @Test
    fun `should show no strategy label when strategy value is null`() = runComposeUiTest {
        // Given
        val uiState = HomeUiState.Success(
            data = aHomeResponse().copy(
                globalState = aGlobalState(strategy = FieldResult.Success(null))
            )
        )

        // When
        setContent { HomeContent(uiState = uiState) }

        // Then
        onNodeWithText("COMFORT").assertDoesNotExist()
        onNodeWithText("ECONOMY").assertDoesNotExist()
    }

    @Test
    fun `should show ECONOMY strategy label when strategy value is ECONOMY`() = runComposeUiTest {
        // Given
        val uiState = HomeUiState.Success(
            data = aHomeResponse().copy(
                globalState = aGlobalState(
                    heatingActive = FieldResult.Success(true),
                    strategy = FieldResult.Success("ECONOMY")
                )
            )
        )

        // When
        setContent { HomeContent(uiState = uiState) }

        // Then
        onNodeWithText("ECONOMY").assertIsDisplayed()
    }

    @Test
    fun `should show heating inactive indicator when heatingActive value is false`() = runComposeUiTest {
        // Given
        val uiState = HomeUiState.Success(
            data = aHomeResponse().copy(
                globalState = aGlobalState(heatingActive = FieldResult.Success(false))
            )
        )

        // When
        setContent { HomeContent(uiState = uiState) }

        // Then
        onNodeWithText("INATTIVO").assertIsDisplayed()
    }

    @Test
    fun `should show heating active indicator when heatingActive value is true`() = runComposeUiTest {
        // Given
        val uiState = HomeUiState.Success(
            data = aHomeResponse().copy(
                globalState = aGlobalState(heatingActive = FieldResult.Success(true))
            )
        )

        // When
        setContent { HomeContent(uiState = uiState) }

        // Then
        onNodeWithText("ATTIVO").assertIsDisplayed()
    }

    @Test
    fun `should hide humidity section when humidity measurements are null`() = runComposeUiTest {
        // Given
        val uiState = HomeUiState.Success(
            data = aHomeResponse().copy(
                areas = listOf(
                    anArea(measurements = anAreaMeasurements(humidity = null))
                )
            )
        )

        // When
        setContent { HomeContent(uiState = uiState) }

        // Then
        onNodeWithTag("humidity_warning").assertDoesNotExist()
        onNodeWithText("─").assertDoesNotExist()
    }

    @Test
    fun `should hide heating section when heating measurements are null`() = runComposeUiTest {
        // Given
        val uiState = HomeUiState.Success(
            data = aHomeResponse().copy(
                areas = listOf(
                    anArea(measurements = anAreaMeasurements(heating = null))
                )
            )
        )

        // When
        setContent { HomeContent(uiState = uiState) }

        // Then
        onNodeWithTag("temperature_warning").assertDoesNotExist()
        onNodeWithText("─").assertDoesNotExist()
    }

    @Test
    fun `should display warning when relative humidity is Failure`() = runComposeUiTest {
        // Given
        val uiState = HomeUiState.Success(
            data = aHomeResponse().copy(
                areas = listOf(
                    anArea(measurements = anAreaMeasurements(
                        humidity = aHumidityMeasurements(
                            relative = FieldResult.Failure("error")
                        )
                    ))
                )
            )
        )

        // When
        setContent { HomeContent(uiState = uiState) }

        // Then
        onNodeWithTag("humidity_warning").assertIsDisplayed()
    }

    @Test
    fun `should display dash when relative humidity is Success with null value`() = runComposeUiTest {
        // Given
        val uiState = HomeUiState.Success(
            data = aHomeResponse().copy(
                areas = listOf(
                    anArea(measurements = anAreaMeasurements(
                        humidity = aHumidityMeasurements(
                            relative = FieldResult.Success(null)
                        )
                    ))
                )
            )
        )

        // When
        setContent { HomeContent(uiState = uiState) }

        // Then
        onNodeWithText("─").assertIsDisplayed()
    }

    @Test
    fun `should display humidity value as integer percentage when relative is Success with a value`() = runComposeUiTest {
        // Given
        val uiState = HomeUiState.Success(
            data = aHomeResponse().copy(
                areas = listOf(
                    anArea(measurements = anAreaMeasurements(
                        humidity = aHumidityMeasurements(
                            relative = FieldResult.Success(0.6)
                        )
                    ))
                )
            )
        )

        // When
        setContent { HomeContent(uiState = uiState) }

        // Then
        onNodeWithText("60%").assertIsDisplayed()
    }

    @Test
    fun `should display humidity value rounded to nearest integer`() = runComposeUiTest {
        // Given
        val uiState = HomeUiState.Success(
            data = aHomeResponse().copy(
                areas = listOf(
                    anArea(measurements = anAreaMeasurements(
                        humidity = aHumidityMeasurements(
                            relative = FieldResult.Success(0.05)
                        )
                    ))
                )
            )
        )

        // When
        setContent { HomeContent(uiState = uiState) }

        // Then
        onNodeWithText("5%").assertIsDisplayed()
    }

    @Test
    fun `should display warning when currentTemperature is Failure`() = runComposeUiTest {
        // Given
        val uiState = HomeUiState.Success(
            data = aHomeResponse().copy(
                areas = listOf(
                    anArea(measurements = anAreaMeasurements(
                        heating = aHeatingMeasurements(
                            currentTemperature = FieldResult.Failure("error")
                        )
                    ))
                )
            )
        )

        // When
        setContent { HomeContent(uiState = uiState) }

        // Then
        onNodeWithTag("temperature_warning").assertIsDisplayed()
    }

    @Test
    fun `should display dash when currentTemperature is Success with null value`() = runComposeUiTest {
        // Given
        val uiState = HomeUiState.Success(
            data = aHomeResponse().copy(
                areas = listOf(
                    anArea(measurements = anAreaMeasurements(
                        heating = aHeatingMeasurements(
                            currentTemperature = FieldResult.Success(null)
                        )
                    ))
                )
            )
        )

        // When
        setContent { HomeContent(uiState = uiState) }

        // Then
        onNodeWithText("─").assertIsDisplayed()
    }

    @Test
    fun `should display temperature value when currentTemperature is Success with a value`() = runComposeUiTest {
        // Given
        val uiState = HomeUiState.Success(
            data = aHomeResponse().copy(
                areas = listOf(
                    anArea(measurements = anAreaMeasurements(
                        heating = aHeatingMeasurements(
                            currentTemperature = FieldResult.Success(21.5)
                        )
                    ))
                )
            )
        )

        // When
        setContent { HomeContent(uiState = uiState) }

        // Then
        onNodeWithText("21.5°C").assertIsDisplayed()
    }

    @Test
    fun `should display area name`() = runComposeUiTest {
        // Given
        val uiState = HomeUiState.Success(
            data = aHomeResponse().copy(areas = listOf(anArea(name = "Living Room")))
        )

        // When
        setContent { HomeContent(uiState = uiState) }

        // Then
        onNodeWithText("Living Room").assertIsDisplayed()
    }

    @Test
    fun `should display loading indicator when state is Loading`() = runComposeUiTest {
        // When
        setContent { HomeContent(uiState = HomeUiState.Loading) }

        // Then
        onNodeWithTag("home_loading_indicator").assertIsDisplayed()
    }

    @Test
    fun `should display error message when state is Error`() = runComposeUiTest {
        // Given
        val errorMessage = "Network error"

        // When
        setContent { HomeContent(uiState = HomeUiState.Error(errorMessage)) }

        // Then
        onNodeWithText(errorMessage).assertIsDisplayed()
    }
}

