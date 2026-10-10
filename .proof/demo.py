import json
import subprocess
import sys
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT))

def run(argv, expected=0):
    result = subprocess.run(argv, cwd=ROOT, capture_output=True, text=True, timeout=30)
    if result.returncode != expected:
        raise RuntimeError(result.stderr + result.stdout)
    return result.stdout

with tempfile.TemporaryDirectory() as temp:
    path=Path(temp)/'fuel.csv'
    argv=['java','FuelLog',str(path)]
    run(argv+['add','2026-01-01','1000','30.0','6.0','full'])
    run(argv+['add','2026-01-02','1100','10.0','6.0','partial'])
    report=json.loads(run(argv+['add','2026-01-03','1300','20.0','6.0','full']))
    assert report['periods'][0]['litersPer100Km']==10 and report['accountedCost']==180
    invalid=subprocess.run(argv+['add','2026-01-01','1200','1','6','full'],cwd=ROOT,capture_output=True,text=True)
    assert invalid.returncode!=0
    print(json.dumps({'report':report,'regressed_reading_rejected':True},sort_keys=True))
