import React, { useState, useEffect } from "react";
import { motion } from "framer-motion";
import { useActivePageContext } from "../context/ActivePageContext";
import { useAuthContext } from "../context/AuthContext";
import useUserProfile from "../hooks/useUserProfile";
import useUserWeek from "../hooks/useUserWeek";

const TypingLoop: React.FC<{ text: string; speed?: number; pause?: number }> = ({ text, speed = 140, pause = 1200 }) => {
  const [displayed, setDisplayed] = useState("");
  const [isDeleting, setIsDeleting] = useState(false);

  useEffect(() => {
    setDisplayed("");
    setIsDeleting(false);
  }, [text]);

  useEffect(() => {
    let timeout: number | undefined;
    const full = text;

    if (!isDeleting && displayed === full) {
      timeout = window.setTimeout(() => setIsDeleting(true), pause);
    } else if (isDeleting && displayed === "") {
      timeout = window.setTimeout(() => setIsDeleting(false), 500);
    } else {
      timeout = window.setTimeout(() => {
        setDisplayed((prev) => {
          if (isDeleting) return full.slice(0, Math.max(0, prev.length - 1));
          return full.slice(0, Math.min(full.length, prev.length + 1));
        });
      }, isDeleting ? Math.max(20, speed / 2) : speed);
    }

    return () => {
      if (timeout) window.clearTimeout(timeout);
    };
  }, [displayed, isDeleting, text, speed, pause]);

  return <span className="text-lg lg:text-5xl font-semibold">{displayed}<span className="inline-block w-1 h-6 align-middle bg-brown-700 ml-1 animate-pulse" /></span>;
};

