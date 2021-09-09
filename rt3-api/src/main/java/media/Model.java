package media;

import image.Graphics3D;
import image.Graphics2D;

import java.util.Arrays;

/*
 * Copyright (C) 2015 Dane.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
/**
 *
 * @author Dane
 */
public class Model {

    /**
     * The number of triangles drawn in the last frame.
     */
    public static int frameTriangleCount;

    /**
     * Used to check if the mouse is within a triangle for input on a model.
     */
    public static boolean allowInput;

    /**
     * The stored mouse position for checking input.
     */
    public static int mouseX, mouseY;

    /**
     * The closest to the camera a point can be before it becomes a corrected triangle.
     */
    public static final int NEAR_Z = 50;

    /**
     * The farthest a model can be from the camera before it stops being drawn.
     */
    public static final int FAR_Z = 0x7FFF;

    public static final int FAR_Z_1616 = FAR_Z << 16;

    /**
     * The max triangle constant.
     */
    public static final int MAX_TRIANGLE_COUNT = 1024 * 16;

    /**
     * The max vertex constant.
     */
    public static final int MAX_VERTEX_COUNT = 1024 * 16;

    /**
     * Determines whether the scanlines a triangle produces should be clamped on the screen.
     */
    public static boolean[] testTriangleX = new boolean[MAX_TRIANGLE_COUNT];

    /**
     * Determines whether the triangles should be z corrected.
     */
    public static boolean[] correctTriangleZ = new boolean[MAX_TRIANGLE_COUNT];

    /**
     * The horizontal position of a vertex on the screen.
     */
    public static int[] vertexScreenX = new int[MAX_VERTEX_COUNT];

    /**
     * The vertical position of a vertex on the screen.
     */
    public static int[] vertexScreenY = new int[MAX_VERTEX_COUNT];

    /**
     * The distance of a vertex from the screen.
     */
    public static int[] vertexDepth = new int[MAX_VERTEX_COUNT];

    /**
     * The transformed x component of a vertex.
     */
    public static int[] vertexProjectX = new int[MAX_VERTEX_COUNT];

    /**
     * The texture render types for OSRS, currently unused
     */

    public static byte[] textureRenderTypes = new byte[MAX_TRIANGLE_COUNT];
    /**
     * The transformed y component of a vertex.
     */
    public static int[] vertexProjectY = new int[MAX_VERTEX_COUNT];

    /**
     * The corrected vertex components.
     */
    public static int[] tmpX = new int[4], tmpY = new int[4], tmpZ = new int[4], tmpColor = new int[4];

    /**
     * A 16.16 fixed point sine lookup table.
     */
    public static int[] sin = Graphics3D.sin;

    /**
     * A 16.16 fixed point cosine lookup table.
     */
    public static int[] cos = Graphics3D.cos;

    /**
     * A hsl to rgb lookup table.
     */
    public static int[] palette = Graphics3D.palette;

    /**
     * A 16.16 fixed point fraction lookup table.
     */
    public static int[] oneOverFixed1616 = Graphics3D.oneOverFixed1616;


    public void recolor(int src, int dst) {
        for (int k = 0; k < triangleCount; k++) {
            if (triangleColor[k] == src) {
                triangleColor[k] = dst;
            }
        }
    }

    /**
     * Scales the model
     */
    public void scale(int x, int z, int y) {
        for (int v = 0; v < vertexCount; v++) {
            vertexX[v] = (vertexX[v] * x) / 128;
            vertexY[v] = (vertexY[v] * z) / 128;
            vertexZ[v] = (vertexZ[v] * y) / 128;
        }
    }

    /**
     *
     * @param x the x.
     * @param y the y.
     * @param yA the y of a.
     * @param yB the y of b.
     * @param yC the y of c.
     * @param xA the x of a.
     * @param xB the x of b.
     * @param xC the x of c.
     * @return whether x and y is within the boundaries of A, B, C.
     */
    public static final boolean withinTriangle(int x, int y, int yA, int yB, int yC, int xA, int xB, int xC) {
        if (y < yA && y < yB && y < yC) {
            return false;
        }
        if (y > yA && y > yB && y > yC) {
            return false;
        }
        if (x < xA && x < xB && x < xC) {
            return false;
        }
        return !(x > xA && x > xB && x > xC);
    }

    /**
     * Returns the adjusted lightness of the provided hsl value.
     *
     * @param hsl the hsl.
     * @param lightness the lightness.
     * @param type the triangle type.
     * @return if the type == textured then only the lightness value, else the adjusted hsl value.
     */
    public static final int adjustTriangleHSLLightness(int hsl, int lightness, int type) {
        if ((type & 0x2) == 2) {
            if (lightness < 0) {
                lightness = 0;
            } else if (lightness > 127) {
                lightness = 127;
            }
            lightness = 127 - lightness;
            return lightness;
        }

        lightness = lightness * (hsl & 0x7f) >> 7;

        if (lightness < 2) {
            lightness = 2;
        } else if (lightness > 126) {
            lightness = 126;
        }

        return (hsl & 0xff80) + lightness;
    }

