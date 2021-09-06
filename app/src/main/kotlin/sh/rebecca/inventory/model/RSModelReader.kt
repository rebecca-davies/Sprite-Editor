package sh.rebecca.inventory.model

import io.guthix.buffer.readSmallSmart
import io.netty.buffer.Unpooled
import media.Model
import org.springframework.stereotype.Component
import reader.ModelReader
import java.io.InputStream

@Component
class RSModelReader : ModelReader() {

    fun decodeHead(data: ByteArray): ModelHeader {
        var buffer = Unpooled.wrappedBuffer(data)
        buffer.readBytes(data.size - 18)

        var vertices = buffer.readUnsignedShort()
        var faceCount = buffer.readUnsignedShort()
        var texturedFaceCount = buffer.readUnsignedByte()

        var useTextures = buffer.readUnsignedByte()
        var useFacePriority = buffer.readUnsignedByte()
        var useTransparency = buffer.readUnsignedByte()
        var useFaceSkinning = buffer.readUnsignedByte()
        var useVertexSkinning = buffer.readUnsignedByte()

        var xDataOffset = buffer.readUnsignedShort()
        var yDataOffset = buffer.readUnsignedShort()
        var zDataOffset = buffer.readUnsignedShort()
        var faceDataLength = buffer.readUnsignedShort()

        var offset = 0
        var vertexDirectionOffset = offset
        offset += vertices

        var faceTypeOffset = offset
        offset += faceCount

        var facePriorityOffset = offset

        var faceSkinOffset = offset
        if(useFacePriority.toInt() == 255) {
            offset += faceCount
        } else {
            faceSkinOffset = -1
        }

        var texturePointerOffset = offset
        if(useTextures.toInt() == 1) {
            offset += faceCount
        } else {
            texturePointerOffset = -1
        }

        var vertexSkinOffset = offset
        if(useVertexSkinning.toInt() == 1) {
            offset += vertices
        } else {
            vertexSkinOffset = -1
        }

        var faceAlphaOffset = offset
        if(useTransparency.toInt() == 1) {
            offset += faceCount
        } else {
            faceAlphaOffset = -1
        }

        var faceDataOffset = offset
        offset += faceDataLength

        var faceColorDataOffset = offset
        offset += faceCount * 2

        var uvMapFaceOffset = offset
        offset += texturedFaceCount * 6

        var xDataLength = offset
        offset += xDataOffset

        var yDataLength = offset
        offset += yDataOffset

        var zDataLength = offset
        offset += zDataOffset

        return ModelHeader(data, faceDataOffset, faceCount, faceTypeOffset, faceColorDataOffset, vertexDirectionOffset, vertices, xDataLength, yDataLength, zDataLength)
    }

    fun decode(data: ByteArray): Model {
        var header = decodeHead(data)
        val model = Model()
        model.setVertexCount(header.faces)
        model.setTriangleCount(header.faces)
        model.vertexX = IntArray(header.faces)
        model.vertexY = IntArray(header.faces)
        model.vertexZ = IntArray(header.faces)
        model.triangleVertexA = IntArray(header.faces)
        model.triangleVertexB = IntArray(header.faces)
        model.triangleVertexC = IntArray(header.faces)

        model.triangleColor = IntArray(header.faces)

        val directions = Unpooled.wrappedBuffer(data)
        directions.readBytes(header.vertexDirectionOffset)

        val verticesX = Unpooled.wrappedBuffer(data)
        verticesX.readBytes(header.xDataOffset)

        val verticesY = Unpooled.wrappedBuffer(data)
        verticesY.readBytes(header.yDataOffset)

        val verticesZ = Unpooled.wrappedBuffer(data)
        verticesZ.readBytes(header.zDataOffset)

        var baseX = 0
        var baseY = 0
        var baseZ = 0

        for(vertex in 0 until model.triangleCount) {
            var mask = directions.readUnsignedByte()

            var x = 0
            if((mask.toInt() and 1) != 0) {
                x = verticesX.readSmallSmart()
            }

            var y = 0
            if((mask.toInt() and 2) != 0) {
                y = verticesY.readSmallSmart()
            }

            var z = 0
            if((mask.toInt() and 4) != 0) {
                z = verticesZ.readSmallSmart()
            }
            model.vertexX[vertex] = baseX + x
            model.vertexY[vertex] = baseY + y
            model.vertexZ[vertex] = baseZ + z
            baseX = model.vertexX[vertex]
            baseY = model.vertexY[vertex]
            baseZ = model.vertexZ[vertex]

        }

        val colors = Unpooled.wrappedBuffer(data)
        colors.readBytes(header.colorDataOffset)

        for(face in 0 until header.faces) {
            model.triangleColor[face] = colors.readUnsignedShort()
        }

        val faceData = Unpooled.wrappedBuffer(data)
        faceData.readBytes(header.faceDataOffset)

        val types = Unpooled.wrappedBuffer(data)
        types.readBytes(header.faceTypeOffset)

        var faceX = 0
        var faceY = 0
        var faceZ = 0
        var offset = 0

        for(vertex in 0 until header.faces) {
            val type = types.readUnsignedByte()

            if(type.toInt() == 1) {
                faceX = faceData.readSmallSmart() + offset
                offset = faceX
                faceY = faceData.readSmallSmart() + offset
                offset = faceY
                faceZ = faceData.readSmallSmart() + offset
                offset = faceZ

                model.triangleVertexA[vertex] = faceX
                model.triangleVertexB[vertex] = faceY
                model.triangleVertexC[vertex] = faceZ
            }

            if(type.toInt() == 2) {
                faceY = faceZ
                faceZ = faceData.readSmallSmart() + offset
                offset = faceZ

                model.triangleVertexA[vertex] = faceX
                model.triangleVertexB[vertex] = faceY
                model.triangleVertexC[vertex] = faceZ
            }

            if(type.toInt() == 3) {
                faceX = faceZ
                faceZ = faceData.readSmallSmart() + offset
                offset = faceZ

                model.triangleVertexA[vertex] = faceX
                model.triangleVertexB[vertex] = faceY
                model.triangleVertexC[vertex] = faceZ
            }

            if(type.toInt() == 4) {
                val temp = faceX
                faceX = faceY
                faceY = temp
                faceZ = faceData.readSmallSmart() + offset
                offset = faceZ

                model.triangleVertexA[vertex] = faceX
                model.triangleVertexB[vertex] = faceY
                model.triangleVertexC[vertex] = faceZ
            }
        }
        return model
    }

    override fun read(stream: InputStream): Model {
        val modelData = stream.readBytes()
        stream.close()
        return decode(modelData)
    }
}
