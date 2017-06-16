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
	protected String color;
	protected String kmons;
	
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
		this.tileName = tileName;
	}
	
	/**
	 * Returns the cosmetic tile name of this tile, regardless of which command it will be set with.
	 * @return					The ftile/tile/rtile cosmetic tile of this tile, or null if none
	 */
	public String getCosmeticTile() {
		return tileName;
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
	 * Overrides the currrent glyph and places something new in this tile.
	 * @param	glyph			The new glyph to use
	 */
	public void setGlyph(Character glyph) {
		this.glyph = glyph;
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
}