    /**
     * The vertex count.
     */
    public int vertexCount;

    /**
     * The vertices x components.
     */
    public int[] vertexX;

    /**
     * The vertices y components.
     */
    public int[] vertexY;

    /**
     * The vertices z components.
     */
    public int[] vertexZ;

    /**
     * The triangle count.
     */
    public int triangleCount;

    /**
     * The triangles first vertex.
     */
    public int[] triangleVertexA;

    /**
     * The triangles second vertex.
     */
    public int[] triangleVertexB;

    /**
     * The triangles third vertex.
     */
    public int[] triangleVertexC;

    /**
     * The triangles color.
     */
    public int[] triangleColor;

    /**
     * The models priority
     */
    public int priority;

    /**
     * A color component.
     */
    public int[] colorA;

    /**
     * A color component.
     */
    public int[] colorB;

    /**
     * A color component.
     */
    public int[] colorC;

    /**
     * The triangles type.
     */
    public int[] triangleType;

    /**
     * The triangles priorities. (Currently unused due to zbuffer)
     */
    public int[] trianglePriorities;

    /**
     * The triangles alpha component.
     */
    public int[] triangleAlpha;

    /**
     * The width boundaries.
     */
    public int minBoundX, maxBoundX;

    /**
     * The length boundaries.
     */
    public int minBoundZ, maxBoundZ;

    /**
     * The height boundaries.
     */
    public int minBoundY, maxBoundY;

    public int boundLengthXZ;

    /**
     * The depth boundaries. (Currently unused due to zbuffer)
     */
    public int maxDepth, minDepth;

    /**
     * The normals for each vertex.
     */
    public Normal[] normals;

    /**
     * The copies of the original normals for each vertex.
     */
    public Normal[] unmodifiedNormals;

    /**
     * Constructs a new empty model.
     */
    public Model() {
    }

    /**
     * Sets the color of all the triangles.
     *
     * @param hue the hue. (0-63)
     * @param saturation the saturation. (0-7)
     * @param lightness the lightness. (0-127) (0 = black) (127 = white)
     */
    public void setColor(int hue, int saturation, int lightness) {
        setColor((hue << 10) | (saturation << 7) | lightness);
    }

    /**
     * Sets the color of all the triangles. (<b>Note:</b> uses {@code triangleCount})
     *
     * @param hsl the hsl.
     */
    public void setColor(int hsl) {
        if (this.triangleColor == null) {
            this.triangleColor = new int[this.triangleCount];
        }
        Arrays.fill(this.triangleColor, hsl);
    }

    /**
     * Initializes the vertex components and sets the count.
     *
     * @param count the count.
     */
    public void setVertexCount(int count) {
        this.vertexCount = count;
        this.vertexX = new int[count];
        this.vertexY = new int[count];
        this.vertexZ = new int[count];
    }

    /**
     * Initializes the triangle components and sets the count.
     *
     * @param count the count.
     */
    public void setTriangleCount(int count) {
        this.triangleCount = count;
        this.triangleVertexA = new int[count];
        this.triangleVertexB = new int[count];
        this.triangleVertexC = new int[count];
    }

    /**
     * Sets the vertex xyz values.
     *
     * @param index the vertex index.
     * @param x the x.
     * @param y the y.
     * @param z the z.
     * @return the vertex index.
     */
    public final int setVertex(int index, int x, int y, int z) {
        vertexX[index] = x;
        vertexY[index] = y;
        vertexZ[index] = z;
        return index;
    }

    /**
     * Sets the triangle abc vertex pointers.
     *
     * @param index the triangle index.
     * @param a the first vertex.
     * @param b the second vertex.
     * @param c the third vertex.
     * @return the triangle index.
     */
    public final int setTriangle(int index, int a, int b, int c) {
        triangleVertexA[index] = a;
        triangleVertexB[index] = b;
        triangleVertexC[index] = c;
        return index;
    }

    /**
     * Inverts the triangles.
     */
    public void flipBackwards() {
        for (int v = 0; v < vertexCount; v++) {
            vertexZ[v] = -vertexZ[v];
        }

        for (int t = 0; t < triangleCount; t++) {
            int a = triangleVertexA[t];
            triangleVertexA[t] = triangleVertexC[t];
            triangleVertexC[t] = a;
        }
    }

