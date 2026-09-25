#!/usr/bin/env python3
import concurrent.futures
import json
import os
import statistics
import time
import urllib.request
import urllib.error

BASE = os.environ.get("PARFUM_API_BASE", "https://parfum-backend-jvcw.onrender.com/api").rstrip("/")
TIMEOUT = float(os.environ.get("LOAD_TIMEOUT", "20"))
ROUNDS = int(os.environ.get("LOAD_ROUNDS", "5"))

ENDPOINTS = [
    "/health",
    "/productos?size=15&sort=destacado,desc",
]

def request_once(url):
    start = time.perf_counter()
    status = 0
    error = None
    try:
        req = urllib.request.Request(url, headers={"User-Agent": "ParfumPerfCheck/1.0"})
        with urllib.request.urlopen(req, timeout=TIMEOUT) as response:
            status = response.status
            response.read(1024)
    except Exception as exc:
        error = str(exc)
    elapsed = (time.perf_counter() - start) * 1000
    return {"ms": elapsed, "status": status, "error": error}

def percentile(values, p):
    if not values:
        return None
    values = sorted(values)
    idx = max(0, min(len(values) - 1, int(round((len(values) - 1) * p))))
    return values[idx]

def run_stage(workers):
    jobs = []
    started = time.perf_counter()
    with concurrent.futures.ThreadPoolExecutor(max_workers=workers) as pool:
        for _ in range(ROUNDS):
            for endpoint in ENDPOINTS:
                for _ in range(workers):
                    jobs.append(pool.submit(request_once, BASE + endpoint))
        results = [job.result() for job in jobs]
    wall = time.perf_counter() - started

    ok = [r for r in results if 200 <= r["status"] < 400]
    failed = [r for r in results if not (200 <= r["status"] < 400)]
    latencies = [r["ms"] for r in ok]
    return {
        "users": workers,
        "requests": len(results),
        "ok": len(ok),
        "errors": len(failed),
        "error_rate_pct": round((len(failed) / len(results) * 100) if results else 0, 2),
        "p50_ms": round(percentile(latencies, 0.50), 1) if latencies else None,
        "p95_ms": round(percentile(latencies, 0.95), 1) if latencies else None,
        "max_ms": round(max(latencies), 1) if latencies else None,
        "rps": round(len(results) / wall, 2) if wall else None,
        "sample_errors": [r["error"] or f"HTTP {r['status']}" for r in failed[:3]],
    }

print(f"Warming up {BASE} ...")
warm = request_once(BASE + "/health")
print(json.dumps({"warmup": warm}, ensure_ascii=False))

summary = []
for users in (10, 25, 50):
    result = run_stage(users)
    summary.append(result)
    print(json.dumps(result, ensure_ascii=False))

print("SUMMARY_JSON=" + json.dumps(summary, ensure_ascii=False))

# This is a smoke/load diagnostic, not a strict SLA gate.
# Fail only on severe availability problems.
if any(stage["error_rate_pct"] > 20 for stage in summary):
    raise SystemExit("Load test detected >20% errors in at least one stage")
