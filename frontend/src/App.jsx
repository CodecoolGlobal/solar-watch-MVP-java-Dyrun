import {Route, Routes} from "react-router-dom";
import Login from "./pages/Login";
import Register from "./pages/Register";
import SolarWatch from "./pages/SolarWatch";
import "./App.css";
import Layout from "./components/Layout.jsx";
import ProtectedRoute from "./components/ProtectedRoute.jsx";

function App() {

    return (
        <Routes>
            <Route path="/" element={<Layout/>}>
                <Route path="/login" element={<Login/>}/>
                <Route path="/registration" element={<Register/>}/>
                <Route path="/solar-watch" element={<ProtectedRoute><SolarWatch/></ProtectedRoute>}/>
            </Route>
        </Routes>
    );
}

export default App;