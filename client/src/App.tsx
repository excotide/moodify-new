import Home from "./pages/Home";
import Navbar from "./components/Navbar";
import Profile from "./pages/Profile";
import Statistic from "./pages/Statistic";
import Login from "./pages/AuthPage";
import Mood from "./pages/MoodPage";

import { useActivePageContext } from "./context/ActivePageContext";

const App = () => {
  const { activePage } = useActivePageContext();


  const renderPage = () => {
    switch (activePage) {
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
      <Mood />
      <Login />
      <Navbar />
      {renderPage()}
    </div>
  );
};

export default App;
