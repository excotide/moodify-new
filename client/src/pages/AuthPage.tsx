import React, { useState } from "react";
import { motion } from "framer-motion";

const AuthPage: React.FC = () => {
  const [activeSide, setActiveSide] = useState<"register" | "login">("register");

  // register inputs
  const [regUser, setRegUser] = useState("");
  const [regPass, setRegPass] = useState("");

  // login inputs
  const [loginUser, setLoginUser] = useState("");
  const [loginPass, setLoginPass] = useState("");

  // decide when to show buttons:
  // - show a side's button when that side is active (focused by user)
  // - or when that side already has input content (so it's 'in use')
  const showRegisterButton =
    activeSide === "register" || regUser.trim() !== "" || regPass.trim() !== "";
  const showLoginButton =
    activeSide === "login" || loginUser.trim() !== "" || loginPass.trim() !== "";

  // also use same rules to show/hide the heading text and placeholders
  const showRegisterWords = showRegisterButton;
  const showLoginWords = showLoginButton;

  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-gray-200 overflow-hidden py-8">
      <h1 className="text-4xl lg:text-5xl font-extrabold mb-12 text-center">
        MOODIFY: TRACK YOUR MOOD WITH US
      </h1>

      <div className="relative flex flex-col md:flex-row rounded-3xl overflow-hidden shadow-xl w-[92%] max-w-5xl bg-white">
        {/* Animated yellow background */}
        <motion.div
          className="absolute top-0 bottom-0 w-1/2 bg-yellow-300 rounded-3xl"
          animate={{
            left: activeSide === "register" ? "0%" : "50%",
          }}
          transition={{ type: "spring", stiffness: 120, damping: 15 }}
        />

        {/* Register Section */}
        <div
          className="relative z-10 flex flex-col justify-center items-center p-12 md:p-16 md:w-1/2 transition-colors duration-300"
          onMouseEnter={() => setActiveSide("register")}
        >
          {showRegisterWords && <h2 className="text-4xl lg:text-5xl font-bold mb-8">Register</h2>}

          <input
            type="text"
            placeholder={showRegisterWords ? "Set Your Username" : ""}
            value={regUser}
            onChange={(e) => setRegUser(e.target.value)}
            onFocus={() => setActiveSide("register")}
            className="w-96 p-4 mb-5 rounded-full bg-white placeholder-yellow-700 font-semibold focus:outline-none focus:ring-4 focus:ring-yellow-400"
          />
          <input
            type="password"
            placeholder={showRegisterWords ? "Create Password" : ""}
            value={regPass}
            onChange={(e) => setRegPass(e.target.value)}
            onFocus={() => setActiveSide("register")}
            className="w-96 p-4 mb-8 rounded-full bg-white placeholder-yellow-700 font-semibold focus:outline-none focus:ring-4 focus:ring-yellow-400"
          />

          {showRegisterButton && (
            <button
              className="bg-black text-white px-12 py-4 rounded-3xl font-extrabold text-2xl hover:bg-gray-700 transition"
              aria-hidden={!showRegisterButton}
            >
              Create
            </button>
          )}
        </div>

        {/* Login Section */}
        <div
          className="relative z-10 flex flex-col justify-center items-center p-12 md:p-16 md:w-1/2 transition-colors duration-300"
          onMouseEnter={() => setActiveSide("login")}
        >
          {showLoginWords && <h2 className="text-4xl lg:text-5xl font-bold mb-8">Login</h2>}

        <input
            type="text"
            placeholder={showLoginWords ? "Insert Username" : ""}
            value={loginUser}
            onChange={(e) => setLoginUser(e.target.value)}
            onFocus={() => setActiveSide("login")}
            className="w-96 p-4 mb-5 rounded-full bg-white placeholder-yellow-700 font-semibold focus:outline-none focus:ring-4 focus:ring-yellow-400"
          />
          <input
            type="password"
            placeholder={showLoginWords ? "Insert Password" : ""}
            value={loginPass}
            onChange={(e) => setLoginPass(e.target.value)}
            onFocus={() => setActiveSide("login")}
            className="w-96 p-4 mb-8 rounded-full bg-white placeholder-yellow-700 font-semibold focus:outline-none focus:ring-4 focus:ring-yellow-400"
          />

          {showLoginButton && (
            <button
              className="bg-black text-white px-12 py-4 rounded-3xl font-extrabold text-2xl hover:bg-gray-700 transition"
              aria-hidden={!showLoginButton}
            >
              Login
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default AuthPage;
