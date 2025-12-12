import { useActivePageContext } from "../context/ActivePageContext";
import useUserProfile from "../hooks/useUserProfile";
import useUserWeek from "../hooks/useUserWeek";

const Home = () => {
  const { profile, loading } = useUserProfile();
  const { week, days, loading: loadingWeek } = useUserWeek();
  const displayName = loading ? "..." : profile?.username || "User";
  const { setActivePage } = useActivePageContext();
  // highlight sekarang berdasarkan mood, bukan hari ini
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
          <span className="text-lg lg:text-5xl font-semibold">Hi, {displayName}</span>
        </div>
      </div>

      {/* Calendar Section */}
      <div className="mt-10 px-6">
        <div className="bg-white p-4 rounded-3xl shadow-md">
          <div className="bg-purple-200 text-purple-700 px-3 py-1 rounded-full text-sm lg:text-3xl font-medium items-center text-center w-fit mx-auto mb-10">{monthLabel}</div>
          <div className="grid grid-cols-7 gap-4">
            {(loadingWeek || days.length === 0
              ? ["Mon","Tue","Wed","Thu","Fri","Sat","Sun"].map((d, i) => ({ dayShort: d, dayNum: 0, raw: {}, key: i }))
              : days.map((d, i) => ({ ...d, key: i }))
            ).map(({ dayShort, dayNum, raw, key }) => {
              const hasMood = raw && (raw as any).mood != null; // highlight jika ada mood
              return (
                <div key={key} className="text-center">
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
        </div>
      </div>

      {/* Action Buttons Section */}
      <div className="mt-10 px-6 grid grid-cols-2 gap-10">
        <div className="group bg-orange-400 text-white p-4 lg:p-20 rounded-3xl shadow-md flex flex-col items-center transform transition-transform duration-300 hover:scale-105 hover:shadow-2xl cursor-pointer">
          <span className="text-lg lg:text-4xl font-semibold">Track Current Mood</span>
          <button
            className="mt-2 bg-white text-orange-400 px-4 py-2 rounded-full font-bold lg:text-4xl transform transition-transform duration-300 group-hover:scale-105 hover:bg-slate-100 active:scale-95"
            onClick={() => setActivePage("Mood")}
          >
            START
          </button>
        </div>
        <div className="group bg-violet-400 text-white p-4 lg:p-20 rounded-3xl shadow-md flex flex-col items-center transform transition-transform duration-300 hover:scale-105 hover:shadow-2xl cursor-pointer">
          <span className="text-lg lg:text-4xl font-semibold">Weekly Track</span>
          <button className="mt-2 bg-white text-violet-500 px-4 py-2 rounded-full font-bold lg:text-4xl transform transition-transform duration-300 group-hover:scale-105 hover:bg-slate-100 active:scale-95">SEE</button>
        </div>
      </div>
    </div>
      
  );
};

export default Home;