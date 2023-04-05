package net.arwix.extension

import java.util.*

@Suppress("unused")
fun createRandomString() = UUID.randomUUID().toString().replace("-", "")