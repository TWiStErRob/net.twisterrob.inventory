@echo off
set ASSETS=src\main\assets
set ASSETSD=src\debug\assets
set TARGET=test-db-concatenated.sql
set NOW=STRFTIME('%%Y-%%m-%%d %%H:%%M:%%f', 'NOW')
echo. > %TARGET%
echo -- http://stackoverflow.com/questions/2421189/version-of-sqlite-used-in-android >> %TARGET%
echo PRAGMA foreign_keys=ON; >> %TARGET%
echo PRAGMA recursive_triggers = TRUE; -- This is not possible before 3.6.18 >> %TARGET%

echo select %NOW%, 'Initialize once'; >> %TARGET%
echo select %NOW%, 'clean.sql'; >> %TARGET%
type %ASSETS%\MagicHomeInventory.clean.sql >> %TARGET%
echo select %NOW%, 'schema.sql'; >> %TARGET%
type %ASSETS%\MagicHomeInventory.schema.sql >> %TARGET%
echo select %NOW%, 'data.sql'; >> %TARGET%
type %ASSETS%\MagicHomeInventory.data.sql >> %TARGET%
echo select %NOW%, 'development.sql'; >> %TARGET%
type %ASSETSD%\MagicHomeInventory.development.sql >> %TARGET%
echo select %NOW%, 'verify.sql'; >> %TARGET%
type %ASSETSD%\MagicHomeInventory.verify.sql >> %TARGET%
echo select %NOW%, 'test.sql'; >> %TARGET%
type %ASSETSD%\MagicHomeInventory.test.sql >> %TARGET%

echo select %NOW%, 'Tear-down and initialize again'; >> %TARGET%
echo select %NOW%, 'clean.sql'; >> %TARGET%
type %ASSETS%\MagicHomeInventory.clean.sql >> %TARGET%
echo select %NOW%, 'schema.sql'; >> %TARGET%
type %ASSETS%\MagicHomeInventory.schema.sql >> %TARGET%
echo select %NOW%, 'data.sql'; >> %TARGET%
type %ASSETS%\MagicHomeInventory.data.sql >> %TARGET%
echo select %NOW%, 'development.sql'; >> %TARGET%
type %ASSETSD%\MagicHomeInventory.development.sql >> %TARGET%
echo select %NOW%, 'verify.sql'; >> %TARGET%
type %ASSETSD%\MagicHomeInventory.verify.sql >> %TARGET%
echo select %NOW%, 'test.sql'; >> %TARGET%
type %ASSETSD%\MagicHomeInventory.test.sql >> %TARGET%

echo select %NOW%, 'test-db.sql'; >> %TARGET%
type test-db.sql >> %TARGET%
sqlite3 -init %TARGET% %*
