#!/usr/bin/env bash
set -eu

MESSAGE=$(git show -s --format=%B | head -n1 | sed -re 's/[(]#.*[)]//')

git config --global user.email "bot@craft-watch.github.io"
git config --global user.name "Deploy Bot"

git clone --depth 1 git@github.com:craft-watch/craft-watch.github.io.git website
cd website
git rm -r .
cp -r ../frontend/out/. .
git add .
git commit --allow-empty -m "Deployed: ${MESSAGE} (run ID: ${GITHUB_RUN_ID})"
git push
