import { useState } from "react";
import { useNavigate } from "react-router-dom";

function Register() {
    const [user, setUser] = useState({ username: "", password: "" });
    const navigate = useNavigate();
    const [error, setError] = useState(null);

    const handleChange = (e) => {
        setUser({ ...user, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        const response = await fetch("/api/user/register", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(user),
        });
        if (response.ok) {
            navigate("/login");
        } else {
            setError("User already exists");
        }
    };

    return (
        <div className="min-h-screen w-full flex items-center justify-center p-4">
            <form onSubmit={handleSubmit} className="w-full max-w-xs">
                <fieldset className="fieldset bg-base-200 border border-base-300 p-4 rounded-box w-full">
                    <legend className="fieldset-legend">Register</legend>

                    <div className="space-y-4">
                        <div>
                            <label className="fieldset-label">Username</label>
                            <input
                                type="text"
                                name="username"
                                className="input w-full"
                                placeholder="Username"
                                onChange={handleChange}
                            />
                        </div>

                        <div>
                            <label className="fieldset-label">Password</label>
                            <input
                                type="password"
                                name="password"
                                className="input w-full"
                                placeholder="Password"
                                onChange={handleChange}
                            />
                        </div>
                        {error && (
                            <div className="alert alert-error mt-4">
                                <svg xmlns="http://www.w3.org/2000/svg" className="stroke-current shrink-0 h-6 w-6" fill="none" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" />
                                </svg>
                                <span>{error}</span>
                            </div>
                        )}
                        <button type="submit" className="btn btn-neutral w-full mt-4">
                            Register
                        </button>
                    </div>
                </fieldset>
            </form>
        </div>
    );
}

export default Register;