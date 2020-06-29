#!/usr/bin/env bash
set -eu

git config --global user.email "bot@oliver-charlesworth.github.io"
git config --global user.name "CircleCI Bot"

git clone --depth 1 git@github.com:oliver-charlesworth/oliver-charlesworth.github.io.git website
cd website
git rm -r .
cp -r ../frontend/out/* .
git add .
git commit --allow-empty -m "Update from ${CIRCLE_SHA1} (build URL: ${CIRCLE_BUILD_URL})"
git push
