import React, { useState, useEffect } from 'react';
import { useHistory } from 'react-router-dom';
import axiosInstance from '../axiosInstance';
import './css/QnaManagement.css';

const QnaManagement = () => {
  const [qnas, setQnas] = useState([]);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const history = useHistory();

  useEffect(() => {
    fetchQnas();
  }, [page]);

  const fetchQnas = async () => {
    try {
      const response = await axiosInstance.get('/admin/qna', { params: { page } });
      setQnas(response.data.content);
      setTotalPages(response.data.totalPages);
    } catch (error) {
      console.error('Error fetching QnAs:', error);
    }
  };

  const handlePageChange = (newPage) => {
    if (newPage > 0 && newPage <= totalPages) {
      setPage(newPage);
    }
  };

  const handleQnaClick = (itemId, qnaId) => {
    history.push(`/item-detail/${itemId}?qnaId=${qnaId}`);
  };

  return (
    <div className="qna-management-container">
      <h2>QnA 관리</h2>
      <div className="qna-labels">
        <span>제목</span>
        <span>질문 내용</span>
        <span>작성일</span>
        <span>답변 상태</span>
        <span>답변 내용</span>
        <span>답변일</span>
      </div>
      <div className="qna-list">
        {qnas.map(({ qnaDto, qnaAnswerDto }) => (
          <div
            key={qnaDto.id}
            className="qna-row"
            onClick={() => handleQnaClick(qnaDto.itemID, qnaDto.id)}
            style={{ cursor: 'pointer' }}
          >
            <span className="qna-title">{qnaDto.title}</span>
            <span className="qna-content">{qnaDto.content}</span>
            <span>{new Date(qnaDto.createdate).toLocaleDateString()}</span>
            <span className={qnaAnswerDto ? 'answered' : 'unanswered'}>
              {qnaAnswerDto ? '답변 완료' : '미답변'}
            </span>
            <span className="qna-answer">
              {qnaAnswerDto ? qnaAnswerDto.answer : '미답변'}
            </span>
            <span>{qnaAnswerDto ? new Date(qnaAnswerDto.createdate).toLocaleDateString() : '미답변'}</span>
          </div>
        ))}
      </div>
      <div className="pagination">
        <button onClick={() => handlePageChange(page - 1)} disabled={page === 1}>
          이전
        </button>
        <span>
          페이지 {page} / {totalPages}
        </span>
        <button onClick={() => handlePageChange(page + 1)} disabled={page === totalPages}>
          다음
        </button>
      </div>
    </div>
  );
};

export default QnaManagement;