package bencode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Bencode Listener implementation for converting SAX events to DOM.
 */
public class BencodeListenerImpl implements BencodeListener {

	Stack objectStack;
	Map currentMap;
	List currentList;
	Object currentObj;
	String key;

	public void reset() {
		if (objectStack == null) {
			objectStack = new Stack();
		} else {
			objectStack.clear();
		}
		currentMap = null;
		currentList = null;
		key = null;
	}

	public Object getObject() {
		if (currentMap != null) {
			return currentMap;
		}
		if (currentList != null) {
			return currentList;
		}
		return currentObj;
	}

	public Map getMap() {
		return currentMap;
	}

	public List getList() {
		return currentList;
	}

	public void mapStart() {
		Map newMap = new HashMap(32);
		if (objectStack.size() > 0) {
			if (key != null) {
				currentMap.put(key, newMap);
				key = null;
			} else {
				currentList.add(newMap);
			}
		}
		objectStack.push(currentMap = newMap);
	}

	public void mapEnd() {
		currentMap = (Map) objectStack.pop();

		if (objectStack.size() > 0) {
			Object top = objectStack.peek();
			if (top instanceof List) {
				currentList = (List) top;
				currentMap = null;
			} else if (top instanceof Map) {
				currentMap = (Map) top;
				currentList = null;
			}
		}
	}

	public void listStart() {
		List newArray = new ArrayList(32);
		if (objectStack.size() > 0) {
			if (key != null) {
				currentMap.put(key, newArray);
				key = null;
			} else {
				currentList.add(newArray);
			}
		}
		objectStack.push(currentList = newArray);
	}

	public void listEnd() {
		currentList = (List) objectStack.pop();
		if (objectStack.size() > 0) {
			Object top = objectStack.peek();
			if (top instanceof List) {
				currentList = (List) top;
				currentMap = null;
			} else if (top instanceof Map) {
				currentMap = (Map) top;
				currentList = null;
			}
		}
	}

	public void mapKey(String text) {
		key = text;
	}

	public void string(String value) {
		if (key != null) {
			currentMap.put(key, value);
			key = null;
		} else if (currentList != null) {
			currentList.add(value);
		} else {
			currentObj = value;
		}
	}

	public void number(Number value) {
		if (key != null) {
			currentMap.put(key, value);
			key = null;
		} else if (currentList != null) {
			currentList.add(value);
		} else {
			currentObj = value;
		}
	}
}
