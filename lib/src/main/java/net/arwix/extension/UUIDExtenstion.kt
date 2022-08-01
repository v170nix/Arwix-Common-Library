package net.arwix.extension

import java.util.*

fun createRandomString() = UUID.randomUUID().toString().replace("-", "")