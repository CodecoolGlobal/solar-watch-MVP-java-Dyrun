import Navbar from './Navbar.jsx';
import {Outlet} from 'react-router-dom';

function Layout() {
    return (
        <>
            <Navbar/>
            <main className="p-4 flex justify-center">
                <Outlet/>
            </main>
        </>
    );
}

export default Layout;