import {useState} from "react";

function LoginForm({onSubmit}) {
    const [credentials, setCredentials] = useState({username: "", password: ""});

    function handleChange(e) {
        setCredentials({...credentials, [e.target.name]: e.target.value});
    }

    return (
        <form onSubmit={(e) => onSubmit(e, credentials)} className="w-full max-w-xs">
            <fieldset className="fieldset bg-base-200 border border-base-300 p-4 rounded-box w-full">
                <legend className="fieldset-legend">Login</legend>

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
                    <button type="submit" className="btn btn-neutral w-full mt-4">
                        Login
                    </button>
                </div>
            </fieldset>
        </form>
    )
}

export default LoginForm;