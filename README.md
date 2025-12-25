# SQL Tool

[![Maven Central](https://img.shields.io/maven-central/v/io.github.
qwzhang01/seven-sql-parser.svg)](https://search.maven.org/artifact/io.github.qwzhang01/seven-sql-parser)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/Java-17%2B-blue)](https://www.oracle.com/java/technologies/javase-jdk17-downloads.html)

A lightweight, powerful SQL parsing and analysis library for Java applications.
Built on top of JSQLParser, this tool provides comprehensive capabilities for
SQL statement parsing, analysis, and dynamic manipulation without requiring a
database connection.

## Key Features

### 🔍 SQL Parsing & Analysis

- **Multi-Statement Support**: Parse SELECT, INSERT, UPDATE, and DELETE
  statements
- **Deep Analysis**: Extract tables, columns, parameters, conditions, and JOIN
  relationships
- **Smart Parameter Detection**: Identify JDBC placeholders (?) and map them to
  their columns
- **Custom Parameter Formats**: Support for MyBatis-style `#{param}`
  placeholders with automatic conversion

### 🔧 SQL Manipulation

- **Dynamic JOIN Addition**: Programmatically add JOIN clauses to existing SQL
- **Dynamic WHERE Conditions**: Append WHERE conditions intelligently, combining
  with existing ones using AND
- **Clause Merging**: Merge new clauses with existing SQL without breaking the
  structure

### 📊 Information Extraction

- **Table Discovery**: Find all tables referenced in SQL, including those in
  subqueries
- **Alias Resolution**: Automatically resolve and apply table aliases
- **Parameter Mapping**: Map each `?` placeholder to its corresponding table and
  column
- **Nested Query Support**: Handle complex nested queries and subqueries

### 🎯 Advanced Operators

Comprehensive support for SQL operators:

- **Comparison**: `=`, `!=`, `<`, `>`, `<=`, `>=`
- **Range**: `BETWEEN`, `NOT BETWEEN`
- **List**: `IN`, `NOT IN`
- **Pattern Matching**: `LIKE`, `NOT LIKE`
- **NULL Checks**: `IS NULL`, `IS NOT NULL`
- **Logical**: `AND`, `OR`, `NOT`

## Installation

### Maven

```xml

<dependency>
    <groupId>io.github.qwzhang01</groupId>
    <artifactId>seven-sql-parser</artifactId>
    <version>1.1.7</version>
</dependency>
```

### Gradle

```gradle
implementation 'io.github.qwzhang01:seven-sql-parser:1.1.7'
```

## Quick Start

```java
import io.github.qwzhang01.sql.tool.helper.ParserHelper;
import io.github.qwzhang01.sql.tool.model.SqlParam;
import io.github.qwzhang01.sql.tool.model.SqlTable;

import java.util.List;

// Parse a SQL statement
String sql = "SELECT u.name, u.email FROM users u WHERE u.age > ? AND u.status = ?";

// Extract tables
List<SqlTable> tables = ParserHelper.getTables(sql);
System.out.

println("Tables: "+tables);

// Extract parameters
List<SqlParam> params = ParserHelper.getParam(sql);
for(
SqlParam param :params){
        System.out.

println("Parameter "+param.getIndex() +": "+
        param.

getTable() +"."+param.

getColumn());
        }

// Add a WHERE condition dynamically
String modifiedSql = ParserHelper.addWhere(sql, "u.created_at > '2024-01-01'");
System.out.

println("Modified SQL: "+modifiedSql);
```

## Project Architecture

```
seven-sql-parser/
├── exception/                    # Custom exceptions
│   ├── ParseException.java       # Base parsing exception
│   ├── SqlIllegalException.java  # Illegal SQL exception
│   └── UnSupportedException.java # Unsupported feature exception
├── helper/                       # Utility helpers
│   └── ParserHelper.java         # Main API for SQL operations
├── model/                        # Data models
│   ├── SqlParam.java             # Parameter placeholder info
│   └── SqlTable.java             # Table information with aliases
├── wrapper/                      # Wrapper classes
│   ├── SqlParser.java            # SQL statement parser wrapper
│   └── TableParser.java          # Table object parser
└── jsqlparser/                   # JSQLParser integration
    ├── param/
    │   └── ParamExtractor.java   # Parameter extraction utilities
    └── visitor/                  # AST visitors
        ├── CompleteTableVisitor.java  # Alias resolution
        ├── MergeStatementVisitor.java # SQL merging logic
        ├── ParamFinder.java           # Parameter discovery
        ├── SplitStatementVisitor.java # Clause extraction
        └── TableFinder.java           # Table discovery
```

## Core API Usage

### 1. Table Extraction

Extract all tables from SQL, including those in subqueries and JOINs:

```java
import io.github.qwzhang01.sql.tool.helper.ParserHelper;
import io.github.qwzhang01.sql.tool.model.SqlTable;

String sql = """
        SELECT u.name, o.order_date
        FROM users u
        INNER JOIN orders o ON u.id = o.user_id
        WHERE u.status = 'active'
        """;

List<SqlTable> tables = ParserHelper.getTables(sql);
for(
SqlTable table :tables){
        System.out.

println("Table: "+table.getName() +
        ", Alias: "+table.

getAlias());
        }
// Output:
// Table: users, Alias: u
// Table: orders, Alias: o
```

### 2. Parameter Extraction

Extract and map JDBC parameters to their columns:

```java
String sql = "SELECT * FROM users WHERE age > ? AND status = ? AND created_at BETWEEN ? AND ?";

List<SqlParam> params = ParserHelper.getParam(sql);
for(
SqlParam param :params){
        System.out.

println("Parameter #"+param.getIndex() +
        " -> Column: "+param.

getColumn() +
        ", Table: "+param.

getTable());
        }
// Output:
// Parameter #0 -> Column: age, Table: users
// Parameter #1 -> Column: status, Table: users
// Parameter #2 -> Column: created_at, Table: users
// Parameter #3 -> Column: created_at, Table: users
```

### 3. MyBatis Parameter Support

Handle MyBatis-style `#{param}` placeholders:

```java
String myBatisSql = "SELECT * FROM users WHERE name = #{userName} AND age > #{minAge}";

// Convert to standard JDBC placeholders and extract
List<SqlParam> params = ParserHelper.getSpecParam(myBatisSql);
// Internally converts to: SELECT * FROM users WHERE name = ? AND age > ?
```

### 4. Dynamic WHERE Clause Addition

Add WHERE conditions to existing SQL:

```java
String originalSql = "SELECT * FROM users WHERE status = 'active'";

// Add additional WHERE condition
String modifiedSql = ParserHelper.addWhere(originalSql, "age >= 18");
System.out.

println(modifiedSql);
// Output: SELECT * FROM users WHERE status = 'active' AND age >= 18

// Can omit WHERE keyword
String modifiedSql2 = ParserHelper.addWhere(originalSql, "created_at > '2024-01-01'");
// Result: SELECT * FROM users WHERE status = 'active' AND created_at > '2024-01-01'
```

### 5. Dynamic JOIN Addition

Add JOIN clauses to existing SQL:

```java
String originalSql = "SELECT u.* FROM users u WHERE u.status = 'active'";

// Add a JOIN
String joinClause = "INNER JOIN orders o ON u.id = o.user_id";
String modifiedSql = ParserHelper.addJoin(originalSql, joinClause);
System.out.

println(modifiedSql);
// Output: SELECT u.* FROM users u 
//         INNER JOIN orders o ON u.id = o.user_id 
//         WHERE u.status = 'active'
```

### 6. Combined JOIN and WHERE Addition

Add both JOIN and WHERE clauses simultaneously:

```java
String baseSql = "SELECT u.name FROM users u";

String modifiedSql = ParserHelper.addJoinAndWhere(
        baseSql,
        "LEFT JOIN addresses a ON u.id = a.user_id",
        "u.status = 'active' AND a.country = 'USA'"
);

System.out.

println(modifiedSql);
// Output: SELECT u.name FROM users u
//         LEFT JOIN addresses a ON u.id = a.user_id
//         WHERE u.status = 'active' AND a.country = 'USA'
```

## Advanced Examples

### Working with Complex Queries

```java
String complexSql = """
        SELECT 
            u.id, u.name, COUNT(o.id) as order_count
        FROM users u
        LEFT JOIN orders o ON u.id = o.user_id
        WHERE u.status = ?
        GROUP BY u.id, u.name
        HAVING COUNT(o.id) > ?
        ORDER BY order_count DESC
        LIMIT ?
        """;

// Extract all tables
List<SqlTable> tables = ParserHelper.getTables(complexSql);
// Returns: users (alias: u), orders (alias: o)

// Extract parameters
List<SqlParam> params = ParserHelper.getParam(complexSql);
// Returns 3 parameters mapped to their respective columns
```

### Handling Subqueries

```java
String subquerySql = """
        SELECT u.name 
        FROM users u
        WHERE u.id IN (
            SELECT user_id 
            FROM orders 
            WHERE total > ?
        )
        """;

List<SqlTable> tables = ParserHelper.getTables(subquerySql);
// Returns: users, orders (from subquery)

List<SqlParam> params = ParserHelper.getParam(subquerySql);
// Returns parameter mapped to orders.total
```

### Custom Parameter Pattern Matching

```java
import java.util.regex.Pattern;

// Define custom parameter pattern (e.g., :paramName)
Pattern customPattern = Pattern.compile(":(\\w+)");

String sql = "SELECT * FROM users WHERE name = :userName AND age > :minAge";
List<SqlParam> params = ParserHelper.getSpecParam(sql, customPattern);
// Converts :userName and :minAge to ? and extracts parameters
```

## Supported SQL Features

### Statement Types

| Statement | Support Level | Notes                                       |
|-----------|---------------|---------------------------------------------|
| SELECT    | ✅ Full        | Including complex queries, subqueries, CTEs |
| INSERT    | ✅ Full        | Single and batch inserts                    |
| UPDATE    | ✅ Full        | Including JOINs                             |
| DELETE    | ✅ Full        | Including JOINs                             |
| MERGE     | ⚠️ Partial    | Basic support                               |

### SQL Clauses

- ✅ WHERE (all operators)
- ✅ JOIN (INNER, LEFT, RIGHT, FULL, CROSS)
- ✅ GROUP BY
- ✅ HAVING
- ✅ ORDER BY
- ✅ LIMIT / OFFSET
- ✅ UNION / UNION ALL
- ✅ WITH (CTE)

### Operators

- **Comparison**: `=`, `!=`, `<>`, `<`, `>`, `<=`, `>=`
- **Logical**: `AND`, `OR`, `NOT`
- **Range**: `BETWEEN`, `NOT BETWEEN`
- **Pattern**: `LIKE`, `NOT LIKE`, `REGEXP`
- **Set**: `IN`, `NOT IN`, `EXISTS`, `NOT EXISTS`
- **Null**: `IS NULL`, `IS NOT NULL`
- **Arithmetic**: `+`, `-`, `*`, `/`, `%`, `DIV`, `MOD`

## Exception Handling

The library provides clear exception hierarchy:

```java
try{
String sql = "INVALID SQL SYNTAX";
List<SqlTable> tables = ParserHelper.getTables(sql);
}catch(
SqlIllegalException e){
        System.err.

println("Invalid SQL: "+e.getSql());
        System.err.

println("Error: "+e.getMessage());
        }catch(
ParseException e){
        System.err.

println("Parse error: "+e.getMessage());
        }
```

### Exception Types

- **`ParseException`**: Base exception for all parsing errors
- **`SqlIllegalException`**: Thrown when SQL syntax is invalid (includes the
  problematic SQL)
- **`UnSupportedException`**: Thrown when a valid SQL feature is not yet
  supported by this version

## Performance Considerations

- **Lightweight**: No database connection required
- **Fast Parsing**: Leverages JSQLParser's efficient parsing
- **Memory Efficient**: Processes SQL without loading entire database schemas
- **Thread-Safe**: All utility methods are thread-safe

### Best Practices

1. **Reuse ParserHelper**: All methods are static - no need to instantiate
2. **Cache Results**: If parsing the same SQL repeatedly, cache the results
3. **Validate First**: Use try-catch to handle malformed SQL gracefully

## Building from Source

```bash
# Clone the repository
git clone https://github.com/qwzhang01/seven-sql-parser.git
cd seven-sql-parser

# Build with Maven
mvn clean install

# Run tests
mvn test

# Generate Javadoc
mvn javadoc:javadoc
```

## Testing

Comprehensive test coverage including:

- **Unit Tests**: Individual component testing
- **Integration Tests**: End-to-end parsing scenarios
- **Edge Cases**: Complex queries, nested subqueries, error conditions

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ParserHelperTest

# Run with coverage
mvn clean test jacoco:report
```

## Version History

| Version | Date | Changes                |
|---------|------|------------------------|
| 1.1.7   | 2024 | Current stable release |
| 1.0.0   | 2023 | Initial release        |

## Requirements

- **Java**: 17 or higher
- **Maven**: 3.6 or higher (for building)
- **JSQLParser**: 5.1 (automatically managed by Maven)

## Dependencies

This library has minimal dependencies:

```xml

<dependency>
    <groupId>com.github.jsqlparser</groupId>
    <artifactId>jsqlparser</artifactId>
    <version>5.1</version>
</dependency>
```

## Roadmap

- [ ] Support for more database-specific SQL dialects
- [ ] Enhanced DDL statement support (CREATE, ALTER, DROP)
- [ ] Query optimization suggestions
- [ ] SQL formatter and beautifier
- [ ] Performance metrics and profiling

## Contributing

We welcome contributions! Please follow these guidelines:

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/amazing-feature`)
3. **Commit** your changes (`git commit -m 'Add amazing feature'`)
4. **Add tests** for new functionality
5. **Ensure** all tests pass (`mvn test`)
6. **Push** to the branch (`git push origin feature/amazing-feature`)
7. **Open** a Pull Request

### Code Style

- Follow standard Java naming conventions
- Add Javadoc comments for public APIs
- Write unit tests for new features
- Keep methods focused and concise

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file
for details.

## Support & Contact

- **Issues
  **: [GitHub Issues](https://github.com/qwzhang01/seven-sql-parser/issues)
- **Email**: qwzhang01@gmail.com
- **Documentation
  **: [GitHub Wiki](https://github.com/qwzhang01/seven-sql-parser/wiki)

## Acknowledgments

- Built on top of [JSQLParser](https://github.com/JSQLParser/JSqlParser)
- Inspired by the need for lightweight SQL analysis in Java applications

---

**Made with ❤️ by Avin Zhang**