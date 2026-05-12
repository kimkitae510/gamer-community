// src/category/selectStyles.ts
// primary-600: rgb(29, 78, 216)
const PRIMARY = 'rgb(29, 78, 216)';
const PRIMARY_LIGHT = 'rgba(29, 78, 216, 0.1)';
const PRIMARY_HOVER = 'rgb(30, 64, 175)'; // primary-700

const customSelectStyles = {
  control: (provided: any, state: any) => ({
    ...provided,
    background: 'white',
    border: '1px solid #d1d5db',
    borderRadius: '0.5rem',
    boxShadow: state.isFocused ? `0 0 0 2px ${PRIMARY_LIGHT}` : 'none',
    borderColor: state.isFocused ? PRIMARY : '#d1d5db',
    '&:hover': {
      borderColor: PRIMARY,
    },
    minHeight: '40px',
  }),
  menu: (provided: any) => ({
    ...provided,
    background: 'white',
    border: '1px solid #e5e7eb',
    borderRadius: '0.5rem',
    boxShadow: '0 4px 6px -1px rgba(0,0,0,0.1), 0 2px 4px -1px rgba(0,0,0,0.06)',
    zIndex: 50,
  }),
  option: (provided: any, state: any) => ({
    ...provided,
    backgroundColor: state.isSelected
      ? PRIMARY
      : state.isFocused
      ? '#f3f4f6'
      : 'white',
    color: state.isSelected ? 'white' : '#374151',
    '&:hover': {
      backgroundColor: state.isSelected ? PRIMARY_HOVER : '#f3f4f6',
    },
  }),
  multiValue: (provided: any) => ({
    ...provided,
    background: PRIMARY_LIGHT,
    borderRadius: '0.375rem',
    border: `1px solid rgba(29, 78, 216, 0.2)`,
  }),
  multiValueLabel: (provided: any) => ({
    ...provided,
    color: PRIMARY,
    fontWeight: '500',
  }),
  multiValueRemove: (provided: any) => ({
    ...provided,
    color: PRIMARY,
    '&:hover': {
      backgroundColor: 'rgba(29, 78, 216, 0.15)',
      color: PRIMARY_HOVER,
    },
  }),
  placeholder: (provided: any) => ({
    ...provided,
    color: '#9ca3af',
  }),
  singleValue: (provided: any) => ({
    ...provided,
    color: '#374151',
  }),
};

export default customSelectStyles;
