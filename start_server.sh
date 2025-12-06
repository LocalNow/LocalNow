#!/bin/bash
echo ">> [Setup] Checking Python environment..."
python --version
pip --version

echo ">> [Setup] Installing dependencies..."
python -m pip install -r requirements.txt

echo ">> [Setup] Starting Server..."
python app.py
