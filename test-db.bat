@echo off
echo PRAGMA foreign_keys=ON; > test-db-concatenated.sql
echo PRAGMA recursive_triggers = TRUE; -- This is not possible before 3.6.18 > test-db-concatenated.sql
echo select current_timestamp, 'clean.sql'; >> test-db-concatenated.sql
type assets\MagicHomeInventory.clean.sql >> test-db-concatenated.sql
echo select current_timestamp, 'schema.sql'; >> test-db-concatenated.sql
type assets\MagicHomeInventory.schema.sql >> test-db-concatenated.sql
echo select current_timestamp, 'data.sql'; >> test-db-concatenated.sql
type assets\MagicHomeInventory.data.sql >> test-db-concatenated.sql
echo select current_timestamp, 'development.sql'; >> test-db-concatenated.sql
type assets\MagicHomeInventory.development.sql >> test-db-concatenated.sql
echo select current_timestamp, 'test-db.sql'; >> test-db-concatenated.sql
type test-db.sql >> test-db-concatenated.sql
sqlite3 -init test-db-concatenated.sql
