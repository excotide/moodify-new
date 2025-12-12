import Home from "./pages/Home";
import Navbar from "./components/Navbar";
import Profile from "./pages/Profile";
import Statistic from "./pages/Statistic";
import Login from "./pages/AuthPage";
import Mood from "./pages/MoodPage";
import SavedMood from "./pages/SavedMood";

import { useActivePageContext } from "./context/ActivePageContext";
import { useAuthContext } from "./context/AuthContext";

const App = () => {
  const { activePage } = useActivePageContext();
  const { isAuthenticated } = useAuthContext();


  const renderPage = () => {
    switch (activePage) {
      case "Mood":
        return <Mood />;
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
      {/* If not authenticated show only the Login page */}
      {!isAuthenticated ? (
        <Login />
      ) : (
        <>
          <Navbar />
          {renderPage()}
        </>
      )}
    </div>
  );
};

export default App;
