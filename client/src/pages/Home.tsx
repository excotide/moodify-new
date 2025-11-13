const Home = () => {
  return (
    <div className="Home h-screen pt-20 lg:pt-28 bg-zinc-100">

      {/* Header Section */}
      <div className="flex items-center justify-between px-6 py-4">
        <div className="flex items-center gap-2">
          <div className="w-10 h-10 lg:w-12 lg:h-12 bg-zinc-300 rounded-full flex items-center justify-center">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth="1.5" stroke="currentColor" className="w-6 h-6">
              <path strokeLinecap="round" strokeLinejoin="round" d="M12 12c2.485 0 4.5-2.015 4.5-4.5S14.485 3 12 3 7.5 5.015 7.5 7.5 9.515 12 12 12z" />
              <path strokeLinecap="round" strokeLinejoin="round" d="M12 12c-2.485 0-4.5 2.015-4.5 4.5S9.515 21 12 21s4.5-2.015 4.5-4.5-2.015-4.5-4.5-4.5z" />
            </svg>
          </div>
          <span className="text-lg lg:text-5xl font-semibold">Hi, {"{user}"}</span>
        </div>
      </div>

      {/* Calendar Section */}
      <div className="mt-10 px-6">
        <div className="bg-white p-4 rounded-3xl shadow-md">
          <div className="bg-purple-200 text-purple-700 px-3 py-1 rounded-full text-sm lg:text-3xl font-medium items-center text-center w-fit mx-auto mb-10">November 2025</div>
          <div className="grid grid-cols-7 gap-4">
            {['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'].map((day, index) => (
            <div key={index} className="text-center">
              <div className="text-sm font-medium lg:text-3xl text-zinc-500 mb-5">{day}</div>
              <div className={`text-lg lg:text-5xl font-bold ${day === 'Thu' ? 'text-white bg-orange-400 rounded-full w-10 h-10 lg:w-17 lg:h-17 flex items-center justify-center mx-auto' : ''}`}>
                {10 + index}
              </div>
            </div>
            ))}
          </div>
        </div>
      </div>

      {/* Action Buttons Section */}
      <div className="mt-10 px-6 grid grid-cols-2 gap-10">
        <div className="group bg-orange-400 text-white p-4 lg:p-20 rounded-3xl shadow-md flex flex-col items-center transform transition-transform duration-300 hover:scale-105 hover:shadow-2xl cursor-pointer">
          <span className="text-lg lg:text-4xl font-semibold">Track Current Mood</span>
          <button className="mt-2 bg-white text-orange-400 px-4 py-2 rounded-full font-bold lg:text-4xl transform transition-transform duration-300 group-hover:scale-105 hover:bg-slate-100 active:scale-95">START</button>
        </div>
        <div className="group bg-violet-400 text-white p-4 lg:p-20 rounded-3xl shadow-md flex flex-col items-center transform transition-transform duration-300 hover:scale-105 hover:shadow-2xl cursor-pointer">
          <span className="text-lg lg:text-4xl font-semibold">Weekly Track</span>
          <button className="mt-2 bg-white text-violet-500 px-4 py-2 rounded-full font-bold lg:text-4xl transform transition-transform duration-300 group-hover:scale-105 hover:bg-slate-100 active:scale-95">SEE</button>
        </div>
      </div>
    </div>
      
  );
};

export default Home;