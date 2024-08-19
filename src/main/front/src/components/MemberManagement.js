import React, { useState, useEffect } from 'react';
import axiosInstance from '../axiosInstance';
import './css/MemberManagement.css';  

const MemberManagement = () => {
  const [members, setMembers] = useState([]);
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [updatedRoles, setUpdatedRoles] = useState({});

  useEffect(() => {
    fetchMembers();
  }, [page, search]);

  const fetchMembers = async () => {
    try {
      const response = await axiosInstance.get('/admin/member-list', {
        params: {
          page,
          search,
        },
      });
      setMembers(response.data.content);
      setTotalPages(response.data.totalPages);
    } catch (error) {
      console.error('Error fetching members:', error);
    }
  };

  const handleRoleChange = (userId, newRole) => {
    setUpdatedRoles((prevRoles) => ({
      ...prevRoles,
      [userId]: newRole,
    }));
  };

  const handleUpdateRole = async (userId) => {
    try {
      const role = updatedRoles[userId];
      if (!role) {
        alert('변경값이 존재하지 않습니다.');
        return;
      }
      await axiosInstance.patch(`/admin/member-list/${userId}/update-role`, {
        role: role,
      });
      setUpdatedRoles((prevRoles) => ({
        ...prevRoles,
        [userId]: '',
      }));
      fetchMembers(); // Refresh member list
      alert('해당 회원의 권한을 수정하였습니다.');
    } catch (error) {
        alert('권한 수정 실패');
    }
  };

  const handleDeleteMember = async (userId) => {
    const confirmDelete = window.confirm('정말로 해당 회원을 삭제하시겠습니까?');
    if (confirmDelete) {
      try {
        await axiosInstance.delete(`/admin/member-list/${userId}/delete`);
        fetchMembers(); // Refresh member list
        alert('해당 회원을 성공적으로 삭제했습니다.');
      } catch (error) {
        console.error('Error deleting member:', error);
        alert('회원 삭제 실패');
      }
    }
  };

  const handlePageChange = (newPage) => {
    setPage(newPage);
  };

  return (
    <div className="member-management-container">
      <h2>회원 관리</h2>
      <div className="search-container">
        <input
          type="text"
          placeholder="회원 ID 입력"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
        <button onClick={() => fetchMembers()}>검색</button>
      </div>
      <div className="mem-labels">
        <span>ID</span>
        <span>이메일 주소</span>
        <span>휴대폰 번호</span>
        <span>권한</span>
        <span></span>
      </div>
      <div className="members-list">
        {members.map((member) => (
          <div key={member.id} className="mem-item">
            <div className="member-info">
              <span>{member.username}</span>
              <span>{member.email}</span>
              <span>{member.phone}</span>
              <span>
                <select
                  value={updatedRoles[member.id] || member.role}
                  onChange={(e) => handleRoleChange(member.id, e.target.value)}
                >
                  <option value="ROLE_USER">일반유저</option>
                  <option value="ROLE_ADMIN">관리자</option>
                </select>
              </span>
            </div>
            <div className="member-actions">
              <button className="update-btn" onClick={() => handleUpdateRole(member.id)}>수정</button>
              <button className="delete-btn" onClick={() => handleDeleteMember(member.id)}>삭제</button>
            </div>
          </div>
        ))}
      </div>
      <div className="pagination">
        <button onClick={() => handlePageChange(page - 1)} disabled={page === 1}>
          이전
        </button>
        <span>{page} / {totalPages}</span>
        <button onClick={() => handlePageChange(page + 1)} disabled={page === totalPages}>
          다음
        </button>
      </div>
    </div>
  );
};

export default MemberManagement;