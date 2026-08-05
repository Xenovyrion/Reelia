package com.reelia.app.data.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionCompareTest {

    @Test
    fun `component-wise compare beats lexicographic compare`() {
        // A plain string compare would rank "0.13.0" below "0.9.0" ('1' < '9') — this is exactly
        // the bug this function exists to avoid.
        assertTrue(isNewerVersion(remote = "0.13.0", local = "0.9.0"))
        assertFalse(isNewerVersion(remote = "0.9.0", local = "0.13.0"))
    }

    @Test
    fun `equal versions are not newer`() {
        assertFalse(isNewerVersion(remote = "0.22.0", local = "0.22.0"))
    }

    @Test
    fun `differs only in patch version`() {
        assertTrue(isNewerVersion(remote = "0.22.1", local = "0.22.0"))
        assertFalse(isNewerVersion(remote = "0.22.0", local = "0.22.1"))
    }

    @Test
    fun `differing segment counts are padded with zero`() {
        assertTrue(isNewerVersion(remote = "1.0", local = "0.9.9"))
        assertFalse(isNewerVersion(remote = "1.0.0", local = "1.0"))
    }
}
