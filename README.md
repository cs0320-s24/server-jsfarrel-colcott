> **GETTING STARTED:** You must start from some combination of the CSV Sprint code that you and your partner ended up with. Please move your code directly into this repository so that the `pom.xml`, `/src` folder, etc, are all at this base directory.

> **IMPORTANT NOTE**: In order to run the server, run `mvn package` in your terminal then `./run` (using Git Bash for Windows users). This will be the same as the first Sprint. Take notice when transferring this run sprint to your Sprint 2 implementation that the path of your Server class matches the path specified in the run script. Currently, it is set to execute Server at `edu/brown/cs/student/main/server/Server`. Running through terminal will save a lot of computer resources (IntelliJ is pretty intensive!) in future sprints.

# Project Details

Server

Team members: John Farrell (jsfarrel), Charles Olcott (colcott)

Time estimated: 12 hours

GitHub repo: https://github.com/cs0320-s24/server-jsfarrel-colcott

# Design Choices

The program is a backend server built on Spark. It has for GET endpoints: `broadband`, `loadcsv`, 
`viewcsv`, `searchcsv`. Broadband has caching provided by the `APICache` wrapper class. 

Endpoint design choices:
- `GET /broadband`
  - The handler takes in a `BroadbandSource`, a strategy pattern to allow developers to determine how
  data is obtained about the broadband coverage of a county.
  - All parameters must be defined, or an error will be returned (county and state).
  - A timestamp (in form `SimpleDateFormat`) from EST time zone is included with response
  - State and county is included in response.
- `GET /loadcsv`
    - The handler takes in a `ParserState`, a class to manage a `CSVParser` among all endpoints to ensure
      there is only one existing parser for all endpoints. `ParserState` simply contains a `CSVParser` and
      has a setter and getter.
    - All parameters must be defined, or an error will be returned (filepath).
    - Tries to create a `CSVParser` and set the parser in `ParserState` equal to this parser.
    - Returns a success or error object.
- `GET /viewcsv`
    - The handler takes in a `ParserState`, a class to manage a `CSVParser` among all endpoints to ensure
      there is only one existing parser for all endpoints. `ParserState` simply contains a `CSVParser` and
      has a setter and getter.
    - No parameters
    - Tries to get a `CSVParser` from `ParserState` a return rows in response.
    - Will return error if `ParserState` doesn't have a defined `CSVParser`.
- `GET /searchcsv`
    - The handler takes in a `ParserState`, a class to manage a `CSVParser` among all endpoints to ensure
      there is only one existing parser for all endpoints. `ParserState` simply contains a `CSVParser` and
      has a setter and getter.
    - All parameters must be defined, or an error will be returned (value, column specifier/identifier, hasHeaders).
    - Tries to get a `CSVParser` from `ParserState` a return rows in response.
    - Creates a `CSVSearcher` and attempts to search within CSV with given params.
    - Will response with object of results or error.

All responses have "result" field that equals:
- “success” on success
- "error_bad_json" if the request was ill-formed
- "error_bad_request" if the request was missing a needed field, or the field was ill-formed
- "error_datasource" if the given data source wasn't accessible (e.g., the file didn't exist or the ACS API returned an error for a given location)

All endpoints with params requested (`/broadband`, `/loadcsv`, `/searchcsv`) respond with fields that repeat back the parameters given
(e.g. `/loadcsv` response has field “filepath” - the “filepath” given is request)

Successful viewcsv and searchcsv responses have "data" field containing contents of CSV.


The program also includes a CSV search and parse functionality, split into two Classes and parts: parsing and searching. To initialize a
`CSVSearcher`, you must first initialize a `CSVParser` because it it is an input to `CSVSearcher`.

- `CSVSearcher`
    - Inputs: `CSVParser<String[]> parser`, `boolean hasHeader`
    - Functions:
        - `search`: Search takes in a `String value`, `String column`, and
          `ColumnSpecified specification`. Starts by parsing the CSV, then checks returned rows for
          `value` with optional column specification. Returns a list of rows that match the value.
        - `getColumns`: helper for `search` to get a list of columns to check for `value` in. Plural
          columns because no specification will result in all columns being searched. Also, if two
          columns have the same name, both will be searched if queried.
        - `isInteger`: helper for `search` to check if a value represents an integer. For the
          purpose

- `CSVParser`
    - Inputs: `Reader reader`, `CreatorFromRow<T> create`
    - Functions:
        - `parse`: This function has no inputs and outputs a list of rows from the CSV. Uses
          `reader` to get CSV file lines, and splits them using Regex. After splitting, if the
          elements per line differ, `parse` throws an `error` for invalid CSV dimensions. Then,
          the split line is entered to the row constructor, and the result is added to a list.
          The list of constructed rows is returned.

## Cache Configuration

- To use the cache, surround an endpoint handler in a `new APICache()`. The `APICache` has two inputs, 
  the endpoint handler of type `Route` and a `CacheBuilder`. The `CacheBuilder` is where configuration
  of eviction policies go. The `APICache` acts as a proxy to the given `Route`. `APICache` builds a 
  Guava cache using the given `CacheBuilder` and configures the `load()` function to take in a `Request` and return
  the response of calling the given `Route.handle()`.
- By default, there is a 30-second cache of requests and responses to the `GET /broadband` endpoint.
  This default can be modified in `Server.java`:
  ```java
    CacheBuilder<Object, Object> cacheBuilder =
    CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.SECONDS);
    
    // Setting up the handler for the GET /loadcsv, /viewcsv, /searchcsv, /broadband
    Spark.get("broadband", new APICache(new BroadbandHandler(new ACSBroadbandSource()), cacheBuilder));
  ```

# Errors/Bugs

# Tests

There are seven test suites:

- Parse tests - these tests include all the testing for the `parse` function.
- Search tests - these tests include all the testing for the `search` function.
- ACS Broadband Source tests - these tests include checking the Census API data source is valid.
- Broadband Handler tests - these tests include all the testing for the `/broadband` endpoint.
- Load CSV Handler tests - these tests include all the testing for the `/loadcsv` endpoint.
- View CSV Handler tests - these tests include all the testing for the `/viewcsv` endpoint.
- Search CSV Handler tests - these tests include all the testing for the `/searchcsv` endpoint.

# How to

## Run tests

```
mvn test
```

## Build and run

Starts the server, URL/port printed to terminal:

```
mvn package
./run
```

## Use Endpoints

- `GET /broadband`
  - `state`: the state to search for percent broadband coverage within.
  - `county`: the county to search for percent broadband coverage within.
  - Response has `percent` field containing the percent broadband coverage within the specified county.
- `GET /loadcsv`
  - `filepath`: the filepath to load a CSV from. Must be within the data directory. Must be defined.
  - Response returns `result:success` if loaded successfully. 
- `GET /viewcsv`
  - No params.
  - Response has `data` field containing an array of CSV rows.
- `GET /searchcsv`
  - `toSearch`: the value being searched for in CSV. Must be defined.
  - `hasHeaders`: `"true"` if CSV has headers, `"false"` if CSV doesn't have headers. Must be defined.
  - `columnSpecifier`: `"name"` if specifying for a column by name, `index` if specifying for a column by index. Must be defined if `columnIdentifier` is defined.
  - `columnIdentifier`: the value representing the column to search for `toSearch` within. Must be defined if `columnSpecifier` is defined.
  - Response has `data` field containing an array of CSV rows that are valid search results. Not specifying a `columnSpecifier` or `columnIdentifier` will search for `toSearch` in all columns.