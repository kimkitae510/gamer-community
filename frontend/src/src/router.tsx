import { BrowserRouter, Routes, Route } from "react-router-dom";
import LoginPage from "./pages/auth/LoginPage";
import JoinPage from "./pages/auth/JoinPage";
import PostListPage from "./pages/posts/PostListPage";
import PostDetailPage from "./pages/posts/PostDetailPage";
import PostWritePage from "./pages/posts/PostWritePage";
import Header from "./components/Header";
import HomePage from "./pages/posts/HomePage";
import PostEditPage from "./pages/posts/PostEditPage"; 
import CategoryBoardPage from "./category/CategoryBoardPage";
import GameReviewPage from "./pages/posts/ReviewPage";
import CategoryCreatePage from "./category/CategoryCreatePage";
import TrendingPage from "./pages/trending/TrendingPage";


export default function Router() {
  return (
  <BrowserRouter>
      <Header />
      <Routes>
        <Route path="/" element={<HomePage />} />                          {/*< 메인 홈 */}
        <Route path="/login" element={<LoginPage />} />                    {/*< 로그인 */}
        <Route path="/join" element={<JoinPage />} />                      {/*< 회원가입 */}
        <Route path="/trending" element={<TrendingPage />} />              {/*< 인기글 */}
        <Route path="/board/create" element={<CategoryCreatePage />} />    {/*< 게시판 생성 */}
        <Route path="/:categoryName" element={<CategoryBoardPage />} />    {/*< 카테고리별 게시판 */}
        <Route path="/:categoryName/game/:subCategoryId" element={<PostListPage />} />                          {/*< 게시글 목록 */}
        <Route path="/:categoryName/game/:subCategoryId/posts/write" element={<PostWritePage />} />             {/*< 게시글 작성 */}
        <Route path="/:categoryName/game/:subCategoryId/posts/edit/:postId" element={<PostEditPage />} />       {/*< 게시글 수정 */}
        <Route path="/:categoryName/game/:subCategoryId/posts/view/:postId" element={<PostDetailPage />} />     {/*< 게시글 상세 */}
        <Route path="/:categoryName/game/:gameId/reviews" element={<GameReviewPage />} />                       {/*< 게임 리뷰 */}
      </Routes>
    </BrowserRouter>
  );
}
