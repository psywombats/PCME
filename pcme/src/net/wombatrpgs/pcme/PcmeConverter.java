/**
 *  PcmeConverter.java
 *  Created on Jun 11, 2017 7:51:11 PM for project pcme
 *  Author: psy_wombats
 *  Contact: psy_wombats@wombatrpgs.net
 */
package net.wombatrpgs.pcme;

import java.io.File;
import java.util.ArrayList;
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
	protected DcssTile[][] dcssMap;
	
	protected HashSet<Character> unusedGlyphs;
	protected HashMap<Character, ArrayList<DcssTile>> cosmeticTiles;
	protected HashMap<Character, ArrayList<DcssTile>> colorTiles;
	protected HashMap<Character, ArrayList<DcssTile>> tilesByGlyph;
	protected HashMap<String, ArrayList<DcssTile>> kmonsTiles;
	protected HashMap<String, String> tileSubsByTarget;
	protected HashMap<String, String> ftileSubsByTarget;
	protected HashMap<String, String> rtileSubsByTarget;
	protected HashMap<String, String> colorsByTarget;
	
	/**
	 * Creates a new conversion job for a given file.
	 * @param	inputFile		The .tmx file to convert
	 */
	public PcmeConverter(File inputFile) {
		this.inputFile = inputFile;
		cosmeticTiles = new HashMap<Character, ArrayList<DcssTile>>();
		colorTiles = new HashMap<Character, ArrayList<DcssTile>>();
		tilesByGlyph = new HashMap<Character, ArrayList<DcssTile>>();
		tileSubsByTarget = new HashMap<String, String>();
		ftileSubsByTarget = new HashMap<String, String>();
		rtileSubsByTarget = new HashMap<String, String>();
		colorsByTarget = new HashMap<String, String>();
		kmonsTiles = new HashMap<String, ArrayList<DcssTile>>();
		
		String characterString = "`~!&-_:;\"'pqrsuyzDEFHJKLMNOQRSTZ?";
		unusedGlyphs = new HashSet<Character>();
		for (int i = 0; i < characterString.length(); i += 1) {
			unusedGlyphs.add(characterString.charAt(i));
		}
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
		// ObjectGroup objectLayer = (ObjectGroup) map.getLayer(2);
		
		// Basic layer pass
		dcssMap = new DcssTile[tileLayer.getHeight()][tileLayer.getWidth()];
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
				
				// cosmetic tile
				if (tile.getProperties().getProperty("tile") != null) {
					dcssMap[y][x].setCosmeticTile(tile.getProperties().getProperty("tile"));
					registerCosmeticTile(dcssMap[y][x]);
				}
				if (tile.getProperties().getProperty("color") != null) {
					dcssMap[y][x].setColor(tile.getProperties().getProperty("color"));
					registerColorTile(dcssMap[y][x]);
				}
				
				// kmons
				if (tile.getProperties().getProperty("kmons") != null) {
					dcssMap[y][x].setKmons(tile.getProperties().getProperty("kmons"));
					registerKmonsTile(dcssMap[y][x]);
				}
			}
		}
		
		// Feature layer pass
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
						if (existingTile.getCosmeticTile() != null) {
							unregisterCosmeticTile(existingTile);
						}
						existingTile.setGlyph(glyph);
						if (existingTile.getCosmeticTile() != null) {
							registerCosmeticTile(existingTile);
						}
					} else {
						System.err.println("kfeat not supported right now");
						return null;
					}
				}
				
				// kmons
				if (tile.getProperties().getProperty("kmons") != null) {
					dcssMap[y][x].setKmons(tile.getProperties().getProperty("kmons"));
					registerKmonsTile(dcssMap[y][x]);
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
		
		// TILE fill-in-the-blank pass
		for (Character glyph : cosmeticTiles.keySet()) {
			HashMap<String, ArrayList<DcssTile>> cosmeticTypes = new HashMap<>();
			for (DcssTile tile : cosmeticTiles.get(glyph)) {
				String cosmeticName = tile.getCosmeticTile();
				ArrayList<DcssTile> sameCosmetic = cosmeticTypes.get(cosmeticName);
				if (sameCosmetic == null) {
					sameCosmetic = new ArrayList<DcssTile>();
					cosmeticTypes.put(cosmeticName, sameCosmetic);
				}
				sameCosmetic.add(tile);
			}
			if (cosmeticTypes.size() == 1) {
				// uniform substitution
				DcssTile sampleTile = cosmeticTiles.get(glyph).get(0);
				Character baseGlyph = sampleTile.getOriginalGlyph();
				registerCosmetic(baseGlyph, sampleTile.getGlyph(), sampleTile.getCosmeticTile());
			} else {
				// nonuniform substitution
				for (String cosmeticName : cosmeticTypes.keySet()) {
					ArrayList<DcssTile> tiles = cosmeticTypes.get(cosmeticName);
					Character subChar = consumeUnusedGlyph();
					DcssTile sampleTile = tiles.get(0);
					Character baseGlyph = sampleTile.getOriginalGlyph();
					registerCosmetic(baseGlyph, subChar, sampleTile.getCosmeticTile());
					for (DcssTile tile : tiles) {
						tile.setGlyph(subChar);
					}
				}
			}
		}
		for (Character glyph : colorTiles.keySet()) {
			HashMap<String, ArrayList<DcssTile>> colorTypes = new HashMap<>();
			for (DcssTile tile : colorTiles.get(glyph)) {
				if (tile.getOriginalGlyph() != tile.getGlyph()) {
					// the color is only for the thing on top right now -- colored features not in
					continue;
				}
				String colorName = tile.getColor();
				ArrayList<DcssTile> sameColor = colorTypes.get(colorName);
				if (sameColor == null) {
					sameColor = new ArrayList<DcssTile>();
					colorTypes.put(colorName, sameColor);
				}
				sameColor.add(tile);
			}
			if (colorTypes.size() == 1) {
				// uniform substitution
				DcssTile sampleTile = colorTiles.get(glyph).get(0);
				registerColor(sampleTile.getGlyph(), sampleTile.getColor());
			} else {
				// nonuniform substitution
				for (String colorName : colorTypes.keySet()) {
					ArrayList<DcssTile> tiles = colorTypes.get(colorName);
					Character subChar = consumeUnusedGlyph();
					registerColor(subChar, colorName);
					for (DcssTile tile : tiles) {
						tile.setGlyph(subChar);
					}
				}
			}
		}
		
		// Kmons
		for (String monster : kmonsTiles.keySet()) {
			ArrayList<DcssTile> tiles = kmonsTiles.get(monster);
			Character glyph = consumeUnusedGlyph();
			for (DcssTile tile : tiles) {
				tile.setGlyph(glyph);
			}
			appendCode("kmons", glyph + " = " + monster);
		}
		
		// Tile combined printout
		for (String target : tileSubsByTarget.keySet()) {
			String subs = tileSubsByTarget.get(target);
			appendCode("tile", subs + " : " + target);
		}
		for (String target : ftileSubsByTarget.keySet()) {
			String subs = ftileSubsByTarget.get(target);
			appendCode("ftile", subs + " : " + target);
		}
		for (String target : rtileSubsByTarget.keySet()) {
			String subs = rtileSubsByTarget.get(target);
			appendCode("rtile", subs + " : " + target);
		}
		for (String target : colorsByTarget.keySet()) {
			String subs = colorsByTarget.get(target);
			appendCode("colour", subs + " : " + target);
		}
		
		// Custom stuffs
		if (map.getProperties().getProperty("code") != null) {
			result += map.getProperties().getProperty("code") + "\n";
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
		for (int i = command.length(); i < 12 - 1; i += 1) {
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
	 * Registers a given tile in the colored tile list. Assume the tile has already has its color.
	 * @param	tile			The tile register
	 */
	protected void registerColorTile(DcssTile tile) {
		ArrayList<DcssTile> sameGlyph = colorTiles.get(tile.getGlyph());
		if (sameGlyph == null) {
			sameGlyph = new ArrayList<DcssTile>();
			colorTiles.put(tile.getGlyph(), sameGlyph);
		}
		sameGlyph.add(tile);
	}
	
	/**
	 * Registers a given tile in the cosmetic (tile, ftile, rtile) list. Assumes the tile already
	 * has had its cosmetic tile set.
	 * @param	tile			The tile register
	 */
	protected void registerCosmeticTile(DcssTile tile) {
		ArrayList<DcssTile> sameGlyph = cosmeticTiles.get(tile.getGlyph());
		if (sameGlyph == null) {
			sameGlyph = new ArrayList<DcssTile>();
			cosmeticTiles.put(tile.getGlyph(), sameGlyph);
		}
		sameGlyph.add(tile);
	}
	
	/**
	 * Unregisters a given tile from its by-glyph cosmetic associations. Necessary to call when a
	 * tile changes glyph.
	 * @param	tile			The tile to unregister
	 */
	protected void unregisterCosmeticTile(DcssTile tile) {
		ArrayList<DcssTile> sameGlyph = cosmeticTiles.get(tile.getGlyph());
		sameGlyph.remove(tile);
	}
	
	/**
	 * Picks a substitution character unused elsewhere on the map and returns it. Afterwards, that
	 * glyph is marked used.
	 * @return					A previously unused glyph
	 */
	protected Character consumeUnusedGlyph() {
		Character subChar = (Character)unusedGlyphs.toArray()[0];
		unusedGlyphs.remove(subChar);
		return subChar;
	}
	
	/**
	 * Registers a cosmetic transformation to be combined into a ftile/rtile/whatever later on. It's
	 * not printed out immediately so as to condense multiple tiles being subbed to the same thing.
	 * The base glyph is used to determine the command, while the subglyph is the character to be
	 * replaced.
	 * @param	baseGlyph		The base type of glyph being substituted, to determine command
	 * @param	subGlyph		The glyph to declare the tile for
	 * @param	cosmeticName	The tile to declare it as
	 */
	protected void registerCosmetic(Character baseGlyph, Character subGlyph, String cosmeticName) {
		HashMap<String, String> subsByTarget;
		if (baseGlyph == '.') {
			subsByTarget = ftileSubsByTarget;
		} else if (baseGlyph == 'x') {
			subsByTarget = rtileSubsByTarget;
		} else {
			subsByTarget = tileSubsByTarget;
		}
		String sameTarget = subsByTarget.get(cosmeticName);
		if (sameTarget == null) {
			sameTarget = "" + subGlyph;
		} else {
			sameTarget += subGlyph;
		}
		subsByTarget.put(cosmeticName, sameTarget);
	}
	
	/**
	 * Registers a coloring to be combined into a color command later on. It's not printed out
	 * immediately so as to condense multiple tiles using the same color.
	 * @param	subGlyph		The glyph to be colored
	 * @param	colorName		The name of the color to use
	 */
	protected void registerColor(Character subGlyph, String colorName) {
		String sameTarget = colorsByTarget.get(colorName);
		if (sameTarget == null) {
			sameTarget = "" + subGlyph;
		} else {
			sameTarget += subGlyph;
		}
		colorsByTarget.put(colorName, sameTarget);
	}
	
	/**
	 * Registers a kmons tile to have a command added later. It should have already have kmons set.
	 * @param	tile			The tile to register
	 */
	protected void registerKmonsTile(DcssTile tile) {
		ArrayList<DcssTile> sameMonster = kmonsTiles.get(tile.getKmons());
		if (sameMonster == null) {
			sameMonster = new ArrayList<DcssTile>();
			kmonsTiles.put(tile.getKmons(), sameMonster);
		}
		sameMonster.add(tile);
	}
}