    /**
     * Calculates the normals.
     */
    public final void calculateNormals() {
        if (normals == null) {
            normals = new Normal[vertexCount];

            for (int n = 0; n < vertexCount; n++) {
                normals[n] = new Normal();
            }
        }

        for (int t = 0; t < triangleCount; t++) {
            int a = triangleVertexA[t];
            int b = triangleVertexB[t];
            int c = triangleVertexC[t];

            int dxAB = vertexX[b] - vertexX[a];
            int dyAB = vertexY[b] - vertexY[a];
            int dzAB = vertexZ[b] - vertexZ[a];

            int dxCA = vertexX[c] - vertexX[a];
            int dyCA = vertexY[c] - vertexY[a];
            int dzCA = vertexZ[c] - vertexZ[a];

            int lX = (dyAB * dzCA) - (dyCA * dzAB);
            int lY = (dzAB * dxCA) - (dzCA * dxAB);
            int lZ = (dxAB * dyCA) - (dxCA * dyAB);

            // while it's too large, shrink it by half
            for (; (lX > 8192 || lY > 8192 || lZ > 8192 || lX < -8192 || lY < -8192 || lZ < -8192);) {
                lX >>= 1;
                lY >>= 1;
                lZ >>= 1;
            }

            int length = (int) Math.sqrt((double) (lX * lX + lY * lY + lZ * lZ));

            if (length <= 0) {
                length = 1;
            }

            // normalizing
            lX = (lX * 256) / length;
            lY = (lY * 256) / length;
            lZ = (lZ * 256) / length;

            if (triangleType == null || (triangleType[t] & 0x1) == 0) {
                Normal n = normals[a];
                n.x += lX;
                n.y += lY;
                n.z += lZ;
                n.magnitude++;

                n = normals[b];
                n.x += lX;
                n.y += lY;
                n.z += lZ;
                n.magnitude++;

                n = normals[c];
                n.x += lX;
                n.y += lY;
                n.z += lZ;
                n.magnitude++;
            }
        }
    }

    /**
     * Calculates the normals and then applies lighting values to each vertex.
     *
     * @param minIntensity the minimum lightness.
     * @param intensity the light lightness.
     * @param x the light source x.
     * @param y the light source y.
     * @param z the light source z.
     * @param apply whether to calculate lighting and y boundaries or copy normals and calculate boundaries.
     */
    public final void applyLighting(int minIntensity, int intensity, int x, int y, int z, boolean apply) {
        int lightMagnitude = (int) Math.sqrt((double) (x * x + y * y + z * z));
        int lightIntensity = intensity * lightMagnitude >> 8;

        if (colorA == null) {
            colorA = new int[triangleCount];
            colorB = new int[triangleCount];
            colorC = new int[triangleCount];
        }

        if (normals == null) {
            normals = new Normal[vertexCount];

            for (int n = 0; n < vertexCount; n++) {
                normals[n] = new Normal();
            }
        }

        for (int t = 0; t < triangleCount; t++) {
            int a = triangleVertexA[t];
            int b = triangleVertexB[t];
            int c = triangleVertexC[t];

            int dxAB = vertexX[b] - vertexX[a];
            int dyAB = vertexY[b] - vertexY[a];
            int dzAB = vertexZ[b] - vertexZ[a];

            int dxCA = vertexX[c] - vertexX[a];
            int dyCA = vertexY[c] - vertexY[a];
            int dzCA = vertexZ[c] - vertexZ[a];

            int lX = dyAB * dzCA - dyCA * dzAB;
            int lY = dzAB * dxCA - dzCA * dxAB;
            int lZ = dxAB * dyCA - dxCA * dyAB;

            // while it's too large, shrink it by half
            for (; (lX > 8192 || lY > 8192 || lZ > 8192 || lX < -8192 || lY < -8192 || lZ < -8192);) {
                lX >>= 1;
                lY >>= 1;
                lZ >>= 1;
            }

            int length = (int) Math.sqrt((double) (lX * lX + lY * lY + lZ * lZ));

            if (length <= 0) {
                length = 1;
            }

            // normalizing
            lX = (lX * 256) / length;
            lY = (lY * 256) / length;
            lZ = (lZ * 256) / length;

            if (triangleType == null || (triangleType[t] & 0x1) == 0) {
                Normal n = normals[a];
                n.x += lX;
                n.y += lY;
                n.z += lZ;
                n.magnitude++;

                n = normals[b];
                n.x += lX;
                n.y += lY;
                n.z += lZ;
                n.magnitude++;

                n = normals[c];
                n.x += lX;
                n.y += lY;
                n.z += lZ;
                n.magnitude++;
            } else {
                int lightness = minIntensity + (x * lX + y * lY + z * lZ) / (lightIntensity + lightIntensity / 2);
                colorA[t] = adjustTriangleHSLLightness(triangleColor[t], lightness, triangleType[t]);
            }
        }

        if (apply) {
            calculateLighting(minIntensity, lightIntensity, x, y, z);
        } else {
            unmodifiedNormals = new Normal[vertexCount];

            for (int v = 0; v < vertexCount; v++) {
                Normal current = normals[v];
                Normal copy = unmodifiedNormals[v] = new Normal();
                copy.x = current.x;
                copy.y = current.y;
                copy.z = current.z;
                copy.magnitude = current.magnitude;
            }
        }

        if (apply) {
            calculateYBoundaries();
        } else {
            calculateBoundaries();
        }
    }

