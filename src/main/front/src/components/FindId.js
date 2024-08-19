import React, { useState } from 'react';
import { useHistory } from 'react-router-dom';
import axiosInstance from '../axiosInstance';
import './css/Login.css';

function FindId() {
  const [email, setEmail] = useState('');
  const [message, setMessage] = useState('');
  const [isError, setIsError] = useState(false);
  const history = useHistory();

  const handleFindId = async () => {
    try {
      const response = await axiosInstance.post('/find/findId', { email });
      const msg = response.data;
      if (msg.includes("does not exist") || msg.includes("Please log in with that social account")) {
        setIsError(true);
      } else {
        setIsError(false);
      }
      setMessage(msg);
    } catch (error) {
      console.error('아이디 찾기 에러:', error);
      setIsError(true);
      setMessage("아이디 찾기 실패. 이메일을 확인해주세요.");
    }
  };

  const goToLogin = () => {
    history.push('/login');
  };

  return (
    <div className="login-container">
      <h2>아이디 찾기</h2>
      <div className="login-form">
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
        <button onClick={handleFindId} className="login-btn">아이디 찾기</button>
        {message && <div className={`message ${isError ? 'error' : 'success'}`}>{message}</div>}
        <div>
            <button onClick={goToLogin} className="go-to-login-btn">로그인 화면으로 이동</button>
        </div>
      </div>
    </div>
  );
}

export default FindId;