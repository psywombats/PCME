/**
 *  DcssTile.java
 *  Created on Jun 13, 2017 10:33:14 PM for project pcme
 *  Author: psy_wombats
 *  Contact: psy_wombats@wombatrpgs.net
 */
package net.wombatrpgs.pcme;

/**
 * Info struct for what will result in one glyph to the DCSS map.
 */
public class DcssTile {

	protected Character glyph;
	protected Character originalGlyph;
	protected String tileName;
	protected String frtileName;
	protected String color;
	protected String kmons;
	protected String kfeat;
	
	/**
	 * Creates a new tile with the given glyph.
	 * @param	glyph			The glyph used by this tile, at least initially
	 */
	public DcssTile(Character glyph) {
		this.glyph = glyph;
		this.originalGlyph = glyph;
	}
	
	/**
	 * Gives this tile a cosmetic appearance, separate from glyph. The exact command used to set the
	 * cosmetic tile varies by initial glyph -- rocks are given rtile, floors ftile, etc.
	 * @param	tileName		The cosmetic tilename used by this tile
	 */
	public void setCosmeticTile(String tileName) {
		this.frtileName = tileName;
	}
	
	/**
	 * Sets the tile name used in a feature pass. Same as the cosmetic appearance, but for something
	 * on the feature layer.
	 * @param	tileName		The cosmetic tilename used by this tile
	 */
	public void setSecondaryTileName(String tileName) {
		this.tileName = tileName;
	}
	
	/**
	 * Returns the cosmetic tile name of this tile's feature on the feature layer.
	 * @return					The tile name cosmetic of this tile
	 */
	public String getTileName() {
		return tileName;
	}
	
	/**
	 * Returns the cosmetic tile name of this tile's underlying lower chip tile, set with f/rtile.
	 * @return					The tile name cosmetic of this tile
	 */
	public String getFrtileName() {
		return frtileName;
	}
	
	/**
	 * Returns the glyph this tile would use if printed immediately. Only returns the topmost glyph,
	 * so anything unusual underneath will have to be picked up by the kfeat pass.
	 * @return					The current glyph
	 */
	public Character getGlyph() {
		return glyph;
	}
	
	/**
	 * Returns the glyph this tile originally represented. For instance, for colored floors, returns
	 * a '.' rather than the substitution character.
	 * @return					The original glyph
	 */
	public Character getOriginalGlyph() {
		return originalGlyph;
	}
	
	/**
	 * Adds a glyph from a further layer on to apply to this tile. Should not be called for
	 * administrative glyph changes, just from the map
	 * @param	newGlyph		The new glyph to use
	 */
	public void addGlyph(Character newGlyph) {
		if (glyph != null) {
			if (glyph != '.' && glyph != ' ') {
				kfeat = "" + glyph;
			}
		}
		glyph = newGlyph;
	}
	
	/**
	 * Replaces the glyph used by this tile, forgetting the old one completely. Should only ever
	 * be called once on a tile probably.
	 * @param	newGlyph		The new glyph to use
	 */
	public void assignGlyph(Character newGlyph) {
		this.glyph = newGlyph;
	}
	
	/**
	 * Sets the named color of this tile, for tiles that will be colored in console mode. See
	 * colour.h or something for the value list. Unabashedly American spelling.
	 * @param	color			The color to use for this tile
	 */
	public void setColor(String color) {
		this.color = color;
	}
	
	/**
	 * Returns the color used for this tile in console mode.
	 * @return					The color used for console, or null if none
	 */
	public String getColor() {
		return this.color;
	}
	
	/**
	 * Sets the kmons value of this tile. This will eventually result in a kmons command setting its
	 * glyph to the specified monster.
	 * @param	monster			The monster to define this tile via kmons
	 */
	public void setKmons(String monster) {
		this.kmons = monster;
	}
	
	/**
	 * Returns the monster defined via kmons as standing on this tile.
	 * @return					The kmons monster of this tile
	 */
	public String getKmons() {
		return kmons;
	}
	
	/**
	 * Sets the feature of this tile. Its glyph will be replaced.
	 * @param	kfeat			The feature to be set as this tile
	 */
	public void setKfeat(String kfeat) {
		this.kfeat = kfeat;
	}
	
	/**
	 * Returns the kfeat assigned to this tile.
	 * @return					The feature to be set as this tile
	 */
	public String getKfeat() {
		return kfeat;
	}
	
	/**
	 * Checks if this tile shares a "prototype tile" with the supplied tile. Tiles sharing a
	 * prototype can be represented with the same glyph.
	 * @param	tile			The tile to check
	 * @return					True if the tiles share a prototype
	 */
	public boolean sharesPrototype(DcssTile tile) {
		return	nullCompare(glyph, tile.glyph) &&
				nullCompare(originalGlyph, tile.originalGlyph) &&
				nullCompare(tileName, tile.tileName) &&
				nullCompare(frtileName, tile.frtileName) &&
				nullCompare(kmons, tile.kmons) &&
				nullCompare(color, tile.color) &&
				nullCompare(kfeat, tile.kfeat);
				
	}
	
	/**
	 * Checks o1 and o2 for equality in a null-safe way.
	 * @param	o1				One of two objects to compare
	 * @param	o2				One of two objects to compare
	 * @return					True if both are equal, false otherwise
	 */
	protected boolean nullCompare(Object o1, Object o2) {
		if (o1 == null) {
			return o2 == null;
		} else {
			return o1.equals(o2);
		}
	}
}