    /**
     * Calculates the colors of each component depending on their normals.
     *
     * @param minIntensity the minimum lightness.
     * @param intensity the intensity.
     * @param x the light source x.
     * @param y the light source y.
     * @param z the light source z.
     */
    public final void calculateLighting(int minIntensity, int intensity, int x, int y, int z) {
        if (colorA == null) {
            colorA = new int[triangleCount];
            colorB = new int[triangleCount];
            colorC = new int[triangleCount];
        }

        for (int t = 0; t < triangleCount; t++) {
            int a = triangleVertexA[t];
            int b = triangleVertexB[t];
            int c = triangleVertexC[t];

            if (triangleType == null) {
                int color = triangleColor[t];

                Normal n = normals[a];
                int lightness = minIntensity + ((x * n.x + y * n.y + z * n.z) / (intensity * n.magnitude));

                colorA[t] = adjustTriangleHSLLightness(color, lightness, 0);

                n = normals[b];
                lightness = minIntensity + ((x * n.x + y * n.y + z * n.z) / (intensity * n.magnitude));
                colorB[t] = adjustTriangleHSLLightness(color, lightness, 0);

                n = normals[c];
                lightness = minIntensity + ((x * n.x + y * n.y + z * n.z) / (intensity * n.magnitude));
                colorC[t] = adjustTriangleHSLLightness(color, lightness, 0);
            } else if ((triangleType[t] & 0x1) == 0) {
                int color = triangleColor[t];
                int info = triangleType[t];
                int lightness;

                Normal n = normals[a];
                lightness = minIntensity + ((x * n.x + y * n.y + z * n.z) / (intensity * n.magnitude));
                colorA[t] = adjustTriangleHSLLightness(color, lightness, info);

                n = normals[b];
                lightness = minIntensity + ((x * n.x + y * n.y + z * n.z) / (intensity * n.magnitude));
                colorB[t] = adjustTriangleHSLLightness(color, lightness, info);

                n = normals[c];
                lightness = minIntensity + ((x * n.x + y * n.y + z * n.z) / (intensity * n.magnitude));
                colorC[t] = adjustTriangleHSLLightness(color, lightness, info);
            }
        }
    }

    /**
     * Used when normals are calculated and shading is applied.
     */
    public final void calculateYBoundaries() {
        maxBoundY = 0;
        boundLengthXZ = 0;
        minBoundY = 0;

        for (int v = 0; v < vertexCount; v++) {
            int x = vertexX[v];
            int y = vertexY[v];
            int z = vertexZ[v];

            if (-y > maxBoundY) {
                maxBoundY = -y;
            }

            if (y > minBoundY) {
                minBoundY = y;
            }

            // couldn't think of a better name. This is squared.
            int length2 = x * x + z * z;

            if (length2 > boundLengthXZ) {
                boundLengthXZ = length2;
            }
        }

        boundLengthXZ = (int) Math.sqrt((double) boundLengthXZ);
        maxDepth = (int) Math.sqrt((double) (boundLengthXZ * boundLengthXZ + maxBoundY * maxBoundY));
        minDepth = maxDepth + (int) Math.sqrt((double) (boundLengthXZ * boundLengthXZ + minBoundY * minBoundY));
    }

    /**
     * Used when normals are calculated, but no shading is applied.
     */
    public void calculateBoundaries() {
        boundLengthXZ = 0;

        minBoundX = 999999;
        maxBoundX = -999999;

        maxBoundY = 0;
        minBoundY = 0;

        maxBoundZ = -99999;
        minBoundZ = 99999;

        for (int v = 0; v < vertexCount; v++) {
            int x = vertexX[v];
            int y = vertexY[v];
            int z = vertexZ[v];

            if (x < minBoundX) {
                minBoundX = x;
            }

            if (x > maxBoundX) {
                maxBoundX = x;
            }

            if (z < minBoundZ) {
                minBoundZ = z;
            }

            if (z > maxBoundZ) {
                maxBoundZ = z;
            }

            if (-y > maxBoundY) {
                maxBoundY = -y;
            }

            if (y > minBoundY) {
                minBoundY = y;
            }

            // couldn't think of a better name. This is squared.
            int length2 = x * x + z * z;

            if (length2 > boundLengthXZ) {
                boundLengthXZ = length2;
            }
        }

        boundLengthXZ = (int) Math.sqrt((double) boundLengthXZ);
        maxDepth = (int) Math.sqrt((double) (boundLengthXZ * boundLengthXZ + maxBoundY * maxBoundY));
        minDepth = maxDepth + (int) Math.sqrt((double) (boundLengthXZ * boundLengthXZ + minBoundY * minBoundY));
    }

    /**
     * Translates all the vertices by the direction provided.
     *
     * @param x the x.
     * @param y the y.
     * @param z the z.
     */
    public void translate(int x, int y, int z) {
        for (int v = 0; v < this.vertexCount; v++) {
            this.vertexX[v] += x;
            this.vertexY[v] += y;
            this.vertexZ[v] += z;
        }
    }

