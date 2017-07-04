/**
 *  PcmeConverter.java
 *  Created on Jun 11, 2017 7:51:11 PM for project pcme
 *  Author: psy_wombats
 *  Contact: psy_wombats@wombatrpgs.net
 */
package net.wombatrpgs.pcme;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
	protected StringBuilder result;
	protected DcssTile[][] dcssMap;
	
	protected HashSet<Character> usedGlyphs, subGlyphs, firstPassGlyphs;
	protected ArrayList<ArrayList<DcssTile>> tilesByPrototype;
	
	/**
	 * Creates a new conversion job for a given file.
	 * @param	inputFile		The .tmx file to convert
	 */
	public PcmeConverter(File inputFile) {
		this.inputFile = inputFile;
		
		result = new StringBuilder();
		tilesByPrototype = new ArrayList<ArrayList<DcssTile>>();
		
		String characterString = "`~!&-_:;\"'pqrsuyzDEFHJKLMNOQRSTZ?";
		usedGlyphs = new HashSet<Character>();
		subGlyphs = new HashSet<Character>();
		firstPassGlyphs = new HashSet<Character>();
		for (int i = 0; i < characterString.length(); i += 1) {
			subGlyphs.add(characterString.charAt(i));
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
		
		if (map.getLayerCount() != 4) {
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
		if (!TileLayer.class.isAssignableFrom(map.getLayer(2).getClass())) {
			reportError("Tag layer bad type " + map.getLayer(1).getClass());
			return null;
		}
		if (!ObjectGroup.class.isAssignableFrom(map.getLayer(3).getClass())) {
			reportError("Object layer bad type " + map.getLayer(3).getClass());
			return null;
		}
		TileLayer tileLayer = (TileLayer) map.getLayer(0);
		TileLayer featureLayer = (TileLayer) map.getLayer(1);
		TileLayer tagLayer = (TileLayer) map.getLayer(2);
		// ObjectGroup objectLayer = (ObjectGroup) map.getLayer(3);
		
		// Basic layer pass
		dcssMap = new DcssTile[tileLayer.getHeight()][tileLayer.getWidth()];
		for (int y = 0; y < tileLayer.getHeight(); y += 1) {
			for (int x = 0; x < tileLayer.getWidth(); x += 1) {
				Tile tile = tileLayer.getTileAt(x, y);
				
				if (tile == null) {
					dcssMap[y][x] = new DcssTile(' ');
					continue;
				}
				
				String glyphString = tile.getProperties().getProperty("glyph");
				if (glyphString != null) {
					if (glyphString.length() != 1) {
						reportError("Bad glyph in tile " + tile + ": " + glyphString);
						return null;
					} else {
						Character glyph = glyphString.charAt(0);
						dcssMap[y][x] = new DcssTile(glyph);
						firstPassGlyphs.add(glyph);
					}
				} else {
					dcssMap[y][x] = new DcssTile('.');
				}
				
				if (tile.getProperties().getProperty("tile") != null) {
					if (dcssMap[y][x].glyph == '.') {
						dcssMap[y][x].setCosmeticTile(tile.getProperties().getProperty("tile"));
					} else {
						dcssMap[y][x].setSecondaryTileName(tile.getProperties().getProperty("tile"));
					}
				}
				if (tile.getProperties().getProperty("color") != null) {
					dcssMap[y][x].setColor(tile.getProperties().getProperty("color"));
				}
				if (tile.getProperties().getProperty("kmons") != null) {
					dcssMap[y][x].setKmons(tile.getProperties().getProperty("kmons"));
				}
				if (tile.getProperties().getProperty("kfeat") != null) {
					dcssMap[y][x].setKfeat(tile.getProperties().getProperty("kfeat"));
				}
				if (tile.getProperties().getProperty("prefGlyphs") != null) {
					dcssMap[y][x].addPreferredGlyphs(tile.getProperties().getProperty("prefGlyphs"));
				}
			}
		}
		
		// Feature layer pass
		for (int y = 0; y < featureLayer.getHeight(); y += 1) {
			for (int x = 0; x < featureLayer.getWidth(); x += 1) {
				Tile tile = featureLayer.getTileAt(x, y);
				if (tile == null) {
					continue;
				}
				
				DcssTile existingTile = dcssMap[y][x];
				
				String glyphString = tile.getProperties().getProperty("glyph");
				if (glyphString != null) {
					if (glyphString.length() != 1) {
						reportError("Bad glyph in tile " + tile + ": " + glyphString);
						continue;
					}
					Character glyph = glyphString.charAt(0);
					existingTile.addGlyph(glyph);
				}
				
				if (tile.getProperties().getProperty("kmons") != null) {
					existingTile.setKmons(tile.getProperties().getProperty("kmons"));
				}
				if (tile.getProperties().getProperty("tile") != null) {
					existingTile.setSecondaryTileName(tile.getProperties().getProperty("tile"));
				}
				if (tile.getProperties().getProperty("kfeat") != null) {
					existingTile.setKfeat(tile.getProperties().getProperty("kfeat"));
				}
				if (tile.getProperties().getProperty("prefGlyphs") != null) {
					existingTile.addPreferredGlyphs(tile.getProperties().getProperty("prefGlyphs"));
				}
			}
		}
		
		// Tag layer pass
		for (int y = 0; y < tagLayer.getHeight(); y += 1) {
			for (int x = 0; x < tagLayer.getWidth(); x += 1) {
				Tile tile = tagLayer.getTileAt(x, y);
				if (tile == null) {
					continue;
				}
				
				DcssTile existingTile = dcssMap[y][x];
				if (tile.getProperties().getProperty("kprop") != null) {
					existingTile.setKprop(tile.getProperties().getProperty("kprop"));
				}
				if (tile.getProperties().getProperty("kmask") != null) {
					existingTile.setKmask(tile.getProperties().getProperty("kmask"));
				}
			}
		}
		
		// Break up tiles by proto
		for (int y = 0; y < featureLayer.getHeight(); y += 1) {
			for (int x = 0; x < featureLayer.getWidth(); x += 1) {
				DcssTile tile = dcssMap[y][x];
				boolean foundProto = false;
				for (ArrayList<DcssTile> prototypeSet : tilesByPrototype) {
					if (tile.sharesPrototype(prototypeSet.get(0))) {
						foundProto = true;
						prototypeSet.add(tile);
						break;
					}
				}
				if (!foundProto) {
					ArrayList<DcssTile> prototypeSet = new ArrayList<DcssTile>();
					prototypeSet.add(tile);
					tilesByPrototype.add(prototypeSet);
				}
			}
		}
		
		// Assign glyphs to each proto
		Collections.sort(tilesByPrototype, new Comparator<ArrayList<DcssTile>>() {
			@Override public int compare(ArrayList<DcssTile> o1, ArrayList<DcssTile> o2) {
				return o2.size() - o1.size();
			}
		});
		for (ArrayList<DcssTile> tileSet : tilesByPrototype) {
			DcssTile sampleTile = tileSet.get(0);
			firstPassGlyphs.add(sampleTile.getGlyph());
		}
		for (ArrayList<DcssTile> tileSet : tilesByPrototype) {
			DcssTile sampleTile = tileSet.get(0);
			Character newGlyph;
			Character glyph = sampleTile.getGlyph();
			if (!usedGlyphs.contains(glyph)) {
				newGlyph = sampleTile.getGlyph();
				usedGlyphs.add(newGlyph);
			} else {
				newGlyph = consumeUnseenGlyph(sampleTile.getPreferredGlyphs());
			}
			if (sampleTile.getGlyph() != newGlyph) {
				for (DcssTile tile : tileSet) {
					tile.assignGlyph(newGlyph);
				}
			}
		}
		
		// Basic headers
		result.append("########################################");
		result.append("########################################\n");
		if (map.getProperties().getProperty("comment") != null) {
			result.append("# " + map.getProperties().getProperty("comment") + "\n");
		}
		result.append("NAME:       ");
		if (map.getProperties().getProperty("name") != null) {
			result.append(map.getProperties().getProperty("name"));
		} else {
			result.append(map.getFilename().substring(map.getFilename().lastIndexOf('\\') + 1,
					map.getFilename().indexOf('.')));
		}
		result.append("\n");
		appendPropertyIfExists("tags");
		appendPropertyIfExists("place");
		appendPropertyIfExists("depth");
		appendPropertyIfExists("weight");
		appendPropertyIfExists("chance");
		appendPropertyIfExists("orient");
		
		// f/rtile
		HashMap<String, String> cosmeticFtiles = new HashMap<String, String>();
		HashMap<String, String> cosmeticRtiles = new HashMap<String, String>();
		for (ArrayList<DcssTile> tiles : tilesByPrototype) {
			DcssTile tile = tiles.get(0);
			if (tile.getFrtileName() == null) {
				continue;
			}
			if (tile.getGlyph() == 'x') {
				addGlyphToCommand(cosmeticRtiles, tile.getFrtileName(), tile.glyph);
			} else {
				addGlyphToCommand(cosmeticFtiles, tile.getFrtileName(), tile.glyph);
			}
		}
		printCommandMap(cosmeticFtiles, "ftile", "=");
		printCommandMap(cosmeticRtiles, "rtile", "=");
		
		// tile
		HashMap<String, String> costmeticTiles = new HashMap<String, String>();
		for (ArrayList<DcssTile> tiles : tilesByPrototype) {
			DcssTile tile = tiles.get(0);
			if (tile.getTileName() == null) {
				continue;
			}
			addGlyphToCommand(costmeticTiles, tile.getTileName(), tile.glyph);
		}
		printCommandMap(costmeticTiles, "tile", "=");

		// Colo(u)r
		HashMap<String, String> colorTiles = new HashMap<String, String>();
		for (ArrayList<DcssTile> tiles : tilesByPrototype) {
			DcssTile tile = tiles.get(0);
			if (tile.getColor() == null) {
				continue;
			}
			addGlyphToCommand(colorTiles, tile.getColor(), tile.glyph);
		}
		printCommandMap(colorTiles, "colour", "=");
		
		// Kmons
		HashMap<String, String> kmonsTiles = new HashMap<String, String>();
		for (ArrayList<DcssTile> tiles : tilesByPrototype) {
			DcssTile tile = tiles.get(0);
			if (tile.getKmons() == null) {
				continue;
			}
			addGlyphToCommand(kmonsTiles, tile.getKmons(), tile.glyph);
		}
		printCommandMap(kmonsTiles, "kmons", "=");
		
		// Kfeat
		HashMap<String, String> kfeatTiles = new HashMap<String, String>();
		for (ArrayList<DcssTile> tiles : tilesByPrototype) {
			DcssTile tile = tiles.get(0);
			if (tile.getKfeat() != null) {
				addGlyphToCommand(kfeatTiles, tile.getKfeat(), tile.glyph);
			} else if (tile.getFrtileName() != null && tile.getOriginalGlyph() != '.') {
				addGlyphToCommand(kfeatTiles, "" + tile.getOriginalGlyph(), tile.glyph);
			} else if (tile.getTileName() != null) {
				addGlyphToCommand(kfeatTiles, "" + tile.getOriginalGlyph(), tile.glyph);
			}
			
		}
		printCommandMap(kfeatTiles, "kfeat", "=");
		
		// Kmask
		HashMap<String, String> kmaskTiles = new HashMap<String, String>();
		for (ArrayList<DcssTile> tiles : tilesByPrototype) {
			DcssTile tile = tiles.get(0);
			if (tile.getKmask() == null) {
				continue;
			}
			addGlyphToCommand(kmaskTiles, tile.getKmask(), tile.glyph);
		}
		printSplitCommandMap(kmaskTiles, "kmask", "=");
		
		// Kprop
		HashMap<String, String> kpropTiles = new HashMap<String, String>();
		for (ArrayList<DcssTile> tiles : tilesByPrototype) {
			DcssTile tile = tiles.get(0);
			if (tile.getKprop() == null) {
				continue;
			}
			addGlyphToCommand(kpropTiles, tile.getKprop(), tile.glyph);
		}
		printSplitCommandMap(kpropTiles, "kprop", "=");
		
		// Custom stuffs
		if (map.getProperties().getProperty("code") != null) {
			result.append(map.getProperties().getProperty("code") + "\n");
		}
		
		appendPropertyIfExists("items");
		appendPropertyIfExists("mons");
		
		// Map
		result.append("MAP\n");
		boolean noRotate = false;
		if (map.getProperties().getProperty("tags") != null) {
			noRotate = map.getProperties().getProperty("tags").contains("no_rotate");
		}
		if (map.getHeight() <= map.getWidth() || noRotate) {
			for (int y = 0; y < map.getHeight(); y += 1) {
				StringBuilder line = new StringBuilder();
				for (int x = 0; x < map.getWidth(); x += 1) {
					line.append(dcssMap[y][x].getGlyph());
				}
				result.append(line + "\n");
			}
		} else {
			for (int x = 0; x < map.getWidth(); x += 1) {
				StringBuilder line =  new StringBuilder();
				for (int y = 0; y < map.getHeight(); y += 1) {
					line.append(dcssMap[y][x].getGlyph());
				}
				result.append(line + "\n");
			}
		}

		result.append("ENDMAP\n");
		
		return result.toString();
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
		result.append(command.toUpperCase() + ":");
		for (int i = command.length(); i < 12 - 1; i += 1) {
			result.append(" ");
		}
		result.append(code + "\n");
	}
	
	/**
	 * Picks a substitution character not used in the main map display. Afterwards, that glyph is
	 * marked used. Can provide some preferred glyphs so that plants usually end up with 'p' etc,
	 * where the first available in the string will be used.
	 * @param	preferredGlyphs	A concatenated string of glyphs, to be checked in order
	 * @return					A previously unseen glyph
	 */
	protected Character consumeUnseenGlyph(String preferredGlyphs) {
		Character glyph = null;
		for (int i = 0; i < preferredGlyphs.length(); i += 1) {
			Character preferredGlyph = preferredGlyphs.charAt(i);
			if (!usedGlyphs.contains(preferredGlyph) && !firstPassGlyphs.contains(preferredGlyph)) {
				glyph = preferredGlyph;
				break;
			}
		}
		if (glyph == null) {
			for (Character subGlyph : subGlyphs) {
				if (!usedGlyphs.contains(subGlyph) && !firstPassGlyphs.contains(subGlyph)) {
					glyph = subGlyph;
					break;
				}
			}
		}
		subGlyphs.remove(glyph);
		usedGlyphs.add(glyph);
		return glyph;
	}
	
	/**
	 * Appends a glyph to a string based on a key value.
	 * @param	command			The command map to append to
	 * @param	key				The key to look up in the command map
	 * @param	glyph			The glyph to append to the value found
	 */
	protected void addGlyphToCommand(HashMap<String, String> command, String key, Character glyph) {
		String value = command.get(key);
		if (value == null) {
			value = "";
		}
		value += glyph;
		command.put(key, value);
	}
	
	/**
	 * Appends codes to the result string based on the values in a command map. The map maps left
	 * hand values to right hand values via the supplied op, that will appear between the two.
	 * @param	map				The map of lvalues to rvalues
	 * @param	command			The command name being printed
	 * @param	op				The operation defining the lvalue/rvalue relation, usually : or =
	 */
	protected void printCommandMap(HashMap<String, String> map, String command, String op) {
		for (String key : map.keySet()) {
			appendCode(command, map.get(key) + " " + op + " " + key);
		}
	}
	
	/**
	 * Appends codes to the result string based on the values in a command map. The right hand
	 * side is split and the command is printed once per space-seperated value there.
	 * @param	map				The map of lvalues to rvalues
	 * @param	command			The command name being printed
	 * @param	op				The operation defining the lvalue/rvalue relation, usually : or =
	 */
	protected void printSplitCommandMap(HashMap<String, String> map, String command, String op) {
		for (String key : map.keySet()) {
			String[] values = key.split("\\s+");
			for (String value : values) {
				appendCode(command, map.get(key) + " " + op + " " + value);
			}
		}
	}
}
