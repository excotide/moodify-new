import React, { useState, useEffect } from "react";
import { useActivePageContext } from "../context/ActivePageContext";
import { useAuthContext } from "../context/AuthContext";
import { motion } from "framer-motion";
// import { Home, List, User } from "lucide-react";

const moods = [
  { name: "ANGRY", emoji: "😠" },
  { name: "SAD", emoji: "😭" },
  { name: "NEUTRAL", emoji: "😐" },
  { name: "HAPPY", emoji: "😊" },
  { name: "JOY", emoji: "😁" },
];

const MoodPage: React.FC = () => {
  const [selectedMood, setSelectedMood] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState<string | null>(null);
  const { setActivePage } = useActivePageContext();
  const { user } = useAuthContext();
  const [note, setNote] = useState("");
  const NOTE_MAX = 280;

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if ((e.key === "Enter" || e.keyCode === 13) && selectedMood) {
        handleConfirm();
      }
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedMood]);

  const MOOD_VALUE: Record<string, number> = {
    ANGRY: 1,
    SAD: 2,
    NEUTRAL: 3,
    HAPPY: 4,
    JOY: 5,
  };

  // Jika user sudah input mood untuk hari ini, langsung arahkan ke SavedMood
  useEffect(() => {
    function localYMD(d: Date) {
      const y = d.getFullYear();
      const m = String(d.getMonth() + 1).padStart(2, "0");
      const day = String(d.getDate()).padStart(2, "0");
      return `${y}-${m}-${day}`;
    }
    const userId = user?.uuid || localStorage.getItem("userUuid");
    if (!userId) return;
    try {
      // gunakan kunci per-user agar tidak menimpa antar akun
      const raw = localStorage.getItem(`lastMoodEntry:${userId}`);
      if (!raw) return;
      const entry = JSON.parse(raw);
      const today = localYMD(new Date());
      let entryDate: string | undefined = entry?.date;
      if (!entryDate && entry?.createdAt) {
        entryDate = localYMD(new Date(entry.createdAt));
      }
      if (entryDate === today) {
        setActivePage("SavedMood");
      }
    } catch {}
  }, [setActivePage, user?.uuid]);

  const handleConfirm = async () => {
    if (!selectedMood) return;
    setSaveError(null);
    setSaving(true);
    const moodNumber = MOOD_VALUE[selectedMood];
    const userId = user?.uuid || localStorage.getItem("userUuid");
    if (!userId) {
      setSaveError("User ID tidak ditemukan. Silakan login ulang.");
      setSaving(false);
      return;
    }
    try {
      const res = await fetch(`http://localhost:8080/api/mood-entries/users/${userId}/mood`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ mood: moodNumber, reason: note.trim() }),
      });
      if (!res.ok) {
        let msg = res.statusText || "Gagal menyimpan mood";
        try {
          const errBody = await res.json();
          msg = errBody.message || errBody.error || JSON.stringify(errBody);
        } catch {}
        setSaveError(msg);
        setSaving(false);
        return;
      }
      const data = await res.json();
      // simpan respon API di kunci khusus per-user agar tidak saling menimpa
      localStorage.setItem(`lastMoodEntry:${userId}`, JSON.stringify({ ...data, userId }));
      // arahkan ke halaman SavedMood
      setActivePage("SavedMood");
    } catch (e) {
      setSaveError("Jaringan bermasalah. Coba lagi.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="flex flex-col items-center min-h-screen bg-linear-150 from-orange-300 to-yellow-300">

      {/* Main Section */}
      <div className="flex pt-20 flex-col items-center justify-center mt-16 text-center px-4 lg:px-0">
        {/* Big Smile */}
        <div className="text-9xl lg:text-[10rem] text-brown-700 font-bold mb-6">😊</div>

        <h2 className="text-3xl lg:text-5xl font-extrabold text-brown-700 mb-12">
          HOW’S YOUR MOOD TODAY?
        </h2>

        {/* Mood Cards */}
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 gap-8 lg:gap-10">
          {moods.map((mood) => (
            <motion.button
              key={mood.name}
              whileHover={{ scale: 1.1 }}
              whileTap={{ scale: 0.95 }}
              onClick={() => setSelectedMood(mood.name)}
              className={`flex flex-col items-center justify-center w-32 sm:w-36 md:w-40 h-40 sm:h-44 md:h-48 rounded-3xl shadow-md transition-all ${
                selectedMood === mood.name
                  ? "bg-yellow-400 shadow-lg scale-105"
                  : "bg-white"
              }`}
            >
              <span className="text-6xl sm:text-7xl lg:text-8xl mb-3">{mood.emoji}</span>
              <span className="font-extrabold text-brown-800 text-sm sm:text-base md:text-lg lg:text-xl">{mood.name}</span>
            </motion.button>
          ))}
        </div>

        {/* Selected Mood Feedback */}
        {selectedMood && (
          <>
            {/* Textarea sederhana saja */}
            <textarea
              value={note}
              onChange={(e) => setNote(e.target.value.slice(0, NOTE_MAX))}
              rows={1}
              placeholder={`Catatan untuk mood ${selectedMood?.toLowerCase()}. Contoh: Hari ini aku merasa lebih tenang...`}
              className="mt-10 w-full max-w-2xl mx-auto block h-12 rounded-full border border-yellow-300/70 bg-white/90 focus:outline-none focus:ring-4 focus:ring-yellow-300/60 focus:border-yellow-400 px-5 py-3 text-brown-800 placeholder-brown-400/70 shadow-sm resize-none"
            />
            <div className="mt-2 text-xs lg:text-sm text-brown-600 w-full max-w-2xl mx-auto flex justify-end">
              <span>{note.length}/{NOTE_MAX}</span>
            </div>

            <motion.button
              whileHover={{ scale: selectedMood ? 1.03 : 1 }}
              whileTap={{ scale: selectedMood ? 0.98 : 1 }}
              onClick={handleConfirm}
              disabled={!selectedMood || saving}
              className={`mt-6 px-8 py-4 rounded-full text-xl font-bold transition-all duration-200 ${
                selectedMood && !saving
                  ? "bg-black text-white hover:brightness-105"
                  : "bg-gray-300 text-gray-600 cursor-not-allowed"
              }`}
              aria-disabled={!selectedMood}
            >
              {saving ? "Menyimpan..." : "Simpan"}
            </motion.button>

            {saveError && (
              <div className="mt-4 text-sm text-red-600 font-semibold">{saveError}</div>
            )}
          </>
        )}
      </div>
    </div>
  );
};

export default MoodPage;
