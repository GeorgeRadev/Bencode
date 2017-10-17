package bencode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import bencode.Bencode;
import bencode.BencodeBuilder;
import bencode.BencodePath;
import junit.framework.TestCase;

public class BencodeTest extends TestCase {

	public void testMap() throws Exception {
		byte[] content = "d3:bar4:test3:fooi42ee".getBytes("UTF-8");
		Bencode bencode = new Bencode("UTF-8");
		Object result = bencode.decode(new ByteArrayInputStream(content));
		byte[] bytes = bencode.encode(result);
		result = bencode.decode(new ByteArrayInputStream(bytes));
		System.out.println(result);

		assertEquals("test", BencodePath.getString(result, "bar"));
		assertEquals(42L, BencodePath.getLong(result, "foo"));
	}

	public void testList() throws Exception {
		String content = "l4:testi42ee";
		Bencode bencode = new Bencode("UTF-8");
		Object result = bencode.decode(new ByteArrayInputStream(content.getBytes("UTF-8")));
		byte[] bytes = bencode.encode(result);
		result = bencode.decode(new ByteArrayInputStream(bytes));

		assertEquals("test", BencodePath.getString(result, "0"));
		assertEquals(42L, BencodePath.getLong(result, "1"));
	}

	public void testString() throws Exception {
		String content = "4:test";
		Bencode bencode = new Bencode("UTF-8");
		Object result = bencode.decode(new ByteArrayInputStream(content.getBytes("UTF-8")));
		byte[] bytes = bencode.encode(result);
		result = bencode.decode(new ByteArrayInputStream(bytes));

		assertEquals("test", BencodePath.getString(result));
	}

	public void testLong() throws Exception {
		String content = "i42e";
		Bencode bencode = new Bencode("UTF-8");
		Object result = bencode.decode(new ByteArrayInputStream(content.getBytes("UTF-8")));
		byte[] bytes = bencode.encode(result);
		result = bencode.decode(new ByteArrayInputStream(bytes));

		assertEquals(42L, BencodePath.getLong(result));
	}

	public void testDouble() throws Exception {
		String content = "i42.13e";
		Bencode bencode = new Bencode("UTF-8");
		Object result = bencode.decode(new ByteArrayInputStream(content.getBytes("UTF-8")));
		byte[] bytes = bencode.encode(result);
		result = bencode.decode(new ByteArrayInputStream(bytes));

		assertEquals(42.13d, BencodePath.getDouble(result), 0.000d);
	}

	public void testDecodeEncode() throws Exception {
		String content = "d3:mapd10:map-item-14:test10:map-item-25:valuee4:listl11:list-item-111:list-item-2e6:numberi123456e6:string3:stre";
		Bencode bencode = new Bencode("UTF-8");
		Object result = bencode.decode(new ByteArrayInputStream(content.getBytes("UTF-8")));
		byte[] bytes = bencode.encode(result);
		result = bencode.decode(new ByteArrayInputStream(bytes));

		assertEquals("test", BencodePath.getString(result, "map.map-item-1"));
		assertEquals("value", BencodePath.getString(result, "map.map-item-2"));
		assertEquals(123456L, BencodePath.getLong(result, "number"));
		assertEquals("list-item-1", BencodePath.getString(result, "list.0"));
		assertEquals("list-item-2", BencodePath.getString(result, "list.1"));
		assertEquals("str", BencodePath.getString(result, "string"));
	}

	public void testBuilder() throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BencodeBuilder builder = new BencodeBuilder(out);
		builder.startMap();
		builder.addKeyValue("key", "value").addKeyValue("for", "while");
		builder.addKey("array");
		builder.startArray().addValue("for").addValue("value").addValue(10).endArray();
		builder.endMap();

		byte[] bencodeBytes = out.toByteArray();

		String bencodeString = new String(bencodeBytes);
		assertEquals("d3:key5:value3:for5:while5:arrayl3:for5:valuei10eee", bencodeString);

		Bencode bencode = new Bencode("UTF-8");
		Object result = bencode.decode(new ByteArrayInputStream(bencodeBytes));
		byte[] bytes = bencode.encode(result);
		result = bencode.decode(new ByteArrayInputStream(bytes));

		assertEquals("value", BencodePath.getString(result, "key"));
		assertEquals("while", BencodePath.getString(result, "for"));
		assertEquals("for", BencodePath.getString(result, "array.0"));
		assertEquals("value", BencodePath.getString(result, "array.1"));
		assertEquals(10L, BencodePath.getLong(result, "array.2"));
	}

	public void testStringLength() throws Exception {
		String content = "d1234567890123456789:bareeeeeeee";
		Bencode bencode = new Bencode("UTF-8");
		try {
			bencode.decode(new ByteArrayInputStream(content.getBytes("UTF-8")));
			fail();
		} catch (Exception e) {
			// ok
		}
	}
}
