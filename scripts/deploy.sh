#!/usr/bin/env bash
set -eu

MESSAGE=$(git show -s --format=%B | head -n1 | sed -re 's/[(]#.*[)]//')

git config --global user.email "bot@craft-watch.github.io"
git config --global user.name "Deploy Bot"

echo "PWD"
pwd

echo "LS HERE"
ls -l

echo "LS ONE UP"
ls -l ../

git clone --depth 1 git@github.com:craft-watch/craft-watch.github.io.git website
cd website
git rm -r .
cp -r ../frontend/out/. .
git add .
git commit --allow-empty -m "Deployed: ${MESSAGE} (run ID: ${GITHUB_RUN_ID})"
git push
