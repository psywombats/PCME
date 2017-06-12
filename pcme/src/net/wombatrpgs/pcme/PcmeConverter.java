/**
 *  PcmeConverter.java
 *  Created on Jun 11, 2017 7:51:11 PM for project pcme
 *  Author: psy_wombats
 *  Contact: psy_wombats@wombatrpgs.net
 */
package net.wombatrpgs.pcme;

import java.io.File;
import java.util.ArrayList;

import tiled.core.Map;
import tiled.core.ObjectGroup;
import tiled.core.Tile;
import tiled.core.TileLayer;
import tiled.io.TMXMapReader;

/**
 * Individual conversion job for pcme.
 */
public class PcmeConverter {
	
	protected File inputFile;
	
	/**
	 * Creates a new conversion job for a given file.
	 * @param	inputFile		The .tmx file to convert
	 */
	public PcmeConverter(File inputFile) {
		this.inputFile = inputFile;
	}

	/**
	 * Converts the source .tmx information into a string for use in a .des dcss file.
	 * @return					The equivalent info for dcss
	 */
	public String convertToDcssString() {
		TMXMapReader reader = new TMXMapReader();
		Map map = null;
		try {
			map = reader.readMap(inputFile.getAbsolutePath());
		} catch (Exception e) {
			reportError("TMX error");
			e.printStackTrace();
			return "";
		}
		
		if (map.getLayerCount() != 3) {
			reportError("Expecting 3 layers, found " + map.getLayerCount());
			return "";
		}
		if (!TileLayer.class.isAssignableFrom(map.getLayer(0).getClass())) {
			reportError("Tile layer bad type " + map.getLayer(0).getClass());
			return "";
		}
		if (!TileLayer.class.isAssignableFrom(map.getLayer(1).getClass())) {
			reportError("Feature layer bad type " + map.getLayer(1).getClass());
			return "";
		}
		if (!ObjectGroup.class.isAssignableFrom(map.getLayer(2).getClass())) {
			reportError("Object layer bad type " + map.getLayer(2).getClass());
			return "";
		}
		TileLayer tileLayer = (TileLayer) map.getLayer(0);
		TileLayer featureLayer = (TileLayer) map.getLayer(1);
		ObjectGroup objectLayer = (ObjectGroup) map.getLayer(2);
		
		ArrayList<ArrayList<Character>> glyphs = new ArrayList<ArrayList<Character>>();
		for (int y = 0; y < tileLayer.getHeight(); y += 1) {
			glyphs.add(new ArrayList<Character>());
			for (int x = 0; x < tileLayer.getWidth(); x += 1) {
				Tile tile = tileLayer.getTileAt(x, y);
				
				// blank space
				if (tile == null) {
					glyphs.get(y).add(' ');
					continue;
				}
				
				// raw glyph
				String glyph = tile.getProperties().getProperty("glyph");
				if (glyph != null) {
					if (glyph.length() != 1) {
						reportError("Bad glyph in tile " + tile + ": " + glyph);
						glyphs.get(y).add('.');
					} else {
						glyphs.get(y).add(glyph.charAt(0));
					}
				}
				
				// TODO: more types
			}
		}
		
		for (int y = 0; y < featureLayer.getHeight(); y += 1) {
			for (int x = 0; x < featureLayer.getHeight(); x += 1) {
				Tile tile = featureLayer.getTileAt(x, y);
				if (tile == null) {
					continue;
				}
				
				Character existingGlyph = glyphs.get(y).get(x);
				
				String glyph = tile.getProperties().getProperty("glyph");
				if (glyph != null) {
					if (glyph.length() != 1) {
						reportError("Bad glyph in tile " + tile + ": " + glyph);
					} else {
						glyphs.get(y).set(x, glyph.charAt(0));
					}
				}
			}
		}
		
		String result = "";
		result += "##############################################################################";
		result += "\n";
		result += "NAME:       ";
		if (map.getProperties().getProperty("name") != null) {
			result += map.getProperties().getProperty("name");
		} else {
			result += map.getFilename().substring(map.getFilename().lastIndexOf('\\') + 1,
					map.getFilename().indexOf('.'));
		}
		result += "\n";
		result = appendPropertyIfExists(result, map, "place");
		result = appendPropertyIfExists(result, map, "tags");
		result = appendPropertyIfExists(result, map, "weight");
		result = appendPropertyIfExists(result, map, "chance");
		result = appendPropertyIfExists(result, map, "orient");
		if (map.getProperties().getProperty("code") != null) {
			result += map.getProperties().getProperty("code") + "\n";
		}
		
		result += "MAP\n";
		for (ArrayList<Character> row : glyphs) {
			String line = "";
			for (Character glyph : row) {
				line += glyph;
			}
			result += line + "\n";
		}
		result += "ENDMAP\n";
		
		return result;
	}
	
	/**
	 * Prints an error message with the relevant file info maybe.
	 * @param	errorMessage	The error context to print
	 */
	protected void reportError(String errorMessage) {
		System.err.println(inputFile + ": " + errorMessage);
	}
	
	/**
	 * If a property with the given name exists, appends it to the result string. The property name
	 * is automatically converted to uppercase in the result string, but is case-insensitive in the
	 * reading.
	 * @param	result			The current result string
	 * @param	map				The map with the properties to read from
	 * @param	propertyName	The name of the property to append
	 * @return					The result string possibly with the property value appended
	 */
	protected String appendPropertyIfExists(String result, Map map, String propertyName) {
		String value = null;
		if (map.getProperties().getProperty(propertyName.toLowerCase()) != null) {
			value = map.getProperties().getProperty(propertyName.toLowerCase());
		} else if (map.getProperties().getProperty(propertyName.toUpperCase()) != null) {
			value = map.getProperties().getProperty(propertyName.toUpperCase());
		}
		if (value != null) {
			String newResult = result + propertyName.toUpperCase() + ":";
			for (int i = propertyName.length(); i < 12 - 1; i += 1) {
				newResult += " ";
			}
			newResult += value + "\n";
			return newResult;
		} else {
			return result;
		}
	}
}
