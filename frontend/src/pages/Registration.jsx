import {useState} from "react";
import {useNavigate} from "react-router-dom";
import Error from "../components/Error.jsx";
import RegistrationForm from "../components/forms/RegistrationForm.jsx";

function Registration() {
    const navigate = useNavigate();
    const [error, setError] = useState(false);
    const [errorMsg, setErrorMsg] = useState("");

    async function handleSubmit(e, user) {
        e.preventDefault();
        try {
            const response = await fetch("/api/user/register", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(user),
            });
            if (response.ok) {
                setError(false);
                navigate("/login");
            } else {
                setErrorMsg("User already exists");
                setError(true);
            }
        } catch (err) {
            setErrorMsg(err.message);
            setError(true);
        }
    }

    return (
        <div className="min-h-screen w-full flex flex-col items-center justify-center p-4">
            <RegistrationForm onSubmit={handleSubmit}/>
            <Error errorMsg={errorMsg} display={error}/>
        </div>
    );
}

export default Registration;