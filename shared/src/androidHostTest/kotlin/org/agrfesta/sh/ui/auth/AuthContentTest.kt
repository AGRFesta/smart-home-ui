package org.agrfesta.sh.ui.auth

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalTestApi::class)
class AuthContentTest {

    @Test
    fun `should display instructions text`() = runComposeUiTest {
        // When
        setContent { AuthContent(onTokenSaved = {}) }

        // Then
        onNodeWithText("Inquadra il QR code").assertIsDisplayed()
    }

    @Test
    fun `should display permission request button when camera permission has not been granted`() = runComposeUiTest {
        // When
        setContent { QrAuthContent(permissionState = CameraPermissionState.Denied, onRequestPermission = {}, onTokenSaved = {}) }

        // Then
        onNodeWithTag("auth_request_permission_button").assertIsDisplayed()
    }

    @Test
    fun `should display QR scanner viewfinder when camera permission is granted`() = runComposeUiTest {
        // When
        setContent { QrAuthContent(permissionState = CameraPermissionState.Granted, onRequestPermission = {}, onTokenSaved = {}) }

        // Then
        onNodeWithTag("auth_qr_viewfinder").assertIsDisplayed()
    }

    @Test
    fun `should call request permission when permission button is clicked`() = runComposeUiTest {
        // Given
        var permissionRequested = false
        setContent {
            QrAuthContent(
                permissionState = CameraPermissionState.Denied,
                onRequestPermission = { permissionRequested = true },
                onTokenSaved = {}
            )
        }

        // When
        onNodeWithTag("auth_request_permission_button").performClick()

        // Then
        permissionRequested shouldBe true
    }

    @Test
    fun `should call onTokenSaved with the scanned value when QR scanner delivers a result`() = runComposeUiTest {
        // Given
        var savedToken: String? = null
        setContent {
            QrAuthContent(
                permissionState = CameraPermissionState.Granted,
                onRequestPermission = {},
                onTokenSaved = { savedToken = it },
                qrScannerContent = { onResult ->
                    Button(
                        onClick = { onResult("scanned-token") },
                        modifier = Modifier.testTag("fake_scan_trigger")
                    ) { Text("Scan") }
                }
            )
        }

        // When
        onNodeWithTag("fake_scan_trigger").performClick()

        // Then
        savedToken shouldBe "scanned-token"
    }

    @Test
    fun `should display permission denied message when camera permission is permanently denied`() = runComposeUiTest {
        // When
        setContent { QrAuthContent(permissionState = CameraPermissionState.PermanentlyDenied, onRequestPermission = {}, onTokenSaved = {}) }

        // Then
        onNodeWithText("Permesso fotocamera negato. Abilitalo dalle impostazioni.").assertIsDisplayed()
    }
}
