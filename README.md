# SQL Tool

A comprehensive SQL parsing and analysis tool for Java applications, providing powerful capabilities for SQL statement parsing, analysis, and manipulation.

## Features

### Core Functionality
- **SQL Parsing**: Parse various SQL statements (SELECT, INSERT, UPDATE, DELETE)
- **SQL Analysis**: Extract detailed information about tables, fields, conditions, and parameters
- **SQL Cleaning**: Remove comments and normalize SQL formatting
- **Parameter Mapping**: Generate parameter mappings for prepared statements
- **Join Analysis**: Parse and analyze JOIN operations and relationships

### Supported SQL Operations
- **SELECT**: Complete SELECT statement parsing with support for:
  - Field selection and aliases
  - Table joins (INNER, LEFT, RIGHT, FULL OUTER)
  - WHERE conditions with complex operators
  - GROUP BY and HAVING clauses
  - ORDER BY with ASC/DESC
  - LIMIT and OFFSET
- **INSERT**: INSERT statement parsing with field-value mapping
- **UPDATE**: UPDATE statement parsing with SET clauses and WHERE conditions
- **DELETE**: DELETE statement parsing with WHERE conditions

### Advanced Features
- **Operator Support**: Comprehensive operator support including:
  - Comparison operators (=, !=, <, >, <=, >=)
  - Range operators (BETWEEN, NOT BETWEEN)
  - List operators (IN, NOT IN)
  - Pattern matching (LIKE, NOT LIKE)
  - NULL checks (IS NULL, IS NOT NULL)
- **Table Alias Resolution**: Automatic table alias detection and resolution
- **Field Type Classification**: Categorize fields by usage (SELECT, CONDITION, INSERT, UPDATE_SET)
- **Parameter Counting**: Accurate parameter count calculation for prepared statements

## Project Structure

```
sql-tool/
├── src/main/java/io/github/qwzhang01/sql/tool/
│   ├── enums/           # Enumeration classes
│   │   ├── FieldType.java      # Field type definitions
│   │   ├── OperatorType.java   # SQL operator types
│   │   ├── SqlType.java        # SQL statement types
│   │   └── TableType.java      # Table type classifications
│   ├── helper/          # Helper utilities
│   │   ├── SqlGatherHelper.java    # Main SQL analysis helper
│   │   └── SqlParseHelper.java     # SQL parsing utilities
│   ├── model/           # Data models
│   │   ├── SqlCondition.java   # SQL condition representation
│   │   ├── SqlField.java       # SQL field information
│   │   ├── SqlGather.java      # Comprehensive SQL analysis result
│   │   ├── SqlJoin.java        # JOIN operation details
│   │   ├── SqlObj.java         # Base SQL object
│   │   ├── SqlParam.java       # SQL parameter information
│   │   └── SqlTable.java       # Table information
│   └── parser/          # SQL parsers
│       ├── MySqlPureSqlParser.java # MySQL-specific SQL parser
│       ├── MySqlSqlCleaner.java    # SQL cleaning utilities
│       └── MySqlSqlCompare.java    # SQL comparison utilities
└── src/test/java/       # Test cases
    └── io/github/qwzhang01/sql/tool/
        ├── helper/      # Helper class tests
        └── parser/      # Parser tests
```

## Usage Examples

### Basic SQL Analysis

```java
import io.github.qwzhang01.sql.tool.helper.SqlGatherHelper;
import io.github.qwzhang01.sql.tool.model.SqlGather;

// Analyze a SELECT statement
String sql = "SELECT u.name, u.email FROM users u WHERE u.age > ? AND u.status = ?";
SqlGather analysis = SqlGatherHelper.analysis(sql);

// Get table information
List<SqlGather.Table> tables = analysis.getTables();
System.out.

println("Main table: "+tables.get(0).

tableName());

// Get field conditions
List<SqlGather.Field> conditions = analysis.getConditions();
for(
SqlGather.Field condition :conditions){
        System.out.

println("Field: "+condition.columnName() +
        ", Operator: "+condition.

operatorType() +
        ", Param Count: "+condition.

paramCount());
        }

// Get parameter mappings
List<SqlGather.ParameterFieldMapping> parameters = analysis.getParameterMappings();
for(
SqlGather.ParameterFieldMapping param :parameters){
        System.out.

println("Parameter "+param.index() +
        " -> "+param.

tableName() +"."+param.

fieldName());
        }
```

### Parameter Extraction

```java
import io.github.qwzhang01.sql.tool.helper.SqlGatherHelper;
import io.github.qwzhang01.sql.tool.model.SqlParam;

String sql = "INSERT INTO users (name, email, age) VALUES (?, ?, ?)";
List<SqlParam> params = SqlGatherHelper.param(sql);

for (SqlParam param : params) {
    System.out.println("Field: " + param.getFieldName() + 
                      ", Table: " + param.getTableName());
}
```

### SQL Cleaning

```java
import io.github.qwzhang01.sql.tool.parser.MySqlSqlCleaner;

MySqlSqlCleaner cleaner = new MySqlSqlCleaner();
String dirtySql = "SELECT * FROM users /* this is a comment */ WHERE age > 18 -- another comment";
String cleanSql = cleaner.cleanSql(dirtySql);
System.out.println("Clean SQL: " + cleanSql);
// Output: SELECT * FROM users WHERE age > 18
```

## Supported SQL Patterns

### SELECT Statements
- Simple SELECT: `SELECT * FROM table`
- Field selection: `SELECT field1, field2 FROM table`
- Table aliases: `SELECT t.field FROM table t` or `SELECT t.field FROM table AS t`
- JOINs: `SELECT * FROM table1 t1 JOIN table2 t2 ON t1.id = t2.id`
- Complex WHERE: `SELECT * FROM table WHERE field BETWEEN ? AND ? OR field IN (?, ?, ?)`

### INSERT Statements
- Simple INSERT: `INSERT INTO table (field1, field2) VALUES (?, ?)`
- Table aliases: `INSERT INTO table t (field1, field2) VALUES (?, ?)`

### UPDATE Statements
- Simple UPDATE: `UPDATE table SET field1 = ? WHERE field2 = ?`
- Table aliases: `UPDATE table t SET t.field1 = ? WHERE t.field2 = ?`

### DELETE Statements
- Simple DELETE: `DELETE FROM table WHERE field = ?`
- Table aliases: `DELETE FROM table t WHERE t.field = ?`

## Testing

The project includes comprehensive test suites covering:

- **SqlGatherHelperTest**: Tests for the main analysis functionality
- **MySqlPureSqlParserTest**: Tests for SQL parsing logic
- **MySqlSqlCleanerTest**: Tests for SQL cleaning operations
- **WhereTest**: Tests for WHERE clause parsing
- **JoinTest**: Tests for JOIN operation parsing

Run tests using Maven:
```bash
mvn test
```

## Requirements

- Java 17 or higher
- Maven 3.6 or higher

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## Support

For questions, issues, or contributions, please visit the project repository or create an issue.