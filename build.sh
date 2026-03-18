#!/usr/bin/env bash
# ============================================================
# LazyBukkit Build Script
# Produces a single runnable server jar: lazybukkit.jar
#
# Usage:
#   ./build.sh              Full build → target/lazybukkit.jar
#   ./build.sh --skip-tests Skip unit tests
#
# Then run:
#   java -Xmx2G -jar target/lazybukkit.jar nogui
# ============================================================
set -e

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
SRC_DIR="$PROJECT_DIR/src/main/java"
TEST_DIR="$PROJECT_DIR/src/test/java"
CORE_DIR="$PROJECT_DIR/lazybukkit-core"
BOOT_DIR="$PROJECT_DIR/bootstrap"
LIB_DIR="$PROJECT_DIR/lib"
BUILD_DIR="$PROJECT_DIR/target"

# Parse flags
SKIP_TESTS=false
for arg in "$@"; do
    case "$arg" in
        --skip-tests) SKIP_TESTS=true ;;
    esac
done

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

echo -e "${BOLD}${CYAN}"
echo "  _                    ____        _    _    _ _   "
echo " | |    __ _ _____   _| __ ) _   _| | _| | _(_) |_ "
echo " | |   / _\` |_  / | | |  _ \\| | | | |/ / |/ / | __|"
echo " | |__| (_| |/ /| |_| | |_) | |_| |   <|   <| | |_ "
echo " |_____\\__,_/___|\\__, |____/ \\__,_|_|\\_\\_|\\_\\_|\\__|"
echo "                 |___/                              "
echo -e "${NC}"
echo -e "${CYAN}  Build System${NC}"
echo ""

# ----------------------------------------------------------
# Step 1: Check Java
# ----------------------------------------------------------
if ! command -v java &>/dev/null || ! command -v javac &>/dev/null; then
    echo -e "${RED}ERROR: Java JDK not found. Install JDK 17+.${NC}"
    exit 1
fi
JAVA_VER=$(javac -version 2>&1)
echo -e "${GREEN}[OK]${NC} $JAVA_VER"

# ----------------------------------------------------------
# Step 2: Download dependencies
# ----------------------------------------------------------
echo ""
echo -e "${YELLOW}[1/7] Downloading dependencies...${NC}"
mkdir -p "$LIB_DIR"

download() {
    local name="$1" url="$2"
    if [ ! -f "$LIB_DIR/$name" ]; then
        echo "  ↓ $name"
        curl -sL -o "$LIB_DIR/$name" "$url"
    fi
}

# Compile deps (for old Bukkit API source)
download "guava-10.0.1.jar"        "https://repo1.maven.org/maven2/com/google/guava/guava/10.0.1/guava-10.0.1.jar"
download "snakeyaml-1.9.jar"       "https://repo1.maven.org/maven2/org/yaml/snakeyaml/1.9/snakeyaml-1.9.jar"
download "json-simple-1.1.jar"     "https://repo1.maven.org/maven2/com/googlecode/json-simple/json-simple/1.1/json-simple-1.1.jar"
download "commons-lang-2.3.jar"    "https://repo1.maven.org/maven2/commons-lang/commons-lang/2.3/commons-lang-2.3.jar"
download "ebean-2.7.3.jar"         "https://repo1.maven.org/maven2/org/avaje/ebean/2.7.3/ebean-2.7.3.jar"
download "persistence-api-1.0.jar" "https://repo1.maven.org/maven2/javax/persistence/persistence-api/1.0/persistence-api-1.0.jar"

# Test deps
download "junit-4.11.jar"          "https://repo1.maven.org/maven2/junit/junit/4.11/junit-4.11.jar"
download "hamcrest-core-1.3.jar"   "https://repo1.maven.org/maven2/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar"
download "hamcrest-library-1.3.jar" "https://repo1.maven.org/maven2/org/hamcrest/hamcrest-library/1.3/hamcrest-library-1.3.jar"

echo -e "${GREEN}[OK]${NC} Compile dependencies ready"

# ----------------------------------------------------------
# Step 3: Download Paper server
# ----------------------------------------------------------
echo ""
echo -e "${YELLOW}[2/7] Getting Paper server...${NC}"

PAPER_JAR=""
# Find existing paper jar
for f in "$LIB_DIR"/paper-*.jar; do
    if [ -f "$f" ]; then
        PAPER_JAR="$f"
        break
    fi
done

