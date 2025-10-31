Horse Racing Database System (Java + MySQL)

This project implements a simple Horse Racing Database System with Admin and Guest features using MySQL as the datastore and Java as the application runtime.

**Scope**
- Admin
  - Add a new race with results
  - Delete an owner and all related information
  - Move a horse between stables (by horse ID)
  - Approve a new trainer to join a stable
- Guest
  - Browse names/ages of horses and their trainer by owner last name
  - Browse trainers who trained winners, including winning horse and race details
  - Browse trainers and total prize winnings, sorted by winnings
  - List tracks with count of races and total participating horses
- Additional
  - Use appropriate API(s)
  - Procedural SQL:
    - Stored procedure to delete an owner and related information
    - Trigger to copy horse info to `old_info` whenever a horse is deleted

**Database Schema**
- MySQL latest version recommended.
- DDL is provided in `src/main/resources/db/schema.sql:1`.
- Stored procedure is provided in `src/main/resources/db/procedures.sql:1`.
- Trigger is provided in `src/main/resources/db/triggers.sql:1`.

Notes on DDL fidelity and fixes:
- The supplied schema used differing sizes for `stableId` across tables and `gender char` without a length. In MySQL these cause foreign key/type issues. The SQL files here standardize `stableId` to `VARCHAR(15)` everywhere and use `CHAR(1)` for `gender`. Otherwise the structure is unchanged.

**Setup: MySQL**
- Create and load schema, procedure, and trigger:
  - Windows PowerShell:
    - `mysql -u root -p -e "source src\\main\\resources\\db\\schema.sql; source src\\main\\resources\\db\\procedures.sql; source src\\main\\resources\\db\\triggers.sql;"`
  - macOS/Linux:
    - `mysql -u root -p -e "source src/main/resources/db/schema.sql; source src/main/resources/db/procedures.sql; source src/main/resources/db/triggers.sql;"`

**Project Structure**
- Java (Maven) project with main class at `src/main/java/org/yourcompany/yourproject/Hours_Racing_Java.java:1`.
- Database assets under `src/main/resources/db/`.

**Next Implementation Steps (recommendation)**
- Add MySQL JDBC dependency and a `Database` helper for connections.
- Build a small console UI with Admin/Guest menus that call service methods.
- Implement DAO/service methods that issue SQL for each requirement.
- Add simple input validation and result formatting.

If you want, I can scaffold the JDBC config, DAO/service interfaces, and a console menu to start wiring features.

**RacingFX (JavaFX) App**
- A JavaFX starter app is scaffolded under `racingfx` using your POM.
- Run it:
  - Windows PowerShell/CMD: `cd racingfx && mvn -q javafx:run`
  - macOS/Linux: `cd racingfx && mvn -q javafx:run`
- Main class: `racingfx/src/main/java/com/example/racingfx/MainApp.java:1`
- JDK: 17 (as per `racingfx/pom.xml:1`). Keep your system JDK 17 active for this subproject.

**RacingFX Packages (Step 3)**
- `racingfx/src/main/java/com/example/racingfx/db/Database.java:1` — JDBC helper using env vars `RACING_DB_URL|USER|PASS` or defaults.
- `racingfx/src/main/java/com/example/racingfx/dao/` — DAOs
  - `AdminDao.java:1`, `AdminDaoImpl.java:1` — add race+results, delete owner+related, move horse, approve trainer.
  - `GuestDao.java:1`, `GuestDaoImpl.java:1` — horses by owner last name, winning trainers, trainer winnings, track stats.
- `racingfx/src/main/java/com/example/racingfx/model/` — POJOs for UI/view models.
- `racingfx/src/main/java/com/example/racingfx/ui/` — UI skeleton
  - `MainView.java:1` (TabPane), `AdminTab.java:1`, `GuestTab.java:1` (forms/tables placeholders).

Note: `MainApp` still shows the Step 2 DB test button. We can switch it to load `MainView` when you’re ready.
