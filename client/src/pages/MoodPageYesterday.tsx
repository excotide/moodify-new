import React, { useState, useEffect } from "react";
import { useActivePageContext } from "../context/ActivePageContext";
import { useAuthContext } from "../context/AuthContext";
import { motion } from "framer-motion";
import useUserWeek from "../hooks/useUserWeek";

const moods = [
  { name: "MARAH", emoji: "😠" },
  { name: "SEDIH", emoji: "😭" },
  { name: "NETRAL", emoji: "😐" },
  { name: "SENANG", emoji: "😊" },
  { name: "GEMBIRA", emoji: "😁" },
];

const MOOD_VALUE: Record<string, number> = {
  MARAH: 1,
  SEDIH: 2,
  NETRAL: 3,
  SENANG: 4,
  GEMBIRA: 5,
};

function localYMD(d: Date) {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${y}-${m}-${day}`;
}

const MoodPageYesterday: React.FC = () => {
  const { setActivePage } = useActivePageContext();
  const { user } = useAuthContext();
  const [selectedMood, setSelectedMood] = useState<string | null>(null);
  const [note, setNote] = useState("");
  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState<string | null>(null);
  const [missingDates, setMissingDates] = useState<string[]>([]);
  const [selectedDate, setSelectedDate] = useState<string | null>(null);
  const NOTE_MAX = 280;
  const todayDate = localYMD(new Date());
  const { week, loading: loadingWeek } = useUserWeek();

   /* ============================ LOGIKA NENTUIN DAFTAR TANGGAL YG AVAILABLE ============================ */
  useEffect(() => {
    if (loadingWeek) return;
    if (!week || !week.length) return;
    const gaps = week
      .filter(w => w.date < todayDate && (w.mood == null))
      .map(w => w.date)
      .sort((a,b) => a.localeCompare(b));
    setMissingDates(gaps);
    setSelectedDate(gaps.length ? gaps[gaps.length - 1] : null);
  }, [week, loadingWeek, todayDate]);

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if ((e.key === "Enter" || e.keyCode === 13) && selectedMood && selectedDate) {
        handleConfirm();
      }
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [selectedMood, selectedDate]);

  const handleConfirm = async () => {
    if (!selectedMood || !selectedDate) return;
    setSaveError(null);
    setSaving(true);
    const userId = user?.uuid || localStorage.getItem("userUuid");
    if (!userId) {
      setSaveError("User ID tidak ditemukan. Silakan login ulang.");
      setSaving(false);
      return;
    }
    const moodNumber = MOOD_VALUE[selectedMood];
    try {
      const res = await fetch(`http://localhost:8080/api/mood-entries/users/${userId}/mood/past`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ date: selectedDate, mood: moodNumber, reason: note.trim() }),
      });
      if (!res.ok) {
        let msg = res.statusText || "Gagal menyimpan mood kemarin";
        try {
          const errBody = await res.json();
          msg = errBody.message || errBody.error || JSON.stringify(errBody);
        } catch {}
        setSaveError(msg);
        setSaving(false);
        return;
      }
      const data = await res.json();
      try {
        const datedRaw = localStorage.getItem(`moodEntries:${userId}`);
        const map = datedRaw ? JSON.parse(datedRaw) : {};
        map[selectedDate] = { ...data, date: selectedDate };
        localStorage.setItem(`moodEntries:${userId}`, JSON.stringify(map));
      } catch {}
      try {
        const lastRaw = localStorage.getItem(`lastMoodEntry:${userId}`);
        let shouldOverwrite = true;
        if (lastRaw) {
          const entry = JSON.parse(lastRaw);
          const entryDate: string | undefined = entry?.date || (entry?.createdAt ? localYMD(new Date(entry.createdAt)) : undefined);
          if (entryDate === todayDate) shouldOverwrite = false;
        }
        if (shouldOverwrite) {
          localStorage.setItem(`lastMoodEntry:${userId}`, JSON.stringify({ ...data, date: selectedDate, userId }));
        }
      } catch {}
      try {
        localStorage.setItem(`lastPastMoodEntry:${userId}`, JSON.stringify({ ...data, date: selectedDate, userId }));
      } catch {}
      setActivePage("YesterdeySavedMood");
    } catch (e) {
      setSaveError("Jaringan bermasalah. Coba lagi.");
    } finally {
      setSaving(false);
    }
  };

  if (!loadingWeek && missingDates.length === 0) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center bg-linear-150 from-purple-300 to-violet-300 px-4">
        <div className="text-7xl mb-6">✅</div>
        <h1 className="text-3xl lg:text-5xl font-extrabold text-violet-800 mb-6 text-center">Tidak ada tanggal kosong</h1>
        <p className="mb-8 text-violet-900 text-center max-w-xl">Semua mood hari-hari sebelumnya sudah terisi. Bagus sekali! Silakan kembali ke beranda atau isi mood hari ini.</p>
        <div className="flex gap-4">
          <button onClick={() => setActivePage("Home")} className="px-6 py-3 rounded-full bg-white text-violet-800 font-bold border border-violet-700 hover:bg-violet-50">Beranda</button>
          <button onClick={() => setActivePage("Mood")} className="px-6 py-3 rounded-full bg-black text-white font-bold hover:brightness-110">Mood Hari Ini</button>
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-col items-center min-h-screen bg-linear-150 from-violet-300 to-purple-300">
      <div className="flex pt-20 flex-col items-center justify-center mt-16 text-center px-4 lg:px-0">
        <div className="text-9xl lg:text-[10rem] text-violet-700 font-bold mb-6">🕒</div>
        <h2 className="text-3xl lg:text-5xl font-extrabold text-violet-800 mb-8">FILL MISSED PAST MOOD</h2>
        {missingDates.length > 0 && (
          <div className="mb-10 w-full max-w-xs">
            <label className="block text-violet-800 font-semibold mb-2 text-sm lg:text-base">Pilih Tanggal (kemarin / hari-hari kosong):</label>
            <select
              value={selectedDate || ''}
              onChange={e => setSelectedDate(e.target.value || null)}
              className="w-full px-4 py-2 rounded-full border border-violet-300 bg-white text-violet-800 font-medium focus:outline-none focus:ring-4 focus:ring-violet-300/60"
            >
              {missingDates.map(d => (
                <option key={d} value={d}>{d}</option>
              ))}
            </select>
          </div>
        )}
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 gap-8 lg:gap-10">
          {moods.map((mood) => (
            <motion.button
              key={mood.name}
              whileHover={{ scale: 1.1 }}
              whileTap={{ scale: 0.95 }}
              onClick={() => setSelectedMood(mood.name)}
              className={`flex flex-col items-center justify-center w-32 sm:w-36 md:w-40 h-40 sm:h-44 md:h-48 rounded-3xl shadow-md transition-all ${
                selectedMood === mood.name ? "bg-violet-400 shadow-lg scale-105" : "bg-white"
              }`}
            >
              <span className="text-6xl sm:text-7xl lg:text-8xl mb-3">{mood.emoji}</span>
              <span className="font-extrabold text-violet-800 text-sm sm:text-base md:text-lg lg:text-xl">{mood.name}</span>
            </motion.button>
          ))}
        </div>
        {selectedMood && selectedDate && (
          <>
            <textarea
              value={note}
              onChange={(e) => setNote(e.target.value.slice(0, NOTE_MAX))}
              rows={1}
              placeholder={`Catatan untuk mood kemarin (${selectedMood?.toLowerCase()})...`}
              className="mt-10 w-full max-w-2xl mx-auto block h-12 rounded-full border border-violet-300/70 bg-white/90 focus:outline-none focus:ring-4 focus:ring-violet-300/60 focus:border-violet-400 px-5 py-3 text-violet-800 placeholder-violet-400/70 shadow-sm resize-none"
            />
            <div className="mt-2 text-xs lg:text-sm text-violet-700 w-full max-w-2xl mx-auto flex justify-end">
              <span>{note.length}/{NOTE_MAX}</span>
            </div>
            <motion.button
              whileHover={{ scale: selectedMood ? 1.03 : 1 }}
              whileTap={{ scale: selectedMood ? 0.98 : 1 }}
              onClick={handleConfirm}
              disabled={!selectedMood || saving}
              className={`mt-6 px-8 py-4 rounded-full text-xl font-bold transition-all duration-200 ${
                selectedMood && !saving ? "bg-black text-white hover:brightness-105" : "bg-gray-300 text-gray-600 cursor-not-allowed"
              }`}
            >
              {saving ? "Menyimpan..." : "Simpan Mood Kemarin"}
            </motion.button>
            {saveError && <div className="mt-4 text-sm text-red-600 font-semibold">{saveError}</div>}
          </>
        )}
      </div>
    </div>
  );
};

export default MoodPageYesterday;