package sh.rebecca.inventory.model

import io.guthix.buffer.readSmallSmart
import io.netty.buffer.Unpooled
import media.Model
import org.springframework.stereotype.Component
import reader.ModelReader
import java.io.InputStream

@Component
class RSModelReader : ModelReader() {

    fun decode317Model(data: ByteArray): Model {
        val header = decode317Header(data)
        val model = Model()
        model.setVertexCount(header.vertices)
        model.setTriangleCount(header.faces)
        model.vertexX = IntArray(header.vertices)
        model.vertexY = IntArray(header.vertices)
        model.vertexZ = IntArray(header.vertices)
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

        for(vertex in 0 until model.vertexCount) {
            val mask = directions.readUnsignedByte()

            var x = 0
            var y = 0
            var z = 0
            if((mask.toInt() and 1) != 0) {
                x = verticesX.readSmallSmart()
            }

            if((mask.toInt() and 2) != 0) {
                y = verticesY.readSmallSmart()
            }

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

    fun decode317Header(data: ByteArray): ModelHeader {
        val buffer = Unpooled.wrappedBuffer(data)
        buffer.readBytes(data.size - 18)

        val vertices = buffer.readUnsignedShort()
        val faceCount = buffer.readUnsignedShort()
        val texturedFaceCount = buffer.readUnsignedByte()

        val useTextures = buffer.readUnsignedByte()
        val useFacePriority = buffer.readUnsignedByte()
        val useTransparency = buffer.readUnsignedByte()
        val useFaceSkinning = buffer.readUnsignedByte()
        val useVertexSkinning = buffer.readUnsignedByte()

        val xDataOffset = buffer.readUnsignedShort()
        val yDataOffset = buffer.readUnsignedShort()
        val zDataOffset = buffer.readUnsignedShort()
        val faceDataLength = buffer.readUnsignedShort()

        var offset = 0
        val vertexDirectionOffset = offset
        offset += vertices

        val faceTypeOffset = offset
        offset += faceCount

        var facePriorityOffset = offset
        if(useFacePriority.toInt() == 255) {
            offset += faceCount
        } else {
            facePriorityOffset = -1
        }

        var faceSkinOffset = offset
        if(useFaceSkinning.toInt() == 1) {
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

        val faceDataOffset = offset
        offset += faceDataLength

        val faceColorDataOffset = offset
        offset += faceCount * 2

        var uvMapFaceOffset = offset
        offset += texturedFaceCount * 6

        val xDataLength = offset
        offset += xDataOffset

        val yDataLength = offset
        offset += yDataOffset

        val zDataLength = offset
        offset += zDataOffset

        return ModelHeader(data, faceDataOffset, faceCount, faceTypeOffset, faceColorDataOffset, vertexDirectionOffset, vertices, xDataLength, yDataLength, zDataLength)
    }

    override fun read(stream: InputStream): Model {
        val modelData = stream.readBytes()
        stream.close()
        return decode317Model(modelData)
    }
}
