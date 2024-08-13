import React, { useState, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
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
  const location = useLocation();

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const categoryParam = params.get('category') || '';
    setCategory(categoryParam);
    fetchItems(categoryParam, page, search);
  }, [location.search, page, search]);

  const fetchItems = async (categoryParam = category, pageParam = page, searchParam = search) => {
    try {
      const response = await axiosInstance.get(`/items/item-list${categoryParam ? `/${categoryParam}` : ''}`, {
        params: {
          page: pageParam,
          search: searchParam,
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
    fetchItems(e.target.value, 1, search);
  };

  const handlePageChange = (newPage) => {
    if (newPage > 0 && newPage <= totalPages) {
      setPage(newPage);
    }
  };

  const getFirstImageUrl = (imageUrls) => {
    if (!imageUrls) return ''; // Return an empty string if imageUrls is null or undefined
    const urls = imageUrls.split(',');
    return urls[0];
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
            <Link to={`/item-detail/${item.id}`}>
              <img src={getFirstImageUrl(item.imageUrl)} alt={item.itemname} className="item-image" />
              <p className="item-name">{item.itemname}</p>
              <p className="item-price">
                <strong>가격:</strong> {item.price}원
              </p>
            </Link>
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

export default ItemList;