// 2026/09/03 22:04 추가
// 정적 리소스 매핑 설정 추가
// 로컬 PC나 서버에 업로드 된 파일을 웹 브라우저에서 읽을 수 있게 하려면 외부에서 접속할 수 있게끔 하여야함.

package com.example.practsimboard.controller

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration // Spring의 설정 클래스
class Webconfig : WebMvcConfigurer { // WebMvcConfigurer를 상속해서 Spring MVC설정을 추가로 변경
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) { // addResourceHandlers를 오버라이딩하였으며
        // Spring에게 정적파일을 어떻게 제공할지 설정하는 함수임
        registry.addResourceHandler("/uploads/**") // 웹 주소가 /upload/로 시작하는 요청을 처리할 것임
            // uploads/cat.jpg 또는 uploads/dog.png와 같은 요청을 처리할것임 **은 /뒤에 뭐가오든 상관없음임 (웹 주소)
            .addResourceLocations("file:uploads/") // uploads/** 요청이 들어오면 uploads/ 폴더에서 찾을것임
            // 예를 들어 uploads/cat.jpg 를 요청하면 uploads/cat.jpg를 찾을 것임 (실제 파일 위치)
    }
}