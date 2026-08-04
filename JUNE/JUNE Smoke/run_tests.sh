#!/bin/bash

# ===================================================
#   Trexo Test Automation Unix/Linux Execution Runner
# ===================================================

# Set defaults
SUITE_XML_FILE="testng.xml"
BROWSER="chrome"
TEST_ENV="qa"
HEADLESS="true"

# Parse key=value arguments
for arg in "$@"
do
    case $arg in
        suite=*)
        SUITE_XML_FILE="${arg#*=}"
        shift
        ;;
        browser=*)
        BROWSER="${arg#*=}"
        shift
        ;;
        env=*)
        TEST_ENV="${arg#*=}"
        shift
        ;;
        headless=*)
        HEADLESS="${arg#*=}"
        shift
        ;;
        *)
        # Unknown option
        ;;
    esac
done

echo "==================================================="
echo "  Trexo Test Automation Execution Configuration"
echo "==================================================="
echo "  - TestNG Suite XML: $SUITE_XML_FILE"
echo "  - Browser:           $BROWSER"
echo "  - Environment:       $TEST_ENV"
echo "  - Headless:          $HEADLESS"
echo "==================================================="
echo ""

# Locate Maven
if command -v mvn >/dev/null 2>&1; then
    echo "Global 'mvn' command found."
    MAVEN_CMD="mvn"
else
    echo "Global 'mvn' not found. Checking for bundled Maven distribution..."
    SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
    BUNDLED_MVN="$SCRIPT_DIR/apache-maven-3.9.12-bin/apache-maven-3.9.12/bin/mvn"
    
    if [ -f "$BUNDLED_MVN" ]; then
        echo "Bundled Maven found at: $BUNDLED_MVN"
        chmod +x "$BUNDLED_MVN"
        MAVEN_CMD="$BUNDLED_MVN"
    else
        echo "ERROR: Maven was not found globally or in the bundled directory."
        echo "Please install Maven or ensure it is in your PATH."
        exit 1
    fi
fi

# Run the command
echo "Running Maven command:"
echo "$MAVEN_CMD clean test -DsuiteXmlFile=$SUITE_XML_FILE -Dbrowser=$BROWSER -Denv=$TEST_ENV -Dheadless=$HEADLESS"
echo ""

"$MAVEN_CMD" clean test -DsuiteXmlFile="$SUITE_XML_FILE" -Dbrowser="$BROWSER" -Denv="$TEST_ENV" -Dheadless="$HEADLESS"
EXIT_CODE=$?

if [ $EXIT_CODE -eq 0 ]; then
    echo ""
    echo "Execution successful!"
    exit 0
else
    echo ""
    echo "Execution failed with exit code $EXIT_CODE!"
    exit $EXIT_CODE
fi
