import {useState} from "react";

const CURRENT_DATE_STRING = new Date().toISOString().split('T')[0];

function SolarWatchForm({onSubmit}) {
    const [city, setCity] = useState("Budapest");
    const [date, setDate] = useState(CURRENT_DATE_STRING);

    return (
        <form onSubmit={(e) => onSubmit(e, city, date)}>
            <div className="flex gap-2 mb-4">
                <input
                    type="text"
                    value={city}
                    onChange={(e) => setCity(e.target.value)}
                    placeholder="Enter city"
                    className="input input-bordered w-full"
                />
                <input
                    type="date"
                    value={date}
                    onChange={(e) => setDate(e.target.value)}
                    placeholder="Enter date"
                    required={true}
                    max={CURRENT_DATE_STRING}
                    className="input input-bordered w-full"
                />
                <button
                    type="submit"
                    className="btn btn-primary"
                >
                    Get Sunrise/Sunset
                </button>
            </div>
        </form>
    )
}

export default SolarWatchForm;