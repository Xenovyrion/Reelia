package com.reelia.app.data.update

/** Component-wise comparison ("0.9.0" < "0.13.0") rather than a string/lexicographic
 * compare, which would wrongly rank "0.13.0" below "0.9.0" ('1' < '9'). */
fun isNewerVersion(remote: String, local: String): Boolean {
    val remoteParts = remote.split(".").map { it.toIntOrNull() ?: 0 }
    val localParts = local.split(".").map { it.toIntOrNull() ?: 0 }
    for (i in 0 until maxOf(remoteParts.size, localParts.size)) {
        val r = remoteParts.getOrElse(i) { 0 }
        val l = localParts.getOrElse(i) { 0 }
        if (r != l) return r > l
    }
    return false
}
