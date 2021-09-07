package sh.rebecca.inventory.input


class MouseController

fun fixRotation(rotation: Int): Int {
    var rotated = rotation
    if(rotation <= 0) rotated = 2047
    if(rotation >= 2048) rotated = 1
    return rotated
}

