@echo off
setlocal

set TARGET_DIR="%2"
if not exist %TARGET_DIR% mkdir %TARGET_DIR%
pushd %TARGET_DIR%

set ASSETS=%1\src\main\assets
set ASSETSD=%1\src\debug\assets
set BASE_NAME=test-db
set TARGET="%BASE_NAME%-concatenated.sql"
set NOW=STRFTIME('%%Y-%%m-%%d %%H:%%M:%%f', 'NOW')

echo. > %TARGET%
echo -- Built from from %ASSETS% >> %TARGET%
echo -- http://stackoverflow.com/questions/2421189/version-of-sqlite-used-in-android >> %TARGET%
echo PRAGMA foreign_keys=ON; >> %TARGET%
echo PRAGMA recursive_triggers = TRUE; -- This is not possible before 3.6.18 >> %TARGET%

echo select %NOW%, 'Initialize once'; >> %TARGET%
echo select %NOW%, 'clean.sql'; >> %TARGET%
type "%ASSETS%\MagicHomeInventory.clean.sql" >> %TARGET%
echo select %NOW%, 'schema.sql'; >> %TARGET%
type "%ASSETS%\MagicHomeInventory.schema.sql" >> %TARGET%
echo select %NOW%, 'data.sql'; >> %TARGET%
type "%ASSETS%\MagicHomeInventory.data.sql" >> %TARGET%
echo select %NOW%, 'data.Categories.sql'; >> %TARGET%
type "%ASSETS%\MagicHomeInventory.data.Categories.sql" >> %TARGET%
echo select %NOW%, 'init.sql'; >> %TARGET%
type "%ASSETS%\MagicHomeInventory.init.sql" >> %TARGET%
echo select %NOW%, 'development.sql'; >> %TARGET%
type "%ASSETSD%\MagicHomeInventory.development.sql" >> %TARGET%
echo select %NOW%, 'verify.sql'; >> %TARGET%
type "%ASSETSD%\MagicHomeInventory.verify.sql" >> %TARGET%
echo select %NOW%, 'test.sql'; >> %TARGET%
type "%ASSETSD%\MagicHomeInventory.test.sql" >> %TARGET%

echo select %NOW%, 'Tear-down and initialize again'; >> %TARGET%
echo select %NOW%, 'clean.sql'; >> %TARGET%
type "%ASSETS%\MagicHomeInventory.clean.sql" >> %TARGET%
echo select %NOW%, 'schema.sql'; >> %TARGET%
type "%ASSETS%\MagicHomeInventory.schema.sql" >> %TARGET%
echo select %NOW%, 'data.sql'; >> %TARGET%
type "%ASSETS%\MagicHomeInventory.data.sql" >> %TARGET%
echo select %NOW%, 'data.Categories.sql'; >> %TARGET%
type "%ASSETS%\MagicHomeInventory.data.Categories.sql" >> %TARGET%
echo select %NOW%, 'init.sql'; >> %TARGET%
type "%ASSETS%\MagicHomeInventory.init.sql" >> %TARGET%
echo select %NOW%, 'development.sql'; >> %TARGET%
type "%ASSETSD%\MagicHomeInventory.development.sql" >> %TARGET%
echo select %NOW%, 'verify.sql'; >> %TARGET%
type "%ASSETSD%\MagicHomeInventory.verify.sql" >> %TARGET%
echo select %NOW%, 'test.sql'; >> %TARGET%
type "%ASSETSD%\MagicHomeInventory.test.sql" >> %TARGET%

echo select %NOW%, '%BASE_NAME%.sql'; >> %TARGET%

echo .backup %BASE_NAME%.sqlite >> %TARGET%

type "%~dp0test-db.sql" >> %TARGET%

sqlite3 -init %TARGET% %3 %4 %5 %6 %7 %8 %9

popd
endlocal
