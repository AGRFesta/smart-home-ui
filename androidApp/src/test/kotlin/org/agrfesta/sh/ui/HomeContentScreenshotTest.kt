package org.agrfesta.sh.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import org.agrfesta.sh.ui.api.Area
import org.agrfesta.sh.ui.api.AreaMeasurements
import org.agrfesta.sh.ui.api.FieldResult
import org.agrfesta.sh.ui.api.GlobalState
import org.agrfesta.sh.ui.api.HeatingMeasurements
import org.agrfesta.sh.ui.api.HomeResponse
import org.agrfesta.sh.ui.api.HumidityMeasurements
import org.agrfesta.sh.ui.home.HomeContent
import org.agrfesta.sh.ui.home.HomeUiState
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "port-xxhdpi")
@OptIn(ExperimentalTestApi::class)
class HomeContentScreenshotTest {

    @Test
    fun homeContent_loading() = runComposeUiTest {
        setContent {
            MaterialTheme { Surface { HomeContent(uiState = HomeUiState.Loading) } }
        }
        onRoot().captureRoboImage()
    }

    @Test
    fun homeContent_error() = runComposeUiTest {
        setContent {
            MaterialTheme { Surface { HomeContent(uiState = HomeUiState.Error("Network error")) } }
        }
        onRoot().captureRoboImage()
    }

    @Test
    fun homeContent_success() = runComposeUiTest {
        val uiState = HomeUiState.Success(
            data = HomeResponse(
                globalState = GlobalState(
                    heatingActive = FieldResult.Success(true),
                    strategy = FieldResult.Success("COMFORT")
                ),
                areas = listOf(
                    Area(
                        id = "living-room",
                        name = "Soggiorno",
                        measurements = AreaMeasurements(
                            heating = HeatingMeasurements(
                                currentTemperature = FieldResult.Success(21.5)
                            ),
                            humidity = HumidityMeasurements(
                                relative = FieldResult.Success(0.55)
                            )
                        )
                    )
                )
            )
        )
        setContent {
            MaterialTheme { Surface { HomeContent(uiState = uiState) } }
        }
        onRoot().captureRoboImage()
    }
}
