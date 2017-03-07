# BSV
BSV stands for "Binary-Separated Values". It is like CSV but use binary character as values seperator, and support complex data type such as Array, Object.

BSV is used in our production environment which collect 30 billion record each day, and reduce 40% bandwidth compare to JSON. In our case, we use BSV and GZIP together.

# How To Use
* Define schema
```
  majorVersion: "03"
  minorVersion: "1"
  variantNumber: "0"
  beanClass: "com.meizu.bigdata.uxip.bsv.Schema030x"
  fields:
  - name: "s"
    type: "string"
  - name: "i"
    type: "int"
  - name: "n"
    type: "string"
  - name: "array"
    type: "list"
  - name: "map"
    type: "map"
```
* Load schema and build context
```
  BsvContext context = new BsvContextBuilder()
            .yamlSchema("classpath:com/github/bsv/schema03.0.0.yaml")
            .yamlSchema("classpath:com/github/bsv/schema03.0.1.yaml")
            .yamlSchema("classpath:com/github/bsv/schema03.1.0.yaml")
            .build();
```

* Create deserilizer from data input
```
 BsvDeserializer deserializer = context.createDeserializer(inputStream);
```

* Read data until return null
```
deserializer.next()
```

Read Unit Test for more details.
