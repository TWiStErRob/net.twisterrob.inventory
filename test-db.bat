@echo off
set ASSETS=src\main\assets
echo PRAGMA foreign_keys=ON; > test-db-concatenated.sql
echo PRAGMA recursive_triggers = TRUE; -- This is not possible before 3.6.18 > test-db-concatenated.sql
echo select current_timestamp, 'clean.sql'; >> test-db-concatenated.sql
type %ASSETS%\MagicHomeInventory.clean.sql >> test-db-concatenated.sql
echo select current_timestamp, 'schema.sql'; >> test-db-concatenated.sql
type %ASSETS%\MagicHomeInventory.schema.sql >> test-db-concatenated.sql
echo select current_timestamp, 'data.sql'; >> test-db-concatenated.sql
type %ASSETS%\MagicHomeInventory.data.sql >> test-db-concatenated.sql
echo select current_timestamp, 'development.sql'; >> test-db-concatenated.sql
type %ASSETS%\MagicHomeInventory.development.sql >> test-db-concatenated.sql
echo select current_timestamp, 'test-db.sql'; >> test-db-concatenated.sql
type test-db.sql >> test-db-concatenated.sql
sqlite3 -init test-db-concatenated.sql %*