    /**
     * Draws this model that does not take input and ignores NearZ/FarZ constants. (<b>Warning:</b> not for drawing
     * models on a scene.)
     *
     * @param pitch the pitch.
     * @param yaw the yaw.
     * @param roll the roll.
     * @param sceneX the camera x.
     * @param sceneY the camera y.
     * @param sceneZ the camera z.
     * @param cameraPitch the camera pitch.
     */
    public void draw(int pitch, int yaw, int roll, int eyePitch, int sceneX, int sceneY, int sceneZ) {
        final int centerX = Graphics3D.centerX;
        final int centerY = Graphics3D.centerY;

        int pitchSine = sin[pitch];
        int pitchCosine = cos[pitch];

        int yawSine = sin[yaw];
        int yawCosine = cos[yaw];

        int rollSine = sin[roll];
        int rollCosine = cos[roll];

        int cameraPitchSine = sin[eyePitch];
        int cameraPitchCosine = cos[eyePitch];

        for (int v = 0; v < vertexCount; v++) {
            int x = vertexX[v];
            int y = vertexY[v];
            int z = vertexZ[v];

            if (roll != 0) {
                int z0 = y * rollSine + x * rollCosine >> 16;
                y = y * rollCosine - x * rollSine >> 16;
                x = z0;
            }

            if (pitch != 0) {
                int x0 = y * pitchCosine - z * pitchSine >> 16;
                z = y * pitchSine + z * pitchCosine >> 16;
                y = x0;
            }

            if (yaw != 0) {
                int y0 = z * yawSine + x * yawCosine >> 16;
                z = z * yawCosine - x * yawSine >> 16;
                x = y0;
            }

            x += sceneX;
            y += sceneY;
            z += sceneZ;

            int x0 = y * cameraPitchCosine - z * cameraPitchSine >> 16;
            z = y * cameraPitchSine + z * cameraPitchCosine >> 16;
            y = x0;

            if(z <= 0) {
                z = 1;
            }
            vertexDepth[v] = z;
            vertexScreenX[v] = centerX + (x << 9) / z;
            vertexScreenY[v] = centerY + (y << 9) / z;
        }

        draw(0, false, false);
    }

    public void drawSimple(int pitch, int yaw, int roll, int eyePitch, int eyeX, int eyeY, int eyeZ) {
        int centerX = Graphics3D.centerX;
        int centerY = Graphics3D.centerY;
        int sinPitch = sin[pitch];
        int cosPitch = cos[pitch];
        int sinYaw = sin[yaw];
        int cosYaw = cos[yaw];
        int sinRoll = sin[roll];
        int cosRoll = cos[roll];
        int sinEyePitch = sin[eyePitch];
        int cosEyePitch = cos[eyePitch];
        int midZ = ((eyeY * sinEyePitch) + (eyeZ * cosEyePitch)) >> 16;

        for (int v = 0; v < vertexCount; v++) {
            int x = vertexX[v];
            int y = vertexY[v];
            int z = vertexZ[v];

            // Local Space -> Model Space

            if (roll != 0) {
                int x_ = ((y * sinRoll) + (x * cosRoll)) >> 16;
                y = ((y * cosRoll) - (x * sinRoll)) >> 16;
                x = x_;
            }

            if (pitch != 0) {
                int y_ = ((y * cosPitch) - (z * sinPitch)) >> 16;
                z = ((y * sinPitch) + (z * cosPitch)) >> 16;
                y = y_;
            }

            if (yaw != 0) {
                int x_ = ((z * sinYaw) + (x * cosYaw)) >> 16;
                z = ((z * cosYaw) - (x * sinYaw)) >> 16;
                x = x_;
            }

            // Model Space -> View Space

            x += eyeX;
            y += eyeY;
            z += eyeZ;

            int y_ = ((y * cosEyePitch) - (z * sinEyePitch)) >> 16;
            z = ((y * sinEyePitch) + (z * cosEyePitch)) >> 16;
            y = y_;

            // View Space -> Screen Space

            if(z <= 0) {
                z = 1;
            }

            vertexScreenX[v] = centerX + ((x << 9) / z);
            vertexScreenY[v] = centerY + ((y << 9) / z);
            vertexDepth[v] = z - midZ;

            // Store viewspace coordinates to be transformed into screen space later (textured or clipped triangles)

        }
        try {
            draw(0, false, false);
        } catch (Exception ignored) {
        }
    }

