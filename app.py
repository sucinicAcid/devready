import streamlit as st
import requests
import json

from interview_voice import voice_input_component
from resume_upload import resume_upload_component

st.set_page_config(
    page_title="AI 모의 기술 면접",
    page_icon="💼",
    layout="wide"
)

# --- CSS 스타일 ---
st.markdown("""
<style>
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;400;500;700&display=swap');
* { font-family: 'Noto Sans KR', sans-serif; }

.main-title {
    font-size: 2.2rem;
    font-weight: 700;
    color: #1E3A8A;
    margin-bottom: 0.5rem;
    text-align: center;
    padding: 1rem 0 0.5rem 0;
}
.subtitle {
    font-size: 1.1rem;
    color: #4B5563;
    margin-bottom: 1.2rem;
    text-align: center;
}

.card {
    background-color: white;
    border-radius: 10px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.07);
    padding: 1.1rem;
    height: 100%;
    margin-bottom: 1rem;
}
.card-icon {
    font-size: 2.2rem;
    margin-bottom: 0.7rem;
    text-align: center;
}
.card-title {
    font-size: 1.1rem;
    font-weight: 600;
    color: #1E3A8A;
    margin-bottom: 0.3rem;
    text-align: center;
}
.card-text {
    color: #4B5563;
    font-size: 0.93rem;
    line-height: 1.5;
}

hr {
    margin: 1.5rem 0 1rem 0;
    border: 0;
    height: 1px;
    background-image: linear-gradient(to right, rgba(0, 0, 0, 0), rgba(0, 0, 0, 0.1), rgba(0, 0, 0, 0));
}

.settings-container {
    background-color: #F9FAFB;
    padding: 1.2rem;
    border-radius: 10px;
    margin-bottom: 1.5rem;
    border: 1px solid #E5E7EB;
    max-width: 600px;
    margin-left: auto;
    margin-right: auto;
}

.settings-title {
    font-size: 1.1rem;
    font-weight: 600;
    color: #1E3A8A;
    margin-bottom: 1rem;
}

.stSelectbox > div > div {
    font-size: 1.2rem !important;
    min-height: 2.7rem !important;
}
.stSelectbox > div > div > select {
    font-size: 1.2rem !important;
    min-height: 2.7rem !important;
}
label {
    font-size: 1.1rem !important;
    font-weight: 600;
}

div[data-testid="stButton"] button {
    background-color: #1E3A8A;
    color: white;
    border-radius: 8px;
    border: none;
    padding: 0.6rem 1.1rem;
    font-weight: 500;
    font-size: 1.1rem;
    margin-bottom: 0.5rem;
    transition: background-color 0.3s;
}
div[data-testid="stButton"] button:hover {
    background-color: #2563EB;
}
            
div[data-testid="stSpinner"] {
    display: flex;
    justify-content: center;
    align-items: center;
}

@media (max-width: 900px) {
    .main-title { font-size: 1.5rem; }
    .settings-container { padding: 0.8rem; }
    .card { padding: 0.7rem; }
}
</style>
""", unsafe_allow_html=True)

# --- 상단 타이틀 및 안내 ---
st.markdown('<div class="main-title">AI 모의 기술 면접 서비스 💼</div>', unsafe_allow_html=True)
st.markdown('<div class="subtitle">실제 면접처럼 연습하고, AI로 피드백 받아보세요!</div>', unsafe_allow_html=True)

# --- 카드 안내 ---
col1, col2, col3 = st.columns(3)
with col1:
    st.markdown("""
    <div class="card">
        <div class="card-icon">📝</div>
        <div class="card-title">이력서 업로드</div>
        <div class="card-text">이력서를 분석하여 맞춤형 면접 질문을 생성합니다. 본인의 경력과 기술 스택에 최적화된 질문을 받아보세요.</div>
    </div>
    """, unsafe_allow_html=True)
with col2:
    st.markdown("""
    <div class="card">
        <div class="card-icon">🎤</div>
        <div class="card-title">음성 답변</div>
        <div class="card-text">실제 면접처럼 음성으로 답변하고, 답변 내용에 기반한 꼬리질문을 받아 더 깊이 있는 연습이 가능합니다.</div>
    </div>
    """, unsafe_allow_html=True)
with col3:
    st.markdown("""
    <div class="card">
        <div class="card-icon">📊</div>
        <div class="card-title">AI 피드백</div>
        <div class="card-text">답변 내용을 AI가 분석하여 강점과 개선점을 제시합니다. 실제 면접관의 시선으로 객관적인 피드백을 받아보세요.</div>
    </div>
    """, unsafe_allow_html=True)

st.markdown('<hr>', unsafe_allow_html=True)

