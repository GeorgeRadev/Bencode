package bencode;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Builds Bencode format presentation in a chain calls manner:<br/>
 * example: <br/>
 * 
 * {@code
 * ByteArrayOutputStream out = new ByteArrayOutputStream();
		BencodeBuilder builder = new BencodeBuilder(out);
		builder.startMap();
		builder.addKeyValue("key", "value").addKeyValue("for", "while");
		builder.addKey("array");
		builder.startArray().addValue("for").addValue("value").addValue(10).endArray();
		builder.endMap();

		byte[] bencodeBytes = out.toByteArray();
}
 */
public class BencodeBuilder {
	/** Number */
	static final byte NUMBER = 'i';

	/** List */
	static final byte LIST = 'l';

	/** Dictionary */
	static final byte DICTIONARY = 'd';

	/** End of type */
	static final byte TERMINATOR = 'e';

	/** Separator between length and string */
	static final byte SEPARATOR = ':';

	final OutputStream out;
	final String charset;

	public BencodeBuilder(OutputStream out) {
		this(out, "UTF-8");
	}

	public BencodeBuilder(OutputStream out, String charset) {
		this.out = out;
		this.charset = charset;
	}

	public final BencodeBuilder put(String key, String value) throws IOException {
		addKeyValue(key, value);
		return this;
	}

	public final BencodeBuilder put(String key, Object value) throws IOException {
		addKeyValue(key, value);
		return this;
	}

	public final BencodeBuilder addKeyValue(String key, String value) throws IOException {
		addKey(key);
		addValue(value);
		return this;
	}

	public final BencodeBuilder addKeyValue(String key, Object value) throws IOException {
		addKey(key);
		addValue(value);
		return this;
	}

	public final BencodeBuilder addKey(String key) throws IOException {
		addString(key);
		return this;
	}

	public final BencodeBuilder addValue(String str) throws IOException {
		addString(str);
		return this;
	}

	public final BencodeBuilder addValue(double value) throws IOException {
		byte[] bytes = String.valueOf(value).getBytes(charset);
		out.write(NUMBER);
		out.write(bytes);
		out.write(TERMINATOR);
		return this;
	}

	public final BencodeBuilder addValue(long value) throws IOException {
		byte[] bytes = String.valueOf(value).getBytes(charset);
		out.write(NUMBER);
		out.write(bytes);
		out.write(TERMINATOR);
		return this;
	}

	public final BencodeBuilder addValue(int value) throws IOException {
		byte[] bytes = String.valueOf(value).getBytes(charset);
		out.write(NUMBER);
		out.write(bytes);
		out.write(TERMINATOR);
		return this;
	}

	public final BencodeBuilder startMap() throws IOException {
		out.write(DICTIONARY);
		return this;
	}

	public final BencodeBuilder endMap() throws IOException {
		out.write(TERMINATOR);
		return this;
	}

	public final BencodeBuilder startArray() throws IOException {
		out.write(LIST);
		return this;
	}

	public final BencodeBuilder endArray() throws IOException {
		out.write(TERMINATOR);
		return this;
	}

	protected void addString(String str) throws IOException {
		if (str == null) {
			out.write('0');
			out.write(SEPARATOR);
			return;
		}
		byte[] bytes = str.getBytes(charset);
		byte[] lenBytes = String.valueOf(bytes.length).getBytes(charset);
		out.write(lenBytes);
		out.write(SEPARATOR);
		out.write(bytes);
	}

	public final BencodeBuilder addValue(Object value) throws IOException {
		if (value == null) {
			addString(null);
		} else if (value instanceof String) {
			addString((String) value);
		} else if (value instanceof Boolean) {
			addString(value.toString());
		} else if (value instanceof Double) {
			addValue(((Double) value).doubleValue());
		} else if (value instanceof Number) {
			addValue(((Number) value).longValue());
		} else if (value instanceof List) {
			List theList = (List) value;
			startArray();
			for (Iterator i = theList.iterator(); i.hasNext();) {
				addValue(i.next());
			}
			endArray();

		} else if (value instanceof Map) {
			Map theMap = (Map) value;
			startMap();
			for (Iterator i = theMap.entrySet().iterator(); i.hasNext();) {
				Entry element = (Entry) i.next();
				Object key = element.getKey();
				if (key == null) {
					continue;
				}
				addString(String.valueOf(element.getKey()));
				addValue(element.getValue());
			}
			endMap();

		} else {
			addString(String.valueOf(value));
		}
		return this;
	}
}
