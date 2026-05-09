"""
Risk-based total test case prioritisation for the LMS backend.

The script reads test metadata and simulated changed components, assigns a priority
score to each JUnit test class, and writes the ordered list to test-results.
Jenkins then passes the generated order to Maven Surefire with -Dtest=...
"""

from __future__ import annotations

import csv
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
METADATA_FILE = ROOT / "tools" / "test-metadata.csv"
CHANGED_COMPONENTS_FILE = ROOT / "tools" / "changed-components.txt"
OUTPUT_DIR = ROOT / "test-results"
SELECTED_TESTS_FILE = OUTPUT_DIR / "selected-tests.txt"
REPORT_FILE = OUTPUT_DIR / "prioritization-report.json"


def read_changed_components() -> set[str]:
    if not CHANGED_COMPONENTS_FILE.exists():
        return set()
    return {
        line.strip().lower()
        for line in CHANGED_COMPONENTS_FILE.read_text(encoding="utf-8").splitlines()
        if line.strip() and not line.strip().startswith("#")
    }


def priority_score(row: dict[str, str], changed_components: set[str]) -> float:
    criticality = int(row["criticality"])
    past_failures = int(row["past_failures"])
    estimated_seconds = int(row["estimated_seconds"])
    component = row["component"].strip().lower()

    changed_component_bonus = 5 if component in changed_components else 0
    fast_feedback_bonus = max(0, 8 - estimated_seconds) * 0.25

    return (criticality * 2.0) + (past_failures * 1.5) + changed_component_bonus + fast_feedback_bonus


def main() -> None:
    changed_components = read_changed_components()
    OUTPUT_DIR.mkdir(exist_ok=True)

    with METADATA_FILE.open(newline="", encoding="utf-8") as handle:
        rows = list(csv.DictReader(handle))

    ranked = sorted(rows, key=lambda row: priority_score(row, changed_components), reverse=True)
    selected_tests = [row["test_class"] for row in ranked]

    SELECTED_TESTS_FILE.write_text(",".join(selected_tests), encoding="utf-8")

    report = {
        "technique": "risk-based total test case prioritisation",
        "changed_components": sorted(changed_components),
        "scoring_model": "criticality*2 + past_failures*1.5 + changed_component_bonus + fast_feedback_bonus",
        "selected_execution_order": [
            {
                "rank": index + 1,
                "test_class": row["test_class"],
                "component": row["component"],
                "requirement": row["requirement"],
                "criticality": int(row["criticality"]),
                "past_failures": int(row["past_failures"]),
                "estimated_seconds": int(row["estimated_seconds"]),
                "priority_score": round(priority_score(row, changed_components), 2),
            }
            for index, row in enumerate(ranked)
        ],
    }
    REPORT_FILE.write_text(json.dumps(report, indent=2), encoding="utf-8")

    print("Prioritised LMS backend test execution order:")
    for item in report["selected_execution_order"]:
        print(f"{item['rank']}. {item['test_class']} "
              f"component={item['component']} score={item['priority_score']}")


if __name__ == "__main__":
    main()
