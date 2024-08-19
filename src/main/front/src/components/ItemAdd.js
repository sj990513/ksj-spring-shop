import React, { useState } from 'react';
import { useHistory } from 'react-router-dom';
import axiosInstance from '../axiosInstance';
import './css/ItemAdd.css';  // ItemAdd.css를 별도로 사용

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

const ItemAdd = () => {
  const [itemData, setItemData] = useState({
    itemname: '',
    price: '',
    stock: '',
    category: '',
    description: '',
    imageUrl: '',
  });
  const [files, setFiles] = useState([]);
  const history = useHistory();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setItemData((prevData) => ({
      ...prevData,
      [name]: value,
    }));
  };

  const handleFileChange = (e) => {
    const selectedFiles = Array.from(e.target.files);
    const validFiles = selectedFiles.filter(file => file.type === 'image/jpeg' || file.type === 'image/png');

    if (validFiles.length !== selectedFiles.length) {
      alert('JPG 및 PNG 파일만 업로드할 수 있습니다.');
    }

    setFiles(validFiles);
  };

  const uploadToCloudinary = async (file) => {
    const url = `https://api.cloudinary.com/v1_1/dm8jlhglf/upload`;
    const formData = new FormData();
    formData.append('file', file);
    formData.append('upload_preset', 'y4j4ewxk'); // replace with your upload preset

    const response = await fetch(url, {
      method: 'POST',
      body: formData,
    });

    const data = await response.json();
    return data.secure_url;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const uploadedImageUrls = await Promise.all(files.map(file => uploadToCloudinary(file)));
      const imageUrlString = uploadedImageUrls.join(',');  // Join the URLs into a single string

      const newItemData = {
        ...itemData,
        imageUrl: imageUrlString,  // Send as a single string
      };

      await axiosInstance.post('/admin/item-list/add-item', newItemData);
      alert('아이템이 성공적으로 추가되었습니다.');
      history.push('/admin/menu2'); // 아이템 관리 페이지로 이동
    } catch (error) {
      console.error('Error adding item:', error);
      alert('아이템 추가 실패');
    }
  };

  return (
    <div className="item-add-container">
      <h2>아이템 추가</h2>
      <form onSubmit={handleSubmit} className="item-add-form">
        <div className="form-group">
          <label htmlFor="itemname">아이템명</label>
          <input
            type="text"
            id="itemname"
            name="itemname"
            value={itemData.itemname}
            onChange={handleChange}
            required
          />
        </div>
        <div className="form-group">
          <label htmlFor="price">가격</label>
          <input
            type="number"
            placeholder="숫자만 입력"
            id="price"
            name="price"
            value={itemData.price}
            onChange={handleChange}
            required
          />
        </div>
        <div className="form-group">
          <label htmlFor="stock">재고</label>
          <input
            type="number"
            placeholder="숫자만 입력"
            id="stock"
            name="stock"
            value={itemData.stock}
            onChange={handleChange}
            required
          />
        </div>
        <div className="form-group">
          <label htmlFor="category">카테고리</label>
          <select
            id="category"
            name="category"
            value={itemData.category}
            onChange={handleChange}
            required
          >
            <option value="">선택하세요</option>
            {categories.map((cat) => (
              <option key={cat.value} value={cat.value}>
                {cat.label}
              </option>
            ))}
          </select>
        </div>
        <div className="form-group">
          <label htmlFor="description">설명</label>
          <textarea
            id="description"
            name="description"
            value={itemData.description}
            onChange={handleChange}
            required
          />
        </div>
        <div className="form-group">
          <label htmlFor="imageUrl">이미지 첨부</label>
          <input
            type="file"
            id="imageUrl"
            name="imageUrl"
            accept="image/jpeg, image/png"
            multiple
            onChange={handleFileChange}
            required
          />
        </div>
        <button type="submit" className="iasubmit-btn">추가</button>
      </form>
    </div>
  );
};

export default ItemAdd;