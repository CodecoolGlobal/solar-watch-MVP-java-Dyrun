import {Route, Routes} from "react-router-dom";
import Login from "./pages/Login";
import Registration from "./pages/Registration.jsx";
import SolarWatch from "./pages/SolarWatch";
import "./App.css";
import Layout from "./components/Layout.jsx";
import ProtectedRoute from "./components/ProtectedRoute.jsx";

function App() {

    return (
        <Routes>
            <Route path="/" element={<Layout/>}>
                <Route path="/login" element={<Login/>}/>
                <Route path="/registration" element={<Registration/>}/>
                <Route path="/solar-watch" element={<ProtectedRoute><SolarWatch/></ProtectedRoute>}/>
            </Route>
        </Routes>
    );
}

export default App;