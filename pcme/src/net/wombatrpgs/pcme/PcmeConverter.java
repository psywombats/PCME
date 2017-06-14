/**
 *  PcmeConverter.java
 *  Created on Jun 11, 2017 7:51:11 PM for project pcme
 *  Author: psy_wombats
 *  Contact: psy_wombats@wombatrpgs.net
 */
package net.wombatrpgs.pcme;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;

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
	protected Map map;
	protected String result;
	
	protected HashSet<Character> unusedGlyphs;
	protected HashMap<String, ArrayList<DcssTile>> kfeatTiles;
	protected DcssTile[][] dcssMap;
	protected HashMap<Character, ArrayList<DcssTile>> tilesByGlyph;
	protected HashMap<String, Character> kfeatSubs;
	
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
		map = null;
		try {
			map = reader.readMap(inputFile.getAbsolutePath());
		} catch (Exception e) {
			reportError("TMX error");
			e.printStackTrace();
			return null;
		}
		
		if (map.getLayerCount() != 3) {
			reportError("Expecting 3 layers, found " + map.getLayerCount());
			return null;
		}
		if (!TileLayer.class.isAssignableFrom(map.getLayer(0).getClass())) {
			reportError("Tile layer bad type " + map.getLayer(0).getClass());
			return null;
		}
		if (!TileLayer.class.isAssignableFrom(map.getLayer(1).getClass())) {
			reportError("Feature layer bad type " + map.getLayer(1).getClass());
			return null;
		}
		if (!ObjectGroup.class.isAssignableFrom(map.getLayer(2).getClass())) {
			reportError("Object layer bad type " + map.getLayer(2).getClass());
			return null;
		}
		TileLayer tileLayer = (TileLayer) map.getLayer(0);
		TileLayer featureLayer = (TileLayer) map.getLayer(1);
		ObjectGroup objectLayer = (ObjectGroup) map.getLayer(2);
		
		String characterString = "`~!&-_:;\"'pqrsuyzDEFHJKLMNOQRSTZ?";
		unusedGlyphs = new HashSet<Character>();
		for (int i = 0; i < characterString.length(); i += 1) {
			unusedGlyphs.add(characterString.charAt(i));
		}
		
		// Basic layer pass
		dcssMap = new DcssTile[tileLayer.getHeight()][tileLayer.getWidth()];
		tilesByGlyph = new HashMap<>();
		for (int y = 0; y < tileLayer.getHeight(); y += 1) {
			for (int x = 0; x < tileLayer.getWidth(); x += 1) {
				Tile tile = tileLayer.getTileAt(x, y);
				
				// blank space
				if (tile == null) {
					dcssMap[y][x] = new DcssTile(' ');
					continue;
				}
				
				// raw glyph
				String glyphString = tile.getProperties().getProperty("glyph");
				if (glyphString != null) {
					if (glyphString.length() != 1) {
						reportError("Bad glyph in tile " + tile + ": " + glyphString);
						return null;
					} else {
						Character glyph = glyphString.charAt(0);
						dcssMap[y][x] = new DcssTile(glyph);
						unusedGlyphs.remove(glyph);
						registerTileGlyph(dcssMap[y][x]);
					}
				}
				
				// TODO: more types
			}
		}
		
		// Feature layer pass
		kfeatTiles = new HashMap<String, ArrayList<DcssTile>>();
		for (int y = 0; y < featureLayer.getHeight(); y += 1) {
			for (int x = 0; x < featureLayer.getHeight(); x += 1) {
				Tile tile = featureLayer.getTileAt(x, y);
				if (tile == null) {
					continue;
				}
				
				DcssTile existingTile = dcssMap[y][x];
				Character existingGlyph = existingTile.getGlyph();
				
				String glyphString = tile.getProperties().getProperty("glyph");
				if (glyphString != null) {
					if (glyphString.length() != 1) {
						reportError("Bad glyph in tile " + tile + ": " + glyphString);
						continue;
					}
					Character glyph = glyphString.charAt(0);
					unusedGlyphs.remove(glyph);
					if (existingGlyph == ' ' || existingGlyph == '.' || existingGlyph == glyph) {
						existingTile.setGlyph(glyph);
					} else {
						// an odd glyph has been placed above some terrain
						unregisterTileGlyph(existingTile);
						existingTile.addOverlaidGlyph(glyph);
						registerTileGlyph(existingTile);
						registerKfeatTile(existingTile);
					}
				}
			}
		}
		
		// KFEAT fill-in-the-blank pass
		// format is "xy" -> 'z' where z represents x lying on top of y
		kfeatSubs = new HashMap<String, Character>();
		for (String kfeatKey : kfeatTiles.keySet()) {
			ArrayList<DcssTile> subbedTiles = kfeatTiles.get(kfeatKey);
			DcssTile sampleTile = subbedTiles.get(0);
			if (completelyCoveredByKfeat(sampleTile.getGlyph())) {
				// we don't need to introduce a new glyph because all of them will be transformed
				kfeatSubs.put(kfeatKey, sampleTile.getGlyph());
			} else {
				// some have the underlying feature, some don't
				Character subChar = (Character)unusedGlyphs.toArray()[0];
				unusedGlyphs.remove(subChar);
				kfeatSubs.put(kfeatKey, sampleTile.getGlyph());
				for (DcssTile tile : subbedTiles) {
					tile.kfeatSubstitute(subChar);
				}
			}
		}
		
		// Basic headers
		result = "";
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
		appendPropertyIfExists("place");
		appendPropertyIfExists("tags");
		appendPropertyIfExists("weight");
		appendPropertyIfExists("chance");
		appendPropertyIfExists("orient");
		if (map.getProperties().getProperty("code") != null) {
			result += map.getProperties().getProperty("code") + "\n";
		}
		
		// Kfeat
		for (String kfeatKey : kfeatSubs.keySet()) {
			Character subChar = kfeatSubs.get(kfeatKey);
			Character bottomGlyph = kfeatKey.charAt(0);
			Character topGlyph = kfeatKey.charAt(1);
			appendCode("kfeat", subChar + " : " + bottomGlyph);
			// TODO: handle the top character
			// I think eventually we'll need to parse $, d, and 1 etc to mons and items
			// then kfeat those in
			// At the moment this code just nukes the feature layer entirely
			if (!kfeatKey.endsWith("" + subChar)) {
				// non-uniform transformation
			}
		}
		
		// Map
		result += "MAP\n";
		for (int y = 0; y < featureLayer.getHeight(); y += 1) {
			String line = "";
			for (int x = 0; x < featureLayer.getHeight(); x += 1) {
				line += dcssMap[y][x].getGlyph();
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
	 * @param	propertyName	The name of the property to append
	 */
	protected void appendPropertyIfExists(String propertyName) {
		String value = null;
		if (map.getProperties().getProperty(propertyName.toLowerCase()) != null) {
			value = map.getProperties().getProperty(propertyName.toLowerCase());
		} else if (map.getProperties().getProperty(propertyName.toUpperCase()) != null) {
			value = map.getProperties().getProperty(propertyName.toUpperCase());
		}
		if (value != null) {
			appendCode(propertyName, value);
		}
	}
	
	/**
	 * Appends a code line to the result in the format COMMAND:STUFF, except it fills in proper
	 * whitespace after the colon. Uppercases the command.
	 * @param	command			The command name preceding the colon
	 * @param	code			The code line following the colon
	 */
	protected void appendCode(String command, String code) {
		result += command.toUpperCase() + ":";
		for (int i = code.length(); i < 12 - 1; i += 1) {
			result += " ";
		}
		result += code + "\n";
	}
	
	/**
	 * Given a tile, adds it to the tilesByGlyph map.
	 * @param	tile			The tile to add to the map
	 */
	protected void registerTileGlyph(DcssTile tile) {
		ArrayList<DcssTile> sameTile = tilesByGlyph.get(tile.getGlyph());
		if (sameTile == null) {
			sameTile = new ArrayList<DcssTile>();
			tilesByGlyph.put(tile.getGlyph(), sameTile);
		}
		sameTile.add(tile);
	}
	
	/**
	 * Given a tile, removes it from the tilesByGlyph map.
	 * @param	tile			The tile to remove from the map
	 */
	protected void unregisterTileGlyph(DcssTile tile) {
		tilesByGlyph.get(tile.getGlyph()).remove(tile);
	}
	
	/**
	 * Given a glyph, checks if every glyph of that type will have the same kfeat transformation
	 * applied to it. Basically, if every thing of gold is put in water, this will return true,
	 * but if half the gold's on dry ground, it returns false.
	 * @param	glyph			The glyph to check
	 * @return					True if a uniform transformation will be applied
	 */
	protected boolean completelyCoveredByKfeat(Character glyph) {
		return kfeatTiles.get(glyph).size() == tilesByGlyph.get(glyph).size(); 
	}
	
	/**
	 * Registers a given tile in the kfeat list. Assumes the tile has already had its kfeat tile
	 * set.
	 * @param	tile			The tile to register
	 */
	protected void registerKfeatTile(DcssTile tile) {
		String key = "" + tile.getUnderlaidGlyph() + tile.getGlyph();
		ArrayList<DcssTile> sameSub = kfeatTiles.get(key);
		if (sameSub == null) {
			sameSub = new ArrayList<DcssTile>();
			kfeatTiles.put(key, sameSub);
		}
		sameSub.add(tile);
	}
}
