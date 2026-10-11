#!/usr/bin/env python3
"""Controlled read-only load test. Does not log in or create orders."""
import asyncio,json,os,time,statistics
from collections import Counter
from urllib.parse import urljoin
import aiohttp

BASE = os.getenv("PARFUM_API_BASE", "https://parfum-backend-jvcw.onrender.com/api").rstrip("/")+"/"
STAGES = [int(v) for v in os.getenv("STAGES","100,300,500").split(",")]
WINDOW = max(10,int(os.getenv("SECONDS_PER_STAGE","20")))
THINK = 10
PATHS = ("/productos?page=0&size=16","/productos?page=0&size=16",
         "/productos?page=0&size=16","/productos/destacados","/health")

def pctl(values,p):
    if not values:return None
    a=sorted(values)
    return round(a[min(len(a)-1,int((len(a)-1)*p))],1)

async def stage(session,users):
    started=time.monotonic()
    rows=[]
    async def visitor(i):
        await asyncio.sleep((i/users)*THINK)
        while time.monotonic()-started<WINDOW:
            t=time.monotonic()
            try:
                async with session.get(urljoin(BASE,PATHS[i%len(PATHS)].lstrip("/"))) as r:
                    payload=await r.read()
                    rows.append((r.status, (time.monotonic()-t)*1000,len(payload)))
            except Exception as exc:
                rows.append((0, (time.monotonic()-t)*1000,0))
            await asyncio.sleep(THINK)
    await asyncio.gather(*(visitor(i) for i in range(users)))
    successful=[x[1] for x in rows if 200<=x[0]<400]
    failures=len(rows)-len(successful)
    elapsed=time.monotonic()-started
    return dict(virtual_users=users,requests=len(rows),requests_per_second=round(len(rows)/elapsed,1),
                failures=failures,error_rate_pct=round(100*failures/max(1,len(rows)),2),
                p50_ms=pctl(successful,.5),p95_ms=pctl(successful,.95),
                p99_ms=pctl(successful,.99),max_ms=round(max(successful),1) if successful else None,
                statuses=dict(Counter(str(x[0]) for x in rows)))

async def main():
    print("Read-only Parfum test:",BASE,flush=True)
    connector=aiohttp.TCPConnector(limit=160,limit_per_host=160)
    async with aiohttp.ClientSession(connector=connector,timeout=aiohttp.ClientTimeout(total=15),headers={"User-Agent":"ParfumPerfProbe/1.0"}) as s:
        async with s.get(urljoin(BASE,"health")) as r:
            print("WARMUP",r.status,flush=True)
            if r.status!=200: raise SystemExit("Unhealthy backend")
        results=[]
        for count in STAGES:
            result=await stage(s,count)
            results.append(result)
            print("STAGE_RESULT="+json.dumps(result),flush=True)
            if result["error_rate_pct"]>10 or (result["p95_ms"] or 99999)>10000:
                print("HALT: high error rate or latency",flush=True)
                break
        print("SUMMARY_JSON="+json.dumps(results),flush=True)

asyncio.run(main())
