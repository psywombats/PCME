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
	protected String frtileName;
	protected String color;
	protected String kmons;
	protected String kfeat;
	protected String kprop, kmask;
	
	protected String preferredGlyphs;
	
	/**
	 * Creates a new tile with the given glyph.
	 * @param	glyph			The glyph used by this tile, at least initially
	 */
	public DcssTile(Character glyph) {
		this.glyph = glyph;
		this.originalGlyph = glyph;
		
		this.preferredGlyphs = "";
		addGlyphSynonyms(glyph);
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
	 * Returns the cosmetic tile name of this tile's underlying lower chip tile, set with f/rtile.
	 * @return					The tile name cosmetic of this tile
	 */
	public String getCosmeticTile() {
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
	 * Sets the associated property data with this tile. Non-destructive.
	 * @param	kprop			The property data to set
	 */
	public void setKprop(String kprop) {
		this.kprop = kprop;
	}
	
	/**
	 * Returns the property data for this tile
	 * @return					The tile property string
	 */
	public String getKprop() {
		return kprop;
	}
	
	/**
	 * Sets the associated mask data with this tile. Non-destructive.
	 * @param	kmask			The mask data to set
	 */
	public void setKmask(String kmask) {
		this.kmask = kmask;
	}
	
	/**
	 * Returns the masking data for this tile.
	 * @return					The masking property string
	 */
	public String getKmask() {
		return kmask;
	}
	
	/**
	 * Adds some preferred glyphs to the beginning of this tile's preferred glyphs string.
	 * @param	glyphString		The sequence of glyphs to prefer
	 */
	public void addPreferredGlyphs(String glyphString) {
		this.preferredGlyphs = glyphString + this.preferredGlyphs;
	}
	
	/**
	 * Tiles keep track of what glyphs are best suited for their display should their original not
	 * be available. These are not guaranteed to be unused.
	 * @return					A set of glyphs preferred to be used, concatenated together
	 */
	public String getPreferredGlyphs() {
		return this.preferredGlyphs;
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
				nullCompare(frtileName, tile.frtileName) &&
				nullCompare(kmons, tile.kmons) &&
				nullCompare(color, tile.color) &&
				nullCompare(kfeat, tile.kfeat) &&
				nullCompare(kmask, tile.kmask) &&
				nullCompare(kprop, tile.kprop);
				
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
	
	/**
	 * Given a glyph, adds it and its lookalikes to the preferred glyphs string.
	 * @param	glyph			The glyph to process
	 */
	protected void addGlyphSynonyms(Character glyph) {
		preferredGlyphs += glyph;
		String g;
		switch (glyph) {
		case '.':										g = "'\"`_";				break;
		case 'x': case 'X':								g = "x#+=Z";				break;
		case 'c':										g = "CSs#";					break;
		case 'm': case 'n': case 'o':					g = "mnoMNO";				break;
		case 'v':										g = "VMm";					break;
		case 'b':										g = "BcCyY";				break;
		case 't':										g = "T!^/";					break;
		case '+':										g = "=dD/";					break;
		case '=':										g = "+rRdD/";				break;
		case 'w': case 'W':								g = "WwYy~ZzO";				break;
		case 'l':										g = "L^~_";					break;
		case '{': case '[': case '(': case '<':			g = "{[(<uU^";				break;
		case '}': case ']': case ')': case '>':			g = "}])>dDv";				break;
		case 'A': case 'G': case 'I':					g = "gaiGAI/?!";			break;
		case 'B': case 'C':								g = "BCbc_!?";				break;
		case 'T': case 'U': case 'V': case 'Y':			g = "TUVYtuvy";				break;
		case '!': case '?':								g = "!?O";					break;
		case 'p':										g = "Pqrs";					break;
		case 'q':										g = "Qprs";					break;
		case 'r':										g = "Rqps";					break;
		case 's':										g = "Spqr";					break;
		case 'u':										g = "UvV";					break;
		case 'y':										g = "YzZ";					break;
		case 'z':										g = "ZyY";					break;
		case 'J':										g = "jKLMN";				break;
		case 'K':										g = "KJLMN";				break;
		case 'L':										g = "lJKMN";				break;
		case 'M':										g = "mJKLN";				break;
		case 'N':										g = "nJKLM";				break;
		case 'O':										g = "o0qQ";					break;
		case 'Q':										g = "qo`";					break;
		case 'R':										g = "rsS";					break;
		case 'S':										g = "sr";					break;
		case 'Z':										g = "zYy";					break;
		case '^':										g = "tT6";					break;
		case '8':										g = "!X\"";					break;
		case '9':										g = "!?X\"";				break;
		case '0':										g = "?!eo";					break;
		case '%': case '*': case '|':					g = "$*?,i";				break;
		case '$':										g = "%g,";					break;
		case 'd':										g = "D,*";					break;
		case 'e':										g = "E,*";					break;
		case 'f':										g = "F,*";					break;
		case 'g':										g = "G,*";					break;
		case 'h':										g = "H,*";					break;
		case 'i':										g = "I,*";					break;
		case 'j':										g = "J,*";					break;
		case 'k':										g = "K,*";					break;
		case '1': case '2': case '3': case '4': case '5': case '6': case '7':
														g = "!?e#o";				break;
		default: 										g = "";						break;
		}
		preferredGlyphs = preferredGlyphs + g;
	}
}
