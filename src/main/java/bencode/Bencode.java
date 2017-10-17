package bencode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Bencode format parser and builder. parser is SAX like and also has
 * implemented DOM listener for the SAX events. Builder uses
 * {@link BencodeBuilder} for deep traversing.
 */
public class Bencode {
	/** Number */
	static final int NUMBER = 'i';

	/** List */
	static final int LIST = 'l';

	/** Dictionary */
	static final int DICTIONARY = 'd';

	/** End of type */
	static final int TERMINATOR = 'e';

	/** Separator between length and string */
	static final int SEPARATOR = ':';

	/** Separator between length and string */
	static final int STRING_LIMMIT = 1024 * 1024;

	public Bencode(String charset) {
		this.charset = charset;
	}

	public Object decode(InputStream in) throws IOException {
		BencodeListenerImpl l = new BencodeListenerImpl();
		decode(in, l);
		return l.getObject();
	}

	public void decode(InputStream in, BencodeListener listener) throws IOException {
		this.in = in;
		this.listener = listener;
		listener.reset();
		pos = 0;
		bufferPos = 0;
		currentByte = readByte();

		if (currentByte == -1) {
			return;
		}
		scanElement();
	}

	public void encode(Object obj, OutputStream out) throws IOException {
		BencodeBuilder builder = new BencodeBuilder(out);
		objectToBencode(builder, obj);
	}

	public byte[] encode(Object obj) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		encode(obj, out);
		return out.toByteArray();
	}

	private static final void objectToBencode(BencodeBuilder builder, Object obj) throws IOException {
		if (obj == null) {
			builder.addValue(null);
		} else if (obj instanceof Map) {
			builder.startMap();
			for (Iterator i = ((Map) obj).entrySet().iterator(); i.hasNext();) {
				Map.Entry entry = (Entry) i.next();
				Object key = entry.getKey();
				if (key == null) {
					continue;
				}
				builder.addKey(key.toString());
				objectToBencode(builder, entry.getValue());
			}
			builder.endMap();
		} else if (obj instanceof List) {
			builder.startArray();
			for (Iterator i = ((List) obj).iterator(); i.hasNext();) {
				objectToBencode(builder, i.next());
			}
			builder.endArray();
		} else if (obj instanceof Date) {
			builder.addValue(((Date) obj).getTime());
		} else if (obj instanceof Calendar) {
			builder.addValue(((Calendar) obj).getTimeInMillis());
		} else {
			builder.addValue(obj);
		}
	}

	protected InputStream in;
	protected BencodeListener listener;
	protected int currentByte;
	protected long pos;
	protected int bufferPos;
	protected byte[] buffer = new byte[128];
	protected final String charset;

	protected int readByte() throws IOException {
		currentByte = in.read();
		pos++;
		return currentByte;
	}

	protected void ensureLength(int l) {
		if (buffer.length < l) {
			byte[] _buffer = new byte[l];
			System.arraycopy(buffer, 0, _buffer, 0, bufferPos);
			buffer = _buffer;
		}
	}

	protected void appendBuffer(int c) {
		if (bufferPos >= buffer.length) {
			byte[] _buffer = new byte[buffer.length << 1];
			System.arraycopy(buffer, 0, _buffer, 0, buffer.length);
			buffer = _buffer;
		}
		buffer[bufferPos] = (byte) c;
		bufferPos++;
	}

	protected void scanElement() throws IOException {
		switch (currentByte) {
		case NUMBER:
			// eat i
			readByte();
			bufferPos = 0;
			if (currentByte == '+' || currentByte == '-' || (currentByte >= '0' && currentByte <= '9')) {
				// long or double
				if (currentByte == '+' || currentByte == '-') {
					appendBuffer(currentByte);
					readByte();// eat sign
				}
				readDigits();

				boolean decimal = false;
				if (currentByte == '.') {
					decimal = true;
					appendBuffer(currentByte);
					readByte();// eat .
					readDigits();
				}

				String theNumber = new String(buffer, 0, bufferPos, charset);

				if (decimal) {
					if (bufferPos > 0 && buffer[bufferPos - 1] == '.') {
						bufferPos--;
					}
					listener.number(new Double(theNumber));
				} else {
					listener.number(new Long(theNumber));
				}

			}
			if (currentByte != TERMINATOR) {
				throw new IOException("missing number terminator e at offset: " + pos);
			}
			// eat e
			readByte();
			break;

		case LIST:
			// eat l
			readByte();
			listener.listStart();
			while (true) {
				scanElement();
				if (currentByte == TERMINATOR)
					break;
				if (currentByte == -1) {
					throw new IOException("unexpected end of list at offset: " + pos);
				}
			}
			// eat e
			readByte();
			listener.listEnd();
			break;

		case DICTIONARY:
			// eat d
			readByte();
			listener.mapStart();
			while (true) {
				// check for string map key
				String str = readString();
				listener.mapKey(str);
				// get the map value
				scanElement();
				if (currentByte == TERMINATOR)
					break;
				if (currentByte == -1) {
					throw new IOException("unexpected end of map at offset: " + pos);
				}
			}
			// eat e
			readByte();
			listener.mapEnd();
			break;

		default:
			String str = readString();
			listener.string(str);
			break;
		}
	}

	protected String readString() throws IOException {
		// the string in format len+SEPARATOR+content
		if (currentByte < '0' || currentByte > '9') {
			throw new IOException("expecting len:string at offset: " + pos);
		}
		int len = readNumber();
		if (currentByte == -1) {
			throw new IOException("unexpected end of len:string at offset: " + pos);
		}
		if (len < 0) {
			throw new IOException("length of the len:string does not fit int size at offset: " + pos);
		}
		ensureLength(len);
		int l = in.read(buffer, 0, len);
		if (l < len) {
			throw new IOException("cannot read len:string len=" + len + " at offset: " + pos);
		}
		pos += l;
		String str = new String(buffer, 0, len, charset);
		readByte();
		return str;
	}

	protected int readNumber() throws IOException {
		int result = 0;
		while (true) {
			if (currentByte >= '0' && currentByte <= '9') {
				result = result * 10;
				result += (0x0F & currentByte);
				if (result < 0) {
					// in case the length exceeds Integer.MAX_VALUE
					result = -1;
					break;
				}
			} else {
				break;
			}
			readByte();
		}
		return result;
	}

	protected final void readDigits() throws IOException {
		while (currentByte >= '0' && currentByte <= '9') {
			appendBuffer((char) currentByte);
			readByte();
		}
		return;
	}

	public static long toLong(String s) {
		return toLong(s, 0);
	}

	public static long toLong(String str, int i) {
		if (str == null)
			return i;
		try {
			return Long.parseLong(str, 10);
		} catch (NumberFormatException numberformatexception) {
			return i;
		}
	}

}
