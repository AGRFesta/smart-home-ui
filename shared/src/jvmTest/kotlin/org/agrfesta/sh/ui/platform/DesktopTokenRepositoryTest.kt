package org.agrfesta.sh.ui.platform

import io.kotest.matchers.shouldBe
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class DesktopTokenRepositoryTest {

    private lateinit var tempDir: java.nio.file.Path
    private lateinit var repository: DesktopTokenRepository

    @BeforeTest
    fun setUp() {
        tempDir = createTempDirectory("pikesta-test")
        repository = DesktopTokenRepository(tempDir)
    }

    @AfterTest
    fun tearDown() {
        tempDir.toFile().deleteRecursively()
    }

    @Test
    fun `hasToken returns false when token file does not exist`() {
        // Then
        repository.hasToken() shouldBe false
    }

    @Test
    fun `hasToken returns true when token file exists`() {
        // Given
        tempDir.resolve("token").toFile().writeText("some-token")

        // Then
        repository.hasToken() shouldBe true
    }

    @Test
    fun `saveToken creates token file with given token`() {
        // Given
        val token = "my-secret-token"

        // When
        repository.saveToken(token)

        // Then
        tempDir.resolve("token").toFile().readText() shouldBe token
    }

    @Test
    fun `getToken returns stored token`() {
        // Given
        val token = "my-secret-token"
        tempDir.resolve("token").toFile().writeText(token)

        // Then
        repository.getToken() shouldBe token
    }

    @Test
    fun `getToken returns null when token file does not exist`() {
        // Then
        repository.getToken() shouldBe null
    }
}
