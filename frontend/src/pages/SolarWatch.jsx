import {useState} from "react";
import Error from "../components/Error.jsx";
import SolarWatchForm from "../components/forms/SolarWatchForm.jsx";
import SolarWatchContent from "../components/SolarWatchContent.jsx";
import {useAuth} from "../components/AuthProvider.jsx";

function SolarWatch() {
    const [data, setData] = useState(null);
    const [error, setError] = useState(false);
    const [errorMsg, setErrorMsg] = useState("");
    const {user, logout} = useAuth();

    function handleError(display, errorMsg) {
        setError(display);
        setErrorMsg(errorMsg);
    }

    async function handleSubmit(e, city, date) {
        e.preventDefault();
        const token = user.jwt;
        try {
            const response = await fetch(`/api/sunrise-sunset?city=${city}&date=${date}`, {
                headers: {Authorization: `Bearer ${token}`},
            });
            if (response.ok) {
                const result = await response.json();
                setData({
                    ...result,
                    sunrise: result.sunrise.replace('T', ' ').substring(0, 19),
                    sunset: result.sunset.replace('T', ' ').substring(0, 19)
                });
                handleError(false);
            } else if (response.status === 400) {
                const result = await response.json();
                setData(null);
                handleError(true, result.errorMsg);
            } else if (response.status === 401) {
                handleError(true, "Unauthorized request, redirecting to login...");
                setTimeout(() => {
                    logout();
                }, 2000);
            } else {
                handleError(true, "Something went wrong...");
            }
        } catch (err) {
            handleError(true, err.message);
        }
    }

    return (
        <div className="p-4 max-w-md mx-auto">
            <SolarWatchForm onSubmit={handleSubmit} onError={handleError}/>
            {data && (
                <SolarWatchContent data={data}/>
            )}
            <Error errorMsg={errorMsg} display={error}/>
        </div>
    );
}

export default SolarWatch;