    /**
     * Draws the model.
     *
     * @param pitch the model pitch.
     * @param yaw the model yaw.
     * @param cameraPitchSine the camera pitch sine.
     * @param cameraPitchCosine the camera pitch cosine.
     * @param cameraYawSine the camera yaw sine.
     * @param cameraYawCosine the camera yaw cosine.
     * @param sceneX the scene x.
     * @param sceneY the scene y.
     * @param sceneZ the scene z.
     * @param bitset the model bitset. (Used to identify model in the case of input)
     */
    public void draw(int pitch, int yaw, int cameraPitchSine, int cameraPitchCosine, int cameraYawSine, int cameraYawCosine, int sceneX, int sceneY, int sceneZ, int bitset) {
        int a = sceneZ * cameraYawCosine - sceneX * cameraYawSine >> 16;
        int farZ = sceneY * cameraPitchSine + a * cameraPitchCosine >> 16;
        int c = boundLengthXZ * cameraPitchCosine >> 16;

        int nearZ = farZ + c;

        if (nearZ <= NEAR_Z || farZ >= FAR_Z) {
            return;
        }

        int e = sceneZ * cameraYawSine + sceneX * cameraYawCosine >> 16;

        int minX = e - boundLengthXZ << 9;

        if (minX / nearZ >= Graphics2D.halfWidth) {
            return;
        }

        int maxX = e + boundLengthXZ << 9;

        if (maxX / nearZ <= -Graphics2D.halfWidth) {
            return;
        }

        int h = sceneY * cameraPitchCosine - a * cameraPitchSine >> 16;
        int i = boundLengthXZ * cameraPitchSine >> 16;

        int maxY = h + i << 9;

        if (maxY / nearZ <= -Graphics2D.halfHeight) {
            return;
        }

        int k = i + (maxBoundY * cameraPitchCosine >> 16);
        int minY = h - k << 9;

        if (minY / nearZ >= Graphics2D.halfHeight) {
            return;
        }

        int m = c + (maxBoundY * cameraPitchSine >> 16);
        boolean project = false;

        if (farZ - m <= NEAR_Z) {
            project = true;
        }

        boolean hasInput = false;

        if (bitset > 0 && allowInput) {
            int maxZ = farZ - c;

            if (maxZ <= NEAR_Z) {
                maxZ = NEAR_Z;
            }

            if (e > 0) {
                minX /= nearZ;
                maxX /= maxZ;
            } else {
                maxX /= nearZ;
                minX /= maxZ;
            }

            if (h > 0) {
                minY /= nearZ;
                maxY /= maxZ;
            } else {
                maxY /= nearZ;
                minY /= maxZ;
            }

            int x = mouseX - Graphics3D.centerX;
            int y = mouseY - Graphics3D.centerY;

            if (x > minX && x < maxX && y > minY && y < maxY) {
                hasInput = true;
            }
        }

        int centerX = Graphics3D.centerX;
        int centerY = Graphics3D.centerY;

        int pitchSine = 0;
        int pitchCosine = 0;

        int yawSine = 0;
        int yawCosine = 0;

        if (pitch != 0) {
            pitchSine = sin[pitch];
            pitchCosine = cos[pitch];
        }

        if (yaw != 0) {
            yawSine = sin[yaw];
            yawCosine = cos[yaw];
        }

        for (int v = 0; v < vertexCount; v++) {
            int x = vertexX[v];
            int y = vertexY[v];
            int z = vertexZ[v];

            if (pitch != 0) {
                int w = (y * pitchCosine - z * pitchSine) >> 16;
                z = (y * pitchSine + z * pitchCosine) >> 16;
                y = w;
            }

            if (yaw != 0) {
                int w = (z * yawSine + x * yawCosine) >> 16;
                z = (z * yawCosine - x * yawSine) >> 16;
                x = w;
            }

            x += sceneX;
            y += sceneY;
            z += sceneZ;

            int w = z * cameraYawSine + x * cameraYawCosine >> 16;
            z = z * cameraYawCosine - x * cameraYawSine >> 16;
            x = w;

            w = y * cameraPitchCosine - z * cameraPitchSine >> 16;
            z = y * cameraPitchSine + z * cameraPitchCosine >> 16;
            y = w;

            vertexDepth[v] = z;

            if (z >= NEAR_Z) {
                vertexScreenX[v] = centerX + (x << 9) / z;
                vertexScreenY[v] = centerY + (y << 9) / z;
            } else {
                vertexScreenX[v] = -5000;
                project = true;
            }

            if (project) {
                vertexProjectX[v] = x;
                vertexProjectY[v] = y;
            }
        }

        try {
            draw(bitset, project, hasInput);
        } catch (Exception ex) {

        }

    }

