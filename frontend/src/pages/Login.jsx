import {useState} from "react";
import {useNavigate} from "react-router-dom";
import Error from "../components/Error.jsx";
import LoginForm from "../components/forms/LoginForm.jsx";
import {useAuth} from "../components/AuthProvider.jsx";

function Login() {
    const navigate = useNavigate();
    const [error, setError] = useState(false);
    const [errorMsg, setErrorMsg] = useState("");
    const {login} = useAuth();

    async function handleSubmit(e, credentials) {
        e.preventDefault();
        try {
            const response = await fetch("/api/user/login", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(credentials),
            });
            if (response.ok) {
                const data = await response.json();
                login(data);
                setError(false);
                navigate("/solar-watch");
            } else {
                setErrorMsg("Invalid username or password");
                setError(true);
            }

        } catch (error) {
            setErrorMsg(error.message);
            setError(true);
        }
    }

    return (
        <div className="min-h-screen w-full flex flex-col items-center justify-center p-4">
            <LoginForm onSubmit={handleSubmit}/>
            <Error errorMsg={errorMsg} display={error}/>
        </div>
    );
}

export default Login;