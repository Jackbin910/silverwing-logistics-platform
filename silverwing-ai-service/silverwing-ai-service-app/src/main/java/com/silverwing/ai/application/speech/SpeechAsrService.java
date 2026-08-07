package com.silverwing.ai.application.speech;

import com.silverwing.ai.client.AsrClient;
import com.silverwing.common.domain.ResultCode;
import com.silverwing.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 语音识别应用服务
 * <p>对外提供"音频转文字"能力，调用本地部署的语音模型服务（ASR）。
 * 调用失败统一抛出 i18n 业务异常，便于前端友好提示。</p>
 *
 * @author silverwing
 */
@Slf4j
@Service
public class SpeechAsrService {

    private final AsrClient asrClient;

    public SpeechAsrService(AsrClient asrClient) {
        this.asrClient = asrClient;
    }

    /**
     * 将上传的音频文件识别为文字
     *
     * @param audio 音频文件（multipart 字段名 audio）
     * @return 识别出的文字
     * @throws BusinessException 音频为空或识别失败时抛出
     */
    public String recognize(MultipartFile audio) {
        if (audio == null || audio.isEmpty()) {
            throw BusinessException.i18n(ResultCode.BAD_REQUEST, "ai.asr.audio.empty");
        }
        String text = asrClient.recognize(audio);
        if (text == null || text.isBlank()) {
            throw BusinessException.i18n(ResultCode.INTERNAL_SERVER_ERROR, "ai.asr.recognize.failed");
        }
        return text;
    }
}