    /**
     * Draws the model after checking for z correction, input, and sufficient triangle area.
     *
     * @param bitset the model bitset. (For input)
     * @param projected whether the model has corrected triangles.
     * @param hasInput whether the model can take input.
     */
    private void draw(int bitset, boolean projected, boolean hasInput) {
        for (int t = 0; t < triangleCount; t++) {
            if (triangleType == null || triangleType[t] != -1) {
                int a = triangleVertexA[t];
                int b = triangleVertexB[t];
                int c = triangleVertexC[t];
                int xA = vertexScreenX[a];
                int xB = vertexScreenX[b];
                int xC = vertexScreenX[c];

                if (projected && (xA == -5000 || xB == -5000 || xC == -5000)) {
                    correctTriangleZ[t] = true;
                    drawTriangle(t);
                } else {
                    if (hasInput && withinTriangle(mouseX, mouseY, vertexScreenY[a], vertexScreenY[b], vertexScreenY[c], xA, xB, xC)) {
                        //hoveredBitsets[hoverCount++] = bitset;
                        hasInput = false;
                    }

                    // ((xA - xB) * (yC - yB)) - ((yA - yB) * (xC - xB))
                    int area = ((xA - xB) * (vertexScreenY[c] - vertexScreenY[b])) - ((vertexScreenY[a] - vertexScreenY[b]) * (xC - xB));

                    // change to > 0 to only allow front faces, < 0 for back faces, and != 0 for both faces.
                    if (area > 0) {
                        correctTriangleZ[t] = false;
                        testTriangleX[t] = xA < 0 || xB < 0 || xC < 0 || xA > Graphics2D.rightX || xB > Graphics2D.rightX || xC > Graphics2D.rightX;
                        drawTriangle(t);
                    }
                }
            }
        }
    }

    /**
     * Draws the triangle.
     *
     * @param index the triangle index.
     */
    private void drawTriangle(int index) {
        frameTriangleCount++;

        if (correctTriangleZ[index]) {
            drawCorrectedTriangle(index);
        } else {
            int a = triangleVertexA[index];
            int b = triangleVertexB[index];
            int c = triangleVertexC[index];

            Graphics3D.testX = testTriangleX[index];

            if (triangleAlpha == null) {
                Graphics3D.alpha = 0;
            } else {
                Graphics3D.alpha = triangleAlpha[index];
            }

            int type;

            if (triangleType == null) {
                type = 0;
            } else {
                type = triangleType[index] & 0x3;
            }

            if (type == 0) {
                Graphics3D.fillShadedTriangleDepth(vertexScreenX[a], vertexScreenY[a], vertexDepth[a], vertexScreenX[b], vertexScreenY[b], vertexDepth[b], vertexScreenX[c], vertexScreenY[c], vertexDepth[c], colorA[index], colorB[index], colorC[index]);
            } else if (type == 1) {
                Graphics3D.fillTriangleDepth(vertexScreenX[a], vertexScreenY[a], vertexDepth[a], vertexScreenX[b], vertexScreenY[b], vertexDepth[b], vertexScreenX[c], vertexScreenY[c], vertexDepth[c], palette[colorA[index]]);
            }
        }
    }

