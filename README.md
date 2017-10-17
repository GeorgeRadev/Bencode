# Bencode
Bencode format parser and builder in Java 1.4.
Requires JDK 1.4 or higher and junit.jar for tests

### Examples

#### Bencode parsing
```java
byte[] content = "d3:bar4:test3:fooi42ee".getBytes("UTF-8");

Bencode bencode = new Bencode("UTF-8");
Object result = bencode.decode(new ByteArrayInputStream(content));
byte[] bytes = bencode.encode(result);
result = bencode.decode(new ByteArrayInputStream(bytes));

System.out.println(result);
```

Outputs: ```{foo=42, bar=test}```

#### Bencode creation:
```java
ByteArrayOutputStream out = new ByteArrayOutputStream();
BencodeBuilder builder = new BencodeBuilder(out);

builder.startMap();
builder.addKeyValue("key", "value").addKeyValue("for", "while");
builder.addKey("array");
builder.startArray().addValue("for").addValue("value").addValue(10).endArray();
builder.endMap();

byte[] bencodeBytes = out.toByteArray();
String bencodeString = new String(bencodeBytes);
System.out.println(result);
```

Outputs: ```d3:key5:value3:for5:while5:arrayl3:for5:valuei10eee```