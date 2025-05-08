from fastapi import FastAPI
from langchain_openai import ChatOpenAI
from langchain_core.messages import SystemMessage, HumanMessage, AIMessage
from dotenv import load_dotenv
from pydantic import BaseModel
import uuid
from datetime import datetime

# 서버 시작시 한번 실행
load_dotenv()
LLM_NAME = 'gpt-3.5-turbo'
llm = ChatOpenAI(model_name=LLM_NAME)
question_prompt = '''
너는 개발자 기술 면접관이야.
이력서, category(Backend,OS,DB 등), difficulty(practice,real)에 따라 적절한 하나의 면접 질문을 생성해줘.
질문앞에 "면접질문: "과 같은 형식은 절대 쓰지마. 오직 질문만 답변해줘.\n\n
'''
tail_question_prompt = '''
너는 개발자 기술 면접관이야.
이력서, category(Backend,OS,DB 등), difficulty(practice,real), 대화기록에 따라 적절한 하나의 꼬리 질문을 생성해줘.
질문앞에 "면접질문: "과 같은 형식은 절대 쓰지마. 오직 질문만 답변해줘.\n\n
'''

app = FastAPI()

class QuestionRequest(BaseModel):
    resume_text: str # 이력서
    category: str # Backend, OS, DB 등 이력서 추출 키워드
    difficulty: str # practice, real    연습, 실전 모드

class TailQuestionRequest(BaseModel):
    resume_text: str # 이력서
    category: str # Backend, OS, DB 등 이력서 추출 키워드
    difficulty: str # practice, real    연습, 실전 모드
    chat_history: list[str] # chat history

def generate_custom_uuid():
    date_str = datetime.now().strftime("%Y%m%d")
    random_str = str(uuid.uuid4())[:4]
    return f"{date_str}-{random_str}"


@app.post("/question", response_model=dict())
def post_question(request: QuestionRequest):

    response = llm.invoke([
        SystemMessage(
            question_prompt + 
            f'이력서: {request.resume_text}\n\n' + 
            f'category: {request.category}' + 
            f'difficulty: {request.difficulty}'
        )
    ])
    return {
        "question_id": "q-" + generate_custom_uuid(),
        "question_text": response.content,
        "category": request.category,
        "difficulty": request.difficulty
    }


@app.post("/tail-question", response_model=dict())
def post_tail_question(request: TailQuestionRequest):

    messages = [SystemMessage(
        tail_question_prompt + 
        f'이력서: {request.resume_text}\n\n' + 
        f'category: {request.category}' + 
        f'difficulty: {request.difficulty}'
    )]
    for i in range(len(request.chat_history)):
        if i%2 == 0:
            messages.append(AIMessage(request.chat_history[i]))
        else:
            messages.append(HumanMessage(request.chat_history[i]))
    
    response = llm.invoke(messages)
    return {
        "tail_question_id": "tq-" + generate_custom_uuid(),
        "tail_question_text": response.content,
        "category": request.category,
        "difficulty": request.difficulty
    }