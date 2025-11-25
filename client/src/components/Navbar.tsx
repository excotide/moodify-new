import { House, Grid, User } from "lucide-react";
import { useActivePageContext } from "../context/ActivePageContext";

const Navbar = () => {
  const { activePage, setActivePage } = useActivePageContext();

  return (
    // ==================================NAVBAR======================================
    <div className="fixed top-0 left-0 w-full z-50 pointer-events-auto">
      <div className="mx-auto max-w-5xl px-4 sm:px-6 py-4 sm:py-6 bg-white rounded-b-3xl shadow-md grid grid-cols-3 items-center">
        <button
          aria-label="Home"
          className={`flex items-center justify-center ${activePage === "Home" ? "text-black" : "text-zinc-400"}`}
          onClick={() => setActivePage("Home")}
        >
          <House className="w-8 h-8 lg:w-12 lg:h-12" />
        </button>

        <button
          aria-label="EntryPage"
          className={`flex items-center justify-center ${activePage === "Statistic" ? "text-black" : "text-zinc-400"}`}
          onClick={() => setActivePage("Statistic")}
        >
          <Grid className="w-8 h-8 lg:w-12 lg:h-12" />
        </button>

        <button
          aria-label="Profile"
          className={`flex items-center justify-center ${activePage === "Profile" ? "text-black" : "text-zinc-400"}`}
          onClick={() => setActivePage("Profile")}
        >
          <User className="w-8 h-8 lg:w-12 lg:h-12" />
        </button>
      </div>
    </div>
  );
};

export default Navbar;