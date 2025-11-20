export type WeekItem = {
  date: string;        // e.g. "2025-11-20"
  dayName: string;     // e.g. "THURSDAY"
  weekNumber: number;
  mood: number | null;
  createdAt: string | null;
};