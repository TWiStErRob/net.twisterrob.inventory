Small module to hold [generated code](build/generated-src/antlr/main) for SQLite3 grammar so that .sql files are parsable.

It is necessary to [patch](src/main/antlr/SQLiteParser.g4.patch) the Grammar a bit, so that it can parse SQL files in this project.
For more details see https://sqlite.org/forum/forumpost/77468323b2c68c39.

ANTLR Playground: http://lab.antlr.org/
