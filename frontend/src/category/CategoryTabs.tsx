import { Link, useLocation } from "react-router-dom";
import { useEffect, useState } from "react";
import { categoryService } from "../api/services";
import type { Category } from "../api/types";

export default function CategoryTabs() {
  const location = useLocation();
  const [parents, setParents] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    categoryService.getParents()
      .then((data) => {
        setParents(data);
        setLoading(false);
      })
      .catch((err) => {
        console.error("상위 카테고리 로딩 실패", err);
        setLoading(false);
      });
  }, []);

  const currentCategory = location.pathname.split("/")[1];

  if (loading) {
    return (
      <nav className="flex gap-8">
        {[1, 2, 3, 4, 5].map((i) => (
          <div key={i} className="animate-pulse flex items-center h-14">
            <div className="h-4 w-20 bg-neutral-200 rounded"></div>
          </div>
        ))}
      </nav>
    );
  }

  return (
    <nav className="flex gap-8 overflow-x-auto">
        {parents.map((parent) => {
          const isActive = parent.name.toLowerCase() === currentCategory.toLowerCase();
          
          return (
            <Link
              key={parent.id}
              to={`/${parent.name}`}
              className={`group relative flex items-center gap-1.5 h-14 text-sm whitespace-nowrap transition-colors ${
                isActive
                  ? "text-neutral-900 font-semibold"
                  : "text-neutral-600 font-medium hover:text-neutral-900"
              }`}
            >
              <span>{parent.name}</span>
              {/* 언더라인 */}
              <div className={`absolute bottom-0 left-0 right-0 h-0.5 bg-primary-600 transition-transform origin-left ${
                isActive ? "scale-x-100" : "scale-x-0 group-hover:scale-x-100"
              }`}></div>
            </Link>
          );
        })}
      </nav>
  );
}
