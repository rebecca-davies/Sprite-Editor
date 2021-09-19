package sh.rebecca.inventory.model

import io.Buffer
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

        val directions = Buffer(data)
        directions.position = header.vertexDirectionOffset

        val verticesX = Buffer(data)
        verticesX.position = header.xDataOffset

        val verticesY = Buffer(data)
        verticesY.position = header.yDataOffset

        val verticesZ = Buffer(data)
        verticesZ.position = header.zDataOffset

        var baseX = 0
        var baseY = 0
        var baseZ = 0

        for(vertex in 0 until model.vertexCount) {
            val mask = directions.read()

            var x = 0
            var y = 0
            var z = 0
            if((mask and 1) != 0) {
                x = verticesX.readSmart()
            }

            if((mask and 2) != 0) {
                y = verticesY.readSmart()
            }

            if((mask and 4) != 0) {
                z = verticesZ.readSmart()
            }
            model.vertexX[vertex] = baseX + x
            model.vertexY[vertex] = baseY + y
            model.vertexZ[vertex] = baseZ + z
            baseX = model.vertexX[vertex]
            baseY = model.vertexY[vertex]
            baseZ = model.vertexZ[vertex]
        }

        val colors = Buffer(data)
        colors.position = header.colorDataOffset

        for(face in 0 until header.faces) {
            model.triangleColor[face] = colors.readUShort()
        }

        val faceData = Buffer(data)
        faceData.position = header.faceDataOffset

        val types = Buffer(data)
        types.position = header.faceTypeOffset

        var faceX = 0
        var faceY = 0
        var faceZ = 0
        var offset = 0

        for(vertex in 0 until header.faces) {
            val type = types.read()

            if(type == 1) {
                faceX = faceData.readSmart() + offset
                offset = faceX
                faceY = faceData.readSmart() + offset
                offset = faceY
                faceZ = faceData.readSmart() + offset
                offset = faceZ

                model.triangleVertexA[vertex] = faceX
                model.triangleVertexB[vertex] = faceY
                model.triangleVertexC[vertex] = faceZ
            }

            if(type == 2) {
                faceY = faceZ
                faceZ = faceData.readSmart() + offset
                offset = faceZ

                model.triangleVertexA[vertex] = faceX
                model.triangleVertexB[vertex] = faceY
                model.triangleVertexC[vertex] = faceZ
            }

            if(type == 3) {
                faceX = faceZ
                faceZ = faceData.readSmart() + offset
                offset = faceZ

                model.triangleVertexA[vertex] = faceX
                model.triangleVertexB[vertex] = faceY
                model.triangleVertexC[vertex] = faceZ
            }

            if(type == 4) {
                val temp = faceX
                faceX = faceY
                faceY = temp
                faceZ = faceData.readSmart() + offset
                offset = faceZ

                model.triangleVertexA[vertex] = faceX
                model.triangleVertexB[vertex] = faceY
                model.triangleVertexC[vertex] = faceZ
            }
        }
        return model
    }

    fun decode317Header(data: ByteArray): ModelHeader {
        val buffer = Buffer(data)
        buffer.position = data.size - 18

        val vertices = buffer.readUShort()
        val faceCount = buffer.readUShort()
        val texturedFaceCount = buffer.read()

        val useTextures = buffer.read()
        val useFacePriority = buffer.read()
        val useTransparency = buffer.read()
        val useFaceSkinning = buffer.read()
        val useVertexSkinning = buffer.read()

        val xDataOffset = buffer.readUShort()
        val yDataOffset = buffer.readUShort()
        val zDataOffset = buffer.readUShort()
        val faceDataLength = buffer.readUShort()

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
