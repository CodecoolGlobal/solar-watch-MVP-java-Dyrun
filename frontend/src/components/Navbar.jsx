import { Link, useNavigate } from "react-router-dom";

function Navbar({ isAuthenticated, onLogout }) {
    const navigate = useNavigate();

    const handleLogout = () => {
        onLogout();
        navigate("/login");
    };

    return (
        <nav className="sticky top-0 z-50 bg-blue-600 p-4 text-white flex justify-between items-center shadow-lg">
            <h1 className="text-xl font-bold">SolarWatch</h1>
            <div className="space-x-4">
                {!isAuthenticated ? (
                    <>
                        <Link to="/login" className="btn">Login</Link>
                        <Link to="/registration" className="btn">Register</Link>
                    </>
                ) : (
                    <>
                        <Link to="/solar-watch" className="btn">SolarWatch</Link>
                        <button onClick={handleLogout} className="btn btn-error">Logout</button>
                    </>
                )}
            </div>
        </nav>
    );

}

export default Navbar;