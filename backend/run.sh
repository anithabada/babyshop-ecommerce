#!/bin/bash
# =========================================================
# BabyShop Backend - Run Script
# Starts the server. Must be run from the backend/ directory
# (or via ./run.sh) so the relative path to ../frontend resolves.
# Make sure you've run ./build.sh at least once first, and that
# MySQL is running with the babyshop_db database created
# (see ../README.md for setup instructions).
# =========================================================
set -e
cd "$(dirname "$0")"

if [ ! -d "bin" ]; then
  echo "No build found. Running build.sh first..."
  ./build.sh
fi

echo "Starting BabyShop backend on http://localhost:8080 ..."
java -cp "bin;lib/*" com.babyshop.Main
