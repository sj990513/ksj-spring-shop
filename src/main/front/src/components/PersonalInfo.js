import React, { useState, useContext } from 'react';
import axiosInstance from '../axiosInstance';
import { AuthContext } from '../context/AuthContext';
import './MyPage.css';

const PersonalInfo = ({ userData, setUserData }) => {
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState({
    nickname: userData.nickname,
    password: '',
    confirmPassword: '',
    email: userData.email,
    address: userData.address,
  });
  const [validationStatus, setValidationStatus] = useState({
    nickname: true, // Assuming initial values are valid
    password: true,
    confirmPassword: true,
    email: true,
  });
  const [deleteInput, setDeleteInput] = useState('');
  const [showDeleteConfirmation, setShowDeleteConfirmation] = useState(false);

  const { setIsLoggedIn } = useContext(AuthContext);

  const getRoleDisplayName = (role) => {
    switch (role) {
      case 'ROLE_ADMIN':
        return '관리자';
      case 'ROLE_USER':
        return '일반회원';
      default:
        return 'Unknown Role';
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value,
    });

    // 입력값 변경 시 유효성 검사
    validateField(name, value);
  };

  const validateField = (name, value) => {
    let isValid = false;

    switch (name) {
      case 'password':
        isValid = value.length >= 6 || value === '';
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

    // 기존의 값들과 다른 경우에만 중복 검사 수행
    if (value !== userData[name] && validationStatus[name]) {
      try {
        const response = await axiosInstance.get(`/signup/check-${name}`, {
          params: { [name]: value },
        });
        setValidationStatus({ ...validationStatus, [name]: !response.data });
      } catch (error) {
        setValidationStatus({ ...validationStatus, [name]: false });
      }
    } else if (value === userData[name]) {
      // 기존 값과 동일하면 유효성 상태를 true로 설정
      setValidationStatus({ ...validationStatus, [name]: true });
    }
  };

  const handleEdit = () => {
    setIsEditing(true);
  };

  const handleCancel = () => {
    setIsEditing(false);
    setFormData({
      nickname: userData.nickname,
      password: '',
      confirmPassword: '',
      email: userData.email,
      address: userData.address,
    });
    setValidationStatus({
      nickname: true,
      password: true,
      confirmPassword: true,
      email: true,
    });
  };

  const handleSave = async () => {
    if (!validationStatus.password || !validationStatus.confirmPassword || !validationStatus.nickname || !validationStatus.email) {
      alert("입력값을 확인해주세요.");
      return;
    }

    if (formData.password && formData.password !== formData.confirmPassword) {
      alert("비밀번호가 일치하지 않습니다.");
      return;
    }

    try {
      const response = await axiosInstance.patch('/user-info/user/update', {
        ...userData,
        nickname: formData.nickname,
        password: formData.password || undefined,
        email: formData.email,
        address: formData.address,
      });
      alert('정보가 업데이트되었습니다.');
      setIsEditing(false);
      setUserData({
        ...userData,
        nickname: formData.nickname,
        email: formData.email,
        address: formData.address,
      });
    } catch (error) {
      console.error('There was an error updating the user data!', error);
      alert('업데이트에 실패했습니다.');
    }
  };

  const handleDelete = () => {
    setShowDeleteConfirmation(true);
  };

  const confirmDelete = async () => {
    if (deleteInput === "삭제를 원합니다") {
      const isConfirmed = window.confirm("정말로 회원 탈퇴를 원하십니까?");
      if (isConfirmed) {
        try {
          await axiosInstance.delete('/user-info/user/delete-user');
          alert('회원 탈퇴가 완료되었습니다.');
          setIsLoggedIn(false);
          handleLogout();
        } catch (error) {
          console.error('There was an error deleting the user!', error);
          alert('회원 탈퇴에 실패했습니다.');
        }
      }
    } else {
      alert('정확히 안내문구를 입력해주세요.');
    }
  };

  const handleLogout = async () => {
    try {
      await axiosInstance.post('/logout');
      localStorage.removeItem('access');
      setIsLoggedIn(false);
      window.location.href = "/";
    } catch (error) {
      console.error('There was an error logging out!', error);
    }
  };

  return (
    <div className="personal-info">
      <h2>개인정보</h2>
      <div className="user-info">
        <div className="form-group">
          <label><strong>아이디</strong></label>
          <p>{userData.username}</p>
        </div>
        <div className="form-group">
          <label><strong>닉네임</strong></label>
          {isEditing ? (
            <>
              <input
                type="text"
                name="nickname"
                value={formData.nickname}
                onChange={handleInputChange}
                onBlur={handleBlur}
                required
              />
              {isEditing && validationStatus.nickname === false && (
                <p className="error-text">닉네임은 2자리 이상이어야 하며, 이미 존재하는 닉네임입니다.</p>
              )}
              {isEditing && validationStatus.nickname === true && (
                <p className="success-text">사용 가능한 닉네임입니다.</p>
              )}
            </>
          ) : (
            <p>{userData.nickname}</p>
          )}
        </div>
        <div className="form-group">
          <label><strong>비밀번호</strong></label>
          {isEditing ? (
            <>
              <input
                type="password"
                name="password"
                placeholder="새 비밀번호"
                value={formData.password}
                onChange={handleInputChange}
                required
              />
              <input
                type="password"
                name="confirmPassword"
                placeholder="비밀번호 확인"
                value={formData.confirmPassword}
                onChange={handleInputChange}
                required
              />
              {formData.password !== formData.confirmPassword && (
                <p className="error-text">비밀번호가 일치하지 않습니다.</p>
              )}
              {formData.password === formData.confirmPassword && formData.password.length >= 6 && (
                <p className="success-text">비밀번호가 일치합니다.</p>
              )}
            </>
          ) : (
            <p>********</p>
          )}
        </div>
        {isEditing && validationStatus.password === false && (
          <p className="error-text">비밀번호는 6자리 이상이어야 합니다.</p>
        )}
        <div className="form-group">
          <label><strong>이메일</strong></label>
          {isEditing ? (
            <>
              <input
                type="email"
                name="email"
                value={formData.email}
                onChange={handleInputChange}
                onBlur={handleBlur}
                required
              />
              {isEditing && validationStatus.email === false && (
                <p className="error-text">유효한 이메일 주소여야 하며, 이미 존재하는 이메일입니다.</p>
              )}
              {isEditing && validationStatus.email === true && (
                <p className="success-text">사용 가능한 이메일입니다.</p>
              )}
            </>
          ) : (
            <p>{userData.email}</p>
          )}
        </div>
        <div className="form-group">
          <label><strong>핸드폰</strong></label>
          <p>{userData.phone}</p>
        </div>
        <div className="form-group">
          <label><strong>주소</strong></label>
          {isEditing ? (
            <input
              type="text"
              name="address"
              value={formData.address}
              onChange={handleInputChange}
              required
            />
          ) : (
            <p>{userData.address}</p>
          )}
        </div>
        <div className="form-group">
          <label><strong>권한</strong></label>
          <p>{getRoleDisplayName(userData.role)}</p>
        </div>
        <div className="form-group">
          <label><strong>가입일</strong></label>
          <p>{new Date(userData.createDate).toLocaleDateString()}</p>
        </div>
      </div>
      {isEditing ? (
        <div className="button-group">
          <button className="save-btn" onClick={handleSave}>저장하기</button>
          <button className="cancel-btn" onClick={handleCancel}>취소하기</button>
        </div>
      ) : (
        <div className="button-group">
          <button className="edit-btn" onClick={handleEdit}>수정하기</button>
          <button className="delete-btn" onClick={handleDelete}>회원 탈퇴</button>
        </div>
      )}
      {showDeleteConfirmation && (
        <div className="delete-confirmation">
          <p>정말로 회원 탈퇴를 원하시면 "삭제를 원합니다"를 입력하세요.</p>
          <input
            type="text"
            value={deleteInput}
            onChange={(e) => setDeleteInput(e.target.value)}
          />
          <button className="confirm-btn" onClick={confirmDelete}>확인</button>
          <button className="cancel-btn" onClick={() => setShowDeleteConfirmation(false)}>취소</button>
        </div>
      )}
    </div>
  );
};

export default PersonalInfo;