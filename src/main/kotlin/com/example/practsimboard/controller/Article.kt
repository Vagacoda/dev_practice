package com.example.practsimboard.controller

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
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
    var imageUrl: String? = null,
    // 2026/09/08 21:32 추가
    // 조회수 기능 추가
    var viewCount: Int = 0
)

// 2026/09/13 22:16 댓글 기능 추가
@Entity
class Comment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id : Long? = null,
    // 댓글 id

    var content: String,
    // 댓글 내용

    @ManyToOne
    @JoinColumn(name="article_id")
    var article : Article
    // 댓글 여러개가 게시물 하나에 가능
)

// 2026/09/12 17:16 게시글 검색 기능 추가.
// 레파지토리(Repository)는 DB조회와 수정, 삭제하는 도구
// 현재는 하나의 소스파일에서 엔터티, 서비스, 컨트롤러를 작성하였지만
// 하나의 소스파일에서 모두 작성하면 굉장히 비 효율적인 코드가 됨.
interface ArticleRepository : JpaRepository<Article, Long>{
    fun findByTitleContains(keyword: String): List<Article>
}

interface CommentRepository : JpaRepository<Comment, Long> {
    fun findByArticleId(articleId: Long): List<Comment>
}
// Comment중에서 article.id가 특정 값(ex: commentRepository.findByArticleId(5))인 comment만 찾음

@Controller
class ArticleController
    (
    private val articleRepository: ArticleRepository,
    private val commentRepository: CommentRepository
) {
    // 1. @GetMapping("/") 로 수정
    @GetMapping("/")
    fun index(model: Model): String {
        val articles = articleRepository.findAll()
        model.addAttribute("articles", articles)
        return "board"
    }

    // 2026/09/04 22:45 추가
    @GetMapping("/article/{id}") // 브라우저에서 /article/숫자 형태로 GET요청이 오면 아래 함수 실행
    fun detail(@PathVariable id: Long, model: Model): String{
        // 주소안의 {id}값을 id 변수로 받아오고 Model객체를  model로 선언
        val article = articleRepository.findById(id).orElseThrow{
            // articleRepository.findById(id)는 게시글 id를 조회함
            // orElseThrow는 찾는 id가 없으면 메시지를 내보냄
            IllegalArgumentException("Can't find article")
        }

        // 2026/09/08 21:33 추가
        // 조회수 기능임
        article.viewCount++
        articleRepository.save(article)

        // 2026/09/13 22:26 댓글 기능 추가
        val comments = commentRepository.findByArticleId(id)
        // 댓글 주소 findByArticle(5)

        model.addAttribute("article", article)
        model.addAttribute("comments", comments)
        // article 데이터를 HTML로 전달
        // comment 데이터를 HTML로 전달

        return "article"
        // article 화면을 보여줌
    }

    // 2026/09/05 22:22 추가
    // 게시글 수정창 추가
    @GetMapping("/article/{id}/edit")
    fun editform(@PathVariable id: Long, model: Model): String {
        var article = articleRepository.findById(id).orElseThrow{
            IllegalArgumentException("Can't find article")
        }
        model.addAttribute("article", article)
        return "article_edit"
    }

    // 2026//09/12 17:25 게시글 검색 추가
    @GetMapping("/search")
    fun search(@RequestParam keyword: String, model: Model): String {
        val articles = articleRepository.findByTitleContains(keyword)

        model.addAttribute("articles", articles)

        return "board"
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
    // 2026/09/05 22:26 추가
    // 게시글 수정창
    @PostMapping("/article/{id}/edit")
    fun edit(
        @PathVariable id: Long, //게시글 번호
        @RequestParam title: String, // HTML의 name='title'입력값 받음
        @RequestParam content: String //HTML의 name="content"입력값 받음
        // 실제 수정 저장을 담당하는 함수임
    ): String {
        val article = articleRepository.findById(id).orElseThrow { //수정할 기존 게시글을 데이터 베이스에서 찾음
            IllegalArgumentException("Can't find article")
        }
        article.title = title // 기존의 제목을 새로 입력한 title로 바꿈
        article.content = content // 기존의 내용을 새로 입력한 content로 바꿈
        articleRepository.save(article) // 바꿈 article을 데이터 베이스에 저장함

        return "redirect:/article/$id" // 저장이 끝나면 상세 페이지로 이동
    }
    // 게시글 삭제
    @PostMapping("article/{id}/delete")
    fun delete(@PathVariable id: Long): String{
        val article = articleRepository.findById(id).orElseThrow{
            IllegalArgumentException("Can't find article")
        }
        articleRepository.delete(article)
        return "redirect:/"
    }

    // 2026/09/13 22:27 댓글 기능 추가
    @PostMapping("/article/{id}/comment")
    fun writeComment(
        @PathVariable id: Long, // article(5)에서
        @RequestParam content: String // html의 content내용을 가져옴
    ):String {
        val article = articleRepository.findById(id).orElseThrow{
            IllegalArgumentException("Can't find article")
        }

        if (content.isBlank()) {
            return "redirect:/article/$id"
        }

        val comment = Comment( // 댓글 객체임
            content = content, // test commnet
            article = article // 5번 게시글
        )

        commentRepository.save(comment)

        return "redirect:/article/$id"
    }

    // 2026/09/15 20:54 댓글 삭제 기능 추가
    @PostMapping("/article/{articleId}/comment/{commentId}/delete")
    fun deleteComment(
        @PathVariable articleId: Long,
        @PathVariable commentId: Long): String{
        val comment = commentRepository.findById(commentId).orElseThrow {
            IllegalArgumentException("Can't find Comment")
        }

        if (comment.article.id != articleId) {
            throw IllegalArgumentException("Can't delete Article")
        }

        commentRepository.delete(comment)

        return "redirect:/article/$articleId"
    }
}