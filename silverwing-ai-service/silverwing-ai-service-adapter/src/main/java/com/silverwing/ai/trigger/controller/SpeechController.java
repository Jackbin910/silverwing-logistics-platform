package com.silverwing.ai.trigger.controller;

import com.silverwing.ai.application.speech.SpeechAsrService;
import com.silverwing.common.annotation.SkipAuth;
import com.silverwing.common.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 语音能力 Controller
 * 提供语音转文字（ASR）接口，对接本地部署的语音模型服务
 */
@Tag(name = "语音能力", description = "语音识别（ASR）等语音相关接口")
@RestController
@RequestMapping("/speech")
@RequiredArgsConstructor
public class SpeechController {

    private final SpeechAsrService speechAsrService;

    /**
     * 语音转文字
     * <p>接收音频文件（字段名 audio），调用本地语音模型识别后返回文字。</p>
     *
     * @param audio 上传的音频文件
     * @return 识别出的文字
     */
    @Operation(summary = "语音转文字", description = "将上传的音频文件识别为文字")
    @PostMapping(value = "/asr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SkipAuth
    public Result<String> asr(@RequestParam("audio") MultipartFile audio) {
        String text = speechAsrService.recognize(audio);
        return Result.success(text);
    }
}
