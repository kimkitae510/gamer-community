import axios from '../axios';

interface AiAskResponse {
  taskId: number;
  message: string;
}

interface AiPollResponse {
  taskId: number;
  status: 'PENDING' | 'COMPLETED' | 'FAILED';
  content: string | null;
}

export const aiService = {
  //< AI 질문 요청 (비동기) — taskId 반환
  requestAsk: (postId: number): Promise<AiAskResponse> =>
    axios.post<AiAskResponse>(`/ai/ask/${postId}`).then((res) => res.data),

  //< 폴링: AI 답변 생성 상태 조회
  poll: (taskId: number): Promise<AiPollResponse> =>
    axios.get<AiPollResponse>(`/ai/poll/${taskId}`).then((res) => res.data),

  //< AI 질문 요청 + 1초 간격 폴링으로 답변 대기
  askAi: async (postId: number, onProgress?: (status: string) => void): Promise<string> => {
    // 1. 비동기 요청 → taskId 받기
    const { taskId } = await aiService.requestAsk(postId);

    // 2. 1초 간격 폴링
    return new Promise((resolve, reject) => {
      const interval = setInterval(async () => {
        try {
          const result = await aiService.poll(taskId);
          onProgress?.(result.status);

          if (result.status === 'COMPLETED') {
            clearInterval(interval);
            resolve(result.content || '');
          } else if (result.status === 'FAILED') {
            clearInterval(interval);
            reject(new Error('AI 답변 생성에 실패했습니다.'));
          }
          // PENDING이면 계속 폴링
        } catch (err) {
          clearInterval(interval);
          reject(err);
        }
      }, 1000);
    });
  },

  //< 남은 AI 사용 횟수 조회
  getRemainingUsage: (): Promise<number> =>
    axios.get<number>('/ai/usage/remaining').then((res) => res.data),
};