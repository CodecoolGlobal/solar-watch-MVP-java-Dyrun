import {useAuth} from "./AuthProvider.jsx";
import {useNavigate} from "react-router-dom";
import {useEffect} from "react";

function ProtectedRoute({children}) {
    const {user} = useAuth();
    const navigate = useNavigate();

    useEffect(() => {
        if (!user) {
            navigate("/login", {replace: true});
        }
    }, [user, navigate]);

    return user ? children : null;
}

export default ProtectedRoute;