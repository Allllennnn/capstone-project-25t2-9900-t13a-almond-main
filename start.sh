#!/usr/bin/env bash
set -e

echo "🔄 Pulling the latest code"
git pull origin main

echo "🔄 Updating config submodules (if configuration is managed via submodules)"
git submodule update --init --recursive

echo "🚀 Building and starting all services in one command"
docker-compose up -d --build

echo "✅ All services have been started:"
echo "   • Frontend → http://localhost:3000"
echo "   • Java backend → http://localhost:8080"
echo "   • Python Agent → http://localhost:8000/docs"