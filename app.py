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
    font-size: 2.5rem;
    font-weight: 700;
    margin-bottom: 0.4rem;
    margin-top: 1rem;
    color: #22223b;
    letter-spacing: -1.5px;
}
.subtitle {
    font-size: 1.2rem;
    color: #4a4e69;
    margin-bottom: 2.5rem;
}

.card {
    background: #fff;
    border-radius: 18px;
    box-shadow: 0 2px 16px 0 rgba(34,34,59,0.07), 0 1.5px 4px 0 rgba(34,34,59,0.04);
    padding: 2rem 1.2rem 1.5rem 1.2rem;
    margin-bottom: 1.5rem;
    text-align: center;
    transition: box-shadow 0.2s;
    border: 1px solid #e0e1dd;
}
.card:hover {
    box-shadow: 0 4px 32px 0 rgba(34,34,59,0.13), 0 3px 8px 0 rgba(34,34,59,0.07);
    border-color: #c9ada7;
}
.card-icon {
    font-size: 2.5rem;
    margin-bottom: 0.7rem;
}
.card-title {
    font-size: 1.25rem;
    font-weight: 600;
    color: #22223b;
    margin-bottom: 0.5rem;
}
.card-text {
    font-size: 1rem;
    color: #4a4e69;
    line-height: 1.5;
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
            index=0
        )

        selected_difficulty = difficulty_options[selected_label]

        st.session_state.selected_difficulty = selected_difficulty

    with colB:
        if selected_difficulty == "practice":
            selected_category = st.selectbox(
                "카테고리 선택",
                ["OS", "Database", "Network", "Java", "Algorithm"],
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

            st.write("로그인 성공, 세션 쿠키:", session.cookies)
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




# --- 면접 시작 로직, 첫 질문 생성 init ---
# f"{API_URL}/api/resumes/{resume_id}/tags"

if start_interview:


    # 연습 모드 init


    if selected_difficulty == "practice":
        
        if "resumeId" not in st.session_state:
            st.session_state.resumeId = None

        try:
            with st.spinner("면접 시작 중..."):

                # 1. 키워드 등록, 면접 시작(POST)

                interview_payload = {
                    "field": selected_category
                }


                interview_resp = st.session_state.api_session.post(
                    f"{API_URL}/api/interviews", json=interview_payload
                )
                if not interview_resp.ok:
                    st.error(f"면접 시작 실패: {interview_resp.status_code} {interview_resp.text}")
                    st.write("응답 본문:", interview_resp.text)
                else:
                    st.success("면접 시작 성공!")
                    #st.write("응답:", interview_resp.json())

                interview_id = interview_resp.json()["result"]["data"]["interviewId"]

                # GET /next-question으로 첫 질문 요청

                question_resp = st.session_state.api_session.get(
                    f"{API_URL}/api/interviews/{interview_id}/next-question"
                )

                if not question_resp.ok:
                    st.error(f"질문 가져오기 실패: {question_resp.text}")
                    st.stop()


                question_data = question_resp.json()

                question_content = question_data.get("result", {}).get("data", {}).get("content")

                print(question_content)

                if not question_content:
                    st.error("질문이 없습니다. 면접을 다시 시작해주세요.")
                    st.stop()


                st.session_state.messages = [{"role": "assistant", "content": question_content}]

                st.session_state.current_question = question_content

                st.session_state.interviewId = interview_id  # 세션 상태에 interviewId 저장


        except Exception as e:
            st.error(f"오류: {e}")






    # 실전 모드 init

    # f"{API_URL}/api/resumes/{resume_id}/tags"


    if selected_difficulty == "real":

        if st.session_state.resume_text is None:
            st.error("실전 모드에서는 이력서를 업로드해주세요.")
            st.stop()

        try:
            with st.spinner("이력서 등록 중..."):
                # 1. 이력서 등록
                resume_payload = {"content": st.session_state.resume_text}

                resume_resp = st.session_state.api_session.post(
                    f"{API_URL}/api/resumes", json=resume_payload
                )
                if not resume_resp.ok:
                    st.error(f"이력서 등록 실패: {resume_resp.text}")
                    st.stop()

                # 이력서 등록 성공, ID 저장
                resume_id = resume_resp.json()["result"]["data"]["id"]

                st.session_state.resume_id = resume_id

            with st.spinner("키워드(태그) 생성 중..."):
                tag_url = f"{API_URL}/api/resumes/{resume_id}/tags"
                tag_resp = st.session_state.api_session.post(tag_url)
                print("tag_resp.status_code:", tag_resp.status_code)
                print("tag_resp.text:", tag_resp.text)
                if not tag_resp.ok:
                    st.error(f"키워드 생성 실패: {tag_resp.status_code} / {tag_resp.text}")
                    st.stop()
                # 필요하다면 keywords 등 저장

            with st.spinner("실전 인터뷰 세션 생성 중..."):
                # 2. 인터뷰 세션 생성 (이력서 기반)
                interview_resp = st.session_state.api_session.post(
                    f"{API_URL}/api/resumes", json={"content": st.session_state.resume_text}
                )


                if not interview_resp.ok:
                    st.error(f"실전 인터뷰 생성 실패: {interview_resp.status_code} / {interview_resp.text}")
                    st.stop()

            with st.spinner("첫 질문 생성 중..."):
                # 3. 첫 질문 요청 (resumeId 기반)
                question_url = f"{API_URL}/api/resumes/{resume_id}/next-question"
                question_resp = st.session_state.api_session.get(question_url)
                print("question_resp.status_code:", question_resp.status_code)
                print("question_resp.text:", question_resp.text)
                if not question_resp.ok:
                    st.error(f"첫 질문 생성 실패: {question_resp.status_code} / {question_resp.text}")
                    st.stop()
                question_data = question_resp.json()["result"]["data"]
                question = (
                    question_data.get("createdQuestion")
                    or question_data.get("question")
                    or question_data.get("content")
                    or "질문 생성 결과가 없습니다."
                )

                st.session_state.messages = [{"role": "assistant", "content": question}]
                st.session_state.current_question = question
                st.rerun()

        except Exception as e:
            st.error(f"오류: {e}")






# --- AI 평가 결과 표시 ---

def show_evaluation():
    selected_difficulty = st.session_state.get("selected_difficulty", "practice")
    evaluation_text = None

    try:
        with st.spinner("AI 평가를 받고 있습니다..."):

            # 연습모드 평가

            if selected_difficulty == "practice":
                interview_id = st.session_state.get("interviewId")
                if not interview_id:
                    st.error("연습 모드 interviewId가 없습니다.")
                    return
                eval_resp = st.session_state.api_session.get(
                    f"{API_URL}/api/interviews/{interview_id}/evaluation"
                )
                if eval_resp.ok:
                    evaluation_text = eval_resp.json().get("result", {}).get("data")
                else:
                    st.error(f"AI 평가 실패: {eval_resp.status_code} {eval_resp.text}")
                    return
                

            # 실전모드 평가

            elif selected_difficulty == "real":
                resume_id = st.session_state.get("resume_id")
                if not resume_id:
                    st.error("실전 모드 resume_id가 없습니다.")
                    return
                eval_resp = st.session_state.api_session.get(
                    f"{API_URL}/api/resumes/{resume_id}/evaluation"
                )
                if eval_resp.ok:
                    evaluation_text = eval_resp.json().get("result", {}).get("data")
                else:
                    st.error(f"AI 평가 실패: {eval_resp.status_code} {eval_resp.text}")
                    return
            else:
                st.error(f"알 수 없는 모드입니다. (현재 값: {selected_difficulty})")
                return

    except Exception as e:
        st.error(f"AI 평가 중 오류: {e}")
        return


    # 챗봇에 평가 결과 추가

    st.session_state.messages.append({
        "role": "assistant",
        "content": f"**AI 평가 결과**\n\n{evaluation_text if evaluation_text else '평가 결과를 받아오지 못했습니다.'}"
    })
    st.session_state.current_question = None
    st.success("면접이 종료되었습니다.")
    st.rerun()




# 연습 모드 종료 API POST

def finish_interview():
    interviewId = st.session_state.get("interviewId")
    if not interviewId:
        st.error("인터뷰 ID가 없습니다.")
        return

    response = st.session_state.api_session.post(
        f"{API_URL}/api/interviews/{interviewId}/finish"
    )

    if response.ok:
        st.success("면접이 정상적으로 종료되었습니다.")
        # 필요하다면 추가 안내 메시지, 상태 변경 등
    else:
        st.error(f"면접 종료 실패: {response.status_code} {response.text}")





# --- 면접 종료 ---
if end_interview and st.session_state.messages:
    show_evaluation()

    if st.session_state.selected_difficulty == "practice":
        finish_interview()
        
    st.success("면접이 종료되었습니다.")
    st.rerun()




# --- 챗봇 대화 UI ---
for msg in st.session_state.messages:
    with st.chat_message(msg["role"]):
        st.markdown(msg["content"])






# --- 답변 입력 및 "잘 모르겠어요" 버튼 ---

def process_answer(answer_text):

    selected_difficulty = st.session_state.get("selected_difficulty", "practice")

    if answer_text and st.session_state.current_question:
        st.session_state.messages.append({"role": "user", "content": answer_text})
        try:
            with st.spinner("꼬리 질문 생성 중..."):



                # 연습 모드 (practice/interview) 꼬리질문 생성, 사용자 답변 처리

                if selected_difficulty == "practice":
                    interviewId = st.session_state.get("interviewId")
                    if not interviewId:
                        st.error("연습 모드 interviewId가 없습니다.")
                        return

                    # 프론트에서 꼬리질문 카운트 관리
                    followup_count = st.session_state.get("followup_count", 0)

                    if followup_count < 2:
                        # 1. 꼬리질문 생성
                        followup_payload = {"content": answer_text}
                        
                        response = st.session_state.api_session.post(
                            f"{API_URL}/api/interviews/{interviewId}/follow-up",
                            json={"content": answer_text}
                        )
                        if not response.ok:
                            st.error(f"꼬리질문 생성 실패: {response.status_code} {response.text}")
                            return

                        data = response.json().get("result", {}).get("data", {})
                        print("follow-up data:", data)

                        followup_question = (
                            data.get("followUpQuestion")
                            or data.get("content")
                            or data.get("question")
                            or data.get("createdQuestion")
                            or data.get("additionalProp1")
                            or data.get("additionalProp2")
                            or data.get("additionalProp3")
                        )

                        if followup_question:
                            st.session_state.current_question = followup_question
                            st.session_state.messages.append({"role": "assistant", "content": followup_question})
                            st.session_state.followup_count = followup_count + 1
                        else:
                            st.session_state.current_question = None
                            st.session_state.messages.append({
                                "role": "assistant",
                                "content": "더 이상 꼬리질문이 없습니다."
                            })
                        st.rerun()


                    else:
                        # 2. 꼬리질문 2회 후 next-question으로 테마 전환
                        next_question_resp = st.session_state.api_session.get(
                            f"{API_URL}/api/interviews/{interviewId}/next-question"
                        )
                        if not next_question_resp.ok:
                            st.error(f"다음 질문 가져오기 실패: {next_question_resp.status_code} {next_question_resp.text}")
                            return

                        data = next_question_resp.json().get("result", {}).get("data", {})
                        next_question = data.get("content")
                        next_question_id = data.get("questionId")

                        if next_question:
                            st.session_state.current_question = next_question
                            st.session_state.messages.append({"role": "assistant", "content": next_question})
                            st.session_state.current_question_id = next_question_id
                            st.session_state.followup_count = 0  # 카운트 리셋
                        else:
                            st.session_state.current_question = None
                            st.session_state.messages.append({
                                "role": "assistant",
                                "content": "더 이상 질문이 없습니다. 수고하셨습니다!"
                            })
                        st.rerun()



                # 실전 모드 (real/resume) 꼬리질문 생성, 사용자 답변 처리



                elif selected_difficulty == "real":

                    resume_id = st.session_state.get("resume_id")
                    if not resume_id:
                        st.error("실전 모드 resume_id가 없습니다.")
                        return

                    #st.session_state.messages.append({"role": "user", "content": answer_text})

                    followup_payload = {
                        "content": answer_text
                    }
                    response = st.session_state.api_session.post(
                        f"{API_URL}/api/resumes/{resume_id}/follow-up",
                        json=followup_payload
                    )

                    if response.ok:
                        # data는 문자열(질문)임!
                        data = response.json().get("result", {}).get("data", None)
                        next_question = data if isinstance(data, str) else None

                        if next_question:
                            st.session_state.current_question = next_question
                            st.session_state.messages.append({"role": "assistant", "content": next_question})
                        else:
                            st.session_state.current_question = None
                            st.session_state.messages.append({
                                "role": "assistant",
                                "content": "더 이상 질문이 없습니다. 수고하셨습니다!"
                            })
                        st.rerun()
                    else:
                        st.error(f"꼬리질문 생성 실패: {response.status_code} {response.text}")






                else:
                    st.error(f"알 수 없는 모드입니다. (현재 값: {selected_difficulty})")

                    
        except Exception as e:
            st.error(f"오류: {e}")
            st.rerun()







# --- 답변 출력 챗봇 UI ---


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


# --- 잘 모르겠어요 버튼 로직 ---

    if skip_question and st.session_state.current_question:
        selected_difficulty = st.session_state.get("selected_difficulty", "practice")
        try:
            with st.spinner("새 질문 생성 중..."):
                if selected_difficulty == "practice":
                    interviewId = st.session_state.get("interviewId")
                    if not interviewId:
                        st.error("연습 모드 interviewId가 없습니다.")
                        st.stop()

                    # 1. 답변 없이 빈 content로 제출

                    response = st.session_state.api_session.post(
                        f"{API_URL}/api/interviews/{interviewId}/answer",
                        json={"content": ""}
                    )
                    if not response.ok:
                        st.error(f"질문 스킵 실패: {response.status_code} {response.text}")
                        st.stop()

                    # 2. 후속 질문 요청

                    next_question_resp = st.session_state.api_session.get(
                        f"{API_URL}/api/interviews/{interviewId}/next-question"
                    )

                    if not next_question_resp.ok:
                        st.error(f"후속 질문 가져오기 실패: {next_question_resp.status_code} {next_question_resp.text}")
                        st.stop()

                    data = next_question_resp.json().get("result", {}).get("data", {})
                    next_question = data.get("content")
                    next_question_id = data.get("questionId")

                    if next_question:
                        st.session_state.current_question = next_question
                        st.session_state.messages.append({"role": "assistant", "content": next_question})
                        st.session_state.current_question_id = next_question_id
                    else:
                        st.session_state.current_question = None
                        st.session_state.messages.append({
                            "role": "assistant",
                            "content": "더 이상 질문이 없습니다. 수고하셨습니다!"
                        })
                    #st.rerun()

                elif selected_difficulty == "real":
                    resume_id = st.session_state.get("resume_id")
                    if not resume_id:
                        st.error("실전 모드 resume_id가 없습니다.")
                        st.stop()

                    response = st.session_state.api_session.get(
                        f"{API_URL}/api/resumes/{resume_id}/next-question"
                    )
                    if not response.ok:
                        st.error(f"다음 질문 생성 실패: {response.status_code} {response.text}")
                        st.stop()

                    question_data = response.json()["result"]["data"]
                    new_question = question_data.get("createdQuestion", "질문 생성 결과가 없습니다.")
                    st.session_state.current_question = new_question
                    st.session_state.messages.append({"role": "assistant", "content": new_question})
                    st.rerun()
                else:
                    st.error(f"알 수 없는 모드입니다. (현재 값: {selected_difficulty})")
        except Exception as e:
            st.error(f"오류: {e}")


# 챗봇 대화 내역 다운로드 버튼

if "messages" in st.session_state and st.session_state.messages:
    st.download_button(
        label="💾 대화 내역 저장 (JSON)",
        data=json.dumps(st.session_state.messages, ensure_ascii=False, indent=2),
        file_name="chat_history.json",
        mime="application/json"
    )
