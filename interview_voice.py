import streamlit as st
from audiorecorder import audiorecorder
import speech_recognition as sr
import tempfile
import hashlib

def voice_input_component():
    st.subheader("🎤 음성 답변 입력")
    st.caption(
        "1. '녹음 시작' 버튼을 클릭하고 답변을 말하세요.\n"
        "2. '녹음 종료' 버튼을 누르면 자동으로 인식 및 등록됩니다."
    )

    if "last_audio_hash" not in st.session_state:
        st.session_state.last_audio_hash = None

    audio = audiorecorder("🔴 녹음 시작", "⏹️ 녹음 종료")
    recognized_text = None

    current_audio_hash = hashlib.md5(audio.raw_data).hexdigest() if len(audio) > 0 else None

    if len(audio) > 0 and current_audio_hash != st.session_state.last_audio_hash:
        st.session_state.last_audio_hash = current_audio_hash

        with tempfile.NamedTemporaryFile(suffix=".wav", delete=False) as tmpfile:
            audio.export(tmpfile.name, format="wav")
            st.audio(tmpfile.name)

            recognizer = sr.Recognizer()
            with sr.AudioFile(tmpfile.name) as source:
                try:
                    audio_data = recognizer.record(source)
                    recognized_text = recognizer.recognize_google(audio_data, language='ko')
                    st.success("✅ 음성 인식 완료!")
                    st.markdown(f"**인식 결과:**\n{recognized_text}")
                    
                    # 메시지 추가 부분 제거 (주요 수정 부분)
                    st.session_state.user_answer = recognized_text

                except sr.UnknownValueError:
                    st.error("❌ 음성을 이해할 수 없습니다.")
                except sr.RequestError as e:
                    st.error(f"❌ 서비스 접근 오류: {e}")
                except Exception as e:
                    st.error(f"❌ 알 수 없는 오류: {e}")

    return recognized_text  # 단순 텍스트 반환
