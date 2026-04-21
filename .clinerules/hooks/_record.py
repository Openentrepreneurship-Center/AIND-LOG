#!/usr/bin/env python3
"""Cline hook recorder. Reads hook JSON from stdin, appends a flattened
event record to <workspace>/.cline-metrics/events.jsonl, and always
responds with {"cancel": false}. Any error is swallowed so Cline is
never blocked by this hook.
"""
import json
import os
import sys
import traceback
from pathlib import Path


def extract_payload(data: dict) -> dict:
    for key in (
        "taskStart", "taskResume", "taskCancel", "taskComplete",
        "preToolUse", "postToolUse", "userPromptSubmit", "preCompact",
    ):
        if key in data and isinstance(data[key], dict):
            return data[key]
    return {}


def main() -> None:
    try:
        raw = sys.stdin.read()
        data = json.loads(raw) if raw.strip() else {}

        roots = data.get("workspaceRoots") or []
        workspace = Path(roots[0]) if roots else Path.cwd()
        metrics_dir = workspace / ".cline-metrics"
        metrics_dir.mkdir(parents=True, exist_ok=True)

        record = {
            "ts": data.get("timestamp"),
            "taskId": data.get("taskId"),
            "event": data.get("hookName"),
            "clineVersion": data.get("clineVersion"),
            "model": data.get("model"),
            "payload": extract_payload(data),
        }

        with (metrics_dir / "events.jsonl").open("a", encoding="utf-8") as f:
            f.write(json.dumps(record, ensure_ascii=False) + "\n")
    except Exception:
        try:
            err_dir = Path.home() / ".cline-metrics-errors"
            err_dir.mkdir(parents=True, exist_ok=True)
            with (err_dir / "hook.log").open("a", encoding="utf-8") as f:
                f.write(traceback.format_exc() + "\n---\n")
        except Exception:
            pass
    finally:
        sys.stdout.write(json.dumps({"cancel": False}))
        sys.stdout.flush()


if __name__ == "__main__":
    main()
