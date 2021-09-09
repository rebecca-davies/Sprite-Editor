package sh.rebecca.inventory.model

data class ModelHeader(
    val data: ByteArray,
    val faceDataOffset: Int,
    val faces: Int,
    val faceTypeOffset: Int,
    val colorDataOffset: Int,
    val vertexDirectionOffset: Int,
    val vertices: Int,
    val xDataOffset: Int,
    val yDataOffset: Int,
    val zDataOffset: Int
)
