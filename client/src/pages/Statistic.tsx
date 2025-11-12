import { PieChart, Pie, Cell, Legend } from "recharts";

const Statistic = () => {
  const data = [
    { name: "Sad", value: 62.5, color: "#4A90E2" },
    { name: "Angry", value: 25, color: "#FF4D4D" },
    { name: "Happy", value: 12.5, color: "#FFD700" },
  ];

  return (
    <div className="Statistic h-screen pt-12 lg:pt-20 bg-zinc-100 flex flex-col items-center justify-center px-4 lg:px-12">
      <div className="bg-white rounded-3xl shadow-md p-4 lg:p-12 lg:flex flex-row">

        {/* Description */}
        <div className="grid">
          {/* Header Section */}
          <h1 className="text-2xl lg:text-5xl font-bold">
            This <span className="text-blue-500">Week</span>
          </h1>

          {/* Description Section */}
          <div className="bg-gray-200 p-4 rounded-lg shadow-md text-center">
            <p className="text-lg font-medium">
              "62.5% Sad, 25% Angry, 12.5% Happy — not the easiest week, but that’s okay. Emotions come and go, and tracking them means you’re learning to understand yourself better. Brighter days are coming."
            </p>
          </div>

          {/* Recommendation Section */}
          <button className="mt-4 lg:mt-6 bg-yellow-400 text-white px-6 py-3 rounded-lg shadow-md text-lg font-bold">
            We Recommend you..
          </button>
        </div>

        {/* Pie Chart Section */}
        <div className="mt-2 lg:mt-6 flex items-center justify-center">
          <PieChart
            width={window.innerWidth < 768 ? 200 : 500} // Responsif untuk layar kecil
            height={window.innerWidth < 768 ? 200 : 500} // Responsif untuk layar kecil
          >
            <Pie
              data={data}
              dataKey="value"
              nameKey="name"
              cx="50%"
              cy="50%"
              outerRadius={window.innerWidth < 768 ? 70 : 200} // Responsif untuk layar kecil
              fill="#8884d8"
            >
              {data.map((entry, index) => (
                <Cell key={`cell-${index}`} fill={entry.color} />
              ))}
            </Pie>
            <Legend />
          </PieChart>
        </div>
        
      </div>
    </div>
  );
};

export default Statistic;