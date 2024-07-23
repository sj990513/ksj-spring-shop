import React, { useContext } from 'react';
import axiosInstance from '../axiosInstance';
import { AuthContext } from '../context/AuthContext';

function Logout() {
  const { setIsLoggedIn } = useContext(AuthContext);

  const handleLogout = () => {
    axiosInstance.post('/logout')
      .then(() => {
        // Remove access token from local storage
        localStorage.removeItem('access');

        // Update auth context
        setIsLoggedIn(false);
        
        alert("로그아웃");

        // Refresh the page
        window.location.href="/";
      })
      .catch(error => {
        console.error('There was an error logging out!', error);
      });
  };

  return (
    <button onClick={handleLogout}>로그아웃</button>
  );
}

export default Logout;