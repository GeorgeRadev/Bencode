package bencode;

/**
 * Bencode parser SAX listener
 */
public interface BencodeListener {

	/**
	 * in case of reuse of the listener - this is the called before parsing
	 * starts
	 */
	void reset();

	/**
	 * string value was parsed
	 */
	void string(String str);

	/**
	 * number value was parsed
	 */
	void number(Number n);

	/**
	 * list structure is starting
	 */
	void listStart();

	/**
	 * list structure has ended
	 */
	void listEnd();

	/**
	 * dictionary/map structure is starting
	 */
	void mapStart();

	/**
	 * dictionary/map structure has ended
	 */
	void mapEnd();

	/**
	 * dictionary/map key was parsed
	 */
	void mapKey(String key);

}
