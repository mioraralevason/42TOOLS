#!/bin/bash

# Configuration
PYTHON_SCRIPT="/home/mralevas/Documents/GitHub/42TOOLS/SCRIPT/import_42.py"
VENV_PATH="/home/mralevas/Documents/GitHub/42TOOLS/SCRIPT/venv"
LOG_FILE="/home/mralevas/logfile.log"

# Log function for consistent logging
log() {
    echo "$(date '+%Y-%m-%d %H:%M:%S') - $1" >> "$LOG_FILE"
}

# Check if PostgreSQL is running
log "Checking PostgreSQL service..."
if ! pg_isready -h localhost -p 5432 > /dev/null 2>&1; then
    log "PostgreSQL is not running. Attempting to start..."
    sudo systemctl start postgresql
    if [ $? -ne 0 ]; then
        log "Failed to start PostgreSQL."
        exit 1
    fi
fi

# Install system dependencies
log "Installing system dependencies..."
sudo apt update
sudo apt install -y libpq-dev python3-dev build-essential
if [ $? -eq 0 ]; then
    log "System dependencies installed successfully."
else
    log "Error installing system dependencies."
    exit 1
fi

# Activate or create the virtual environment
log "Activating virtual environment..."
if [ -d "$VENV_PATH" ]; then
    source "$VENV_PATH/bin/activate"
else
    log "Virtual environment not found at $VENV_PATH. Creating one..."
    python3 -m venv "$VENV_PATH"
    source "$VENV_PATH/bin/activate"
fi

# Define pip path after venv creation
PIP="$VENV_PATH/bin/pip"

# Install Python dependencies
log "Installing Python dependencies..."
"$PIP" install --upgrade pip
"$PIP" install psycopg2-binary python-dateutil
if [ $? -eq 0 ]; then
    log "Python dependencies installed successfully."
else
    log "Error installing Python dependencies."
    deactivate
    exit 1
fi

# Verify that JSON files exist
log "Checking for JSON files..."
if [ ! -f "/home/mralevas/Documents/GitHub/42TOOLS/SCRIPT/campus65_users.json" ] || \
   [ ! -f "/home/mralevas/Documents/GitHub/42TOOLS/SCRIPT/campus65_location_stats.json" ]; then
    log "Error: One or both JSON files (campus65_users.json, campus65_location_stats.json) are missing."
    deactivate
    exit 1
fi

# Run the Python script
log "Running Python script: $PYTHON_SCRIPT"
python3 "$PYTHON_SCRIPT" >> "$LOG_FILE" 2>&1
if [ $? -eq 0 ]; then
    log "Python script executed successfully."
else
    log "Error executing Python script."
    deactivate
    exit 1
fi

# Deactivate the virtual environment
deactivate
log "Script execution completed."
