package sh.rebecca.inventory.obj


data class Obj(
    var id: Int,
    var model: Int,
    var originalColors: IntArray? = null,
    var replacementColors: IntArray? = null,
    var pitch: Int,
    var yaw: Int,
    var roll: Int,
    var translateX: Int,
    var translateY: Int,
    var zoom: Int,
    var ambient: Int,
    var attenuation: Int,
    var scaleX: Int,
    var scaleY: Int,
    var scaleZ: Int)

