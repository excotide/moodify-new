import React, { useState } from "react";
import { motion } from "framer-motion"; // polymorphism via prop-based behavior
import { useAuthContext } from "../context/AuthContext"; // abstraction & dependency injection
import { useActivePageContext } from "../context/ActivePageContext"; // composition antar state global

const AuthPage: React.FC = () => { // "class" konseptual = encapsulation
  const [activeSide, setActiveSide] = useState<"register" | "login">("register");

  // register inputs
  const [regUser, setRegUser] = useState("");
  const [regPass, setRegPass] = useState("");

  // login inputs
  const [loginUser, setLoginUser] = useState("");
  const [loginPass, setLoginPass] = useState("");

  const showRegisterButton =
    activeSide === "register" || regUser.trim() !== "" || regPass.trim() !== "";
  const showLoginButton =
    activeSide === "login" || loginUser.trim() !== "" || loginPass.trim() !== "";

  const showRegisterWords = showRegisterButton;
  const showLoginWords = showLoginButton;

  // auth context
  const { login } = useAuthContext();
  const { setActivePage } = useActivePageContext();

  // Loading / error states for each flow
  const [loadingLogin, setLoadingLogin] = useState(false);
  const [errorLogin, setErrorLogin] = useState<string | null>(null);
  const [loadingReg, setLoadingReg] = useState(false);
  const [errorReg, setErrorReg] = useState<string | null>(null);

  const handleLogin = async () => { // menyatukan validasi + pemanggilan API (encapsulation)
    setErrorLogin(null);
    const userTrim = loginUser.trim();
    const passTrim = loginPass.trim();
    if (userTrim.length < 3) {
      setErrorLogin("Username must be at least 3 characters");
      return;
    }
    if (passTrim.length < 6) {
      setErrorLogin("Password must be at least 6 characters");
      return;
    }

    setLoadingLogin(true);
    try {
      const res = await fetch("/api/users/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username: loginUser, password: loginPass }),
      });

      if (!res.ok) {
        let msg = res.statusText || "Login failed";
        try {
          const errBody = await res.json();
          msg = errBody.message || errBody.error || JSON.stringify(errBody);
        } catch (e) {}
        setErrorLogin(msg);
        return;
      }

      const data = await res.json();
      const token = data.token || data.accessToken || data?.data?.token;
      const userObj = data.user || data.data || {};
      const userUuid = (data as any)?.userId || (data as any)?.id || userObj?.id || userObj?.uuid;
      const userInfo = typeof userObj === "object" && Object.keys(userObj).length ? userObj : undefined;
      login(token, userUuid, userInfo);
      setActivePage("Home");
    } catch (e) {
      setErrorLogin("Network error. Please try again.");
    } finally {
      setLoadingLogin(false);
    }
  };

  const handleRegister = async () => { // abstraction terhadap endpoint
    setErrorReg(null);
    const userTrim = regUser.trim();
    const passTrim = regPass.trim();
    if (userTrim.length < 3) {
      setErrorReg("Username must be at least 3 characters");
      return;
    }
    if (passTrim.length < 6) {
      setErrorReg("Password must be at least 6 characters");
      return;
    }

    setLoadingReg(true);
    try {
      const res = await fetch("/api/users/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username: regUser, password: regPass }),
      });

      if (!res.ok) {
        let msg = res.statusText || "Registration failed";
        try {
          const errBody = await res.json();
          msg = errBody.message || errBody.error || JSON.stringify(errBody);
        } catch (e) {}
        setErrorReg(msg);
        return;
      }

      const data = await res.json();
      const token = data.token || data.accessToken || data?.data?.token;
      if (token) {
        const userObj = data.user || data.data || {};
        const userUuid = userObj?.id || userObj?.uuid || data.uuid || data.id || data.userId;
        const userInfo = typeof userObj === "object" && Object.keys(userObj).length ? userObj : undefined;
        login(token, userUuid, userInfo);
        setActivePage("Home");
      } else {
        setActiveSide("login");
        setRegUser("");
        setRegPass("");
        setErrorReg("Registration successful. Please log in.");
      }
    } catch (e) {
      setErrorReg("Network error. Please try again.");
    } finally {
      setLoadingReg(false);
    }
  };

  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-gray-200 overflow-hidden py-8">
      <h1 className="text-4xl lg:text-5xl font-extrabold mb-12 text-center">
        MOODIFY: TRACK YOUR MOOD WITH US
      </h1>

      <div className="relative flex flex-col md:flex-row rounded-3xl overflow-hidden shadow-xl w-[92%] max-w-5xl bg-white">
        <motion.div
          className="absolute top-0 bottom-0 w-1/2 bg-yellow-300 rounded-3xl"
          animate={{ left: activeSide === "register" ? "0%" : "50%" }}
          transition={{ type: "spring", stiffness: 120, damping: 15 }}
        /> {/* Polymorphism: motion.div menerima props animasi berbeda tergantung state */}

        {/* Register Section - Composition: bagian terpisah tapi masih dalam komponen induk */}
        <div
          className="relative z-10 flex flex-col justify-center items-center p-8 md:p-12 md:w-1/2 transition-colors duration-300"
          onMouseEnter={() => setActiveSide("register")}
        >
          {showRegisterWords && <h2 className="text-3xl lg:text-4xl font-bold mb-6">Register</h2>}

          <input
            type="text"
            placeholder={showRegisterWords ? "Set Your Username" : ""}
            value={regUser}
            onChange={(e) => setRegUser(e.target.value)}
            onFocus={() => setActiveSide("register")}
            className="w-72 md:w-96 p-3 mb-4 rounded-full bg-white placeholder-yellow-700 font-semibold focus:outline-none focus:ring-4 focus:ring-yellow-400"
          />
          <input
            type="password"
            placeholder={showRegisterWords ? "Create Password" : ""}
            value={regPass}
            onChange={(e) => setRegPass(e.target.value)}
            onFocus={() => setActiveSide("register")}
            className="w-72 md:w-96 p-3 mb-6 rounded-full bg-white placeholder-yellow-700 font-semibold focus:outline-none focus:ring-4 focus:ring-yellow-400"
          />

          <div className="w-full flex flex-col items-center">
            <button
              className="bg-black text-white px-10 py-3 rounded-3xl font-extrabold text-lg hover:bg-gray-700 transition disabled:opacity-60"
              disabled={loadingReg}
              onClick={handleRegister}
            >
              {loadingReg ? "Creating..." : "Create"}
            </button>
            {errorReg && <p className="text-red-500 mt-3 text-center">{errorReg}</p>}
          </div>
        </div>

        {/* Login Section - Composition & conditional highlighting lewat activeSide */}
        <div
          className="relative z-10 flex flex-col justify-center items-center p-8 md:p-12 md:w-1/2 transition-colors duration-300"
          onMouseEnter={() => setActiveSide("login")}
        >
          {showLoginWords && <h2 className="text-3xl lg:text-4xl font-bold mb-6">Login</h2>}

          <input
            type="text"
            placeholder={showLoginWords ? "Insert Username" : ""}
            value={loginUser}
            onChange={(e) => setLoginUser(e.target.value)}
            onFocus={() => setActiveSide("login")}
            className="w-72 md:w-96 p-3 mb-4 rounded-full bg-white placeholder-yellow-700 font-semibold focus:outline-none focus:ring-4 focus:ring-yellow-400"
          />
          <input
            type="password"
            placeholder={showLoginWords ? "Insert Password" : ""}
            value={loginPass}
            onChange={(e) => setLoginPass(e.target.value)}
            onFocus={() => setActiveSide("login")}
            className="w-72 md:w-96 p-3 mb-6 rounded-full bg-white placeholder-yellow-700 font-semibold focus:outline-none focus:ring-4 focus:ring-yellow-400"
          />

          <div className="w-full flex flex-col items-center">
            <button
              className="bg-black text-white px-10 py-3 rounded-3xl font-extrabold text-lg hover:bg-gray-700 transition disabled:opacity-60"
              disabled={loadingLogin}
              onClick={handleLogin}
            >
              {loadingLogin ? "Logging in..." : "Login"}
            </button>
            {errorLogin && <p className="text-red-500 mt-3 text-center">{errorLogin}</p>}
          </div>
        </div>
      </div>
    </div>
  );
};

export default AuthPage;
