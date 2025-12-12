import { useEffect, useState } from "react";
import { useAuthContext } from "../context/AuthContext";

export type WeekItem = {
  date: string;        // e.g. "2025-11-20"
  dayName: string;     // e.g. "THURSDAY"
  weekNumber: number;
  mood: number | null;
  createdAt: string | null;
};

const DAY_SHORT: Record<string, string> = {
  MONDAY: "Mon",
  TUESDAY: "Tue",
  WEDNESDAY: "Wed",
  THURSDAY: "Thu",
  FRIDAY: "Fri",
  SATURDAY: "Sat",
  SUNDAY: "Sun",
};

function parseDayNumber(dateStr: string): number {
  // Safer than new Date(...); avoids TZ shift for day-of-month
  const parts = dateStr.split("-");
  const d = parseInt(parts[2], 10);
  return isNaN(d) ? 0 : d;
}

export default function useUserWeek() {
  const { user } = useAuthContext();
  const [week, setWeek] = useState<WeekItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const id = user?.uuid || localStorage.getItem("userUuid");
    if (!id) return;

    const ac = new AbortController();
    async function run() {
      setLoading(true);
      setError(null);
      try {
        const res = await fetch(`/api/users/${id}/week`, { signal: ac.signal });
        if (!res.ok) {
          let msg = res.statusText || "Gagal memuat data minggu";
          try {
            const err = await res.json();
            msg = err.message || err.error || JSON.stringify(err);
          } catch {}
          setError(msg);
          return;
        }
        const data = (await res.json()) as WeekItem[];
        setWeek(Array.isArray(data) ? data : []);
      } catch (e) {
        if ((e as any)?.name !== "AbortError") {
          setError("Jaringan bermasalah. Coba lagi.");
        }
      } finally {
        setLoading(false);
      }
    }

    run();
    return () => ac.abort();
  }, [user?.uuid]);

  // Turunan yang praktis untuk tampilan: abrev hari dan angka tanggal
  const days = week.map((it) => ({
    dayShort: DAY_SHORT[(it.dayName || "").toUpperCase()] || it.dayName || "",
    dayNum: parseDayNumber(it.date),
    raw: it,
  }));

  return { week, days, loading, error } as const;
}