if [ -z "$PAPER_JAR" ]; then
    echo "  Querying Paper API for latest version..."

    # Get latest MC version supported by Paper
    PAPER_VERSION=$(curl -s "https://api.papermc.io/v2/projects/paper" \
        | python3 -c "import sys,json; print(json.load(sys.stdin)['versions'][-1])" 2>/dev/null)

    if [ -z "$PAPER_VERSION" ]; then
        echo -e "${RED}ERROR: Could not query Paper API. Check internet connection.${NC}"
        exit 1
    fi

    # Get latest build for that version
    PAPER_BUILD=$(curl -s "https://api.papermc.io/v2/projects/paper/versions/$PAPER_VERSION/builds" \
        | python3 -c "import sys,json; print(json.load(sys.stdin)['builds'][-1]['build'])" 2>/dev/null)

    PAPER_FILENAME="paper-${PAPER_VERSION}-${PAPER_BUILD}.jar"
    PAPER_JAR="$LIB_DIR/$PAPER_FILENAME"

    echo "  ↓ $PAPER_FILENAME (~50MB)..."
    curl -sL -o "$PAPER_JAR" \
        "https://api.papermc.io/v2/projects/paper/versions/$PAPER_VERSION/builds/$PAPER_BUILD/downloads/$PAPER_FILENAME"

    echo -e "${GREEN}[OK]${NC} Downloaded Paper $PAPER_VERSION (build $PAPER_BUILD)"
else
    echo -e "${GREEN}[OK]${NC} Using $(basename "$PAPER_JAR")"
fi

# ----------------------------------------------------------
# Step 4: Compile LazyBukkit API
# ----------------------------------------------------------
echo ""
echo -e "${YELLOW}[3/7] Compiling LazyBukkit API...${NC}"

rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR/classes" "$BUILD_DIR/test-classes"

COMPILE_CP="$LIB_DIR/guava-10.0.1.jar:$LIB_DIR/snakeyaml-1.9.jar:$LIB_DIR/json-simple-1.1.jar:$LIB_DIR/commons-lang-2.3.jar:$LIB_DIR/ebean-2.7.3.jar:$LIB_DIR/persistence-api-1.0.jar"

find "$SRC_DIR" -name "*.java" > /tmp/lb_sources.txt
SOURCE_COUNT=$(wc -l < /tmp/lb_sources.txt | tr -d ' ')

javac -source 8 -target 8 \
    -cp "$COMPILE_CP" \
    -d "$BUILD_DIR/classes" \
    -Xlint:-options \
    @/tmp/lb_sources.txt 2>&1

echo -e "${GREEN}[OK]${NC} Compiled $SOURCE_COUNT API source files"

# ----------------------------------------------------------
# Step 5: Run tests
# ----------------------------------------------------------
echo ""
if [ "$SKIP_TESTS" = true ]; then
    echo -e "${YELLOW}[4/7] Tests skipped${NC}"
else
    echo -e "${YELLOW}[4/7] Running tests...${NC}"

    TEST_CP="$BUILD_DIR/classes:$LIB_DIR/junit-4.11.jar:$LIB_DIR/hamcrest-core-1.3.jar:$LIB_DIR/hamcrest-library-1.3.jar:$COMPILE_CP"

    find "$TEST_DIR" -name "*.java" -path "*/lazybukkit/*" > /tmp/lb_tests.txt
    TEST_COUNT=$(wc -l < /tmp/lb_tests.txt | tr -d ' ')

    javac -source 8 -target 8 \
        -cp "$TEST_CP" \
        -d "$BUILD_DIR/test-classes" \
        -Xlint:-options \
        @/tmp/lb_tests.txt 2>&1

    # Find test classes
    TEST_CLASSES=""
    while IFS= read -r file; do
        class=$(echo "$file" | sed "s|$TEST_DIR/||" | sed 's|\.java$||' | tr '/' '.')
        TEST_CLASSES="$TEST_CLASSES $class"
    done < <(grep -l "@Test" "$TEST_DIR" -r --include="*.java" --include="*lazybukkit*" \
        | grep "lazybukkit")

    RUN_CP="$BUILD_DIR/classes:$BUILD_DIR/test-classes:$LIB_DIR/junit-4.11.jar:$LIB_DIR/hamcrest-core-1.3.jar:$LIB_DIR/hamcrest-library-1.3.jar:$COMPILE_CP"

    RESULT=$(java -cp "$RUN_CP" org.junit.runner.JUnitCore $TEST_CLASSES 2>&1)
    echo "$RESULT" | tail -3

    if echo "$RESULT" | grep -q "FAILURES"; then
        echo -e "${RED}[FAIL] Tests failed!${NC}"
        exit 1
    fi
    echo -e "${GREEN}[OK]${NC} $TEST_COUNT test files passed"
