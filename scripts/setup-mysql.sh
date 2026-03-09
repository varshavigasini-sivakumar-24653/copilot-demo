#!/bin/bash
# Run MySQL schema for Expense Tracker.
# Ensure MySQL is running first: e.g. brew services start mysql (macOS) or sudo systemctl start mysql (Linux).

set -e
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SCHEMA="${SCRIPT_DIR}/../backend/src/main/resources/schema.sql"

if [ ! -f "$SCHEMA" ]; then
  echo "Schema not found: $SCHEMA"
  exit 1
fi

echo "Running schema: $SCHEMA"
mysql -u root -p < "$SCHEMA"
echo "Done. Database 'expensetracker' and tables created."
