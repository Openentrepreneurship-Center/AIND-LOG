#!/bin/sh
# One-shot: init git (if needed) and install the post-commit hook.
set -e
cd "$(dirname "$0")/.."
repo_root="$(pwd)"

if [ ! -d .git ]; then
  git init
fi

mkdir -p .git/hooks
ln -sf ../../.cline-metrics/post-commit .git/hooks/post-commit
chmod +x .cline-metrics/post-commit

echo "Installed: $repo_root/.git/hooks/post-commit -> ../../.cline-metrics/post-commit"
