import streamlit as st
from st_audiorec import st_audiorec
import speech_recognition as sr
import tempfile
import hashlib
from pydub import AudioSegment
import io
import os

def voice_input_component():
    with st.container():
        st.subheader("🎤 음성 답변 입력")
        st.info(
            "최초 사용 시 브라우저 마이크 권한 허용 및 컴포넌트 준비로 인해, "
            "첫 녹음은 무음일 수 있습니다. 두 번째부터 정상 녹음이 시작됩니다. "
            "Start - Start - Stop - Start"
        )
        st.caption(
            "1. '녹음 시작' 버튼을 클릭하고 답변을 말하세요.\n"
            "2. '녹음 종료' 버튼을 누르면 자동으로 인식 및 등록됩니다."
        )

        if "last_audio_hash" not in st.session_state:
            st.session_state.last_audio_hash = None

        audio_bytes = st_audiorec()

        recognized_text = None
        MIN_AUDIO_SIZE = 2048  # 2KB 이상만 정상 녹음으로 간주

        current_audio_hash = hashlib.md5(audio_bytes).hexdigest() if audio_bytes else None

        if audio_bytes and current_audio_hash != st.session_state.last_audio_hash:
            st.session_state.last_audio_hash = current_audio_hash

            with tempfile.NamedTemporaryFile(suffix=".wav", delete=False) as tmpfile:
                audio_segment = AudioSegment.from_file(io.BytesIO(audio_bytes), format="wav")
                audio_segment = audio_segment.set_frame_rate(16000).set_channels(1).set_sample_width(2)
                audio_segment.export(tmpfile.name, format="wav")
                st.audio(tmpfile.name)

                file_size = os.path.getsize(tmpfile.name)
                st.write(f"녹음 파일 크기: {file_size} bytes")
                if file_size < MIN_AUDIO_SIZE:
                    st.warning("녹음 준비가 완료되었습니다. 한 번 더 녹음 버튼을 눌러주세요.")
                    return None  # 너무 짧은 파일은 인식 시도하지 않음

                recognizer = sr.Recognizer()
                with sr.AudioFile(tmpfile.name) as source:
                    try:
                        audio_data = recognizer.record(source)
                        recognized_text = recognizer.recognize_google(audio_data, language='ko')
                        st.success("✅ 음성 인식 완료!")
                        st.markdown(f"**인식 결과:**\n{recognized_text}")
                        st.session_state.user_answer = recognized_text

                    except sr.UnknownValueError:
                        st.error("❌ 음성을 이해할 수 없습니다. (무음이거나 잡음일 수 있습니다.)")
                    except sr.RequestError as e:
                        st.error(f"❌ 서비스 접근 오류: {e}")
                    except Exception as e:
                        st.error(f"❌ 알 수 없는 오류: {e}")

        return recognized_text
