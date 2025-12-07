import React, { useEffect, useState } from "react";
import { useActivePageContext } from "../context/ActivePageContext";
import { useAuthContext } from "../context/AuthContext";

type MoodEntry = {
  date?: string;
  dayName?: string;
  weekNumber?: number;
  mood?: number;
  createdAt?: string;
  reason?: string;
  aiComment?: string;
};

const moodEmoji: Record<number, string> = {
  1: "😠",
  2: "😭",
  3: "😐",
  4: "😊",
  5: "😁",
};

const moodLabel: Record<number, string> = {
  1: "MARAH",
  2: "SEDIH",
  3: "NETRAL",
  4: "SENANG",
  5: "GEMBIRA",
};

const SavedMood: React.FC = () => {
  const { setActivePage } = useActivePageContext();
  const { user } = useAuthContext();
  const [entry, setEntry] = useState<MoodEntry | null>(null);

  useEffect(() => {
    try {
      const userId = user?.uuid || localStorage.getItem("userUuid");
      const raw = userId ? localStorage.getItem(`lastMoodEntry:${userId}`) : null;
      if (raw) setEntry(JSON.parse(raw));
    } catch {}
  }, [user?.uuid]);

  if (!entry) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center bg-zinc-100 px-4">
        <p className="text-xl font-semibold mb-4">Data mood tidak ditemukan.</p>
        <button
          onClick={() => setActivePage("Mood")}
          className="px-6 py-3 rounded-full bg-black text-white font-bold hover:brightness-110"
        >
          Pilih Mood Lagi
        </button>
      </div>
    );
  }

  const m = entry.mood || 0;

  return (
    <div className="min-h-screen flex flex-col items-center pt-30 bg-linear-150 from-orange-300 to-yellow-300 px-4">
      <div className="text-8xl mb-6">{moodEmoji[m] || "🙂"}</div>
      <h1 className="text-3xl lg:text-5xl font-extrabold text-brown-700 mb-8">Mood Tersimpan</h1>
      <div className="w-full max-w-3xl bg-white/80 backdrop-blur-sm rounded-3xl shadow-md p-6 lg:p-8">
        <div className="flex flex-wrap items-center gap-4 mb-6">
          <span className="px-4 py-2 rounded-full bg-yellow-400 text-brown-800 font-bold text-sm lg:text-base">
            {moodLabel[m] || "UNKNOWN"}
          </span>
          {entry.date && (
            <span className="text-sm lg:text-base font-medium text-brown-700">
              Tanggal: {entry.date}
            </span>
          )}
          {entry.dayName && (
            <span className="text-sm lg:text-base font-medium text-brown-700">
              Hari: {entry.dayName}
            </span>
          )}
        </div>
        {entry.reason && (
          <div className="mb-6">
            <h2 className="text-lg font-bold text-brown-800 mb-2">Alasan</h2>
            <p className="text-brown-700 leading-relaxed whitespace-pre-wrap">{entry.reason}</p>
          </div>
        )}
        {entry.aiComment && (
          <div className="mb-6">
            <h2 className="text-lg font-bold text-brown-800 mb-2">Komentar AI</h2>
            <p className="text-brown-700 leading-relaxed whitespace-pre-wrap">{entry.aiComment}</p>
          </div>
        )}
        <div className="flex gap-4 mt-4">
          <button
            onClick={() => setActivePage("Home")}
            className="px-6 py-3 rounded-full bg-white border border-brown-700 text-brown-800 font-bold hover:bg-zinc-100"
          >
            Kembali Beranda
          </button>
        </div>
      </div>
    </div>
  );
};

export default SavedMood;
