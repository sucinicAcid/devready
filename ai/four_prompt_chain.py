from langchain_core.messages import SystemMessage, HumanMessage
from langchain_openai import ChatOpenAI
from dotenv import load_dotenv


# 초기 설정
load_dotenv() # .env 파일 읽어 API key 설정
LLM_NAME = 'gpt-3.5-turbo'

llm = ChatOpenAI(model_name=LLM_NAME)

# 총 4개의 프롬프트
prompts = [
    "너는 친절한 요약 전문가야. 사용자의 질문을 간결하게 요약해줘.",
    "너는 비판적인 분석가야. 전달받은 내용을 분석하고 문제점을 알려줘.",
    "너는 해결책 전문가야. 분석된 문제에 대한 구체적인 해결책을 제안해.",
    "너는 발표 준비 코치야. 해결책을 발표용으로 멋지게 정리해줘."
]

# 사용자 질문
current_input = '''
기후 변화에 대응하기 위한 기술은 뭐가 있을까
'''

for i in range(len(prompts)):
    system_msg = SystemMessage(content=prompts[i] + '\n\n 답변은 15자 이내로 해줘\n') # 프롬프트 내용 + 답변글자제한한
    human_msg = HumanMessage(content=current_input) # 사용자 질문

    response = llm.invoke([system_msg, human_msg])
    current_input = response.content  # 현재 챗봇의 응답은 다음 챗봇에 넘길 입력

    print(f"\n\n{i+1}번 챗봇 응답:")
    print(current_input)
