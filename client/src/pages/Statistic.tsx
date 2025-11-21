import { useEffect, useMemo, useState } from "react";
import { PieChart, Pie, Cell, Legend, ResponsiveContainer } from "recharts";
import { useAuthContext } from "../context/AuthContext";

type BreakdownItem = { category: string; percent: number };
type StatsResponse = {
  weekNumber: number;
  completed?: boolean;
  averageScore: number;
  entriesCount: number;
  breakdown: BreakdownItem[];
  aiComment?: string;
  activities?: string[];
};

const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const Statistic = () => {
  const { user } = useAuthContext();
  const [stats, setStats] = useState<StatsResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedWeek, setSelectedWeek] = useState<number | null>(null);
  const [currentWeekNumber, setCurrentWeekNumber] = useState<number | null>(null);

  useEffect(() => {
    const id = user?.uuid || (typeof window !== 'undefined' ? localStorage.getItem('userUuid') : null);
    if (!id) return;
    const acWeek = new AbortController();
    (async () => {
      try {
        const res = await fetch(`${API_BASE}/api/users/${id}/currentWeek`, { signal: acWeek.signal });
        if (!res.ok) return;
        const body = await res.json();
        const candidate = typeof body.weekNumber === 'number' ? body.weekNumber : (typeof body.numberWeek === 'number' ? body.numberWeek : null);
        if (candidate != null) setCurrentWeekNumber(candidate);
      } catch (e) {
        if (import.meta.env.DEV) console.debug('[Statistic] Failed currentWeek', e);
      }
    })();
    return () => acWeek.abort();
  }, [user?.uuid]);

  useEffect(() => {
    const id = user?.uuid || (typeof window !== 'undefined' ? localStorage.getItem('userUuid') : null);
    if (!id) return;
    const effectiveWeek = selectedWeek ?? currentWeekNumber; 
    const ac = new AbortController();

    (async () => {
      setError(null);
      const cacheKey = `userStats:${id}:${effectiveWeek ?? 'auto'}`;
      const cachedStr = typeof window !== 'undefined' ? localStorage.getItem(cacheKey) : null;
      let cached: StatsResponse | null = null;
      if (cachedStr) {
        try { cached = JSON.parse(cachedStr) as StatsResponse; } catch {}
      }
      if (cached) {
        setStats(cached);
        setLoading(false);
      } else {
        setLoading(true);
      }

      const isEmpty = (d: StatsResponse | null) => !d || (d.entriesCount === 0 && (!d.breakdown || d.breakdown.length === 0));
      try {
        const url = effectiveWeek == null
          ? `${API_BASE}/api/users/${id}/stats`
          : `${API_BASE}/api/users/${id}/stats?weekNumber=${effectiveWeek}`;
        const res = await fetch(url, { signal: ac.signal });
        if (!res.ok) {
          if (!cached) {
            let msg = res.statusText || 'Gagal memuat statistik';
            try {
              const body = await res.json();
              msg = body.message || body.error || JSON.stringify(body);
            } catch {}
            setError(msg);
          }
          return;
        }
        const data = (await res.json()) as StatsResponse;
        if (isEmpty(data) && cached) {
          setLoading(false);
          return;
        }
        setStats(data);
        try { localStorage.setItem(cacheKey, JSON.stringify(data)); } catch {}
      } catch (e) {
        if ((e as any)?.name !== 'AbortError') {
          if (!cached) setError('Jaringan bermasalah. Coba lagi.');
        }
      } finally {
        setLoading(false);
      }
    })();
    return () => ac.abort();
  }, [user?.uuid, selectedWeek, currentWeekNumber]);

  const pieData = useMemo(() => {
    const catColor: Record<string, string> = {
      sad: "#4A90E2",
      angry: "#FF4D4D",
      happy: "#FFD700",
      neutral: "#A0AEC0",
      joy: "#34D399",
    };
    const cap = (s: string) => (s ? s.charAt(0).toUpperCase() + s.slice(1).toLowerCase() : s);
    if (!stats?.breakdown || stats.breakdown.length === 0) return [] as { name: string; value: number; color: string }[];
    return stats.breakdown.map((b) => ({
      name: cap(b.category),
      value: b.percent,
      color: catColor[b.category?.toLowerCase()] || "#8884d8",
    }));
  }, [stats]);

  const weekOptions = useMemo(() => {
    const sourceWeek = currentWeekNumber || stats?.weekNumber || 1;
    const maxWeek = Math.min(Math.max(sourceWeek, 1), 52);
    const arr = Array.from({ length: maxWeek }, (_, i) => i + 1);
    return arr;
  }, [currentWeekNumber, stats?.weekNumber]);

  const isIncomplete = useMemo(() => {
    if (!stats) return false;
    if (stats.completed === false) return true;
    if ((stats.completed === undefined || stats.completed === null) && stats.entriesCount < 7) return true;
    return false;
  }, [stats]);

  const isEmptySelectedWeek = useMemo(() => {
    if (!stats) return false;
    if (selectedWeek == null) return false; 
    const noEntries = stats.entriesCount === 0;
    const noBreakdown = !stats.breakdown || stats.breakdown.length === 0;
    return stats.weekNumber === selectedWeek && noEntries && noBreakdown;
  }, [stats, selectedWeek]);

  return (
    <div className="Statistic min-h-screen pt-12 lg:pt-28 bg-zinc-100 flex flex-col items-center px-4 lg:px-12">
      <div className="bg-white rounded-3xl shadow-md p-4 lg:p-12 lg:flex flex-row w-full max-w-6xl mx-auto min-h-[70vh]">

        {/* ============================ DESCRIPTION ============================ */}
        <div className="grid w-full lg:w-1/2 gap-4">
          {/* Header Section */}
          <h1 className="text-2xl lg:text-5xl font-bold flex items-baseline gap-3">
            <span className="text-blue-500">Week</span>
            {!loading && stats?.weekNumber != null && (
              <span className="text-base lg:text-5xl text-blue-500 font-bold">{stats.weekNumber}</span>
            )}
            {!loading && stats && isIncomplete && (
              <span className="ml-2 px-2 py-1 rounded-full text-xs font-semibold bg-amber-100 text-amber-700 border border-amber-200">
                Belum lengkap: {stats.entriesCount}/7
              </span>
            )}
            {!loading && stats && stats.completed === true && (
              <span className="ml-2 px-2 py-1 rounded-full text-xs font-semibold bg-emerald-100 text-emerald-700 border border-emerald-200">
                Lengkap
              </span>
            )}
          </h1>
          {/* ============================ DROPDOWN MINGGU ============================ */}
          <div className="mt-3">
            <label className="block text-xs lg:text-sm text-zinc-600 mb-1">Pilih Minggu</label>
            <select
              value={selectedWeek ?? (currentWeekNumber || stats?.weekNumber || '')}
              onChange={(e) => {
                const v = e.target.value ? parseInt(e.target.value, 10) : NaN;
                setSelectedWeek(Number.isNaN(v) ? null : v);
              }}
              className="px-3 py-2 rounded-lg border border-zinc-300 bg-white text-sm lg:text-base"
            >
              {weekOptions.map((w) => (
                <option key={w} value={w}>Minggu {w}</option>
              ))}
            </select>
          </div>

          <div className="bg-gray-200 p-4 rounded-lg shadow-md text-center min-h-20 flex items-center justify-center">
            {loading ? (
              <p className="text-lg font-medium">Memuat statistik...</p>
            ) : error ? (
              <p className="text-lg font-medium">Belum ada data minggu ini.</p>
            ) : stats && isEmptySelectedWeek ? (
              <p className="text-lg font-medium"></p>
            ) : stats && isIncomplete ? (
              <p className="text-lg font-medium">Minggu ini belum lengkap. Lengkapi mood untuk sisa hari agar statistik penuh.</p>
            ) : stats?.aiComment ? (
              <p className="text-lg font-medium">{stats.aiComment}</p>
            ) : (
              <p className="text-lg font-medium">Belum ada data, silakan masukkan mood hari ini.</p>
            )}
          </div>

          {/* ============================ RECOMMENDATION ============================ */}
          <div className="mt-4 lg:mt-6">
            <div className="bg-yellow-400 text-white px-6 py-3 rounded-t-lg shadow-md text-lg font-bold">We recommend you</div>
            <div className="bg-yellow-50 border border-yellow-200 rounded-b-lg p-4">
              {loading ? (
                <p className="text-sm lg:text-base text-yellow-700">Menyiapkan rekomendasi...</p>
              ) : stats?.activities && stats.activities.length > 0 ? (
                <ul className="list-disc pl-6 text-yellow-800">
                  {stats.activities.map((a, i) => (
                    <li key={i} className="mb-1">{a}</li>
                  ))}
                </ul>
              ) : (
                <p className="text-sm lg:text-base text-yellow-700">Belum ada rekomendasi.</p>
              )}
            </div>
          </div>

          {/* Basic numbers */}
          {/* <div className="mt-4 grid grid-cols-3 gap-3">
            <div className="bg-zinc-100 rounded-lg p-3 text-center">
              <div className="text-xs lg:text-sm text-zinc-500">Entries</div>
              <div className="text-lg lg:text-2xl font-bold">{stats?.entriesCount ?? '-'}</div>
            </div>
            <div className="bg-zinc-100 rounded-lg p-3 text-center">
              <div className="text-xs lg:text-sm text-zinc-500">Avg Score</div>
              <div className="text-lg lg:text-2xl font-bold">{stats?.averageScore ?? '-'}</div>
            </div>
            <div className="bg-zinc-100 rounded-lg p-3 text-center">
              <div className="text-xs lg:text-sm text-zinc-500">Categories</div>
              <div className="text-lg lg:text-2xl font-bold">{stats?.breakdown?.length ?? 0}</div>
            </div>
          </div> */}
        </div>

        {/* ============================ PIE CHART ============================ */}
        <div className="mt-2 lg:mt-6 flex items-center justify-center w-full lg:w-1/2 h-72 lg:h-[500px]">
          {loading ? (
            <div className="text-zinc-500 text-sm lg:text-base">Memuat chart...</div>
          ) : stats && isEmptySelectedWeek ? (
            <div className="text-zinc-500 text-sm lg:text-base"></div>
          ) : stats && isIncomplete ? (
            <div className="text-zinc-500 text-sm lg:text-base"></div>
          ) : pieData.length === 0 ? (
            <div className="text-zinc-500 text-sm lg:text-base"></div>
          ) : (
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={pieData}
                  dataKey="value"
                  nameKey="name"
                  cx="50%"
                  cy="50%"
                  outerRadius={140}
                  fill="#8884d8"
                >
                  {pieData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          )}
        </div>
        
      </div>
    </div>
  );
};

export default Statistic;