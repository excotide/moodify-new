import React from "react";
import { useAuthContext } from "../context/AuthContext";
import { User, Mail, Lock, Cake, VenusAndMars } from "lucide-react";

const Profile: React.FC = () => {
  const { logout } = useAuthContext();
  return (
    <div className="min-h-screen bg-[#FFBE5C] flex flex-col items-center font-sans py-12">

      {/* Profile Section */}
      <div className="flex flex-col md:flex-row items-center gap-8 mt-6 lg:mt-10 w-full justify-center px-4 lg:px-0">
        <div className="bg-white rounded-3xl p-8 lg:p-10 flex flex-col items-center shadow-lg w-72 lg:w-80">
          <User size={140} className="text-[#FFBE5C]" />
          <p className="text-2xl lg:text-3xl font-semibold mt-4">User</p>
        </div>

        {/* Personal Information */}
        <div className="bg-white rounded-3xl p-6 lg:p-10 shadow-lg w-[360px] md:w-[520px] lg:w-[640px]">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-xl lg:text-2xl font-bold text-black">Personal Information</h2>
            <button
              onClick={logout}
              className="bg-[#FFBE5C] text-black px-4 py-2 rounded-xl font-semibold shadow hover:bg-[#ffd07f] active:scale-95 transition"
            >
              Logout
            </button>
          </div>

          {/* Birthday */}
          <div className="flex items-center justify-between py-4 border-b">
            <div className="flex items-center gap-4">
              <Cake size={32} />
              <span className="font-semibold text-lg lg:text-xl">Birthday</span>
            </div>
            <div className="flex items-center gap-2">
              <input
                type="date"
                className="rounded-xl bg-[#FFBE5C] px-4 py-2 outline-none text-black text-sm lg:text-base"
              />
            </div>
          </div>

          {/* Gender */}
          <div className="flex items-center justify-between py-4">
            <div className="flex items-center gap-4">
              <VenusAndMars size={32} />
              <span className="font-semibold text-lg lg:text-xl">Gender</span>
            </div>
            <div className="flex items-center gap-2">
              <select className="rounded-xl bg-[#FFBE5C] px-4 py-2 outline-none text-black text-sm lg:text-base">
                <option value="">Select</option>
                <option value="male">Male</option>
                <option value="female">Female</option>
                <option value="non-binary">Non-binary</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      {/* Account Information */}
      <div className="bg-white rounded-3xl p-6 lg:p-10 shadow-lg mt-8 w-[360px] md:w-[1000px]">
        <h2 className="text-xl lg:text-2xl font-bold mb-6 text-black">Account Information</h2>

        {/* Email */}
        <div className="flex items-center gap-4 py-3">
          <Mail size={30} />
          <span className="font-semibold text-lg lg:text-xl w-32">Email</span>
          <input
            type="email"
            placeholder="Enter your email"
            className="flex-1 rounded-xl bg-[#FFBE5C] px-4 py-2 outline-none text-sm lg:text-base"
          />
        </div>

        {/* Password */}
        <div className="flex items-center gap-4 py-3 mt-4">
          <Lock size={30} />
          <span className="font-semibold text-lg lg:text-xl w-32">Password</span>
          <input
            type="password"
            placeholder="********"
            className="flex-1 rounded-xl bg-[#FFBE5C] px-4 py-2 outline-none text-sm lg:text-base"
          />
        </div>
      </div>
    </div>
  );
};

export default Profile;