const Home = () => {
  const { profile, loading } = useUserProfile();
  const { week, days, loading: loadingWeek } = useUserWeek();
  const displayName = loading ? "..." : profile?.username || "User";
  const { setActivePage } = useActivePageContext();

  
  const MONTHS_ID = [
    "Januari",
    "Februari",
    "Maret",
    "April",
    "Mei",
    "Juni",
    "Juli",
    "Agustus",
    "September",
    "Oktober",
    "November",
    "Desember",
  ];

  const monthLabel = (() => {
    if (loadingWeek) return "...";
    if (!week || week.length === 0) {
      const now = new Date();
      return `${MONTHS_ID[now.getMonth()]} ${now.getFullYear()}`;
    }
    const sorted = [...week].sort((a, b) => a.date.localeCompare(b.date));
    const first = sorted[0];
    const last = sorted[sorted.length - 1];
    const [y1, m1] = first.date.split("-").map((v) => parseInt(v, 10));
    const [y2, m2] = last.date.split("-").map((v) => parseInt(v, 10));
    const label1 = `${MONTHS_ID[(m1 || 1) - 1]} ${y1}`;
    const label2 = `${MONTHS_ID[(m2 || 1) - 1]} ${y2}`;
    return label1 === label2 ? label1 : `${label1} - ${label2}`;
  })();

  const weekNumberLabel = (() => {
    if (loadingWeek) return "";
    if (!week || week.length === 0) return "";
    const nums = Array.from(
      new Set(
        week
          .map((w) => w.weekNumber)
          .filter((n) => typeof n === "number" && !Number.isNaN(n))
      )
    ).sort((a, b) => a - b);
    if (nums.length === 0) return "";
    if (nums.length === 1) return `Minggu ke-${nums[0]}`;
    return `Minggu ke-${nums[0]}-${nums[nums.length - 1]}`;
  })();

  const localYMD = (d: Date) => {
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, "0");
    const day = String(d.getDate()).padStart(2, "0");
    return `${y}-${m}-${day}`;
  };

  const yesterdayDate = localYMD(new Date(Date.now() - 24 * 60 * 60 * 1000));
  const todayDate = localYMD(new Date());

  const missingYesterdayViaWeek = (() => {
    if (loadingWeek || !week || !week.length) return false;
    const item = week.find(w => w.date === yesterdayDate);
    return item ? item.mood == null : false;
  })();

  const { isAuthenticated, user } = useAuthContext();
  const [showYestPopup, setShowYestPopup] = useState(false);

  useEffect(() => {
    if (!isAuthenticated) return;

    let just: string | null = null;
    try {
      just = localStorage.getItem("justLoggedIn");
      if (!just) return; 
    } catch (e) {
      return;
    }

    if (loadingWeek) return;

    (async () => {
      try {
        const key = `dismissedYest:${yesterdayDate}`;
        const dismissed = typeof window !== "undefined" && !!localStorage.getItem(key);
        if (dismissed) return;

        if (missingYesterdayViaWeek) {
          setShowYestPopup(true);
          return;
        }

        try {
          const id = (user as any)?.uuid || localStorage.getItem("userUuid");
          if (!id) return;
          const res = await fetch(`/api/users/${id}/moods/history`);
          if (!res.ok) return;
          const arr = await res.json();
          if (!Array.isArray(arr)) return;
          const found = arr.find((it: any) => it && it.date === yesterdayDate);
          if (found && found.mood == null) {
            setShowYestPopup(true);
          }
        } catch (e) {
        }
      } finally {
        try { localStorage.removeItem("justLoggedIn"); } catch {}
      }
    })();
  }, [isAuthenticated, loadingWeek, missingYesterdayViaWeek, yesterdayDate, user]);

  const dismissYestPopup = () => {
    try {
      const key = `dismissedYest:${yesterdayDate}`;
      if (typeof window !== "undefined") localStorage.setItem(key, "1");
    } catch {}
    setShowYestPopup(false);
  };

  const goToYesterday = () => {
    setActivePage("MoodYesterday");
    dismissYestPopup();
  };

  const anyPastMissing = (() => {
    if (loadingWeek || !week || !week.length) return false;
    return week.some(w => w.date < todayDate && w.mood == null);
  })();

  let cardMessage: string;
  let cardMode: 'yesterday' | 'gap' | 'complete';
  if (missingYesterdayViaWeek) {
    cardMessage = 'Hei, kamu belum input mood kemarin. Input sekarang!';
    cardMode = 'yesterday';
  } else if (anyPastMissing) {
    cardMessage = 'Ada hari sebelumnya belum terisi mood. Lengkapi sekarang!';
    cardMode = 'gap';
  } else {
    cardMessage = 'Terima kasih sudah memasukkan mood setiap hari';
    cardMode = 'complete';
  }

  return (
    <div className="Home h-screen pt-20 lg:pt-28 bg-zinc-100">

      {/* Header Section */}
      <div className="flex items-center justify-between px-6 py-4">
          <div className="flex items-center gap-2">
          <div className="w-10 h-10 lg:w-12 lg:h-12 bg-zinc-300 rounded-full flex items-center justify-center">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth="1.5" stroke="currentColor" className="w-6 h-6">
              <path strokeLinecap="round" strokeLinejoin="round" d="M12 12c2.485 0 4.5-2.015 4.5-4.5S14.485 3 12 3 7.5 5.015 7.5 7.5 9.515 12 12 12z" />
              <path strokeLinecap="round" strokeLinejoin="round" d="M12 12c-2.485 0-4.5 2.015-4.5 4.5S9.515 21 12 21s4.5-2.015 4.5-4.5-2.015-4.5-4.5-4.5z" />
            </svg>
          </div>
          <TypingLoop text={`Hi, ${displayName}`} />
        </div>
      </div>

      {/* Calendar Section */}
      <div className="mt-10 px-6">
        <div className="bg-white p-4 rounded-3xl shadow-md">
          <div className="bg-purple-200 text-purple-700 px-3 py-1 rounded-full text-sm lg:text-3xl font-medium items-center text-center w-fit mx-auto mb-10">
            {monthLabel}
            {weekNumberLabel ? ` • ${weekNumberLabel}` : ""}
          </div>
          {(!loadingWeek && (!week || week.length === 0)) ? (
            <div className="text-center py-10">
              <p className="text-zinc-600 text-base lg:text-2xl font-medium mb-6">belum ada data silahkan masukkan mood hari ini</p>
            </div>
          ) : (
            <div className="grid grid-cols-7 gap-4">
              {(loadingWeek || days.length === 0
                ? ["Mon","Tue","Wed","Thu","Fri","Sat","Sun"].map((d, i) => ({ dayShort: d, dayNum: 0, raw: {}, key: i }))
                : days.map((d, i) => ({ ...d, key: i }))
              ).map(({ dayShort, dayNum, raw, key }) => {
                const hasMood = raw && (raw as any).mood != null; 
                return (
                  <div key={key} className="text-center transform transition-all duration-200 hover:shadow-lg hover:-translate-y-1 rounded-xl cursor-pointer">
                    <div className="text-sm font-medium lg:text-3xl text-zinc-500 mb-5">{dayShort}</div>
                    <div
                      className={`text-lg lg:text-5xl font-bold ${hasMood ? 'text-white bg-orange-400 rounded-full w-10 h-10 lg:w-17 lg:h-17 flex items-center justify-center mx-auto' : ''}`}
                    >
                      {dayNum || ''}
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>

      {/* Action Buttons Section */}
      <div className="mt-10 px-6 grid grid-cols-1 md:grid-cols-2 gap-10 items-stretch">
        <div className="group bg-orange-400 text-white p-4 lg:p-20 rounded-3xl shadow-md flex flex-col items-center justify-between h-full transform transition-transform duration-300 hover:scale-105 hover:shadow-2xl cursor-pointer">
          <span className="text-lg lg:text-4xl font-semibold">Track Current Mood</span>
          <button
            className="mt-2 bg-white text-orange-400 px-4 py-2 rounded-full font-bold lg:text-4xl transform transition-transform duration-300 group-hover:scale-105 hover:bg-slate-100 active:scale-95"
            onClick={() => setActivePage("Mood")}
          >
            START
          </button>
        </div>
        <div className="group bg-violet-400 text-white p-4 lg:p-20 rounded-3xl shadow-md flex flex-col items-center justify-between h-full transform transition-transform duration-300 hover:scale-105 hover:shadow-2xl cursor-pointer">
          <span className="text-lg lg:text-4xl font-semibold">{cardMessage}</span>
          {cardMode === 'yesterday' && (
            <button
              onClick={() => setActivePage('MoodYesterday')}
              className="mt-2 bg-white text-violet-500 px-4 py-2 rounded-full font-bold lg:text-4xl transform transition-transform duration-300 group-hover:scale-105 hover:bg-slate-100 active:scale-95"
            >INPUT KEMARIN</button>
          )}
          {cardMode === 'gap' && (
            <button
              onClick={() => setActivePage('MoodYesterday')}
              className="mt-2 bg-white text-violet-500 px-4 py-2 rounded-full font-bold lg:text-4xl transform transition-transform duration-300 group-hover:scale-105 hover:bg-slate-100 active:scale-95"
            >INPUT</button>
          )}
          {cardMode === 'complete' && (
            <button
              onClick={() => setActivePage('Statistic')}
              className="mt-2 bg-white text-violet-500 px-4 py-2 rounded-full font-bold lg:text-4xl transform transition-transform duration-300 group-hover:scale-105 hover:bg-slate-100 active:scale-95"
            >STATS</button>
          )}
        </div>
      </div>

      {/* Reminder popup for missing yesterday mood */}
      {showYestPopup && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
          <motion.div
            initial={{ opacity: 0, scale: 0.98 }}
            animate={{ opacity: 1, scale: 1 }}
            exit={{ opacity: 0, scale: 0.98 }}
            transition={{ duration: 0.18 }}
            role="dialog"
            aria-modal="true"
            aria-label="Reminder: fill yesterday mood"
            className="max-w-lg w-full bg-white rounded-2xl shadow-xl p-6 md:p-8"
          >
            <div className="flex items-start gap-4">
              <div className="shrink-0 bg-yellow-100 rounded-full p-3">
                <span className="text-2xl">🔔</span>
              </div>
              <div className="flex-1">
                <h3 className="text-lg font-semibold">Hei — kamu belum input mood kemarin</h3>
                <p className="text-sm text-zinc-600 mt-2">Isi sekarang agar statistik mingguan lebih akurat.</p>
              </div>
            </div>

            <div className="mt-6 flex items-center justify-end gap-3">
              <button
                onClick={dismissYestPopup}
                className="px-4 py-2 rounded-lg text-sm font-semibold text-zinc-700 hover:bg-zinc-100 transition"
              >
                Nanti
              </button>
              <button
                onClick={goToYesterday}
                className="px-4 py-2 rounded-lg text-sm font-semibold bg-violet-500 text-white hover:brightness-105 transition"
              >
                Isi sekarang
              </button>
            </div>
          </motion.div>
        </div>
      )}
    </div>
      
  );
};

export default Home;