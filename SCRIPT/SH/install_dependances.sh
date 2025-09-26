#!/bin/bash

LOG_FILE="/home/tiavina/logfile.log"

# Log function
log() {
    echo "$(date '+%Y-%m-%d %H:%M:%S') - $1" >> "$LOG_FILE"
}

log "Updating system packages..."
sudo apt update

log "Installing system dependencies..."
sudo apt install -y libpq-dev python3-dev build-essential
if [ $? -eq 0 ]; then
    log "System dependencies installed successfully."
else
    log "Error installing system dependencies."
    exit 1
fi

# Virtual environment path
VENV_PATH="/home/tiavina/Documents/GitHub/42TOOLS/SCRIPT/venv"

# Create virtual environment if it doesn't exist
if [ ! -d "$VENV_PATH" ]; then
    log "Creating virtual environment at $VENV_PATH..."
    python3 -m venv "$VENV_PATH"
    if [ $? -ne 0 ]; then
        log "Failed to create virtual environment."
        exit 1
    fi
fi

# Activate venv and install Python packages
source "$VENV_PATH/bin/activate"
PIP="$VENV_PATH/bin/pip"

log "Upgrading pip..."
"$PIP" install --upgrade pip

log "Installing Python dependencies..."
"$PIP" install psycopg2-binary python-dateutil
if [ $? -eq 0 ]; then
    log "Python dependencies installed successfully."
else
    log "Error installing Python dependencies."
    deactivate
    exit 1
fi

deactivate
log "Dependency installation completed."
