import { useEffect, useState } from "react";
import { commentService, authService, likeService } from "../../api/services";
import { formatSmartDate } from "../../utils/dateFormat";
import type { Comment } from "../../api/types";

interface CommentSectionProps {
  postId: number;
  postAuthorName: string;
  onCommentCountChange?: (newCount: number) => void;
  commentCount: number;
}

const AI_BOT_ID = "rlarlxo51000";

export default function CommentSection({ postId, postAuthorName, commentCount, onCommentCountChange }: CommentSectionProps) {
  const [comments, setComments] = useState<Comment[]>([]);
  const [newComment, setNewComment] = useState("");
  const [replyContent, setReplyContent] = useState("");
  const [replyParent, setReplyParent] = useState<number | null>(null);
  const [currentUser, setCurrentUser] = useState<{ loginId: string; username: string } | null>(null);
  const [editId, setEditId] = useState<number | null>(null);
  const [editContent, setEditContent] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const fetchCurrentUser = async () => {
    try {
      const user = await authService.getMe();
      setCurrentUser(user);
      return user;
    } catch {
      setCurrentUser(null);
      return null;
    }
  };

  const fetchCommentsWithLikeStatus = async (loginId: string | null) => {
    try {
      const commentList = await commentService.getByPost(postId);
      const attachLikeStatus = async (list: Comment[]): Promise<Comment[]> =>
        Promise.all(list.map(async (c) => {
          let isLiked = false;
          if (loginId && c.author && c.author !== "알 수 없음") {
            try {
              const s = await likeService.getCommentLikeStatus(c.id);
              isLiked = s.isLiked ?? s.liked ?? false;
            } catch { isLiked = false; }
          }
          return { ...c, isLiked, replies: c.replies ? await attachLikeStatus(c.replies) : [] };
        }));
      const updated = await attachLikeStatus(commentList);
      setComments(updated);
      if (onCommentCountChange) onCommentCountChange(getTotalComments(updated));
    } catch {
      setComments([]);
    }
  };

  useEffect(() => {
    const init = async () => {
      const user = await fetchCurrentUser();
      await fetchCommentsWithLikeStatus(user?.loginId ?? null);
    };
    init();
  }, [postId]);

  const addComment = async () => {
    const content = replyParent ? replyContent : newComment;
    if (!content.trim() || !currentUser) return;
    setSubmitting(true);
    try {
      await commentService.create({ postId, content, parentId: replyParent });
      if (replyParent) setReplyContent(""); else setNewComment("");
      setReplyParent(null);
      fetchCommentsWithLikeStatus(currentUser.loginId);
    } catch { alert("댓글 작성 실패"); } finally { setSubmitting(false); }
  };

  const updateComment = async (id: number) => {
    if (!editContent.trim() || !currentUser) return;
    try {
      await commentService.update(id, editContent);
      setEditId(null); setEditContent("");
      fetchCommentsWithLikeStatus(currentUser.loginId);
    } catch { alert("댓글 수정 실패"); }
  };

  const deleteComment = async (id: number) => {
    if (!currentUser) return alert("로그인 필요");
    if (!window.confirm("정말 삭제하시겠습니까?")) return;
    try {
      await commentService.delete(id);
      fetchCommentsWithLikeStatus(currentUser.loginId);
    } catch { alert("댓글 삭제 실패"); }
  };

  const toggleLike = async (commentId: number) => {
    if (!currentUser) return;
    const comment = findCommentById(comments, commentId);
    if (!comment) return;
    const prevLiked = comment.isLiked || false;
    const prevCount = comment.likeCount;
    setComments((prev) => updateLikeInState(prev, commentId, !prevLiked, prevLiked ? prevCount - 1 : prevCount + 1));
    try {
      const result = await likeService.toggleCommentLike(commentId);
      setComments((prev) => updateLikeInState(prev, commentId, result.isLiked ?? result.liked ?? false, result.likeCount));
    } catch {
      setComments((prev) => updateLikeInState(prev, commentId, prevLiked, prevCount));
    }
  };

  const updateLikeInState = (list: Comment[], id: number, isLiked: boolean, likeCount: number): Comment[] =>
    list.map((c) => ({ ...c, isLiked: c.id === id ? isLiked : c.isLiked, likeCount: c.id === id ? likeCount : c.likeCount, replies: c.replies ? updateLikeInState(c.replies, id, isLiked, likeCount) : [] }));

  const findCommentById = (list: Comment[], id: number): Comment | null => {
    for (const c of list) {
      if (c.id === id) return c;
      if (c.replies) { const f = findCommentById(c.replies, id); if (f) return f; }
    }
    return null;
  };

  const getTotalComments = (list: Comment[]): number =>
    list.reduce((total, c) => total + (c.content !== "삭제된 댓글입니다" ? 1 : 0) + (c.replies ? getTotalComments(c.replies) : 0), 0);

  const renderComments = (list: Comment[], depth = 0): JSX.Element[] =>
    list.map((c) => {
      const isAi = c.authorId === AI_BOT_ID;

      if (isAi) return (
        <div key={c.id} className={depth > 0 ? "ml-12 mt-3" : ""}>
          <div className="py-3 border-b border-gray-100">
            <div className="rounded-xl overflow-hidden border border-gray-200 shadow-sm">
              {/* AI 헤더 */}
              <div className="flex items-center gap-3 px-4 py-3 bg-slate-800">
                {/* 커스텀 AI 로고 */}
                <div className="w-8 h-8 rounded-lg bg-slate-700 flex items-center justify-center flex-shrink-0">
                  <svg width="20" height="20" viewBox="0 0 32 32" fill="none">
                    {/* 중앙 코어 */}
                    <circle cx="16" cy="16" r="4" fill="#c7d2fe"/>
                    <circle cx="16" cy="16" r="2" fill="#818cf8"/>
                    {/* 궤도 링 */}
                    <ellipse cx="16" cy="16" rx="10" ry="5" stroke="#6366f1" strokeWidth="1.2" fill="none" strokeDasharray="3 2"/>
                    <ellipse cx="16" cy="16" rx="10" ry="5" stroke="#6366f1" strokeWidth="1.2" fill="none" strokeDasharray="3 2" transform="rotate(60 16 16)"/>
                    <ellipse cx="16" cy="16" rx="10" ry="5" stroke="#6366f1" strokeWidth="1.2" fill="none" strokeDasharray="3 2" transform="rotate(120 16 16)"/>
                    {/* 꼭짓점 노드 */}
                    <circle cx="16" cy="6" r="1.5" fill="#a5b4fc"/>
                    <circle cx="24.7" cy="11" r="1.5" fill="#a5b4fc"/>
                    <circle cx="24.7" cy="21" r="1.5" fill="#a5b4fc"/>
                    <circle cx="16" cy="26" r="1.5" fill="#a5b4fc"/>
                    <circle cx="7.3" cy="21" r="1.5" fill="#a5b4fc"/>
                    <circle cx="7.3" cy="11" r="1.5" fill="#a5b4fc"/>
                  </svg>
                </div>
                <div>
                  <p className="text-white font-semibold text-sm leading-none">AI 도우미</p>
                  <p className="text-slate-400 text-xs mt-0.5">AI가 작성한 답변이에요</p>
                </div>
                <span className="ml-auto text-slate-500 text-xs">{formatSmartDate(c.createdAt)}</span>
              </div>
              {/* AI 본문 */}
              <div className="px-4 py-4 bg-white">
                <p className="text-gray-700 text-sm leading-relaxed whitespace-pre-wrap">{c.content}</p>
              </div>
            </div>
          </div>
          {c.replies && c.replies.length > 0 && <div className="mt-1">{renderComments(c.replies, depth + 1)}</div>}
        </div>
      );

      return (
        <div key={c.id} className={depth > 0 ? "ml-12 mt-4 border-l-2 border-gray-100 pl-4" : ""}>
          <div className="py-4 border-b border-gray-100">
            <div className="flex items-start justify-between mb-3">
              <div className="flex items-center gap-3">
                <div className="w-8 h-8 bg-gray-200 rounded-full flex items-center justify-center text-sm font-medium text-gray-700">
                  {c.author && c.author !== "알 수 없음" ? c.author.charAt(0).toUpperCase() : "?"}
                </div>
                <div>
                  <div className="flex items-center gap-2">
                    <span className="font-medium text-gray-900 text-sm">{c.author || "알 수 없음"}</span>
                    {c.author === postAuthorName && (
                      <span className="px-1.5 py-0.5 bg-primary-100 text-primary-700 text-xs rounded font-medium">작성자</span>
                    )}
                  </div>
                  <div className="text-xs text-gray-500">
                    {formatSmartDate(c.createdAt)}
                    {c.updatedAt && c.updatedAt !== c.createdAt && <span className="ml-2">(수정됨)</span>}
                  </div>
                </div>
              </div>
              <button
                onClick={() => toggleLike(c.id)}
                disabled={!currentUser || !c.author || c.author === "알 수 없음"}
                className={`inline-flex flex-col items-center gap-0.5 px-3 py-1.5 rounded-md text-sm font-medium transition-all min-w-[50px] ${
                  c.isLiked ? "bg-primary-50 text-primary-600 border border-primary-600" : "bg-gray-50 text-gray-500 border border-gray-300 hover:border-gray-400"
                } ${!currentUser || !c.author || c.author === "알 수 없음" ? "opacity-50 cursor-not-allowed" : ""}`}
              >
                <svg className="w-4 h-4" fill={c.isLiked ? "currentColor" : "none"} stroke="currentColor" strokeWidth={c.isLiked ? 0 : 2.5} viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M14 10h4.764a2 2 0 011.789 2.894l-3.5 7A2 2 0 0115.263 21h-4.017c-.163 0-.326-.02-.485-.06L7 20m7-10V5a2 2 0 00-2-2h-.095c-.5 0-.905.405-.905.905 0 .714-.211 1.412-.608 2.006L7 11v9m7-10h-2M7 20H5a2 2 0 01-2-2v-6a2 2 0 012-2h2.5" />
                </svg>
                <span className="font-semibold">{c.likeCount}</span>
              </button>
            </div>

            {editId === c.id ? (
              <div className="space-y-3">
                <textarea className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none text-sm" rows={4} value={editContent} onChange={(e) => setEditContent(e.target.value)} />
                <div className="flex gap-2">
                  <button className="px-3 py-1.5 bg-primary-600 text-white rounded-md text-sm hover:bg-primary-700" onClick={() => updateComment(c.id)}>저장</button>
                  <button className="px-3 py-1.5 bg-gray-200 text-gray-700 rounded-md text-sm hover:bg-gray-300" onClick={() => { setEditId(null); setEditContent(""); }}>취소</button>
                </div>
              </div>
            ) : (
              <p className="text-gray-800 text-sm leading-relaxed whitespace-pre-wrap mb-3">
                {c.content.split(/(@\S+)/).map((part, i) =>
                  part.startsWith("@") ? <span key={i} className="text-primary-600 font-semibold bg-primary-50 px-1 py-0.5 rounded">{part}</span> : part
                )}
              </p>
            )}

            <div className="flex items-center gap-3 text-sm">
              {currentUser && c.author && c.author !== "알 수 없음" && (
                <button className="text-primary-600 font-medium hover:text-primary-700 hover:bg-primary-50 px-2 py-1 rounded transition-all text-xs" onClick={() => setReplyParent(c.id)}>답글</button>
              )}
              {currentUser?.loginId === c.authorId && (
                <>
                  <button className="text-gray-600 hover:text-gray-900 text-xs" onClick={() => { setEditId(c.id); setEditContent(c.content); }}>수정</button>
                  <button className="text-gray-600 hover:text-red-700 text-xs" onClick={() => deleteComment(c.id)}>삭제</button>
                </>
              )}
            </div>

            {replyParent === c.id && currentUser && (
              <div className="mt-4 p-3 bg-gray-50 rounded-md">
                <textarea className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none text-sm" rows={3} placeholder="답글을 입력하세요" value={replyContent} onChange={(e) => setReplyContent(e.target.value)} />
                <div className="flex gap-2 mt-2">
                  <button className="px-3 py-1.5 bg-primary-600 text-white rounded-md text-sm hover:bg-primary-700 disabled:opacity-50" onClick={addComment} disabled={submitting}>{submitting ? "작성 중..." : "작성"}</button>
                  <button className="px-3 py-1.5 bg-gray-200 text-gray-700 rounded-md text-sm hover:bg-gray-300" onClick={() => { setReplyParent(null); setReplyContent(""); }}>취소</button>
                </div>
              </div>
            )}
          </div>
          {c.replies && c.replies.length > 0 && <div className="mt-2">{renderComments(c.replies, depth + 1)}</div>}
        </div>
      );
    });

  return (
    <div>
      <div className="mb-6">
        <h3 className="text-lg font-bold text-gray-900">댓글 <span className="text-primary-600">{commentCount}</span></h3>
      </div>

      <div className="mb-8 p-6 bg-gray-50 rounded-lg">
        {currentUser ? (
          <>
            <textarea className="w-full px-4 py-3 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none" rows={4} placeholder="댓글을 남겨보세요" value={newComment} onChange={(e) => setNewComment(e.target.value)} />
            <div className="flex justify-end mt-3">
              <button
                className={`px-5 py-2 rounded-md font-medium text-sm ${submitting || !newComment.trim() ? "bg-gray-300 text-gray-500 cursor-not-allowed" : "bg-primary-600 text-white hover:bg-primary-700"}`}
                onClick={addComment}
                disabled={submitting || !newComment.trim()}
              >
                {submitting ? "작성 중..." : "댓글 작성"}
              </button>
            </div>
          </>
        ) : (
          <div className="text-center py-4">
            <img src="/images/animal.png" alt="로그인 필요" className="w-16 h-16 mx-auto mb-3 object-contain opacity-60" />
            <p className="text-gray-700 font-medium mb-3">로그인이 필요합니다</p>
            <p className="text-gray-600 text-sm mb-4">댓글을 작성하려면 로그인해주세요</p>
            <button onClick={() => window.location.href = "/login"} className="px-6 py-2 bg-gradient-to-r from-primary-600 to-primary-700 text-white rounded-md hover:from-primary-700 hover:to-primary-800 transition-all font-medium shadow-md">
              로그인하러 가기
            </button>
          </div>
        )}
      </div>

      {comments.length > 0 ? (
        <div className="space-y-4">{renderComments(comments)}</div>
      ) : (
        <div className="py-12 text-center"><p className="text-gray-500">첫 댓글을 남겨보세요</p></div>
      )}
    </div>
  );
}
