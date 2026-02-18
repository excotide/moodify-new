import React, { useEffect, useState } from "react";
import { useAuthContext } from "../context/AuthContext";
import { useUserProfile } from "../hooks/useUserProfile";
import { User, Cake, VenusAndMars, LogOut, Heart, Pencil, Hash } from "lucide-react";

const API_BASE = import.meta.env.VITE_API_URL ?? "";

const Profile: React.FC = () => {
  const { logout } = useAuthContext();
  const { profile, loading: profileLoading } = useUserProfile();
  const [isEditing, setIsEditing] = useState(false);
  const [birthday, setBirthday] = useState("");
  const [gender, setGender] = useState("");
  const [hobby, setHobby] = useState("");
  const [infoLoading, setInfoLoading] = useState(false);
  const [infoSaving, setInfoSaving] = useState(false);
  const [infoError, setInfoError] = useState<string | null>(null);

  const toDateInput = (s?: string | null) => {
    if (!s) return "";
    return s.length >= 10 ? s.slice(0, 10) : s;
  };

  useEffect(() => {
    const id = (profile?.id || undefined) ?? (typeof window !== "undefined" ? localStorage.getItem("userUuid") || undefined : undefined);
    if (!id) return;
    const ac = new AbortController();
    const run = async () => {
      setInfoLoading(true);
      setInfoError(null);
      try {
        const res = await fetch(`${API_BASE}/api/user/${id}/info`, { signal: ac.signal });
        if (!res.ok) {
          let msg = res.statusText || "Gagal memuat informasi personal";
          try { const b = await res.json(); msg = b.message || b.error || JSON.stringify(b); } catch {}
          setInfoError(msg);
          return;
        }
        const data = await res.json() as { birthDate: string | null; gender: string | null; hobbies: string[] | null };
        setBirthday(toDateInput(data.birthDate));
        setGender(data.gender || "");
        setHobby(Array.isArray(data.hobbies) ? data.hobbies.join(", ") : "");
      } catch (e) {
        if ((e as any)?.name !== "AbortError") setInfoError("Jaringan bermasalah. Coba lagi.");
      } finally {
        setInfoLoading(false);
      }
    };
    run();
    return () => ac.abort();
  }, [profile?.id]);

  const handleEditOrSave = async () => {
    if (!isEditing) {
      setIsEditing(true);
      return;
    }
    const id = (profile?.id || undefined) ?? (typeof window !== "undefined" ? localStorage.getItem("userUuid") || undefined : undefined);
    if (!id) { setIsEditing(false); return; }
    setInfoSaving(true);
    setInfoError(null);
    try {
      const hobbies = hobby.split(",").map((s) => s.trim()).filter(Boolean);
      const body = {
        birthDate: birthday ? birthday : null,
        gender: gender || null,
        hobbies,
      };
      const res = await fetch(`${API_BASE}/api/user/${id}/info`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
      });
      if (!res.ok) {
        let msg = res.statusText || "Gagal menyimpan informasi";
        try { const b = await res.json(); msg = b.message || b.error || JSON.stringify(b); } catch {}
        setInfoError(msg);
        return;
      }
      setIsEditing(false);
    } catch (e) {
      setInfoError("Jaringan bermasalah. Coba lagi.");
    } finally {
      setInfoSaving(false);
    }
  };
  return (
    <div className="min-h-screen pt-20 bg-[#FFBE5C] flex flex-col items-center font-sans py-12">

      {/* ============================ PROFILE ============================ */}
      <div className="flex flex-col md:flex-row items-center gap-8 mt-6 lg:mt-10 w-full justify-center px-4 lg:px-0">
        <div className="bg-white rounded-3xl p-8 lg:p-10 flex flex-col items-center shadow-lg w-72 lg:w-80 min-h-[260px] transform transition-all duration-200 hover:shadow-2xl hover:-translate-y-1">
          <User size={140} className="text-[#FFBE5C]" />
          <p className="text-2xl lg:text-3xl font-semibold mt-4">User</p>
          <button
            onClick={logout}
            className="mt-5 bg-[#FFBE5C] text-black px-4 py-2 rounded-xl font-semibold shadow hover:bg-[#ffd07f] active:scale-95 transition flex items-center gap-2"
          >
            <LogOut size={18} />
            Logout
          </button>
        </div>

        {/* ============================ PERSONAL INFORMATION ============================ */}
        <div className="bg-white rounded-3xl p-6 lg:p-10 shadow-lg w-[360px] md:w-[520px] lg:w-[640px] min-h-[360px] transform transition-all duration-200 hover:shadow-2xl hover:-translate-y-1">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-xl lg:text-2xl font-bold text-black">Personal Information</h2>
            <button
              type="button"
              onClick={handleEditOrSave}
              className="flex items-center gap-2 bg-zinc-100 hover:bg-zinc-200 text-zinc-800 px-3 py-2 rounded-lg text-sm font-semibold shadow disabled:opacity-60"
              disabled={infoLoading || infoSaving}
            >
              <Pencil size={16} /> {isEditing ? (infoSaving ? "Menyimpan..." : "Simpan") : "Edit"}
            </button>
          </div>
          {infoError && (
            <div className="mb-3 text-sm text-red-600">{infoError}</div>
          )}

          {/* ============================ BIRTHDAY ============================ */}
          <div className="flex items-center justify-between py-4 border-b min-h-16">
            <div className="flex items-center gap-5">
              <Cake size={32} />
              <span className="font-semibold text-lg lg:text-xl">Birthday</span>
            </div>
            {isEditing ? (
              <div className="flex items-center gap-2">
                <input
                  type="date"
                  value={birthday}
                  onChange={(e) => setBirthday(e.target.value)}
                  className="rounded-xl bg-[#FFBE5C] px-4 py-2 outline-none text-black text-sm lg:text-base"
                  disabled={infoSaving}
                />
              </div>
            ) : (
              <div className="text-right min-w-[180px] text-sm lg:text-base font-medium text-zinc-800">
                {(infoLoading && !birthday) ? "Loading..." : (birthday || "-")}
              </div>
            )}
          </div>

          {/* ============================ GENDER ============================ */}
          <div className="flex items-center justify-between py-4 min-h-16">
            <div className="flex items-center gap-4">
              <VenusAndMars size={32} />
              <span className="font-semibold text-lg lg:text-xl">Gender</span>
            </div>
            {isEditing ? (
              <div className="flex items-center gap-2">
                <select
                  value={gender}
                  onChange={(e) => setGender(e.target.value)}
                  className="rounded-xl bg-[#FFBE5C] px-4 py-2 outline-none text-black text-sm lg:text-base"
                  disabled={infoSaving}
                >
                  <option value="">Select</option>
                  <option value="male">Male</option>
                  <option value="female">Female</option>
                  <option value="non-binary">Non-binary</option>
                </select>
              </div>
            ) : (
              <div className="text-right min-w-[180px] text-sm lg:text-base font-medium text-zinc-800">
                {(infoLoading && !gender) ? "Loading..." : (gender || "-")}
              </div>
            )}
          </div>

          {/* ============================ HOBBY ============================ */}
          <div className="flex items-center justify-between py-4 border-t min-h-16">
            <div className="flex items-center gap-4">
              <Heart size={32} />
              <span className="font-semibold text-lg lg:text-xl">Hobi</span>
            </div>
            {isEditing ? (
              <div className="flex items-center gap-2">
                <input
                  type="text"
                  value={hobby}
                  onChange={(e) => setHobby(e.target.value)}
                  placeholder="Contoh: membaca, olahraga"
                  className="rounded-xl bg-[#FFBE5C] px-4 py-2 outline-none text-black text-sm lg:text-base w-56 md:w-72"
                  disabled={infoSaving}
                />
              </div>
            ) : (
              <div className="text-right min-w-[180px] text-sm lg:text-base font-medium text-zinc-800">
                {(infoLoading && !hobby) ? "Loading..." : (hobby || "-")}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* ============================ ACCOUNT INFORMATION ============================ */}
      <div className="bg-white rounded-3xl p-6 lg:p-10 shadow-lg mt-6 w-[360px] md:w-[1000px] transform transition-all duration-200 hover:shadow-2xl hover:-translate-y-1">
        <h2 className="text-xl lg:text-2xl font-bold mb-6 text-black">Account Information</h2>

        {/*  ============================ ID ============================ */}
        <div className="flex items-center gap-4 py-3 min-h-16">
          <Hash size={30} />
          <span className="font-semibold text-lg lg:text-xl w-32">ID</span>
          <div className="flex-1 rounded-xl bg-[#FFBE5C] px-4 py-2 text-sm lg:text-base font-medium">
            {profileLoading ? "Loading..." : (profile?.id || "-")}
          </div>
        </div>

        {/*  ============================ USERNAME ============================ */}
        <div className="flex items-center gap-4 py-3 mt-4 min-h-16">
          <User size={30} />
          <span className="font-semibold text-lg lg:text-xl w-32">Username</span>
          <div className="flex-1 rounded-xl bg-[#FFBE5C] px-4 py-2 text-sm lg:text-base font-medium">
            {profileLoading ? "Loading..." : (profile?.username || "-")}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Profile;
