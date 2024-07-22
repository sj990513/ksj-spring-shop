import React, { useState, useEffect } from 'react';
import { useHistory } from 'react-router-dom';
import axiosInstance from '../axiosInstance'; // axiosInstance를 import합니다.
import './SignupPage.css'; // CSS 파일을 import합니다.

const SignupPage = () => {
  const history = useHistory();

  const [formData, setFormData] = useState({
    username: '',
    password: '',
    confirmPassword: '',
    nickname: '',
    email: '',
    phone: '',
    address: '',
    verificationCode: '',
  });

  const [validationStatus, setValidationStatus] = useState({
    username: null,
    password: null,
    confirmPassword: null,
    nickname: null,
    email: null,
    phone: null,
    code: null,
  });

  const [isSendingCode, setIsSendingCode] = useState(false);
  const [isVerifyingCode, setIsVerifyingCode] = useState(false);
  const [signupMessage, setSignupMessage] = useState('');

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });

    // 입력값 변경 시 유효성 검사
    validateField(name, value);
  };

  const validateField = (name, value) => {
    let isValid = false;

    switch (name) {
      case 'username':
        isValid = value.length >= 6;
        break;
      case 'password':
        isValid = value.length >= 6;
        break;
      case 'confirmPassword':
        isValid = value === formData.password;
        break;
      case 'nickname':
        isValid = value.length >= 2;
        break;
      case 'email':
        isValid = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
        break;
      case 'phone':
        isValid = /^\d{11}$/.test(value);
        break;
      default:
        isValid = true;
    }

    setValidationStatus((prevStatus) => ({
      ...prevStatus,
      [name]: isValid,
    }));
  };

  const handleBlur = async (e) => {
    const { name, value } = e.target;

    if (validationStatus[name]) {
      try {
        const response = await axiosInstance.get(`/signup/check-${name}`, {
          params: { [name]: value },
        });
        setValidationStatus({ ...validationStatus, [name]: !response.data });
      } catch (error) {
        setValidationStatus({ ...validationStatus, [name]: false });
      }
    }
  };

  const handleSendCode = async () => {
    setIsSendingCode(true);
    try {
      await axiosInstance.post('/signup/send-code', { phoneNumber: formData.phone });
      alert('인증번호가 전송되었습니다.');
    } catch (error) {
      alert('인증번호 전송에 실패했습니다.');
    } finally {
      setIsSendingCode(false);
    }
  };

  const handleVerifyCode = async () => {
    setIsVerifyingCode(true);
    try {
      const response = await axiosInstance.post('/signup/auth', {
        phoneNumber: formData.phone,
        code: formData.verificationCode,
      });
      if (response.status === 200) {
        setValidationStatus({ ...validationStatus, code: true });
        alert('핸드폰 인증 성공');
      }
    } catch (error) {
      setValidationStatus({ ...validationStatus, code: false });
      alert('인증번호가 일치하지 않습니다. 재인증 부탁드립니다.');
    } finally {
      setIsVerifyingCode(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await axiosInstance.post('/signup', formData);
      setSignupMessage(response.data);
      if (response.status === 200) {
        alert('회원가입에 성공했습니다.');
        history.push('/login'); // 회원가입 성공 시 로그인 화면으로 이동
      }
    } catch (error) {
      setSignupMessage('회원가입에 실패했습니다.');
    }
  };

  const isFormValid = Object.values(validationStatus).every((status) => status === true);

  return (
    <div className="signup-container">
      <h2>회원가입</h2>
      <form className="signup-form" onSubmit={handleSubmit}>
        <div className="form-group">
          <label>아이디</label>
          <input type="text" name="username" value={formData.username} onChange={handleChange} onBlur={handleBlur} required />
          {validationStatus.username === false && <p className="error-text">아이디는 6자리 이상이어야 하며, 이미 존재하는 아이디입니다.</p>}
          {validationStatus.username === true && <p className="success-text">사용 가능한 아이디입니다.</p>}
        </div>
        <div className="form-group">
          <label>비밀번호</label>
          <input type="password" name="password" value={formData.password} onChange={handleChange} required />
          {validationStatus.password === false && <p className="error-text">비밀번호는 6자리 이상이어야 합니다.</p>}
        </div>
        <div className="form-group">
          <label>비밀번호 확인</label>
          <input type="password" name="confirmPassword" value={formData.confirmPassword} onChange={handleChange} required />
          {validationStatus.confirmPassword === false && <p className="error-text">비밀번호가 일치하지 않습니다.</p>}
        </div>
        <div className="form-group">
          <label>닉네임</label>
          <input type="text" name="nickname" value={formData.nickname} onChange={handleChange} onBlur={handleBlur} required />
          {validationStatus.nickname === false && <p className="error-text">닉네임은 2자리 이상이어야 하며, 이미 존재하는 닉네임입니다.</p>}
          {validationStatus.nickname === true && <p className="success-text">사용 가능한 닉네임입니다.</p>}
        </div>
        <div className="form-group">
          <label>이메일</label>
          <input type="email" name="email" value={formData.email} onChange={handleChange} onBlur={handleBlur} required />
          {validationStatus.email === false && <p className="error-text">유효한 이메일 주소여야 합니다.</p>}
          {validationStatus.email === true && <p className="success-text">사용 가능한 이메일입니다.</p>}
        </div>
        <div className="form-group">
          <label>핸드폰</label>
          <input type="text" name="phone" value={formData.phone} onChange={handleChange} onBlur={handleBlur} required />
          {validationStatus.phone === false && <p className="error-text">핸드폰 번호는 11자리 숫자여야 하며, 이미 존재하는 핸드폰번호입니다.</p>}
          {validationStatus.phone === true && <p className="success-text">사용 가능한 핸드폰번호입니다.</p>}
          <button type="button" className="send-code-btn" onClick={handleSendCode} disabled={isSendingCode || !validationStatus.phone}>인증번호 전송</button>
        </div>
        <div className="form-group">
          <label>인증번호</label>
          <input type="text" name="verificationCode" value={formData.verificationCode} onChange={handleChange} required />
          <button type="button" className="verify-code-btn" onClick={handleVerifyCode} disabled={isVerifyingCode}>인증번호 확인</button>
        </div>
        <div className="form-group">
          <label>주소</label>
          <input type="text" name="address" value={formData.address} onChange={handleChange} required />
        </div>
        <button type="submit" className="signup-btn" disabled={!isFormValid} style={{ backgroundColor: isFormValid ? 'blue' : 'gray', color: 'white', cursor: isFormValid ? 'pointer' : 'not-allowed' }}>회원가입</button>
      </form>
      {signupMessage && <p>{signupMessage}</p>}
    </div>
  );
};

export default SignupPage;