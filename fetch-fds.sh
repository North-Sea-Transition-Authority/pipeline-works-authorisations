#!/bin/bash
# Fetch the FDS submodule to match the version specified in the submodule hash
# Use when switching branches to ensure you have the correct submodule for that branch

echo 'Matching submodule to specified project version...'
git submodule update --init --recursive
cd fivium-design-system-core
echo 'Updating submodule dependencies...'
npm install
echo 'Building submodule...'
npx gulp build
cd ..
echo 'Rebuilding frontend app components...'
npx gulp buildAll
echo 'Done'