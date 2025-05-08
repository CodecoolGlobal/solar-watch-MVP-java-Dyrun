import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import { useState, useEffect } from "react";
import Login from "./pages/Login";
import Register from "./pages/Register";
import SolarWatch from "./pages/SolarWatch";
import Navbar from "./components/Navbar.jsx";
import "./App.css";

function App() {
    const [isAuthenticated, setIsAuthenticated] = useState(false);

    useEffect(() => {
        const token = localStorage.getItem("jwt");
        setIsAuthenticated(!!token);
    }, []);

    function handleLogout() {
        localStorage.removeItem("jwt");
        setIsAuthenticated(false);
    }

    return (
        <Router>
            <Navbar isAuthenticated={isAuthenticated} onLogout={handleLogout} />
            <Routes>
                <Route path="/login" element={<Login setAuth={setIsAuthenticated} />} />
                <Route path="/registration" element={<Register />} />
                <Route
                    path="/solar-watch"
                    element={isAuthenticated ? <SolarWatch /> : <Navigate to="/login" />}
                />
            </Routes>
        </Router>
    );
}

export default App;