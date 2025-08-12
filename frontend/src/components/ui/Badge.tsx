import React from 'react';

interface BadgeProps {
  children: React.ReactNode;
  variant?: 'primary' | 'secondary' | 'success' | 'warning' | 'danger' | 'info';
  size?: 'sm' | 'md' | 'lg';
  className?: string;
}

export function Badge({
  children,
  variant = 'primary',
  size = 'md',
  className = ''
}: BadgeProps) {
  const variants = {
    primary: 'bg-primary-50 text-primary-700 border-primary-200',
    secondary: 'bg-neutral-100 text-neutral-700 border-neutral-200',
    success: 'bg-success-50 text-success-600 border-success-200',
    warning: 'bg-warning-50 text-warning-600 border-warning-200',
    danger: 'bg-red-50 text-red-600 border-red-200',
    info: 'bg-info-50 text-info-600 border-info-200'
  };

  const sizes = {
    sm: 'px-2 py-0.5 text-xs',
    md: 'px-2.5 py-1 text-sm',
    lg: 'px-3 py-1.5 text-base'
  };

  return (
    <span
      className={`inline-flex items-center font-medium rounded-md border ${variants[variant]} ${sizes[size]} ${className}`}
    >
      {children}
    </span>
  );
}
