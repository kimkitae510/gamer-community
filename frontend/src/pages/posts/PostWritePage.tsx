import { useState, useEffect } from "react";
import { useNavigate, useParams, useLocation } from "react-router-dom";
import { postService, categoryService } from "../../api/services";
import useAuthStore from "../../store/auth";
import type { PostTag } from "../../api/types";

const POST_TAGS: { name: PostTag; description: string }[] = [
  { name: "일반", description: "일반적인 게시글" },
  { name: "질문", description: "궁금한 것을 물어보세요" },
  { name: "정보", description: "유용한 정보 공유" },
  { name: "공략", description: "게임 공략과 팁" },
];

export default function PostWritePage() {
  const { subCategoryId, categoryName: urlCategoryName } =
    useParams<{ subCategoryId: string; categoryName: string }>();
  const navigate = useNavigate();
  const location = useLocation();
  const accessToken = useAuthStore((state) => state.accessToken);

  const [selectedTag, setSelectedTag] = useState<PostTag | null>(null);
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [categoryId, setCategoryId] = useState<number | null>(null);
  const [categoryName, setCategoryName] = useState<string>("");
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  //< 카테고리 유효성 확인 (작성 가능 여부 체크)
  useEffect(() => {
    const checkCategory = async () => {
      try {
        const catIdNum = Number(subCategoryId);
        const category = await categoryService.getChildById(catIdNum);
        
        if (!category.writable) {
          alert("글을 작성할 수 없는 카테고리입니다.");
          navigate(-1);
          return;
        }
        setCategoryId(catIdNum);
        setCategoryName(category.name);
      } catch {
        alert("카테고리를 불러오지 못했습니다.");
        navigate(-1);
      } finally {
        setLoading(false);
      }
    };

    checkCategory();
  }, [subCategoryId, navigate]);

  //< 게시글 작성 제출
  const submit = async () => {
    if (!title.trim() || !content.trim()) {
      alert("제목과 내용을 입력해주세요.");
      return;
    }
    if (!categoryId) {
      alert("카테고리를 선택해주세요.");
      return;
    }
    if (!selectedTag) {
      alert("태그를 선택해주세요.");
      return;
    }
    if (!accessToken) {
      alert("로그인이 필요합니다.");
      navigate("/login", { state: { from: location } });
      return;
    }

    setSubmitting(true);
    try {
      const postId = await postService.create({
        title,
        content,
        categoryId,
        tag: selectedTag,
      });
      
      navigate(`/${urlCategoryName}/game/${subCategoryId}/posts/view/${postId}`);
    } catch (err: any) {
      console.error("글 작성 실패:", err);
      alert(err.response?.data?.message || "글 작성에 실패했습니다.");
    } finally {
      setSubmitting(false);
    }
  };

  //< 뒤로가기 (작성 중이면 확인)
  const goBack = () => {
    if (title.trim() || content.trim()) {
      if (window.confirm("작성 중인 내용이 있습니다. 정말 나가시겠습니까?")) {
        navigate(-1);
      }
    } else {
      navigate(-1);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-white flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin w-8 h-8 border-2 border-neutral-200 border-t-primary-600 rounded-full mx-auto mb-4"></div>
          <p className="text-neutral-600">페이지를 준비하는 중...</p>
        </div>
      </div>
    );
  }

  if (!accessToken) {
    return (
      <div className="min-h-screen bg-neutral-50 flex items-center justify-center px-4">
        <div className="bg-white rounded-2xl border border-neutral-200 p-12 text-center max-w-md">
          <img 
            src="/images/animal.png" 
            alt="로그인 필요" 
            className="w-20 h-20 mx-auto mb-4 object-contain"
          />
          <h2 className="text-2xl font-bold text-neutral-900 mb-2">로그인이 필요합니다</h2>
          <p className="text-neutral-600 mb-8">글을 작성하려면 먼저 로그인해주세요</p>
          <div className="flex flex-col gap-3">
            <button
              onClick={() => navigate("/login", { state: { from: location } })}
              className="w-full px-6 py-3 bg-gradient-to-r from-primary-600 to-primary-700 text-white font-semibold rounded-xl hover:from-primary-700 hover:to-primary-800 shadow-md hover:shadow-lg transition-all"
            >
              로그인하러 가기
            </button>
            <button
              onClick={() => navigate(-1)}
              className="w-full px-6 py-3 bg-neutral-100 text-neutral-700 font-medium rounded-xl hover:bg-neutral-200"
            >
              뒤로 가기
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-neutral-50">
      <div className="max-w-4xl mx-auto px-4 py-8">
        {/* 헤더 */}
        <div className="mb-6">
          <div className="flex items-center justify-between mb-2">
            <h1 className="text-3xl font-bold text-neutral-900">새 글 작성</h1>
            <button
              onClick={goBack}
              className="text-neutral-600 hover:text-neutral-900"
            >
              ✕
            </button>
          </div>
          <p className="text-neutral-500">게시판: {categoryName}</p>
        </div>

        {/* 통합 컨테이너 */}
        <div className="bg-white rounded-2xl border-2 border-neutral-200 shadow-sm p-8 mb-6">
          {/* 태그 선택 */}
          <div className="mb-8">
            <h2 className="text-lg font-bold text-neutral-900 mb-4">글의 유형을 선택하세요</h2>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
              {POST_TAGS.map((tag) => (
                <button
                  key={tag.name}
                  type="button"
                  onClick={() => setSelectedTag(tag.name)}
                  className={`p-4 rounded-xl border-2 transition-all text-left ${
                    selectedTag === tag.name
                      ? "border-primary-500 bg-primary-50"
                      : "border-neutral-200 bg-white hover:border-neutral-300"
                  }`}
                >
                  <h3 className="font-bold text-neutral-900 mb-1">{tag.name}</h3>
                  <p className="text-xs text-neutral-600">{tag.description}</p>
                </button>
              ))}
            </div>
          </div>

          <div className="h-px bg-neutral-200 mb-8"></div>

          {/* 글 작성 폼 */}
          <div className="space-y-6">
            {/* 제목 */}
            <div>
              <label className="block text-sm font-semibold text-neutral-700 mb-2">
                제목
              </label>
              <input
                type="text"
                className="w-full px-4 py-3 border-2 border-neutral-200 rounded-xl focus:border-primary-500 focus:ring-2 focus:ring-primary-100 focus:outline-none transition-all"
                placeholder="제목을 입력하세요"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                maxLength={100}
              />
              <div className="mt-1.5 text-xs text-neutral-500 text-right">
                {title.length}/100자
              </div>
            </div>

            {/* 내용 */}
            <div>
              <label className="block text-sm font-semibold text-neutral-700 mb-2">
                내용
              </label>
              <textarea
                className="w-full px-4 py-3 border-2 border-neutral-200 rounded-xl focus:border-primary-500 focus:ring-2 focus:ring-primary-100 focus:outline-none transition-all resize-none"
                placeholder="내용을 입력하세요"
                rows={15}
                value={content}
                onChange={(e) => setContent(e.target.value)}
              />
              <div className="mt-1.5 text-xs text-neutral-500 text-right">
                {content.length}자
              </div>
            </div>
          </div>
        </div>

        {/* 액션 버튼 */}
        <div className="flex justify-end gap-3">
          <button
            onClick={goBack}
            disabled={submitting}
            className="px-6 py-3 bg-neutral-100 text-neutral-700 font-medium rounded-xl hover:bg-neutral-200 transition-colors"
          >
            취소
          </button>
          <button
            onClick={submit}
            disabled={submitting}
            className={`px-6 py-3 font-semibold rounded-xl shadow-md hover:shadow-lg transition-all ${
              submitting
                ? "bg-neutral-300 text-neutral-500 cursor-not-allowed"
                : "bg-gradient-to-r from-primary-600 to-primary-700 text-white hover:from-primary-700 hover:to-primary-800"
            }`}
          >
            {submitting ? "작성 중..." : "작성 완료"}
          </button>
        </div>
      </div>
    </div>
  );
}
