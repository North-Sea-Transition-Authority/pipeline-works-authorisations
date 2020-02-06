#!/bin/bash
# Update FDS submodule to the lastest commit on the tracked branch
# You will need to commit the submodule hash change after doing this

echo 'Updating submodule to latest remote...'
git submodule update --init --recursive --remote
cd fivium-design-system-core
echo 'Updating submodule dependencies...'
npm install
echo 'Building submodule...'
npx gulp build
cd ..
echo 'Rebuilding frontend app components...'
npx gulp buildAll
echo 'Done'