package net.arwix.extension

fun Int.isBitSet(bitMask: Int): Boolean = (this and bitMask) == bitMask