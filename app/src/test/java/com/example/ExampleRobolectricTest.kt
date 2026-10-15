package com.example

import com.example.data.model.DownloadQuality
import com.example.data.model.PlatformType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `test platform detection from urls`() {
    assertEquals(PlatformType.YOUTUBE, PlatformType.fromUrl("https://www.youtube.com/watch?v=12345"))
    assertEquals(PlatformType.YOUTUBE, PlatformType.fromUrl("https://youtu.be/12345"))
    assertEquals(PlatformType.TIKTOK, PlatformType.fromUrl("https://www.tiktok.com/@user/video/98765"))
    assertEquals(PlatformType.INSTAGRAM, PlatformType.fromUrl("https://www.instagram.com/reel/abcdef/"))
    assertEquals(PlatformType.FACEBOOK, PlatformType.fromUrl("https://fb.watch/xyz123/"))
    assertEquals(PlatformType.GENERIC, PlatformType.fromUrl("https://example.com/video.mp4"))
  }

  @Test
  fun `test download qualities definition`() {
    val qualities = DownloadQuality.values()
    assertTrue(qualities.isNotEmpty())
    assertNotNull(DownloadQuality.HD_1080P)
    assertNotNull(DownloadQuality.AUDIO_MP3)
  }
}
