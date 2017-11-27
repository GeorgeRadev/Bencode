package bencode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * utility class for reading generic Object paths. like XPath for XML.
 */
public class BencodePath {
	protected BencodePath() {
	}

	public static Object getObject(Object obj, String _path) throws Exception {
		Object trace = obj;
		if (_path != null) {
			String[] path = split(_path, '.');
			for (int i = 0; i < path.length; i++) {
				String token = path[i];

				if (trace instanceof Map) {
					trace = ((Map) trace).get(token);

				} else if (trace instanceof List) {
					int ix;
					try {
						ix = Integer.parseInt(token, 10);
					} catch (Exception e) {
						throw new Exception("Path [" + path + "] list lement [" + token + "]  not a number!");
					}
					List list = ((List) trace);

					if (list.size() <= ix) {
						throw new Exception("Path [" + path + "] list [" + token + "]  exceeds size!");
					}
					trace = ((List) trace).get(ix);

				} else {
					throw new Exception("Path [" + path + "] element [" + token + "] not reachable!");
				}

				if (trace == null) {
					throw new Exception("Path [" + path + "] element [" + token + "] not reachable!");
				}
			}
		}
		return trace;
	}

	public static List getList(Object obj, String path) throws Exception {
		Object result = getObject(obj, path);
		if (result instanceof List || result == null) {
			return (List) result;
		} else {
			throw new Exception("Path [" + path + "] does not point to a list!");
		}
	}

	public static Map getMap(Object obj, String path) throws Exception {
		Object result = getObject(obj, path);
		if (result instanceof Map) {
			return (Map) result;
		} else {
			throw new Exception("Path [" + path + "] does not point to a map!");
		}
	}

	public static String getString(Object obj) throws Exception {
		return getString(obj, null);
	}

	public static String getString(Object obj, String path) throws Exception {
		Object result = getObject(obj, path);
		if (result == null) {
			return null;
		} else {
			return String.valueOf(result);
		}
	}

	public static boolean getBoolean(Object obj, String path) throws Exception {
		Object result = getObject(obj, path);
		if (result instanceof Boolean) {
			return Boolean.TRUE.equals(result);
		} else if (result != null) {
			return "true".equalsIgnoreCase(String.valueOf(result));
		} else {
			return false;
		}
	}

	public static Number getNumber(Object obj, String path) throws Exception {
		Object result = getObject(obj, path);
		if (result instanceof Number || result == null) {
			return (Number) result;
		} else {
			try {
				return new Double(String.valueOf(result));
			} catch (Exception e) {
				throw new Exception("Path [" + path + "] does not point to a number!");
			}
		}
	}

	public static int getInteger(Object obj) throws Exception {
		return getInteger(obj, null);
	}

	public static int getInteger(Object obj, String path) throws Exception {
		Number result = getNumber(obj, path);
		return result.intValue();
	}

	public static long getLong(Object obj) throws Exception {
		return getLong(obj, null);
	}

	public static long getLong(Object obj, String path) throws Exception {
		Number result = getNumber(obj, path);
		return result.longValue();
	}

	public static double getDouble(Object obj) throws Exception {
		return getDouble(obj, null);
	}

	public static double getDouble(Object obj, String path) throws Exception {
		Number result = getNumber(obj, path);
		return result.doubleValue();
	}

	/**
	 * Separate string by separator and skip empty tokens
	 */
	public static String[] split(final String string, final char separator) {
		if (string == null || string.length() <= 0) {
			return new String[0];
		}

		List tokens = new ArrayList();
		int i = 0, len = string.length(), s = 0;
		for (; i < len; i++) {
			final char c = string.charAt(i);
			if (c == separator) {
				if (i - s > 0) {
					final String addstr = string.substring(s, i).trim();
					if (addstr.length() > 0)
						tokens.add(addstr);
				}
				s = i + 1;
			}
		}
		// if there is remaining string - add it
		if (len - s > 0) {
			final String addstr = string.substring(s, i).trim();
			if (addstr.length() > 0)
				tokens.add(addstr);
		}

		final String[] result = new String[tokens.size()];
		tokens.toArray(result);
		return result;
	}
}
