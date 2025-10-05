# SQL Tool

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.oracle.com/java/)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.qwzhang01/sql-tool.svg)](https://search.maven.org/artifact/io.github.qwzhang01/sql-tool)

A pure Java SQL parsing tool that supports bidirectional conversion between SQL and objects, with comprehensive MySQL syntax support. No external dependencies required.

## Features

- **Pure Java Implementation**: No external dependencies, lightweight and easy to integrate
- **Bidirectional Conversion**: Parse SQL to objects and generate SQL from objects
- **Comprehensive MySQL Support**: Supports complex SQL syntax including JOINs, subqueries, and advanced conditions
- **Detailed Field Analysis**: Extract table names, aliases, field information, and value counts
- **Advanced WHERE Parsing**: Support for complex conditions like IN, BETWEEN, LIKE, NOT operators
- **JOIN Analysis**: Detailed parsing of JOIN conditions with field-level information
- **SQL Cleaning**: Remove comments and normalize SQL formatting
- **SQL Comparison**: Compare SQL statements for equivalence

## Quick Start

### Maven Dependency

```xml
<dependency>
    <groupId>io.github.qwzhang01</groupId>
    <artifactId>sql-tool</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Basic Usage

```java
import io.github.qwzhang01.sql.tool.parser.MySqlPureSqlParser;
import io.github.qwzhang01.sql.tool.model.SqlObj;

// Create parser instance
MySqlPureSqlParser parser = new MySqlPureSqlParser();

// Parse SQL
String sql = "SELECT u.id, u.name FROM users u WHERE u.age > 18 AND u.status = 'active'";
SqlObj sqlObj = parser.parse(sql);

// Access parsed information
System.out.

println("SQL Type: "+sqlObj.getSqlType());
        System.out.

println("Tables: "+sqlObj.getTableNames());
        System.out.

println("Columns: "+sqlObj.getSelectColumns());
        System.out.

println("WHERE Conditions: "+sqlObj.getWhereConditions());
```

### JOIN Parsing

```java
// Parse JOIN information
String joinSql = "SELECT * FROM users u LEFT JOIN orders o ON u.id = o.user_id";
List<JoinInfo> joins = parser.parseJoin(joinSql);

for (JoinInfo join : joins) {
    System.out.println("JOIN Type: " + join.getJoinType());
    System.out.println("Table: " + join.getTableName());
    System.out.println("Alias: " + join.getAlias());
    System.out.println("Conditions: " + join.getJoinConditions());
}
```

### WHERE Condition Analysis

```java
// Parse WHERE conditions with detailed field information
String whereClause = "WHERE user.age BETWEEN 18 AND 65 AND user.name LIKE '%john%'";
SqlInfo whereInfo = parser.parseWhere(whereClause);

for (WhereCondition condition : whereInfo.getWhereConditions()) {
    FieldInfo fieldInfo = condition.getFieldInfo();
    System.out.println("Field: " + fieldInfo.getFieldName());
    System.out.println("Table: " + fieldInfo.getTableName());
    System.out.println("Operator: " + condition.getOperator());
    System.out.println("Value Count: " + condition.getValueCount());
}
```

### SQL Cleaning

```java
import io.github.qwzhang01.sql.tool.parser.MySqlSqlCleaner;

MySqlSqlCleaner cleaner = new MySqlSqlCleaner();

// Remove comments and normalize formatting
String dirtySql = "SELECT * FROM users /* comment */ WHERE id = 1 -- line comment";
String cleanSql = cleaner.cleanSql(dirtySql);
System.out.println("Clean SQL: " + cleanSql);
```

## Supported SQL Features

### SELECT Statements
- Column selection with aliases
- Table aliases
- WHERE conditions with complex operators
- JOIN operations (INNER, LEFT, RIGHT, FULL, CROSS)
- GROUP BY and HAVING clauses
- ORDER BY with ASC/DESC
- LIMIT clauses

### WHERE Conditions
- Basic operators: `=`, `!=`, `<>`, `<`, `>`, `<=`, `>=`
- Pattern matching: `LIKE`, `NOT LIKE`
- Range conditions: `BETWEEN`, `NOT BETWEEN`
- List conditions: `IN`, `NOT IN`
- NULL checks: `IS NULL`, `IS NOT NULL`
- Logical operators: `AND`, `OR`

### JOIN Operations
- INNER JOIN
- LEFT JOIN / LEFT OUTER JOIN
- RIGHT JOIN / RIGHT OUTER JOIN
- FULL JOIN / FULL OUTER JOIN
- CROSS JOIN
- Complex ON conditions with multiple fields

### MySQL Specific Features
- Backtick identifiers: `` `table_name` ``
- MySQL comment styles: `/* */` and `-- `
- Escape character handling
- Schema.table notation

## Architecture

The library is organized into several key packages:

- **`model`**: Data models representing SQL components (SqlInfo, WhereCondition, JoinInfo, etc.)
- **`parser`**: Core parsing interfaces and implementations
- **`exception`**: Custom exceptions for parsing errors
- **`util`**: Utility classes for common operations

### Key Classes

- **`SqlParser`**: Main interface for SQL parsing operations
- **`MySqlPureSqlParser`**: Pure Java implementation for MySQL
- **`SqlInfo`**: Container for parsed SQL information
- **`WhereCondition`**: Detailed WHERE condition representation
- **`JoinInfo`**: JOIN operation information with detailed conditions
- **`FieldInfo`**: Field-level information including table and alias details

## Examples

### Complex Query Parsing

```java
String complexSql = """
    SELECT u.id, u.name, p.title, COUNT(o.id) as order_count
    FROM users u
    LEFT JOIN profiles p ON u.id = p.user_id
    INNER JOIN orders o ON u.id = o.user_id
    WHERE u.age BETWEEN 18 AND 65
      AND u.status IN ('active', 'premium')
      AND p.title IS NOT NULL
    GROUP BY u.id, u.name, p.title
    HAVING COUNT(o.id) > 5
    ORDER BY order_count DESC
    LIMIT 10
    """;

SqlInfo info = parser.parse(complexSql);

// Access all parsed components
System.out.println("Tables: " + info.getTableNames());
System.out.println("JOINs: " + info.getJoinTables().size());
System.out.println("WHERE conditions: " + info.getWhereConditions().size());
System.out.println("GROUP BY: " + info.getGroupByColumns());
System.out.println("ORDER BY: " + info.getOrderByColumns());
```

### Field Information Extraction

```java
// Extract detailed field information
for (WhereCondition condition : info.getWhereConditions()) {
    FieldInfo field = condition.getFieldInfo();
    System.out.printf("Field: %s.%s (alias: %s), Operator: %s, Values: %d%n",
        field.getTableName(),
        field.getFieldName(),
        field.getTableAlias(),
        condition.getOperator(),
        condition.getValueCount()
    );
}
```

## Building from Source

```bash
# Clone the repository
git clone https://github.com/qwzhang01/sql-tool.git
cd sql-tool

# Build with Maven
mvn clean compile

# Run tests
mvn test

# Create JAR
mvn package
```

## Requirements

- Java 17 or higher
- Maven 3.6+ (for building)

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Changelog

### Version 1.0.0
- Initial release
- Pure Java SQL parsing implementation
- Support for SELECT, INSERT, UPDATE, DELETE statements
- Comprehensive WHERE condition parsing
- JOIN operation analysis
- SQL cleaning and normalization
- Field-level information extraction

## Support

If you encounter any issues or have questions, please [open an issue](https://github.com/qwzhang01/sql-tool/issues) on GitHub.