import React, { useState, useEffect, useContext } from 'react';
import axiosInstance from '../axiosInstance';
import './ItemDetail.css';
import { AuthContext } from '../context/AuthContext';

const Qna = ({ itemId }) => {
  const [qnaTitle, setQnaTitle] = useState('');
  const [qnaContent, setQnaContent] = useState('');
  const [qnaList, setQnaList] = useState([]);
  const [qnaPage, setQnaPage] = useState(1);
  const [qnaTotalPages, setQnaTotalPages] = useState(1);
  const [adminAnswers, setAdminAnswers] = useState({}); // State for admin answers
  const { user } = useContext(AuthContext); // Get the current user from AuthContext

  useEffect(() => {
    fetchQnaList(1); // Fetch first page of Q&A
  }, []);

  useEffect(() => {
    fetchQnaList(qnaPage);
  }, [qnaPage]);

  const fetchQnaList = async (page) => {
    try {
      const response = await axiosInstance.get(`/items/${itemId}/qna`, {
        params: { page: page },
      });
      setQnaList(response.data.content);
      setQnaTotalPages(response.data.totalPages);
    } catch (error) {
      console.error('There was an error fetching the Q&A list!', error);
    }
  };

  const handleQnaTitleChange = (e) => {
    setQnaTitle(e.target.value);
  };

  const handleQnaContentChange = (e) => {
    setQnaContent(e.target.value);
  };

  const handleQnaSubmit = async (e) => {
    e.preventDefault();
    if (qnaTitle.trim() === '' || qnaContent.trim() === '') {
      alert('Q&A 제목과 내용을 입력해주세요.');
      return;
    }

    try {
      await axiosInstance.post(`/items/${itemId}/qna/add-qna`, {
        title: qnaTitle,
        content: qnaContent,
      });
      alert('Q&A가 추가되었습니다.');
      setQnaTitle('');
      setQnaContent('');
      fetchQnaList(qnaPage);
    } catch (error) {
      console.error('There was an error adding the Q&A!', error);
      alert('Q&A 추가에 실패했습니다.');
    }
  };

  const handleAnswerChange = (e, qnaId) => {
    setAdminAnswers((prevAnswers) => ({
      ...prevAnswers,
      [qnaId]: e.target.value,
    }));
  };

  const handleAnswerSubmit = async (qnaId) => {
    const answer = adminAnswers[qnaId];
    if (!answer || answer.trim() === '') {
      alert('답변 내용을 입력해주세요.');
      return;
    }

    try {
      await axiosInstance.post(`/admin/item-list/${itemId}/qna/${qnaId}/answer-qna`, {
        answer,
      });
      alert('답변이 추가되었습니다.');
      setAdminAnswers((prevAnswers) => ({
        ...prevAnswers,
        [qnaId]: '',
      }));
      fetchQnaList(qnaPage);
    } catch (error) {
      console.error('There was an error adding the answer!', error);
      alert('답변 추가에 실패했습니다.');
    }
  };

  return (
    <div className="qna">
      <h3>상품 Q&A</h3>
      <p className="review-notice">
        Q&A는 삭제가 불가능하며 상품 관리자가 답변해 드립니다.
      </p>
      {qnaList && qnaList.length > 0 ? (
        qnaList.map((qna, index) => (
          <div key={index} className="qna-item">
            <p><strong>질문:</strong> {qna.qnaDto.title}</p>
            <p>{qna.qnaDto.content}</p>
            <p><strong>질문 작성일:</strong> {new Date(qna.qnaDto.createdate).toLocaleDateString()}</p>
            <br />
            {qna.qnaAnswerDto ? (
              <>
                <p><strong>답변:</strong> {qna.qnaAnswerDto.answer}</p>
                <p><strong>답변 작성일:</strong> {new Date(qna.qnaAnswerDto.createdate).toLocaleDateString()}</p>
              </>
            ) : (
              <>
                <p>아직 답변이 존재하지 않습니다.</p>
                {user?.role === 'ROLE_ADMIN' && (
                  <div className="admin-answer-form">
                    <textarea
                      placeholder="답변 내용을 입력하세요."
                      value={adminAnswers[qna.qnaDto.id] || ''}
                      onChange={(e) => handleAnswerChange(e, qna.qnaDto.id)}
                      rows="4"
                    />
                    <button onClick={() => handleAnswerSubmit(qna.qnaDto.id)}>답변 달기</button>
                  </div>
                )}
              </>
            )}
          </div>
        ))
      ) : (
        <p>아직 Q&A가 존재하지 않습니다.</p>
      )}

      {qnaTotalPages > 1 && (
        <div className="pagination">
          <button
            onClick={() => setQnaPage(qnaPage - 1)}
            disabled={qnaPage === 1}
          >
            이전
          </button>
          <span>{qnaPage} / {qnaTotalPages}</span>
          <button
            onClick={() => setQnaPage(qnaPage + 1)}
            disabled={qnaPage === qnaTotalPages}
          >
            다음
          </button>
        </div>
      )}

      <div className="add-qna">
        <h3>Q&A 추가</h3>
        <form onSubmit={handleQnaSubmit} className="qna-form">
          <div className="form-group">
            <label>제목:</label>
            <input
              type="text"
              value={qnaTitle}
              onChange={handleQnaTitleChange}
              required
            />
          </div>
          <div className="form-group">
            <label>내용:</label>
            <textarea
              value={qnaContent}
              onChange={handleQnaContentChange}
              rows="4"
              required
            />
          </div>
          <button type="submit">Q&A 추가</button>
        </form>
      </div>
    </div>
  );
};

export default Qna;