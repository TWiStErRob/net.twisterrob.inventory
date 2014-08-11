@echo off
echo PRAGMA foreign_keys=ON; > run.sql
echo PRAGMA recursive_triggers = TRUE; -- This is not possible before 3.6.18 > run.sql
echo select current_timestamp, 'clean.sql'; >> run.sql
type assets\MagicHomeInventory.clean.sql >> run.sql
echo select current_timestamp, 'schema.sql'; >> run.sql
type assets\MagicHomeInventory.v1.schema.sql >> run.sql
echo select current_timestamp, 'data.sql'; >> run.sql
type assets\MagicHomeInventory.v1.data.sql >> run.sql
echo select current_timestamp, 'development.sql'; >> run.sql
type assets\MagicHomeInventory.development.sql >> run.sql
echo select current_timestamp, 'test-db.sql'; >> run.sql
type test-db.sql >> run.sql
sqlite3 -init run.sql
