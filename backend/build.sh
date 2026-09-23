#!/bin/bash
# =========================================================
# BabyShop Backend - Build Script
# Compiles all Java source files into the bin/ directory.
# Run this once (or whenever you change the Java code) before ./run.sh
# =========================================================
set -e
cd "$(dirname "$0")"

echo "Compiling BabyShop backend..."
rm -rf bin
mkdir -p bin

find src -name "*.java" > sources.txt
javac -cp "lib/*" -d bin @sources.txt
rm sources.txt

echo "Build complete. Class files are in ./bin"
echo "Run the app with: ./run.sh"
