#!/bin/bash
set -u

# Vehicle Rental-style local defaults. Edit these if your MySQL differs.
MYSQL_USER="root"
MYSQL_PASSWORD="root"
MYSQL_HOST="127.0.0.1"
MYSQL_PORT="3306"
DATABASE_NAME="railconnect"

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"

if command -v mysql >/dev/null 2>&1; then
  MYSQL_COMMAND="$(command -v mysql)"
elif [ -x "/opt/homebrew/bin/mysql" ]; then
  MYSQL_COMMAND="/opt/homebrew/bin/mysql"
elif [ -x "/usr/local/bin/mysql" ]; then
  MYSQL_COMMAND="/usr/local/bin/mysql"
else
  echo "MySQL was not found. Install MySQL Server and start its service first."
  read -r -p "Press Enter to close..."
  exit 1
fi

echo "Creating database ${DATABASE_NAME} if it does not exist..."
if ! "${MYSQL_COMMAND}" --host="${MYSQL_HOST}" --port="${MYSQL_PORT}" --user="${MYSQL_USER}" --password="${MYSQL_PASSWORD}" --execute="CREATE DATABASE IF NOT EXISTS ${DATABASE_NAME};"; then
  echo "Could not connect to MySQL. Check that MySQL is running and the credentials at the top of this file are correct."
  read -r -p "Press Enter to close..."
  exit 1
fi

echo "Starting RailConnect at http://localhost:8080 ..."
cd "${PROJECT_ROOT}"
if ! command -v mvn >/dev/null 2>&1; then
  echo "Maven was not found. Install Maven or start the project from your IDE."
  read -r -p "Press Enter to close..."
  exit 1
fi

mvn spring-boot:run
exit_code=$?
if [ "${exit_code}" -ne 0 ]; then
  echo "RailConnect stopped with exit code ${exit_code}."
  read -r -p "Press Enter to close..."
fi
exit "${exit_code}"
