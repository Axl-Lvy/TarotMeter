#!/bin/bash

# Script to check that all translation files have the same keys in the same order
# as the base strings.xml file

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Path to the composeResources directory
COMPOSE_RESOURCES_DIR="composeApp/src/commonMain/composeResources"
BASE_STRINGS_FILE="${COMPOSE_RESOURCES_DIR}/values/strings.xml"

# Check if base strings.xml exists
if [ ! -f "$BASE_STRINGS_FILE" ]; then
    echo -e "${RED}Error: Base strings.xml not found at ${BASE_STRINGS_FILE}${NC}"
    exit 1
fi

echo -e "${GREEN}Checking translation files...${NC}"
echo "Base file: ${BASE_STRINGS_FILE}"
echo ""

# Extract string keys from an XML file (in order)
extract_keys() {
    local file=$1
    grep -oP '(?<=<string name=")[^"]*' "$file" || true
}

# Get keys from base file
base_keys=$(extract_keys "$BASE_STRINGS_FILE")
base_keys_count=$(echo "$base_keys" | wc -l)

echo -e "${YELLOW}Base file contains ${base_keys_count} string keys${NC}"
echo ""

# Track if any errors were found
has_errors=0

# Find all values-* directories
for dir in "${COMPOSE_RESOURCES_DIR}"/values-*; do
    if [ -d "$dir" ]; then
        lang_code=$(basename "$dir")
        strings_file="${dir}/strings.xml"

        if [ ! -f "$strings_file" ]; then
            echo -e "${RED}✗ ${lang_code}: strings.xml not found${NC}"
            has_errors=1
            continue
        fi

        echo "Checking ${lang_code}/strings.xml..."

        # Extract keys from translation file
        translation_keys=$(extract_keys "$strings_file")
        translation_keys_count=$(echo "$translation_keys" | wc -l)

        # Check if the number of keys matches
        if [ "$base_keys_count" -ne "$translation_keys_count" ]; then
            echo -e "${RED}✗ ${lang_code}: Key count mismatch (expected ${base_keys_count}, got ${translation_keys_count})${NC}"
            has_errors=1
            continue
        fi

        # Check if keys are in the same order
        diff_output=$(diff <(echo "$base_keys") <(echo "$translation_keys") || true)

        if [ -n "$diff_output" ]; then
            echo -e "${RED}✗ ${lang_code}: Keys do not match or are in different order${NC}"
            echo -e "${YELLOW}Differences:${NC}"
            echo "$diff_output"
            echo ""
            has_errors=1
        else
            echo -e "${GREEN}✓ ${lang_code}: All keys match and are in correct order${NC}"
        fi

        echo ""
    fi
done

# Final result
if [ $has_errors -eq 0 ]; then
    echo -e "${GREEN}✓ All translation files are valid!${NC}"
    exit 0
else
    echo -e "${RED}✗ Translation validation failed!${NC}"
    exit 1
fi
