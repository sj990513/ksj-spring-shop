import React, { useState } from 'react';
import { useHistory } from 'react-router-dom';
import axiosInstance from '../axiosInstance';
import './css/Login.css';

function FindPassword() {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [message, setMessage] = useState('');
  const [isError, setIsError] = useState(false);
  const history = useHistory();

  const handleFindPassword = async () => {
    try {
      const response = await axiosInstance.post('/find/findPassword', { username, email });
      const msg = response.data;
      if (msg === "안내 메일을 전송하였습니다.") {
        setIsError(false);
      } else {
        setIsError(true);
      }
      setMessage(msg);
    } catch (error) {
      console.error('비밀번호 찾기 에러:', error);
      setIsError(true);
      setMessage("비밀번호 찾기 실패. 아이디나 이메일을 확인해주세요.");
    }
  };

  const goToLogin = () => {
    history.push('/login');
  };

  return (
    <div className="login-container">
      <h2>비밀번호 찾기</h2>
      <div className="login-form">
        <div className="form-group">
          <label>아이디</label>
          <input
            type="text"
            placeholder="아이디를 입력하세요"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
          />
        </div>
        <div className="form-group">
          <label>이메일</label>
          <input
            type="email"
            placeholder="이메일을 입력하세요"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </div>
        <button onClick={handleFindPassword} className="login-btn">비밀번호 찾기</button>
        {message && <div className={`message ${isError ? 'error' : 'success'}`}>{message}</div>}
        <div>
            <button onClick={goToLogin} className="go-to-login-btn">로그인 화면으로 이동</button>
        </div>
      </div>
    </div>
  );
}

export default FindPassword;