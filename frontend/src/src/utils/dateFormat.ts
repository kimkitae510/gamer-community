/**
 * 날짜 포맷 유틸리티
 * - 여러 컴포넌트에서 중복 정의되던 날짜 포맷 함수들을 통합
 */

/** 스마트 상대 시간 표시 (방금 전, N분 전, N시간 전, N일 전, 날짜) */
export const formatSmartDate = (dateStr: string): string => {
  const date = new Date(dateStr);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const minutes = Math.floor(diff / (1000 * 60));
  const hours = Math.floor(diff / (1000 * 60 * 60));
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));

  if (minutes < 1) return "방금 전";
  if (minutes < 60) return `${minutes}분 전`;
  if (hours < 24) return `${hours}시간 전`;
  if (days < 7) return `${days}일 전`;

  return date.toLocaleDateString("ko-KR", {
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
};

/** 게시글 목록용 날짜 (방금 전 ~ 7일 전, 이후 YYYY.MM.DD) */
export const formatPostDate = (dateStr: string): string => {
  const date = new Date(dateStr);
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffMinutes = Math.floor(diffMs / (1000 * 60));
  const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

  if (diffMinutes < 1) return "방금 전";
  if (diffHours < 1) return `${diffMinutes}분 전`;
  if (diffDays < 1) return `${diffHours}시간 전`;
  if (diffDays < 7) return `${diffDays}일 전`;

  return date
    .toLocaleDateString("ko-KR", { year: "numeric", month: "2-digit", day: "2-digit" })
    .replace(/\. /g, ".")
    .replace(/\.$/, "");
};

/** 상세 날짜/시각 (YYYY. MM. DD. HH:MM:SS) */
export const formatDateTime = (dateStr: string): string => {
  const date = new Date(dateStr);
  return date.toLocaleString("ko-KR", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
  });
};

/** 뉴스용 날짜 (오늘, 어제, N일 전, 날짜) */
export const formatNewsDate = (dateStr: string): string => {
  const date = new Date(dateStr);
  const now = new Date();
  const diffTime = now.getTime() - date.getTime();
  const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));

  if (diffDays === 0) return "오늘";
  if (diffDays === 1) return "어제";
  if (diffDays < 7) return `${diffDays}일 전`;

  return date.toLocaleDateString("ko-KR", { month: "long", day: "numeric" });
};

/** 게시글 수정 여부 체크 */
export const isEdited = (createdAt: string, updatedAt?: string): boolean => {
  return !!updatedAt && new Date(updatedAt).getTime() !== new Date(createdAt).getTime();
};