# --- 설정 영역 (메인 화면 상단) ---
with st.container():
    st.markdown('<div class="settings-title">면접 설정</div>', unsafe_allow_html=True)
    colA, colB = st.columns(2)
    with colA:
        difficulty_options = {
            "연습 모드": "practice",
            "실전 모드": "real"
        }
        selected_label = st.selectbox(
            "난이도 선택",
            list(difficulty_options.keys()),
            index=0,
            key="select_difficulty"
        )
        selected_difficulty = difficulty_options[selected_label]
    with colB:
        if selected_difficulty == "practice":
            selected_category = st.selectbox(
                "카테고리 선택",
                ["Backend", "Frontend", "OS", "DB", "Network", "Algorithm", "Data Structure"],
                key="select_category"
            )
        else:
            selected_category = None

    # 실전 모드에서만 이력서 업로드
    uploaded_resume = None
    if selected_difficulty == "real":
        uploaded_resume = resume_upload_component()
        if uploaded_resume:
            st.session_state.resume_text = uploaded_resume
            st.success("이력서 업로드 완료!")
    else:
        st.session_state.resume_text = ""

    # 업로드된 이력서 보기 (업로드 폼 바로 아래)
    if st.session_state.resume_text:
        with st.expander("📄 업로드된 이력서 보기", expanded=False):
            st.write(st.session_state.resume_text[:1000] + ("..." if len(st.session_state.resume_text) > 1000 else ""))

    # 면접 시작/종료 버튼
    col_btn1, col_btn2 = st.columns([1, 1])
    with col_btn1:
        start_interview = st.button("🟢 면접 시작", use_container_width=True)
    with col_btn2:
        end_interview = st.button("🔴 면접 종료", use_container_width=True)

# --- 면접 팁 ---
with st.expander("면접 팁", expanded=False):
    st.markdown("""
    ### 기술 면접 팁
    1. 질문을 잘 이해하고 명확하게 답변하세요.
    2. 모르는 내용은 솔직하게 모른다고 말하세요.
    3. 실제 경험과 예시를 포함하면 좋습니다.
    4. 답변 후에는 추가 질문을 기다리세요.
    """)

st.markdown('<hr>', unsafe_allow_html=True)

# --- 인터뷰 대시보드 (챗봇) ---
st.markdown(
    "<h2 style='text-align: center;'>인터뷰 대시보드</h2>",
    unsafe_allow_html=True
)

API_URL = "http://localhost:8000"

# --- 세션 상태 관리 ---
if "resume_text" not in st.session_state:
    st.session_state.resume_text = ""
if "messages" not in st.session_state:
    st.session_state.messages = []
if "current_question" not in st.session_state:
    st.session_state.current_question = None

# --- 면접 시작 로직 ---
if start_interview:
    if selected_difficulty == "real" and not st.session_state.resume_text:
        st.error("실전 모드에서는 이력서를 업로드해주세요.")
    else:
        try:
            with st.spinner("질문 생성 중..."):
                response = requests.post(
                    f"{API_URL}/question",
                    json={
                        "resume_text": st.session_state.resume_text if selected_difficulty == "real" else "",
                        "category": selected_category,
                        "difficulty": selected_difficulty
                    }
                )
                if response.ok:
                    question = response.json()["question_text"]
                    st.session_state.messages = [{"role": "assistant", "content": question}]
                    st.session_state.current_question = question
        except Exception as e:
            st.error(f"오류: {e}")

# --- 면접 종료 ---
if end_interview and st.session_state.messages:
    st.session_state.messages = []
    st.session_state.current_question = None
    st.success("면접이 종료되었습니다.")
    st.rerun()

# --- 챗봇 대화 UI ---
for msg in st.session_state.messages:
    with st.chat_message(msg["role"]):
        st.write(msg["content"])

# --- 답변 입력 및 "잘 모르겠어요" 버튼 ---
def process_answer(answer_text):
    if answer_text and st.session_state.current_question:
        st.session_state.messages.append({"role": "user", "content": answer_text})
        try:
            with st.spinner("꼬리 질문 생성 중..."):
                response = requests.post(
                    f"{API_URL}/tail-question",
                    json={
                        "resume_text": st.session_state.resume_text if selected_difficulty == "real" else "",
                        "category": selected_category,
                        "difficulty": selected_difficulty,
                        "chat_history": [m["content"] for m in st.session_state.messages]
                    }
                )
                if response.ok:
                    tail_question = response.json()["tail_question_text"]
                    st.session_state.current_question = tail_question
                    st.session_state.messages.append({"role": "assistant", "content": tail_question})
                    st.rerun()
        except Exception as e:
            st.error(f"오류: {e}")
            st.rerun()

if st.session_state.messages:
    # 답변 입력 UI를 항상 챗봇 아래에 고정
    col1, col2 = st.columns([6, 1])
    with col1:
        user_input = st.chat_input("답변을 입력하거나 마이크로 녹음하세요.")
        voice_input = voice_input_component()
    with col2:
        skip_question = st.button("잘 모르겠어요", use_container_width=True)

    # 음성 입력 처리 (음성 인식 결과가 있을 때만)
    if voice_input:
        process_answer(voice_input)

    # 텍스트 입력 처리
    if user_input:
        process_answer(user_input)

    # "잘 모르겠어요" 버튼 처리
    if skip_question and st.session_state.current_question:
        try:
            with st.spinner("새 질문 생성 중..."):
                response = requests.post(
                    f"{API_URL}/question",
                    json={
                        "resume_text": st.session_state.resume_text if selected_difficulty == "real" else "",
                        "category": selected_category,
                        "difficulty": selected_difficulty
                    }
                )
                if response.ok:
                    new_question = response.json()["question_text"]
                    st.session_state.current_question = new_question
                    st.session_state.messages.append({"role": "assistant", "content": new_question})
                    st.rerun()
        except Exception as e:
            st.error(f"오류: {e}")

if "messages" in st.session_state and st.session_state.messages:
    st.download_button(
        label="💾 대화 내역 저장 (JSON)",
        data=json.dumps(st.session_state.messages, ensure_ascii=False, indent=2),
        file_name="chat_history.json",
        mime="application/json"
    )

