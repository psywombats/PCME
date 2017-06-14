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
	protected Character kfeatGlyph;
	protected String ftileName;
	protected String rtileName;
	protected String tileName;
	
	/**
	 * Creates a new tile with the given glyph.
	 * @param	glyph			The glyph used by this tile, at least initially
	 */
	public DcssTile(Character glyph) {
		this.glyph = glyph;
	}
	
	/**
	 * Gives this tile a cosmetic appearance, separate from glyph. The exact command used to set the
	 * cosmetic tile varies by initial glyph -- rocks are given rtile, floors ftile, etc.
	 * @param	tileName		The cosmetic tilename used by this tile
	 */
	public void setCosmeticTile(String tileName) {
		if (glyph == '.') {
			this.ftileName = tileName;
		} else if (glyph == 'x') {
			this.rtileName = tileName;
		} else {
			this.tileName = tileName;
		}
	}
	
	/**
	 * Returns the cosmetic tile name of this tile, regardless of which command it will be set with.
	 * @return					The ftile/tile/rtile cosmetic tile of this tile, or null if none
	 */
	public String getCosmeticTile() {
		if (ftileName != null) return ftileName;
		if (rtileName != null) return rtileName;
		if (tileName != null) return tileName;
		return null;
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
	 * Overrides the currrent glyph and places something new in this tile.
	 * @param	glyph			The new glyph to use
	 */
	public void setGlyph(Character glyph) {
		this.glyph = glyph;
	}
	
	/**
	 * Attempts to put a glyph on top of this glyph of a different type. Because DCSS has no
	 * concept of layers, we'll eventually have to use KFEAT to place the lower one. This is used
	 * for stuff like shrubs in water, monsters on items, etc.
	 * @param	glyph			The top glyph
	 */
	public void addOverlaidGlyph(Character glyph) {
		if (this.kfeatGlyph != null) {
			System.err.println("Too many glyphs on tile");
			return;
		}
		this.kfeatGlyph = this.glyph;
		this.glyph = glyph;
	}
	
	/**
	 * Returns the underlying glyph that needs to be placed via kfeat, if one exists. If none does,
	 * returns null.
	 * @return					The underlying kfeat glyph, or null if none
	 */
	public Character getUnderlaidGlyph() {
		return kfeatGlyph;
	}
	
	/**
	 * Performs a substitution where this tile is now represented by a new glyph that will be
	 * subst'd by kfeat at the output step.
	 * @param	kfeatGlyph		The new glyph for this tile
	 */
	public void kfeatSubstitute(Character kfeatGlyph) {
		this.glyph = kfeatGlyph;
		this.kfeatGlyph = null;
	}
}
