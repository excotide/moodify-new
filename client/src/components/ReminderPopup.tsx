import React from "react";
import { motion } from "framer-motion";


interface ReminderPopupProps {
  show: boolean;
  onClose: () => void;
  onGo: () => void;
}

const ReminderPopup: React.FC<ReminderPopupProps> = ({ show, onClose, onGo }) => {
  if (!show) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm">
      <div className="relative w-[90%] max-w-lg px-4">
        {/* layered shadow / base layer to mimic the sliced shadow in design */}
        <div className="absolute left-0 right-0 top-4 -z-10 flex justify-center">
          <div className="w-full max-w-lg rounded-2xl bg-white shadow-[rgba(0,0,0,0.18)_0px_18px_0px] transform translate-y-4 scale-100" style={{filter: 'blur(0px)'}} />
        </div>

        <motion.div
          initial={{ opacity: 0, scale: 0.98, y: -6 }}
          animate={{ opacity: 1, scale: 1, y: 0 }}
          exit={{ opacity: 0, scale: 0.98, y: -6 }}
          transition={{ duration: 0.18 }}
          className="relative bg-white rounded-2xl shadow-lg overflow-hidden border border-zinc-100"
          role="dialog"
          aria-modal="true"
          aria-label="Reminder popup"
        >
          {/* Header with date badge and title */}
          <div className="flex items-center justify-between gap-4 px-5 py-4">
            <div className="flex items-center gap-3">
              <div className="flex flex-col items-center justify-center bg-zinc-100 rounded-lg px-3 py-1 text-center">
                <span className="text-[10px] uppercase text-zinc-500 leading-3">sat</span>
                <span className="text-xl font-bold leading-4">5</span>
              </div>

              <div>
                <div className="text-sm font-semibold tracking-wide text-zinc-700">CALENDAR</div>
                <div className="text-xs text-zinc-400"> </div>
              </div>
            </div>

            <div className="text-xs text-zinc-400">now</div>
          </div>

          {/* divider */}
          <div className="h-px bg-zinc-200" />

          {/* Body content */}
          <div className="px-6 py-6">
            <h4 className="text-base font-semibold text-zinc-800">Hei! kamu belum input mood kemarin</h4>
            <p className="text-sm text-zinc-500 mt-3">Isi sekarang supaya statistik mingguan tetap akurat.</p>
          </div>

          {/* Actions */}
          <div className="px-6 pb-6 flex justify-end gap-3">
            <button
              onClick={onClose}
              className="px-4 py-2 rounded-lg text-sm font-semibold text-zinc-700 hover:bg-zinc-50 transition active:scale-95"
            >
              Nanti
            </button>

            <button
              onClick={onGo}
              className="px-4 py-2 rounded-lg text-sm font-semibold bg-violet-600 text-white hover:brightness-105 transition active:scale-95"
            >
              Isi sekarang
            </button>
          </div>
        </motion.div>
      </div>
    </div>
  );
};

export default ReminderPopup;
