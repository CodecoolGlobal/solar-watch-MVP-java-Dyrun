import { useState } from "react";

function SolarWatch() {
    const [city, setCity] = useState("");
    const [data, setData] = useState(null);
    const [error, setError] = useState(false);

    const handleFetch = async () => {
        const token = localStorage.getItem("jwt");
        const response = await fetch(`/api/sunrise-sunset/?city=${city}`, {
            headers: { Authorization: `Bearer ${token}` },
        });
        if (response.ok) {
            const result = await response.json();
            setData(result);
            setError(false);
        } else {
            setData(null);
            setError(true);
        }
    };

    return (
        <div className="p-4 max-w-md mx-auto">
            <div className="flex gap-2 mb-4">
                <input
                    type="text"
                    value={city}
                    onChange={(e) => setCity(e.target.value)}
                    placeholder="Enter city"
                    className="input input-bordered w-full"
                />
                <button
                    onClick={handleFetch}
                    className="btn btn-primary"
                >
                    Get Sunrise/Sunset
                </button>
            </div>
            {data && (
                <div className="space-y-4">
                    {data.map((sunriseSunset, index) => (
                        <div key={index} className="card bg-base-200 shadow-md">
                            <div className="card-body">
                                <p><strong>Sunrise:</strong> {sunriseSunset.sunrise}</p>
                                <p><strong>Sunset:</strong> {sunriseSunset.sunset}</p>
                                <p><strong>Date:</strong> {sunriseSunset.date}</p>
                            </div>
                        </div>
                    ))}
                </div>
            )}
            {error && (
                <div className="alert alert-error mt-4">
                    <svg xmlns="http://www.w3.org/2000/svg" className="stroke-current shrink-0 h-6 w-6" fill="none" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    <span>There aren't any data for {city}</span>
                </div>
            )}
        </div>
    );
}

export default SolarWatch;