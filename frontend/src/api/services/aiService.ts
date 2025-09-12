import axios from '../axios';

export const aiService = {
  //< AI 자동 답변 요청
  askAi: (postId: number): Promise<string> =>
    axios.post<string>(`/ai/ask/${postId}`).then((res) => res.data),
};