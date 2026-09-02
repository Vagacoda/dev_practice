package com.example.practsimboard.controller

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@Entity
class Article(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var title: String,
    var content: String
)

interface ArticleRepository : JpaRepository<Article, Long>

@Controller
class ArticleController(
    private val articleRepository: ArticleRepository
) {
    // 1. @GetMapping("/") 로 수정
    @GetMapping("/")
    fun index(model: Model): String {
        val articles = articleRepository.findAll()
        model.addAttribute("articles", articles)
        return "board"
    }

    // 2. @PostMapping("/write") 로 수정
    @PostMapping("/write")
    fun write(@RequestParam title: String, @RequestParam content: String): String {
        val article = Article(title = title, content = content)
        articleRepository.save(article)
        return "redirect:/"
    }
}