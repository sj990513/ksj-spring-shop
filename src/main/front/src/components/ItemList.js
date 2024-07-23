import React, { useState, useEffect } from 'react';
import axiosInstance from '../axiosInstance';
import './ItemList.css';

const categories = [
  { value: '', label: '모든상품' },
  { value: 'top', label: '상의' },
  { value: 'pants', label: '하의' },
  { value: 'cap', label: '모자' },
  { value: 'shoes', label: '신발' },
];

const ItemList = () => {
  const [items, setItems] = useState([]);
  const [page, setPage] = useState(1);
  const [search, setSearch] = useState('');
  const [category, setCategory] = useState('');
  const [totalPages, setTotalPages] = useState(1);

  useEffect(() => {
    fetchItems();
  }, [page, search, category]);

  const fetchItems = async () => {
    try {
      const response = await axiosInstance.get(`/items/item-list${category ? `/${category}` : ''}`, {
        params: {
          page: page,
          search: search,
        },
      });
      setItems(response.data.content);
      setTotalPages(response.data.totalPages);
    } catch (error) {
      console.error('There was an error fetching the items!', error);
    }
  };

  const handleSearchChange = (e) => {
    setSearch(e.target.value);
    setPage(1); // Reset to first page on new search
  };

  const handleCategoryChange = (e) => {
    setCategory(e.target.value);
    setPage(1); // Reset to first page on category change
  };

  const handlePageChange = (newPage) => {
    if (newPage > 0 && newPage <= totalPages) {
      setPage(newPage);
    }
  };

  return (
    <div className="item-list-container">
      <h2>상품 목록</h2>
      <div className="search-bar">
        <input
          type="text"
          placeholder="검색어 입력"
          value={search}
          onChange={handleSearchChange}
          className="search-input"
        />
        <select value={category} onChange={handleCategoryChange} className="category-select">
          {categories.map((cat) => (
            <option key={cat.value} value={cat.value}>
              {cat.label}
            </option>
          ))}
        </select>
      </div>
      <div className="item-list">
        {items.map((item) => (
          <div key={item.id} className="item">
            <img src={item.imageUrl} alt={item.name} className="item-image" />
            <h3>{item.name}</h3>
            <p>{item.description}</p>
            <p>
              <strong>가격:</strong> {item.price}원
            </p>
          </div>
        ))}
      </div>
      <div className="pagination">
        <button onClick={() => handlePageChange(page - 1)} disabled={page === 1}>
          이전
        </button>
        <span>
          페이지 {page} of {totalPages}
        </span>
        <button onClick={() => handlePageChange(page + 1)} disabled={page === totalPages}>
          다음
        </button>
      </div>
    </div>
  );
};

export default ItemList;