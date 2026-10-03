package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("JobSaarthi", appName)
  }

  @Test
  fun `token manager stores and clears session`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val tokenManager = com.example.data.repository.TokenManager(context)
    tokenManager.clear()
    assertEquals(false, tokenManager.isLoggedIn())

    tokenManager.saveTokens("mock_access_token", "mock_refresh_token")
    assertEquals(true, tokenManager.isLoggedIn())
    assertEquals("mock_access_token", tokenManager.getAccessToken())
    assertEquals("mock_refresh_token", tokenManager.getRefreshToken())

    tokenManager.clear()
    assertEquals(false, tokenManager.isLoggedIn())
  }
}
