from langchain.chains.history_aware_retriever import create_history_aware_retriever
from langchain.chains.retrieval import create_retrieval_chain
from langchain.chains.combine_documents import create_stuff_documents_chain
from langchain_chroma import Chroma
from langchain_community.chat_message_histories import ChatMessageHistory
from langchain_core.prompts import ChatPromptTemplate, MessagesPlaceholder
from langchain_core.runnables.history import RunnableWithMessageHistory
from langchain_openai import ChatOpenAI, OpenAIEmbeddings
from langchain_community.chat_message_histories import RedisChatMessageHistory
from dotenv import load_dotenv



# 초기 설정
load_dotenv() # .env 파일 읽어 API key 설정
REDIS_URL = "redis://localhost:6379/0" # redis에 chat history 저장
LLM_NAME = 'gpt-3.5-turbo'

# vectordb 생성
# vectordb에 문서저장하는 코드는 만들어야함
# vectordb를 DB에 저장하는 것도 만들어야함
persist_directory = 'docs/chroma/' # vectorDB 저장소
embedding = OpenAIEmbeddings()
vectordb = Chroma(persist_directory=persist_directory, embedding_function=embedding)
retriever = vectordb.as_retriever()

# LLM 설정
llm = ChatOpenAI(model=LLM_NAME, temperature=0)

# RAG Prompt 생성: chat history 기반으로 RAG에 입력할 질문을 재구성
contextualize_prompt = """Given a chat history and the latest user question \
which might reference context in the chat history, formulate a standalone question \
which can be understood without the chat history. Do NOT answer the question, \
just reformulate it if needed and otherwise return it as is."""
contextualize_prompt = ChatPromptTemplate.from_messages(
    [
        ("system", contextualize_prompt),
        MessagesPlaceholder("chat_history"),
        ("human", "{input}"),
    ]
)
history_aware_retriever = create_history_aware_retriever(
    llm, retriever, contextualize_prompt
)



# 최종 Prompt 생성: RAG 결과와 chat history를 기반으로 질문
qa_system_prompt = """You are an assistant for question-answering tasks. \
Use the following pieces of retrieved context to answer the question. \
If you don't know the answer, just say that you don't know. \
Use three sentences maximum and keep the answer concise.\

{context}"""
qa_prompt = ChatPromptTemplate.from_messages(
    [
        ("system", qa_system_prompt),
        MessagesPlaceholder("chat_history"),
        ("human", "{input}"),
    ]
)
question_answer_chain = create_stuff_documents_chain(llm, qa_prompt)



# RAG와 QA chain 형성
rag_chain = create_retrieval_chain(history_aware_retriever, question_answer_chain)


def get_message_history(session_id: str) -> RedisChatMessageHistory:
    return RedisChatMessageHistory(
        session_id=session_id, # 사용자 id(PK) 넣기
        url=REDIS_URL
    )


chatbot = RunnableWithMessageHistory(
    rag_chain,
    get_message_history,
    input_messages_key="input",
    history_messages_key="chat_history",
    output_messages_key="answer",
)

answer = chatbot.invoke(
    {"input": "사과의 색깔은?"},
    config={
        "configurable": {"session_id": "abc123"} # 사용자 id(PK) 넣기
    },
)["answer"]

print(answer)