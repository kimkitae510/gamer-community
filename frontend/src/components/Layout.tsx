import type { ReactNode } from "react";
import { Link } from "react-router-dom";
import api from "../api/axios";
import { useState } from "react";
import { useEffect } from "react";

//< 좌측 카테고리 사이드바 + 메인 컨텐츠 레이아웃
export default function Layout({ children }: { children: ReactNode }) {
  const [categories, setCategories] = useState<{ id: number; name: string }[]>([]);

  //< 카테고리 목록 로드
  useEffect(() => {
    api.get("/categories").then((res) => setCategories(res.data));
  }, []);

  return (
    <div className="flex">
      {/*< 좌측 카테고리 사이드바 */}
      <aside className="w-64 bg-white shadow h-screen p-4">
        <h2 className="text-lg font-bold mb-4">카테고리</h2>
        <ul className="space-y-2">
          {categories.map((c) => (
            <li key={c.id}>
              <Link
                to={`/posts/category/${c.id}`}
                className="block p-2 rounded hover:bg-primary hover:text-white"
              >
                {c.name}
              </Link>
            </li>
          ))}
        </ul>
      </aside>

      {/*< 메인 컨텐츠 */}
      <main className="flex-1 p-6">{children}</main>
    </div>
  );
}