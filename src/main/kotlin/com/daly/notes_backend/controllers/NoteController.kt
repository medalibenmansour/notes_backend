package com.daly.notes_backend.controllers

import com.daly.notes_backend.database.model.Note
import com.daly.notes_backend.database.repository.NoteRepository
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.bson.types.ObjectId
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.time.Instant

// POST http://localhost:8085/notes
// GET http://localhost:8085/notes?ownerId=123
// DELETE http://localhost:8085/notes/123

// Ex : If server URL is https://notes.com --> NoteController will respond to all requests https://notes.com/notes
@RestController
@RequestMapping("notes")
class NoteController(
    private val noteRepository: NoteRepository,
) {
    data class NoteRequest(
        val id: String?,
        @field:NotBlank(message = "Title can't be blank.")
        val title: String,
        val content: String,
        val color: Long
    )

    data class NoteResponse(
        val id: String,
        val title: String,
        val content: String,
        val color: Long,
        val createdAt: Instant
    )

    // Ex : If request is POST http://localhost:8085/notes --> NoteController.save will respond to this
    @PostMapping
    fun save(
        @Valid @RequestBody body: NoteRequest
    ) : NoteResponse {
        val ownerId = SecurityContextHolder.getContext().authentication.principal as String
        val note = noteRepository.save(
            Note(
                id = body.id?.let { ObjectId(it) } ?: ObjectId.get(),
                title = body.title,
                content = body.content,
                color = body.color,
                createdAt = Instant.now(),
                ownerId = ObjectId(ownerId)
            )
        )

        return note.toResponse()
    }

    // Ex : If request is GET http://localhost:8085/notes --> NoteController.findByOwnerId will respond to this
    @GetMapping
    fun findByOwnerId(): List<NoteResponse> {
        val ownerId = SecurityContextHolder.getContext().authentication.principal as String
        return noteRepository.findByOwnerId(ObjectId(ownerId)).map {
            it.toResponse()
        }
    }

    // Ex : If request is DELETE http://localhost:8085/notes/123 --> NoteController.deleteById will respond to this
    @DeleteMapping(path = ["/{id}"])
    fun deleteById(@PathVariable id: String) {
        val note = noteRepository.findById(ObjectId(id)).orElseThrow {
            throw IllegalArgumentException("Note not found.")
        }
        val ownerId = SecurityContextHolder.getContext().authentication.principal as String
        if (note.ownerId.toHexString() == ownerId) {
            noteRepository.deleteById(ObjectId(id))
        }
    }
}

private fun Note.toResponse(): NoteController.NoteResponse {
    return NoteController.NoteResponse(
        id = id.toHexString(),
        title = title,
        content = content,
        color = color,
        createdAt = createdAt
    )
}