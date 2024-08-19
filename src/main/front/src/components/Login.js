import React, { useState, useContext } from 'react';
import { Link } from 'react-router-dom';  
import axiosInstance from '../axiosInstance';
import { useHistory } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import './css/Login.css'; 
import googleIcon from '../image/google.png';
import naverIcon from '../image/naver.png';

function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const history = useHistory();
  const { setIsLoggedIn, setUser } = useContext(AuthContext);

  const handleLogin = async () => {
    try {
      const response = await axiosInstance.post('/login', { username, password });

      const accessToken = response.headers['access'];
      localStorage.setItem('access', accessToken);

      const userInfoResponse = await axiosInstance.get('/check-auth');
      setIsLoggedIn(true);
      setUser(userInfoResponse.data);

      history.push('/');
    } catch (error) {
      console.error('There was an error logging in!', error);
      alert("로그인 실패. 아이디나 비밀번호를 확인해주세요.");
    }
  };

  const onNaverLogin = () => {
    window.location.href = process.env.REACT_APP_OAUTH_NAVER_URL;
  };

  const onGoogleLogin = () => {
    window.location.href = process.env.REACT_APP_OAUTH_GOOGLE_URL;
  };

  return (
    <div className="login-container">
      <h2>로그인</h2>
      <form className="login-form" onSubmit={(e) => { e.preventDefault(); handleLogin(); }}>
        <div className="form-group">
          <label>아이디</label>
          <input
            type="text"
            placeholder="아이디"
            value={username}
            onChange={e => setUsername(e.target.value)}
            required
          />
        </div>
        <div className="form-group">
          <label>비밀번호</label>
          <input
            type="password"
            placeholder="비밀번호"
            value={password}
            onChange={e => setPassword(e.target.value)}
            required
          />
        </div>
        <button type="submit" className="qwerlogin-btn">로그인</button>
      </form>
      <div className="find-buttons">
        <Link to="/find-id">아이디 찾기</Link>
        <Link to="/find-password">비밀번호 찾기</Link>
      </div>
      <div className="oauth-buttons">
        <button onClick={onGoogleLogin} className="oauth-btn google">
          <img src={googleIcon} alt="Google" />
          구글 로그인
        </button>
        <div></div>
        <button onClick={onNaverLogin} className="oauth-btn naver">
          <img src={naverIcon} alt="Naver" />
          네이버 로그인
        </button>
      </div>
    </div>
  );
}

export default Login;