.PHONY: help build test install-dev uninstall-dev clean

JAR_NAME := voxcompose-1.0.0-all.jar
JAR_PATH := build/libs/$(JAR_NAME)
DEV_BIN := $(HOME)/.local/bin
DEV_SCRIPT := $(DEV_BIN)/voxcompose-dev

help:
	@echo "VoxCompose - Make Targets"
	@echo "========================="
	@echo ""
	@echo "  make build        - Build fat JAR"
	@echo "  make test         - Run all tests (Java unit + shell integration)"
	@echo "  make install-dev  - Build and install dev version to ~/.local/bin"
	@echo "  make uninstall-dev - Remove dev version"
	@echo "  make clean        - Clean build artifacts"
	@echo ""

build:
	@echo "Building fat JAR..."
	@./gradlew --no-daemon clean fatJar -q
	@echo "Built: $(JAR_PATH)"

test:
	@echo "Running Java unit tests..."
	@./gradlew test --no-daemon
	@echo ""
	@echo "Running shell integration tests..."
	@./tests/run_tests.sh

install-dev: build
	@echo "Installing dev version..."
	@mkdir -p $(DEV_BIN)
	@echo '#!/bin/bash' > $(DEV_SCRIPT)
	@echo 'exec java -jar "$(CURDIR)/$(JAR_PATH)" "$$@"' >> $(DEV_SCRIPT)
	@chmod +x $(DEV_SCRIPT)
	@echo "Installed: $(DEV_SCRIPT)"
	@echo ""
	@if ! echo "$$PATH" | grep -q "$(DEV_BIN)"; then \
		echo "NOTE: Add to PATH: export PATH=\"$(DEV_BIN):\$$PATH\""; \
	fi
	@echo "Test with: voxcompose-dev --help"

uninstall-dev:
	@rm -f $(DEV_SCRIPT)
	@echo "Removed: $(DEV_SCRIPT)"

clean:
	@./gradlew clean -q
	@echo "Clean complete"
