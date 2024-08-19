import React, { useState, useEffect } from 'react';
import { useHistory } from 'react-router-dom';
import axiosInstance from '../axiosInstance';
import './css/ItemManagement.css';  

const categories = [
  { value: 'top', label: '상의' },
  { value: 'pants', label: '하의' },
  { value: 'cap', label: '모자' },
  { value: 'shoes', label: '신발' },
];

const getCategoryLabel = (value) => {
  const category = categories.find(cat => cat.value === value);
  return category ? category.label : value;
};

const ItemManagement = () => {
  const [items, setItems] = useState([]);
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const history = useHistory();

  useEffect(() => {
    fetchItems();
  }, [page, search]);

  const fetchItems = async () => {
    try {
      const response = await axiosInstance.get('/admin/item-list', {
        params: {
          page,
          search,
        },
      });
      setItems(response.data.content);
      setTotalPages(response.data.totalPages);
    } catch (error) {
      console.error('Error fetching items:', error);
    }
  };

  const handlePageChange = (newPage) => {
    setPage(newPage);
  };

  const handleItemClick = (itemId) => {
    history.push(`/item-detail/${itemId}`);
  };

  const handleAddItem = () => {
    history.push('/admin/item-list/add-item');
  };

  return (
    <div className="aditem-management-container">
      <h2>아이템 관리</h2>
      <button onClick={handleAddItem}>아이템 추가</button>
      <br></br>
      <br></br>
      <div className="adsearch-container">
        <input
          type="text"
          placeholder="상품명 검색"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
        <button onClick={() => fetchItems()}>검색</button>
      </div>
      <div className="aditem-labels">
        <span>상품명</span>
        <span>가격</span>
        <span>재고</span>
        <span>카테고리</span>
      </div>
      <div className="aditems-list">
        {items.map((item) => (
          <div key={item.id} className="aditem-row" onClick={() => handleItemClick(item.id)}>
            <span>{item.itemname}</span>
            <span>{item.price}원</span>
            <span>{item.stock}개</span>
            <span>{getCategoryLabel(item.category)}</span>
          </div>
        ))}
      </div>
      <div className="adpagination">
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

export default ItemManagement;