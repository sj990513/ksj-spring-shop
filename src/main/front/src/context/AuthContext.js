import React, { createContext, useState, useEffect } from 'react';
import axiosInstance from '../axiosInstance';

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  useEffect(() => {
    // Check if the user is logged in
    axiosInstance.get('/check-auth')
      .then(response => {
        setIsLoggedIn(true);
        console.log("로그인상태");
      })
      .catch(error => {
        setIsLoggedIn(false);
        console.log("비로그인상태");
      });
  }, []);

  return (
    <AuthContext.Provider value={{ isLoggedIn, setIsLoggedIn }}>
      {children}
    </AuthContext.Provider>
  );
};