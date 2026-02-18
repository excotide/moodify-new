import { useEffect, useState } from "react";
import { useAuthContext } from "../context/AuthContext";

const API_BASE = import.meta.env.VITE_API_URL ?? "";

export type WeekItem = {
  date: string;        
  dayName: string;    
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
  const parts = dateStr.split("-");
  const d = parseInt(parts[2], 10);
  return isNaN(d) ? 0 : d;
}

export default function useUserWeek() {
  const { user, isAuthenticated } = useAuthContext();
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
        const res = await fetch(`${API_BASE}/api/users/${id}/week`, { signal: ac.signal });
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
        let arr = Array.isArray(data) ? [...data] : [];
        if (arr.length > 0 && arr.length < 7) {
          arr.sort((a, b) => a.date.localeCompare(b.date));
          const last = arr[arr.length - 1];
          const names = [
            "SUNDAY",
            "MONDAY",
            "TUESDAY",
            "WEDNESDAY",
            "THURSDAY",
            "FRIDAY",
            "SATURDAY",
          ];
          const pad = (n: number) => String(n).padStart(2, "0");
          const toYMD = (d: Date) => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;

          const [yy, mm, dd] = last.date.split("-").map((v) => parseInt(v, 10));
          let cursor = new Date(yy, (mm || 1) - 1, dd || 1);
          const baseWeekNumber = typeof last.weekNumber === "number" ? last.weekNumber : 0;

          while (arr.length < 7) {
            cursor = new Date(cursor.getFullYear(), cursor.getMonth(), cursor.getDate() + 1);
            const ymd = toYMD(cursor);
            const dayName = names[cursor.getDay()] || "";
            arr.push({
              date: ymd,
              dayName,
              weekNumber: baseWeekNumber, 
              mood: null,
              createdAt: null,
            });
          }
        }
        setWeek(arr);
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
  }, [user?.uuid, isAuthenticated]);

  const days = week.map((it) => ({
    dayShort: DAY_SHORT[(it.dayName || "").toUpperCase()] || it.dayName || "",
    dayNum: parseDayNumber(it.date),
    raw: it,
  }));

  return { week, days, loading, error } as const;
}
