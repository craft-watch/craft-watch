#!/usr/bin/env bash
set -eu

git config user.email "bot@oliver-charlesworth.github.io"
git config user.name "CircleCI Bot"

git clone git@github.com:oliver-charlesworth/oliver-charlesworth.github.io.git website
cd website
git rm -r .
cp ../build/distributions/* .
git add .
git commit -m "Update"
git push
