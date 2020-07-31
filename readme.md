# LexiDB 
![Build Status](https://github.com/matthewcoole/cdb/workflows/build/badge.svg)
![codecov](https://codecov.io/gh/matthewcoole/cdb/branch/master/graph/badge.svg?token=XdKEOwSdnQ) 
![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/matthewcoole/lexidb.svg?logo=lgtm&logoWidth=18)
## Build
### Required
- [Java JDK 1.12+](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
- [Gradle](https://maven.apache.org/)

Build using the following command in the project directroy;

```
$ gradle build
```

## Deploy

Deploy locally;

```
$ java -jar build/libs/lexidb-2.0.jar /path/to/app.properties
```

## Test

You can test whether the server is running by making a simple API call in your browser; [http://localhost:1189/api/test](http://localhost:1189/api/test)

## Create a corpus

Create a new corpus;

```http request
POST /mycorpus/create

{
  "name": "tokens",
  "sets": [
    {
      "name": "tokens",
      "columns": [
        {
          "name": "token"
        }
...
}
```

insert some files;

```http request
POST /mycorpus/myfile.xml/insert

token   pos sem
When	CS	Z5
it	PPH1	Z8
comes	VVZ	A4.1[i651.2.1
to	II	A4.1[i651.2.2
tropical	JJ	M7/B2-[i652.2.1
diseases	NN2	M7/B2-[i652.2.2
,	,	PUNC
future	JJ	T1.1.3
scientific	JJ	Y1
research	NN1	X2.4
...
```

finally save;

```http request
GET /mycorpus/save
```

## Query

A `GET` request can be made to the endpoint [http://localhost:1189/mycorpus/query](http://localhost:1189/mycorpus/query). The body of the request should be in the form of a JSON query;

```http request
POST /mycorpus/query

{
  "query": {
    "tokens": "{\"pos\": \"JJ\"}"
  }
}
```

This will query the `"tokens"` table and the `"pos"` (part-of-speech) column for the value `"JJ"` and return the results in the form of a `"kwic"` (keyword in context).
