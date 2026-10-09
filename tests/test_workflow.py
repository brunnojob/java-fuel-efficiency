import json
import os
from pathlib import Path
import subprocess
import tempfile
import unittest


class WorkflowTests(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.addCleanup(self.temporary.cleanup)
        self.directory = Path(self.temporary.name)
        home = os.environ.get("JAVA_HOME")
        self.java = str(Path(home) / "bin/java") if home else "java"
        compiler = str(Path(home) / "bin/javac") if home else "javac"
        subprocess.run(
            [
                compiler,
                "-Xlint:all",
                "-Werror",
                "-d",
                str(self.directory),
                str(Path(__file__).resolve().parents[1] / "FuelLog.java"),
            ],
            check=True,
        )

    def raw(self, *arguments):
        return subprocess.run(
            [self.java, "-cp", str(self.directory), "FuelLog", *map(str, arguments)],
            capture_output=True,
            text=True,
        )

    def command(self, *arguments):
        result = self.raw(*arguments)
        self.assertEqual(result.returncode, 0, result.stderr)
        return json.loads(result.stdout) if result.stdout.strip() else None

    def test_persistent_workflow(self):
        file = self.directory / "fuel.csv"
        self.command(file, "add", "2026-01-01", "1000", "30.0", "6.0", "full")
        self.command(file, "add", "2026-01-02", "1100", "10.0", "6.0", "partial")
        result = self.command(file, "add", "2026-01-03", "1300", "20.0", "6.0", "full")
        self.assertEqual(result["periods"][0]["litersPer100Km"], 10)
        self.assertEqual(result["accountedCost"], 180)
        self.assertNotEqual(
            self.raw(file, "add", "2026-01-01", "1200", "1", "6", "full").returncode, 0
        )
