import React, { useEffect, useState } from 'react';
import axiosInstance from '../axiosInstance';

function MainScreen() {
  const [user, setUser] = useState(null);
  const [message, setMessage] = useState('');

  useEffect(() => {
    axiosInstance.get('/')
      .then(response => {
        if (response.data.username) {
          setUser(response.data);
        } else {
          setMessage(response.data);
        }
      })
      .catch(error => {
        console.error('There was an error fetching the user info!', error);
      });
  }, []);

  return (
    <div>
      <h1>메인화면</h1>
      {user ? (
        <div>
          <p>로그인중인 사용자: [{user.nickname}]</p>
        </div>
      ) : (
        <div>
          <p>{message}</p>
        </div>
      )}
    </div>
  );
}

export default MainScreen;