    /**
     * Draws the corrected triangle.
     *
     * @param index the triangle index.
     */
    private void drawCorrectedTriangle(int index) {
        int cx = Graphics3D.centerX;
        int cy = Graphics3D.centerY;
        int n = 0;

        int vA = triangleVertexA[index];
        int vB = triangleVertexB[index];
        int vC = triangleVertexC[index];

        int zA = vertexDepth[vA];
        int zB = vertexDepth[vB];
        int zC = vertexDepth[vC];

        if (zA >= NEAR_Z) {
            tmpX[n] = vertexScreenX[vA];
            tmpY[n] = vertexScreenY[vA];
            tmpZ[n] = zA;
            tmpColor[n++] = colorA[index];
        } else {
            int x = vertexProjectX[vA];
            int y = vertexProjectY[vA];
            int color = colorA[index];

            if (zC >= NEAR_Z) {
                int interpolant = (NEAR_Z - zA) * oneOverFixed1616[zC - zA];
                tmpX[n] = cx + ((x + (((vertexProjectX[vC] - x) * interpolant) >> 16)) << 9) / NEAR_Z;
                tmpY[n] = cy + ((y + (((vertexProjectY[vC] - y) * interpolant) >> 16)) << 9) / NEAR_Z;
                tmpZ[n] = ((zC - zA) * interpolant) >> 16;
                tmpColor[n++] = color + ((colorC[index] - color) * interpolant >> 16);
            }

            if (zB >= NEAR_Z) {
                int interpolant = (NEAR_Z - zA) * oneOverFixed1616[zB - zA];
                tmpX[n] = (cx + (x + ((vertexProjectX[vB] - x) * interpolant >> 16) << 9) / NEAR_Z);
                tmpY[n] = (cy + (y + ((vertexProjectY[vB] - y) * interpolant >> 16) << 9) / NEAR_Z);
                tmpZ[n] = ((zB - zA) * interpolant) >> 16;
                tmpColor[n++] = color + ((colorB[index] - color) * interpolant >> 16);
            }
        }

        if (zB >= NEAR_Z) {
            tmpX[n] = vertexScreenX[vB];
            tmpY[n] = vertexScreenY[vB];
            tmpZ[n] = zB;
            tmpColor[n++] = colorB[index];
        } else {
            int x = vertexProjectX[vB];
            int y = vertexProjectY[vB];
            int color = colorB[index];

            if (zA >= NEAR_Z) {
                int interpolant = (NEAR_Z - zB) * oneOverFixed1616[zA - zB];
                tmpX[n] = (cx + (x + ((vertexProjectX[vA] - x) * interpolant >> 16) << 9) / NEAR_Z);
                tmpY[n] = (cy + (y + ((vertexProjectY[vA] - y) * interpolant >> 16) << 9) / NEAR_Z);
                tmpZ[n] = ((zA - zB) * interpolant) >> 16;
                tmpColor[n++] = color + ((colorA[index] - color) * interpolant >> 16);
            }

            if (zC >= NEAR_Z) {
                int interpolant = (NEAR_Z - zB) * oneOverFixed1616[zC - zB];
                tmpX[n] = (cx + (x + ((vertexProjectX[vC] - x) * interpolant >> 16) << 9) / NEAR_Z);
                tmpY[n] = (cy + (y + ((vertexProjectY[vC] - y) * interpolant >> 16) << 9) / NEAR_Z);
                tmpZ[n] = ((zC - zB) * interpolant) >> 16;
                tmpColor[n++] = color + ((colorC[index] - color) * interpolant >> 16);
            }
        }

        if (zC >= NEAR_Z) {
            tmpX[n] = vertexScreenX[vC];
            tmpY[n] = vertexScreenY[vC];
            tmpZ[n] = zC;
            tmpColor[n++] = colorC[index];
        } else {
            int x = vertexProjectX[vC];
            int y = vertexProjectY[vC];
            int color = colorC[index];

            if (zB >= NEAR_Z) {
                int interpolant = (NEAR_Z - zC) * (oneOverFixed1616[zB - zC]);
                tmpX[n] = (cx + (x + (((vertexProjectX[vB] - x) * interpolant) >> 16) << 9) / NEAR_Z);
                tmpY[n] = (cy + (y + (((vertexProjectY[vB] - y) * interpolant) >> 16) << 9) / NEAR_Z);
                tmpZ[n] = ((zB - zC) * interpolant) >> 16;
                tmpColor[n++] = color + ((colorB[index] - color) * interpolant >> 16);
            }

            if (zA >= NEAR_Z) {
                int interpolant = (NEAR_Z - zC) * oneOverFixed1616[zA - zC];
                tmpX[n] = (cx + (x + (((vertexProjectX[vA] - x) * interpolant) >> 16) << 9) / NEAR_Z);
                tmpY[n] = (cy + (y + (((vertexProjectY[vA] - y) * interpolant) >> 16) << 9) / NEAR_Z);
                tmpZ[n] = ((zA - zC) * interpolant) >> 16;
                tmpColor[n++] = color + ((colorA[index] - color) * interpolant >> 16);
            }
        }

        int xA = tmpX[0];
        int xB = tmpX[1];
        int xC = tmpX[2];

        int yA = tmpY[0];
        int yB = tmpY[1];
        int yC = tmpY[2];

        if (((xA - xB) * (yC - yB) - (yA - yB) * (xC - xB)) > 0) {
            Graphics3D.testX = false;

            if (n == 3) {
                if (xA < 0 || xB < 0 || xC < 0 || xA > Graphics2D.rightX || xB > Graphics2D.rightX || xC > Graphics2D.rightX) {
                    Graphics3D.testX = true;
                }

                int type;

                if (triangleType == null) {
                    type = 0;
                } else {
                    type = triangleType[index] & 0x3;
                }

                if (type == 0) {
                    Graphics3D.fillShadedTriangleDepth(xA, yA, zA, xB, yB, zB, xC, yC, zC, tmpColor[0], tmpColor[1], tmpColor[2]);
                } else if (type == 1) {
                    Graphics3D.fillTriangleDepth(xA, yA, zA, xB, yB, zB, xC, yC, zC, palette[colorA[index]]);
                }
            } else if (n == 4) {
                if (xA < 0 || xB < 0 || xC < 0 || xA > Graphics2D.rightX || xB > Graphics2D.rightX || xC > Graphics2D.rightX || tmpX[3] < 0 || tmpX[3] > Graphics2D.rightX) {
                    Graphics3D.testX = true;
                }

                int type;

                if (triangleType == null) {
                    type = 0;
                } else {
                    type = triangleType[index] & 0x3;
                }

                // one extra
                frameTriangleCount++;

                if (type == 0) {
                    Graphics3D.fillShadedTriangleDepth(xA, yA, zA, xB, yB, zB, xC, yC, zC, tmpColor[0], tmpColor[1], tmpColor[2]);
                    Graphics3D.fillShadedTriangleDepth(xA, yA, zA, xC, yC, zC, tmpX[3], tmpY[3], tmpZ[3], tmpColor[0], tmpColor[2], tmpColor[3]);
                } else if (type == 1) {
                    int rgb = palette[colorA[index]];
                    Graphics3D.fillTriangleDepth(xA, yA, zA, xB, yB, zB, xC, yC, zC, rgb);
                    Graphics3D.fillTriangleDepth(xA, yA, zA, xC, yC, zC, tmpX[3], tmpY[3], tmpZ[3], rgb);
                }
            }
        }
    }

}
