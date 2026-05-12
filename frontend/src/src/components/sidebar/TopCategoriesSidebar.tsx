import { Link } from "react-router-dom";
import type { TopCategory } from "../../api/types";

interface Props {
  topCategories: TopCategory[];
  selectedPeriod: 'daily' | 'weekly' | 'monthly';
  onPeriodChange: (period: 'daily' | 'weekly' | 'monthly') => void;
}

const PERIOD_LABELS = [
  { key: 'daily' as const, label: '일간' },
  { key: 'weekly' as const, label: '주간' },
  { key: 'monthly' as const, label: '월간' },
];

export default function TopCategoriesSidebar({ topCategories, selectedPeriod, onPeriodChange }: Props) {
  return (
    <div className="bg-white rounded-lg border border-neutral-200 p-5">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-base font-bold text-neutral-900">인기 게시판</h3>
        <div className="flex gap-1">
          {PERIOD_LABELS.map(({ key, label }) => (
            <button
              key={key}
              onClick={() => onPeriodChange(key)}
              className={`px-2.5 py-1 text-xs font-medium rounded transition-all ${
                selectedPeriod === key
                  ? 'bg-primary-600 text-white'
                  : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}
            >
              {label}
            </button>
          ))}
        </div>
      </div>

      {topCategories.length === 0 ? (
        <p className="text-sm text-neutral-500 text-center py-4">데이터를 불러오는 중...</p>
      ) : (
        <div className="space-y-3">
          {topCategories.slice(0, 7).map((category, idx) => (
            <Link
              key={category.categoryId}
              to={`/${category.categoryName}/game/${category.categoryId}`}
              className={`block hover:bg-neutral-50 rounded transition-colors ${
                idx === 0 ? 'p-3 bg-neutral-50' : 'p-2.5'
              }`}
            >
              {idx === 0 ? (
                <div className="flex gap-3 items-center">
                  <div className="flex-shrink-0 w-9 h-9 bg-primary-600 rounded flex items-center justify-center text-white font-black text-base">
                    1
                  </div>
                  <div className="flex-shrink-0">
                    <img
                      src={category.imageUrl || ''}
                      alt={category.categoryName}
                      className="w-14 h-14 rounded object-cover"
                      onError={(e) => { e.currentTarget.onerror = null; e.currentTarget.style.display = 'none'; }}
                    />
                  </div>
                  <div className="flex-1 min-w-0">
                    <h4 className="text-sm font-bold text-neutral-900 truncate mb-1 hover:text-primary-600">
                      {category.categoryName}
                    </h4>
                    <div className="text-xs text-neutral-600">
                      평점 <span className="font-bold text-red-600">{category.rating.toFixed(1)}</span>
                      <span className="text-neutral-500 mx-1">·</span>
                      글 <span className="font-bold">{category.postCount}</span>
                    </div>
                  </div>
                </div>
              ) : (
                <div className="flex items-center gap-2.5">
                  <span className="flex-shrink-0 w-6 text-center text-base font-bold text-primary-600">
                    {category.rank}
                  </span>
                  <div className="flex-1 min-w-0">
                    <h4 className="text-sm font-semibold text-neutral-900 truncate hover:text-primary-600">
                      {category.categoryName}
                    </h4>
                    <div className="text-xs text-neutral-500">
                      평점 <span className="font-bold text-red-600">{category.rating.toFixed(1)}</span>
                      <span className="mx-1">·</span>
                      글 <span className="font-bold">{category.postCount}</span>
                    </div>
                  </div>
                </div>
              )}
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
