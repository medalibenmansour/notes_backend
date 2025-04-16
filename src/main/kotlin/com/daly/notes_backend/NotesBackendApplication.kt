package com.daly.notes_backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class NotesBackendApplication

fun main(args: Array<String>) {
	runApplication<NotesBackendApplication>(*args)
}
