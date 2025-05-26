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

# --- 설정 영역 ---
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
            selected_category = "Resume"

    uploaded_resume = None
    if selected_difficulty == "real":
        uploaded_resume = resume_upload_component()
    else:
        st.session_state.resume_text = ""

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

st.markdown(
    "<h2 style='text-align: center;'>인터뷰 대시보드</h2>",
    unsafe_allow_html=True
)

API_URL = "https://devview.site"




# --- 세션 상태 관리 ---
if "resume_text" not in st.session_state:
    st.session_state.resume_text = ""
if "messages" not in st.session_state:
    st.session_state.messages = []
if "current_question" not in st.session_state:
    st.session_state.current_question = None
if "api_session" not in st.session_state:
    st.session_state.api_session = None
if "login_ok" not in st.session_state:
    st.session_state.login_ok = False




# --- 로그인 함수 ---
def login():
    login_url = f"{API_URL}/api/v1/login"
    login_data = {
        "username": "user123",   # 실제 계정 정보로 변경
        "password": "password123"
    }
    headers = {
        "User-Agent": "Mozilla/5.0",
        "Origin": "https://devview.site",
        "Referer": "https://devview.site/"
    }
    session = requests.Session()
    try:
        resp = session.post(login_url, json=login_data, headers=headers)
        if resp.ok:
            st.session_state.api_session = session
            st.session_state.login_ok = True
            # 쿠키 확인 (디버깅용, 필요시 주석처리)
            # st.write("로그인 성공, 세션 쿠키:", session.cookies)
            return True
        else:
            st.session_state.login_ok = False
            st.error(f"로그인 실패: status={resp.status_code}, body={resp.text}")
            return False
    except Exception as e:
        st.session_state.login_ok = False
        st.error(f"로그인 오류: {e}")
        return False




# --- 최초 실행 시 로그인 ---
if st.session_state.api_session is None or not st.session_state.login_ok:
    st.info("서버에 로그인 중입니다...")
    if login():
        st.success("로그인 성공! 면접을 시작할 수 있습니다.")
    else:
        st.stop()




# --- 면접 시작 로직 ---
if start_interview:
    if selected_difficulty == "real" and not st.session_state.resume_text:
        st.error("실전 모드에서는 이력서를 업로드해주세요.")
    else:
        try:
            with st.spinner("이력서 등록 중..."):
                # 1. 이력서 등록
                resume_payload = {"content": "테스트 이력서"}



                resume_resp = st.session_state.api_session.post(
                    f"{API_URL}/api/resumes", json=resume_payload
                )



                if not resume_resp.ok:
                    st.error(f"이력서 등록 실패: {resume_resp.text}")
                    st.stop()


                resume_id = resume_resp.json()["result"]["data"]["id"]
                st.session_state.resume_id = resume_id

            with st.spinner("질문(키워드) 생성 중..."):
                tag_url = f"{API_URL}/api/resumes/{resume_id}/tags"
                tag_resp = st.session_state.api_session.post(tag_url)
                print("tag_resp.status_code:", tag_resp.status_code)
                print("tag_resp.text:", tag_resp.text)
                if not tag_resp.ok:
                    st.error(f"질문 생성 실패: {tag_resp.status_code} / {tag_resp.text}")
                    st.stop()
                keywords = tag_resp.json()["result"]["data"]["keywords"]

                if keywords:
                    question = keywords[0].get("detail", "질문 생성 결과가 없습니다.")
                else:
                    question = "질문 생성 결과가 없습니다."

                st.session_state.messages = [{"role": "assistant", "content": question}]
                st.session_state.current_question = question
                st.rerun()

                st.session_state.messages = [{"role": "assistant", "content": question}]
                st.session_state.current_question = question
                st.rerun()

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
        st.markdown(msg["content"])




# --- 답변 입력 및 "잘 모르겠어요" 버튼 ---
def process_answer(answer_text):
    if answer_text and st.session_state.current_question:
        st.session_state.messages.append({"role": "user", "content": answer_text})
        try:
            with st.spinner("꼬리 질문 생성 중..."):
                response = st.session_state.api_session.post(
                    f"{API_URL}/tail-question",
                    json={
                        "content": st.session_state.resume_text if selected_difficulty == "real" else "",
                        "category": selected_category,
                        "difficulty": selected_difficulty,
                        "chatHistory": [m["content"] for m in st.session_state.messages]
                    }
                )
                if response.ok:
                    tail_question = response.json()["tailQuestion"]
                    st.session_state.current_question = tail_question
                    st.session_state.messages.append({"role": "assistant", "content": tail_question})
                    st.rerun()
                elif response.status_code == 403:
                    st.error("인증이 만료되었습니다. 다시 로그인합니다.")
                    login()
                    st.rerun()
                else:
                    st.error(f"꼬리 질문 생성 실패: {response.text}")
        except Exception as e:
            st.error(f"오류: {e}")
            st.rerun()




if st.session_state.messages:
    col1, col2 = st.columns([6, 1])
    with col1:
        user_input = st.chat_input("답변을 입력하거나 마이크로 녹음하세요.")
        voice_input = voice_input_component()
    with col2:
        skip_question = st.button("잘 모르겠어요", use_container_width=True)

    if voice_input:
        process_answer(voice_input)

    if user_input:
        process_answer(user_input)

    if skip_question and st.session_state.current_question:
        try:
            with st.spinner("새 질문 생성 중..."):
                response = st.session_state.api_session.post(
                    f"{API_URL}/question",
                    json={
                        "resumeText": st.session_state.resume_text if selected_difficulty == "real" else "",
                        "category": selected_category,
                        "difficulty": selected_difficulty
                    }
                )
                if response.ok:
                    new_question = response.json()["question"]
                    st.session_state.current_question = new_question
                    st.session_state.messages.append({"role": "assistant", "content": new_question})
                    st.rerun()
                elif response.status_code == 403:
                    st.error("인증이 만료되었습니다. 다시 로그인합니다.")
                    login()
                    st.rerun()
                else:
                    st.error(f"새 질문 생성 실패: {response.text}")
        except Exception as e:
            st.error(f"오류: {e}")

if "messages" in st.session_state and st.session_state.messages:
    st.download_button(
        label="💾 대화 내역 저장 (JSON)",
        data=json.dumps(st.session_state.messages, ensure_ascii=False, indent=2),
        file_name="chat_history.json",
        mime="application/json"
    )
