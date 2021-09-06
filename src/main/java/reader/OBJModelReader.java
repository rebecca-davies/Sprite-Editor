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
package reader;

import media.Model;
import util.Colors;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Dane
 */
public class OBJModelReader extends ModelReader {

	private static class OBJMaterial {

		public int red, green, blue;
		public int hsl16, rgb;

		public void setRed(float red) {
			this.red = (int) (red * 255);
		}

		public void setGreen(float green) {
			this.green = (int) (green * 255);

		}

		public void setBlue(float blue) {
			this.blue = (int) (blue * 255);
		}
	}

	OBJModelReader() {

	}

	/**
	 * Unused with this class.
	 *
	 * @deprecated
	 */
	@Deprecated
	@Override
	public Model read(InputStream in) throws IOException {
		return null;
	}

	@Override
	public Model read(File file) throws IOException {
		Model model = new Model();

		try (FileInputStream fis = new FileInputStream(file);
			BufferedReader obj = new BufferedReader(new InputStreamReader(fis))) {
			String s;

			// preprocessor
			obj.lines().forEach((line) -> {
				char c = line.charAt(0);

				if (c == 'v') {
					model.vertexCount++;
				} else if (c == 'f') {
					model.triangleCount++;
				} else if (c == 's') {
					model.triangleType = new int[0];
				}
			});

			// reset to the beginning of our file.
			fis.getChannel().position(0);

			// initialize our components
			model.setVertexCount(model.vertexCount);
			model.setTriangleCount(model.triangleCount);
			model.setColor(0, 7, 64);

			// we set this to new int[0] if our model contains smoothshading flags
			if (model.triangleType != null) {
				model.triangleType = new int[model.triangleCount];
				model.colorA = new int[model.triangleCount];
				model.colorB = new int[model.triangleCount];
				model.colorC = new int[model.triangleCount];
			}

			// our pointers
			int vertex = 0, triangle = 0;
			boolean smooth = true;

			// material informations
			Map<String, OBJMaterial> materials = null;
			OBJMaterial material = null;

			while ((s = obj.readLine()) != null) {
				if (s.length() == 0) {
					continue;
				}

				String[] tokens = s.split("[ \r\n]+");

				char c = tokens[0].charAt(0);

				switch (c) {
					case '#': // comment
					case 'o': { // object name
						// continue reading next line
						continue;
					}

					case 'm': { // material lib
						materials = readMatLib(new File(file.getParentFile(), tokens[1]));

						for (OBJMaterial m : materials.values()) {
							m.hsl16 = Colors.rgbToHSL16(m.red, m.green, m.blue);
							m.rgb = (m.red << 16) | (m.green << 8) | m.blue;
						}
						break;
					}

					case 'u': { // use material
						if (materials == null) {
							throw new IOException("use material before matlib loaded");
						}

						material = materials.get(tokens[1]);
						break;
					}

					case 'v': { // vertex
						model.setVertex(vertex++,
							(int) Float.parseFloat(tokens[1]),
							(int) Float.parseFloat(tokens[2]),
							(int) Float.parseFloat(tokens[3])
						);
						break;
					}

					case 's': { // smooth
						smooth = !"off".equals(tokens[1]);
						break;
					}

					case 'f': { // face
						model.setTriangle(triangle,
							Integer.parseInt(tokens[1]) - 1,
							Integer.parseInt(tokens[2]) - 1,
							Integer.parseInt(tokens[3]) - 1
						);

						// set the triangle color to the current material color.
						if (material != null) {
							if (!smooth) {
								model.colorA[triangle] = material.hsl16;
								model.triangleType[triangle] = 1;
							} else {
								model.triangleColor[triangle] = material.hsl16;
							}
						}

						triangle++;
						break;
					}
				}
			}
		}
		return model;
	}

	private final Map<String, OBJMaterial> readMatLib(File f) throws IOException {
		if (!f.exists()) {
			throw new IOException("matlib file not found: " + f);
		}

		Map<String, OBJMaterial> materials = new HashMap<>();
		OBJMaterial material = null;

		try (BufferedReader lib = new BufferedReader(new FileReader(f))) {
			String s;

			READ_LIB:
			while ((s = lib.readLine()) != null) {
				if (s.length() == 0) {
					continue;
				}

				String[] tokens = s.split("[ \r\n]+");

				switch (tokens[0]) {
					case "newmtl": { // new material
						material = new OBJMaterial();
						materials.put(tokens[1], material);
						break;
					}
					case "Kd": { // we're using diffuse as our rgb color
						if (material == null) {
							throw new IOException("read color before material");
						}

						material.setRed(Float.parseFloat(tokens[1]));
						material.setGreen(Float.parseFloat(tokens[2]));
						material.setBlue(Float.parseFloat(tokens[3]));
						break;
					}
				}
			}
		}
		return materials;
	}

}
