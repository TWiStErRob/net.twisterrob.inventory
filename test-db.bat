@echo off
set ASSETS=src\main\assets
set TARGET=test-db-concatenated.sql
echo. > %TARGET%
echo -- http://stackoverflow.com/questions/2421189/version-of-sqlite-used-in-android >> %TARGET%
echo PRAGMA foreign_keys=ON; >> %TARGET%
echo PRAGMA recursive_triggers = TRUE; -- This is not possible before 3.6.18 >> %TARGET%
echo select current_timestamp, 'clean.sql'; >> %TARGET%
type %ASSETS%\MagicHomeInventory.clean.sql >> %TARGET%
echo select current_timestamp, 'schema.sql'; >> %TARGET%
type %ASSETS%\MagicHomeInventory.schema.sql >> %TARGET%
echo select current_timestamp, 'data.sql'; >> %TARGET%
type %ASSETS%\MagicHomeInventory.data.sql >> %TARGET%
echo select current_timestamp, 'development.sql'; >> %TARGET%
type %ASSETS%\MagicHomeInventory.development.sql >> %TARGET%
echo select current_timestamp, 'test-db.sql'; >> %TARGET%
type test-db.sql >> %TARGET%
sqlite3 -init %TARGET% %*
