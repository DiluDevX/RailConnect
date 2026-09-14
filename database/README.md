# Database setup

The canonical MySQL schema and demo data are versioned Flyway migrations in:

- `src/main/resources/db/migration/V1__create_railconnect_schema.sql`
- `src/main/resources/db/migration/V2__seed_demo_data.sql`
- `src/main/resources/db/migration/V3__correct_demo_password_hash.sql`

The recommended setup is `docker compose up -d db` followed by
`mvn spring-boot:run`. Flyway applies missing versions automatically and records them
in `flyway_schema_history`.

If importing manually from the repository root with the MySQL command-line client:

```sql
SOURCE src/main/resources/db/migration/V1__create_railconnect_schema.sql;
SOURCE src/main/resources/db/migration/V2__seed_demo_data.sql;
SOURCE src/main/resources/db/migration/V3__correct_demo_password_hash.sql;
```

Do not edit an applied migration. Add `V4__description.sql`, then V5, and so on.
This preserves repeatable database history for all six members.

