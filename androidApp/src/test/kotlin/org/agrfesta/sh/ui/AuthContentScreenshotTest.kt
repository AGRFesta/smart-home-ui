package org.agrfesta.sh.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import org.agrfesta.sh.ui.auth.CameraPermissionState
import org.agrfesta.sh.ui.auth.QrAuthContent
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "port-xxhdpi")
@OptIn(ExperimentalTestApi::class)
class AuthContentScreenshotTest {

    @Test
    fun authContent_default() = runComposeUiTest {
        setContent {
            MaterialTheme {
                Surface {
                    QrAuthContent(
                        permissionState = CameraPermissionState.Denied,
                        onRequestPermission = {},
                        onTokenSaved = {}
                    )
                }
            }
        }
        onRoot().captureRoboImage()
    }

    @Test
    fun authContent_tokenInvalid() = runComposeUiTest {
        setContent {
            MaterialTheme {
                Surface {
                    QrAuthContent(
                        permissionState = CameraPermissionState.Denied,
                        onRequestPermission = {},
                        onTokenSaved = {},
                        tokenInvalid = true
                    )
                }
            }
        }
        onRoot().captureRoboImage()
    }

    @Test
    fun qrAuthContent() = runComposeUiTest {
        setContent {
            MaterialTheme {
                Surface {
                    QrAuthContent(
                        permissionState = CameraPermissionState.Granted,
                        onRequestPermission = {},
                        onTokenSaved = {},
                        qrScannerContent = { _ ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.DarkGray)
                            )
                        }
                    )
                }
            }
        }
        onRoot().captureRoboImage()
    }
}