fi

# ----------------------------------------------------------
# Step 6: Build core plugin
# ----------------------------------------------------------
echo ""
echo -e "${YELLOW}[5/7] Building core plugin...${NC}"

CORE_CLASSES="$BUILD_DIR/core-classes"
mkdir -p "$CORE_CLASSES"

find "$CORE_DIR/src/main/java" -name "*.java" > /tmp/lb_core.txt
CORE_COUNT=$(wc -l < /tmp/lb_core.txt | tr -d ' ')

javac -source 8 -target 8 \
    -cp "$BUILD_DIR/classes:$COMPILE_CP" \
    -d "$CORE_CLASSES" \
    -Xlint:-options \
    @/tmp/lb_core.txt 2>&1

# Copy resources (plugin.yml etc.)
cp -r "$CORE_DIR/src/main/resources"/* "$CORE_CLASSES/" 2>/dev/null || true

# Include LazyBukkit API classes inside the plugin jar
# (so Paper can load them at runtime)
cp -r "$BUILD_DIR/classes/org/bukkit/lazybukkit" "$CORE_CLASSES/org/bukkit/"

# Package core plugin
(cd "$CORE_CLASSES" && jar cf "$BUILD_DIR/LazyBukkit-Core.jar" .)

CORE_SIZE=$(ls -lh "$BUILD_DIR/LazyBukkit-Core.jar" | awk '{print $5}')
echo -e "${GREEN}[OK]${NC} Core plugin: $CORE_SIZE ($CORE_COUNT source files)"

# ----------------------------------------------------------
# Step 7: Compile bootstrap
# ----------------------------------------------------------
echo ""
echo -e "${YELLOW}[6/7] Building bootstrap...${NC}"

BOOT_CLASSES="$BUILD_DIR/boot-classes"
mkdir -p "$BOOT_CLASSES"

find "$BOOT_DIR/src/main/java" -name "*.java" > /tmp/lb_boot.txt

javac -source 8 -target 8 \
    -d "$BOOT_CLASSES" \
    -Xlint:-options \
    @/tmp/lb_boot.txt 2>&1

echo -e "${GREEN}[OK]${NC} Bootstrap compiled"

# ----------------------------------------------------------
# Step 8: Package lazybukkit.jar
# ----------------------------------------------------------
echo ""
echo -e "${YELLOW}[7/7] Packaging lazybukkit.jar...${NC}"

FINAL_DIR="$BUILD_DIR/lazybukkit-jar"
mkdir -p "$FINAL_DIR/server"

# Bootstrap classes
cp -r "$BOOT_CLASSES"/* "$FINAL_DIR/"

# Embed Paper server
cp "$PAPER_JAR" "$FINAL_DIR/server/paper.jar"

# Embed core plugin
cp "$BUILD_DIR/LazyBukkit-Core.jar" "$FINAL_DIR/server/LazyBukkit-Core.jar"

# Manifest
mkdir -p "$FINAL_DIR/META-INF"
cat > "$FINAL_DIR/META-INF/MANIFEST.MF" <<MANIFEST
Manifest-Version: 1.0
Main-Class: org.bukkit.lazybukkit.bootstrap.LazyBukkitBootstrap
Implementation-Title: LazyBukkit
Implementation-Version: 1.0.0
Built-By: $(whoami)
Build-Date: $(date -u '+%Y-%m-%dT%H:%M:%SZ')
MANIFEST

# Build the final jar
(cd "$FINAL_DIR" && jar cfm "$BUILD_DIR/lazybukkit.jar" META-INF/MANIFEST.MF .)

# Cleanup staging dir
rm -rf "$FINAL_DIR"

FINAL_SIZE=$(ls -lh "$BUILD_DIR/lazybukkit.jar" | awk '{print $5}')

echo -e "${GREEN}[OK]${NC} Built lazybukkit.jar ($FINAL_SIZE)"

# ----------------------------------------------------------
# Done
# ----------------------------------------------------------
echo ""
echo -e "${BOLD}${CYAN}========================================${NC}"
echo -e "${BOLD}${CYAN}  Build Complete!${NC}"
echo -e "${CYAN}========================================${NC}"
echo ""
echo -e "  ${BOLD}target/lazybukkit.jar${NC} ($FINAL_SIZE)"
echo ""
echo -e "  Run the server:"
echo -e "    ${CYAN}java -Xmx2G -jar target/lazybukkit.jar nogui${NC}"
echo ""
echo -e "${CYAN}========================================${NC}"
