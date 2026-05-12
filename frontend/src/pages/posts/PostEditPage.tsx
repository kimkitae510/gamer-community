import { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { postService, categoryService } from "../../api/services";
import type { PostTag } from "../../api/types";

const POST_TAGS: { name: PostTag; description: string }[] = [
  { name: "일반", description: "일반적인 게시글" },
  { name: "질문", description: "궁금한 것을 물어보세요" },
  { name: "정보", description: "유용한 정보 공유" },
  { name: "공략", description: "게임 공략과 팁" },
];

export default function PostEditPage() {
  const { postId } = useParams<{ postId: string }>();
  const navigate = useNavigate();

  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [categoryId, setCategoryId] = useState<number | null>(null);
  const [categoryName, setCategoryName] = useState<string>("로딩 중...");
  const [originalTitle, setOriginalTitle] = useState("");
  const [originalContent, setOriginalContent] = useState("");
  const [selectedTag, setSelectedTag] = useState<PostTag | null>(null);
  const [originalTag, setOriginalTag] = useState<PostTag | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  //< 기존 게시글 데이터 로드
  useEffect(() => {
    if (!postId) return;
    
    setLoading(true);
    postService.getById(Number(postId))
      .then((post) => {
        setTitle(post.title);
        setContent(post.content);
        setCategoryId(post.categoryId);
        setSelectedTag(post.tag);
        setOriginalTitle(post.title);
        setOriginalContent(post.content);
        setOriginalTag(post.tag);
        setLoading(false);
      })
      .catch(() => {
        alert("게시글을 불러오는데 실패했습니다.");
        navigate(-1);
      });
  }, [postId, navigate]);

  //< 카테고리명 조회
  useEffect(() => {
    if (categoryId === null) return;

    categoryService.getChildById(categoryId)
      .then((category) => setCategoryName(category.name))
      .catch(() => setCategoryName("알 수 없는 카테고리"));
  }, [categoryId]);

  //< 게시글 수정 제출 (변경 없으면 그냥 뒤로가기)
  const updatePost = async () => {
    const trimmedTitle = title.trim();
    const trimmedContent = content.trim();

    if (!categoryId) {
      alert("카테고리가 설정되지 않았습니다.");
      return;
    }

    if (!selectedTag) {
      alert("태그를 선택해주세요.");
      return;
    }

    if (!trimmedTitle || !trimmedContent) {
      alert("제목과 내용을 입력해주세요.");
      return;
    }

    if (
      trimmedTitle === originalTitle.trim() &&
      trimmedContent === originalContent.trim() &&
      selectedTag === originalTag
    ) {
      navigate(-1);
      return;
    }

    setSubmitting(true);
    try {
      await postService.update(Number(postId), {
        title: trimmedTitle,
        content: trimmedContent,
        categoryId,
        tag: selectedTag,
      });
      
      alert("게시글이 수정되었습니다.");
      navigate(-1);
    } catch (err) {
      console.error("게시글 수정 실패:", err);
      alert("게시글 수정에 실패했습니다.");
    } finally {
      setSubmitting(false);
    }
  };

  //< 뒤로가기 (수정 중이면 확인)
  const goBack = () => {
    if (
      title !== originalTitle ||
      content !== originalContent ||
      selectedTag !== originalTag
    ) {
      if (window.confirm("수정 중인 내용이 있습니다. 정말 나가시겠습니까?")) {
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
          <p className="text-neutral-600">게시글을 불러오는 중...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-white">
      <div className="max-w-4xl mx-auto px-4 py-8">
        {/* 헤더 */}
        <div className="mb-8">
          <div className="flex items-center justify-between mb-2">
            <h1 className="text-3xl font-bold text-neutral-900">글 수정</h1>
            <button
              onClick={goBack}
              className="text-neutral-600 hover:text-neutral-900"
            >
              ✕
            </button>
          </div>
          <p className="text-neutral-500">게시판: {categoryName}</p>
        </div>

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

        {/* 글 수정 폼 */}
        <div className="space-y-6 mb-8">
          {/* 제목 */}
          <div>
            <label className="block text-sm font-medium text-neutral-700 mb-2">
              제목
            </label>
            <input
              type="text"
              className="w-full px-4 py-3 border border-neutral-300 rounded-xl focus:border-primary-500 focus:ring-2 focus:ring-primary-100 focus:outline-none transition-all"
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
            <label className="block text-sm font-medium text-neutral-700 mb-2">
              내용
            </label>
            <textarea
              className="w-full px-4 py-3 border border-neutral-300 rounded-xl focus:border-primary-500 focus:ring-2 focus:ring-primary-100 focus:outline-none transition-all resize-none"
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

        {/* 액션 버튼 */}
        <div className="flex justify-end gap-3 pb-8">
          <button
            onClick={goBack}
            disabled={submitting}
            className="px-6 py-3 bg-neutral-100 text-neutral-700 font-medium rounded-xl hover:bg-neutral-200"
          >
            취소
          </button>
          <button
            onClick={updatePost}
            disabled={submitting}
            className={`px-6 py-3 font-medium rounded-xl ${
              submitting
                ? "bg-neutral-300 text-neutral-500 cursor-not-allowed"
                : "bg-gradient-to-r from-primary-600 to-primary-700 text-white hover:from-primary-700 hover:to-primary-800"
            }`}
          >
            {submitting ? "수정 중..." : "수정 완료"}
          </button>
        </div>
      </div>
    </div>
  );
}
