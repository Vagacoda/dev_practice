package com.example.practsimboard.controller

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.util.UUID

@Entity
class Article
    (
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var title: String,
    var content: String,
    // 2026/09/03 22:02 추가
    var imageUrl: String? = null
)

interface ArticleRepository : JpaRepository<Article, Long>

@Controller
class ArticleController
    (
    private val articleRepository: ArticleRepository
) {
    // 1. @GetMapping("/") 로 수정
    @GetMapping("/")
    fun index(model: Model): String {
        val articles = articleRepository.findAll()
        model.addAttribute("articles", articles)
        return "board"
    }

    @GetMapping("/article/{id}")
    fun detail(@PathVariable id: Long, model: Model): String{
        val article = articleRepository.findById(id).orElseThrow{
            IllegalArgumentException("Can't find article")
        }
        model.addAttribute("article", article)
        return "article"
    }

    // 2. @PostMapping("/write") 로 수정
    @PostMapping("/write")
    fun write(
        @RequestParam title: String,
        @RequestParam content: String,
        @RequestParam(required = false) image: MultipartFile? // 사용자가 웹에서 업로드한 파일을 서버에서 다루기 위한 객체
        // required 가 false인 이유는 파일이 없어도 괜찮다는 의미임
        // html의 <input type="file" name="image" accept="image/*">와 연결됨
    ): String {
        var imageUrl: String? = null
        if (image != null && !image.isEmpty) { // 이미지가 정상적으로 첨부 된경우
            // 프로젝트 최상단 경로에 uploads 폴더 지정 즉, uploads라는 위치를 사용하겠다고 지정
            val uploadDir = File("uploads")
            if (!uploadDir.exists()) { // uploads 폴더가 존재하지 않는다면 프로젝트 내에 uploads폴더 실제로 생성
                uploadDir.mkdirs()
            }

            // 파일명 중복방지를 위한 UUID생성(ex: 123e4567_image.jpg)
            val fileName = UUID.randomUUID().toString() + "_" + image.originalFilename
            // 상대파일경로가 아닌 절대파일경로로 지정함
            val saveFile = File(uploadDir, fileName).absoluteFile

            //폴더에 물리적으로 파일을 저장함
            image.transferTo(saveFile)

            imageUrl = "/uploads/$fileName"
        }
        val article = Article(
            title = title,
            content = content,
            imageUrl = imageUrl
        )
        articleRepository.save(article)
        return "redirect:/"
    }
}