package com.silverwing.auth.infrastructure.config;

import com.google.code.kaptcha.text.TextProducer;

import java.security.SecureRandom;

/**
 * 算术验证码文本生成器。
 * <p>生成形如 {@code "12+3=?"} 的算式展示文本，并在末尾用 {@code "@"} 拼接正确答案，
 * 例如 {@code "12+3=?@15"}。{@code CaptchaService} 按 {@code "@"} 分割即可得到展示算式与正确答案，
 * 兼容 RuoYi 算术验证码的前端展示方式。</p>
 */
public class MathCaptchaTextCreator implements TextProducer {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String getText() {
        int a = RANDOM.nextInt(10) + 1;
        int b = RANDOM.nextInt(10) + 1;
        int result;
        String operator;
        // 随机选择加法或减法，减法保证非负结果
        if (RANDOM.nextBoolean()) {
            operator = "+";
            result = a + b;
        } else {
            operator = "-";
            // 保证被减数不小于减数，结果非负
            if (a < b) {
                int tmp = a;
                a = b;
                b = tmp;
            }
            result = a - b;
        }
        return a + operator + b + "=?" + "@" + result;
    }
}
