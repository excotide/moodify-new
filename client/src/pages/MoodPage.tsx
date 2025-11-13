import React, { useState, useEffect } from "react";
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
  const [confirmed, setConfirmed] = useState(false);

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

  const handleConfirm = () => {
    if (!selectedMood) return;
    setConfirmed(true);
    // placeholder: replace with actual save/submit logic
    console.log("Mood confirmed:", selectedMood);
  };

  return (
    <div className="flex flex-col items-center min-h-screen bg-linear-150 from-orange-300 to-yellow-300">

      {/* Main Section */}
      <div className="flex flex-col items-center justify-center mt-16 text-center px-4 lg:px-0">
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
            <p className="mt-12 text-xl lg:text-2xl font-semibold text-brown-800">
              You feel <span className="underline">{selectedMood.toLowerCase()}</span> today 🌤️
            </p>

            <motion.button
              whileHover={{ scale: selectedMood ? 1.03 : 1 }}
              whileTap={{ scale: selectedMood ? 0.98 : 1 }}
              onClick={handleConfirm}
              disabled={!selectedMood}
              className={`mt-6 px-8 py-4 rounded-full text-xl font-bold transition-all duration-200 ${
                selectedMood
                  ? "bg-black text-white hover:brightness-105"
                  : "bg-gray-300 text-gray-600 cursor-not-allowed"
              }`}
              aria-disabled={!selectedMood}
            >
              Enter
            </motion.button>

            {confirmed && (
              <div className="mt-4 text-lg text-green-700 font-semibold">Saved ✓</div>
            )}
          </>
        )}
      </div>
    </div>
  );
};

export default MoodPage;
