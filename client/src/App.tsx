import { useEffect } from "react";
import Home from "./pages/Home";
import Navbar from "./components/Navbar";
import Profile from "./pages/Profile";
import Statistic from "./pages/Statistic";
import Login from "./pages/AuthPage";
import Mood from "./pages/MoodPage";
import MoodYesterday from "./pages/MoodPageYesterday";
import SavedMood from "./pages/SavedMood";
import YesterdeySavedMood from "./pages/YesterdeySavedMood";

import { useActivePageContext } from "./context/ActivePageContext";
import { useAuthContext } from "./context/AuthContext";
import { AnimatePresence, motion } from "framer-motion";

const App = () => {
  const { activePage } = useActivePageContext();
  const { isAuthenticated } = useAuthContext();

  useEffect(() => {
    try {
      window.scrollTo({ top: 0, behavior: "smooth" });
    } catch (e) {
      window.scrollTo(0, 0);
    }
  }, [activePage]);


  const renderPage = () => {
    switch (activePage) {
      case "Mood":
        return <Mood />;
        case "YesterdeySavedMood":
          return <YesterdeySavedMood />;
      case "MoodYesterday":
        return <MoodYesterday />;
      case "SavedMood":
        return <SavedMood />;
      case "Home":
        return <Home />;
      case "Statistic":
        return <Statistic />;
      case "Profile":
        return <Profile />;
      default:
        return <Home />;
      
    }
  };

  return (
    <div>
      {!isAuthenticated ? (
        <Login />
      ) : (
        <>
          <Navbar />
          <div>
            <AnimatePresence mode="wait">
              <motion.div
                key={activePage}
                initial={{ opacity: 0, y: 8 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -8 }}
                transition={{ duration: 0.28 }}
              >
                {renderPage()}
              </motion.div>
            </AnimatePresence>
          </div>
        </>
      )}
    </div>
  );
};

export default App;
